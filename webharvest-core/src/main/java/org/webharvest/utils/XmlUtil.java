package org.webharvest.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.w3c.dom.Document;
import org.webharvest.definition.validation.ResourcePathToURITransformer;
import org.webharvest.definition.validation.SchemaComponentFactory;
import org.webharvest.definition.validation.SchemaResolver;
import org.webharvest.definition.validation.SchemaResourcesPostProcessor;
import org.webharvest.definition.validation.SchemaSource;
import org.webharvest.definition.validation.TransformerPair;
import org.webharvest.definition.validation.URIToSchemaSourceTransformer;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML utils - contains common logic for XML handling
 */
public class XmlUtil {

    private static final Map<Integer, SAXParserFactory> saxParserFactoryMap;

    static {
        /* Resolving core XML schemas for version 1.0, 2.0 and 2.1. */
        final SchemaResolver schemaResolver =
            SchemaComponentFactory.getSchemaResolver();
        schemaResolver.addPostProcessor(
                new SchemaResourcesPostProcessor<String>(
                        new TransformerPair<String, URI, SchemaSource>(
                                new ResourcePathToURITransformer(),
                                new URIToSchemaSourceTransformer()),
                        "/config.xsd", "/wh-core-2.0.xsd",
                        "/wh-core-2.1-loose.xsd",
                        "/wh-jndi-2.1.xsd"));
        schemaResolver.refresh();


        final HashMap<Integer, SAXParserFactory> map = new HashMap<Integer, SAXParserFactory>();
        SAXParserFactory factory;

        // non-validating ns-unaware
        factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        map.put(0, factory);

        // validating ns-unaware
        /*factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(false);
        map.put(1, factory);*/

        // non-validating ns-aware
        factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        map.put(2, factory);

        // validating ns-aware
        /*factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(true);
        map.put(3, factory);*/

        saxParserFactoryMap = Collections.unmodifiableMap(map);
    }

    private static final ThreadLocal<DocumentBuilder> documentBuilderTL =
            new ThreadLocal<DocumentBuilder>() {
                @Override
                protected DocumentBuilder initialValue() {
                    try {
                        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    } catch (ParserConfigurationException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };

    private static final ThreadLocal<XPath> xPathBuilderTL =
            new ThreadLocal<XPath>() {
                @Override
                protected XPath initialValue() {
                    return XPathFactory.newInstance().newXPath();
                }
            };

    public static void prettyPrintXml(Document doc, Writer writer) {
        try {
            final Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T evaluateXPath(String xPathExpression, Document document) {
        try {
            return (T) xPathBuilderTL.get().evaluate(xPathExpression, document);
        } catch (XPathExpressionException e) {
            throw Assert.shouldNeverHappen(e);
        }
    }

    public static Document parse(InputSource is) {
        try {
            return documentBuilderTL.get().parse(is);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyPrintXml(String xmlAsString) {
        final StringWriter writer = new StringWriter();
        prettyPrintXml(parse(new InputSource(new StringReader(xmlAsString))), writer);
        return writer.toString();
    }

    /**
     * Evaluates specified XPath expression against given XML text and using given runtime configuration.
     *
     * @param xpath
     * @param xml
     * @param runtimeConfig
     * @return Instance of ListVariable that contains results.
     * @throws XPathException
     */
    public static ListVariable evaluateXPath(String xpath, String xml, RuntimeConfig runtimeConfig) throws XPathException {
        StaticQueryContext sqc = runtimeConfig.getStaticQueryContext();
        Configuration config = sqc.getConfiguration();

        XQueryExpression exp = runtimeConfig.getXQueryExpressionPool().getCompiledExpression(xpath);
        DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
        StringReader reader = new StringReader(xml);

        dynamicContext.setContextItem(sqc.buildDocument(new StreamSource(reader)));

        return createListOfXmlNodes(exp, dynamicContext);
    }

    /**
     * Creates list variable of resulting XML nodes.
     *
     * @param exp
     * @param dynamicContext
     * @return
     * @throws XPathException
     */
    public static ListVariable createListOfXmlNodes(XQueryExpression exp, DynamicQueryContext dynamicContext) throws XPathException {
        final SequenceIterator iter = exp.iterator(dynamicContext);
        final ListVariable listVariable = new ListVariable();

        for (Item item = iter.next(); item != null; item = iter.next()) {
            listVariable.addVariable(new NodeVariable(
                    new XmlNodeWrapper(item, exp.getExecutable().getDefaultOutputProperties())));
        }

        return listVariable;
    }

    public static SAXParserFactory getSAXParserFactory(boolean validating, boolean nsAware) {
        return saxParserFactoryMap.get((validating ? 1 : 0) | (nsAware ? 1 : 0) << 1);
    }
}
