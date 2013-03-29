package org.webharvest.definition;

/**
 * Information about single xml attribute
 */
public class XmlAttribute {

    private String name;
    private String uri;
    private String value;

    public XmlAttribute(String name, String uri, String value) {
        this.name = name;
        this.uri = uri;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getValue() {
        return value;
    }

}