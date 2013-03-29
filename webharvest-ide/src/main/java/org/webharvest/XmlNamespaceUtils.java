package org.webharvest;

import net.sf.saxon.om.NamespaceResolver;
import org.apache.commons.lang.StringUtils;
import org.webharvest.utils.Stack;
import org.webharvest.utils.XmlUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XmlNamespaceUtils {

    @SuppressWarnings({"finally", "ReturnInsideFinallyBlock"})
    public static WHNamespaceResolver getNamespaceResolverFromBrokenXml(String xmlHeadFragment) {
        int openIndex = xmlHeadFragment.lastIndexOf('<');
        int closeIndex = xmlHeadFragment.lastIndexOf('>');

        if (openIndex > closeIndex) {
            if (xmlHeadFragment.endsWith("='")) {
                xmlHeadFragment += "'>";
            } else if (xmlHeadFragment.endsWith("=\"")) {
                xmlHeadFragment += "\">";
            } else {
                xmlHeadFragment += ">";
            }
        }

        final Map<String, Stack<String>> nsMap = new HashMap<String, Stack<String>>();
        try {
            XmlUtil.getSAXParserFactory(false, true).
                    newSAXParser().
                    parse(new InputSource(new StringReader(xmlHeadFragment)), new DefaultHandler() {
                        @Override public void startPrefixMapping(String prefix, String uri) throws SAXException {
                            Stack<String> stack = nsMap.get(prefix);
                            if (stack == null) {
                                stack = new Stack<String>();
                                nsMap.put(prefix, stack);
                            }
                            stack.push(uri);
                        }

                        @Override public void endPrefixMapping(String prefix) throws SAXException {
                            final Stack<String> stack = nsMap.get(prefix);
                            if (stack.size() > 1) {
                                stack.pop();
                            } else {
                                nsMap.remove(prefix);
                            }
                        }
                    });
        } finally {
            return new WHNamespaceResolver(nsMap);
        }
    }

    public static QName parseQName(String qName, NamespaceResolver nsResolver) {
        final String nsPrefix = qName.contains(":") ? StringUtils.substringBefore(qName, ":") : "";
        return new QName(
                nsResolver.getURIForPrefix(nsPrefix, true),
                StringUtils.isNotEmpty(nsPrefix) ? qName.substring(nsPrefix.length() + 1) : qName,
                nsPrefix);
    }

}
