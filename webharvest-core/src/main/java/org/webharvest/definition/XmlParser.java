/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

 * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

 * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
 */
package org.webharvest.definition;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.WHConstants;
import org.webharvest.definition.validation.SchemaComponentFactory;
import org.webharvest.exception.ParserException;
import org.webharvest.utils.Stack;
import org.webharvest.utils.XmlUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

public class XmlParser extends DefaultHandler {

    protected static Logger log = LoggerFactory.getLogger(XmlParser.class);

    XmlNode rootNode;

    // working stack of elements
    private Stack<XmlNode> elementStack = new Stack<XmlNode>();

    private Locator locator;

    public static ElementDefProxy parse(final ConfigSource config) {
        long startTime = System.currentTimeMillis();

        XmlParser handler = new XmlParser();
        try {
            final SAXParserFactory factory = XmlUtil.getSAXParserFactory(false, true);
            factory.setSchema(SchemaComponentFactory.getSchemaFactory().getSchema());
            factory.newSAXParser().parse(new InputSource(config.getReader()), handler);

            log.info("XML parsed in "
                    + (System.currentTimeMillis() - startTime) + "ms.");

        } catch (IOException e) {
            throw new ParserException(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new ParserException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new ParserException(e.getMessage(), e);
        }

        return new ElementDefProxy(handler.rootNode);
    }

    public XmlParser() {
        this.setDocumentLocator(new LocatorImpl());
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    private XmlNode getCurrentNode() {
        return elementStack.isEmpty() ? null : elementStack.peek();
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        XmlNode currNode = getCurrentNode();
        if (currNode != null) {
            currNode.addElement(new String(ch, start, length));
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        final XmlNode currNode = getCurrentNode();
        if (StringUtils.isEmpty(uri)
                || WHConstants.XMLNS_CORE_10_ALIASES.contains(uri)) {
            // if there is no xmlns we assume it is the old WH-config schema,
            // aka 1.0
            uri = WHConstants.XMLNS_CORE_10;
        }
        final XmlNode newNode = new XmlNode(localName, qName, uri, currNode);
        newNode.setLocation(this.locator.getLineNumber(),
                this.locator.getColumnNumber());
        elementStack.push(newNode);

        if (currNode == null) {
            this.rootNode = newNode;
        }

        final int attrsCount = attributes.getLength();
        for (int i = 0; i < attrsCount; i++) {
            newNode.addAttribute(attributes.getLocalName(i),
                    StringUtils.defaultIfEmpty(attributes.getURI(i), uri),
                    attributes.getValue(i));
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!elementStack.isEmpty()) {
            final XmlNode node = getCurrentNode();
            // addElement(String) adds to temporary buffer. Now consolidate
            // cached updates! String content as node elements (based on new
            // line)
            node.flushText();

            elementStack.pop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warning (final SAXParseException e) throws SAXException {
        log.warn(e.getMessage());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error (final SAXParseException e) throws SAXException {
        throw e;
    }

}