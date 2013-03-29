package org.webharvest.utils;

import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.lang.StringUtils;
import org.webharvest.exception.ScraperXQueryException;

import java.util.Properties;

/**
 * @author Vladimir Nikic
 * Date: Sep 4, 2007
 */
public class XmlNodeWrapper {

    private Item item;
    private String stringValue;
    private Properties outputProperties;

    public XmlNodeWrapper(Item item, Properties properties) {
        this.item = item;
        this.outputProperties = properties;
    }

    private String asString() {
        if (stringValue == null) {
            try {
                stringValue = CommonUtil.serializeItem(item, outputProperties);
            } catch (XPathException e) {
                throw new ScraperXQueryException("Error serializing XML item!", e);

            }
        }

        return stringValue;
    }

    public boolean isEmpty() {
        return StringUtils.isEmpty(asString());
    }

    public String toString() {
        return asString();
    }
}
