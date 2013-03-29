package org.webharvest.definition;

import org.webharvest.utils.Assert;
import org.webharvest.utils.CommonUtil;

/**
 * Name of the single element (tag in configuraton xml). It consists of the pair -
 * pure name and xml namespace URI. Both values are required and names are equal
 * if and only if both names and URIs match.
 */
public class ElementName implements Comparable {

    private String name;
    private String uri;

    public ElementName(String name, String uri) {
        Assert.isFalse(CommonUtil.isEmptyString(name), "Element name cannot be empty!");
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return name + ", " + uri;
    }

    @Override
    public int hashCode() {
        return (name + "," + uri).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ElementName) {
            ElementName elementName = (ElementName) obj;
            return name.equals(elementName.name) && uri.equals(elementName.uri);
        }
        return false;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ElementName) {
            ElementName elementName = (ElementName) o;
            int cmp = name.compareTo(elementName.name);
            if (cmp != 0) {
                return cmp;
            } else {
                return uri.compareTo(elementName.uri);
            }
        }

        return -1;
    }

}