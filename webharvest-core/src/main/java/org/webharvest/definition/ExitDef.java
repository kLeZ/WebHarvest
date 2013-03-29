package org.webharvest.definition;

import org.webharvest.runtime.processors.Processor;

/**
 * Definition of exit processor.
 */
public class ExitDef extends WebHarvestPluginDef {

    private String condition;
    private String message;

    public ExitDef(XmlNode xmlNode, Class<? extends Processor> processorClass) {
        super(xmlNode, processorClass);

        this.condition = xmlNode.getAttribute("condition");
        this.message = xmlNode.getAttribute("message");
    }

    public String getCondition() {
        return condition;
    }

    public String getMessage() {
        return message;
    }

    public String getShortElementName() {
        return "exit";
    }

}