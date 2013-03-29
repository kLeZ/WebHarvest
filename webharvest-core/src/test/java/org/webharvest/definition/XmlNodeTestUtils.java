package org.webharvest.definition;


public class XmlNodeTestUtils {

    public static final String NAMESPACE_10 = "xmlns='http://web-harvest.sourceforge.net/schema/1.0/config' " +
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
            "xsi:schemaLocation='http://web-harvest.sourceforge.net/schema/1.0/config config.xsd'";

    public static final String NAMESPACE_21 = "xmlns='http://web-harvest.sourceforge.net/schema/2.1/core' " +
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
            "xsi:schemaLocation='http://web-harvest.sourceforge.net/schema/2.1/core wh-core-2.1-loose.xsd'";

    /**
     * Instantiates {@link XmlNode} for given part of XML file and namespace. It
     * firstly wrappes given part of XML file by {@code <config>} element with
     * namespace attributes, then instantes {@link XmlNode} for whole
     * {@code <config>} element and finally returns first child of config node
     * which is a {@link XmlNode} for given part of XML file.
     */
    public static XmlNode createXmlNode(final String xmlPart, final String namespace) {
        final StringBuilder builder = new StringBuilder().append("<config ").
            append(namespace).append(">").append(xmlPart).append("</config>");
        final XmlNode configNode = XmlParser.parse(
                new BufferConfigSource(builder.toString())).getNode();

        return (XmlNode) configNode.getElementList().get(0);
    }

}
