package org.webharvest.definition;

import org.webharvest.runtime.processors.Processor;

/**
 * Definition of function call parameter.
 */
public class CallParamDef extends WebHarvestPluginDef {

    private String name;

    public CallParamDef(XmlNode xmlNode, Class<? extends Processor> processorClass) {
        super(xmlNode, processorClass);
        this.name = xmlNode.getAttribute("name");
    }

    public String getName() {
        return name;
    }

    public String getShortElementName() {
        return "call-param";
    }

}