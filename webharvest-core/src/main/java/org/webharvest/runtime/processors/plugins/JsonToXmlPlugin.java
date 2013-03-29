package org.webharvest.runtime.processors.plugins;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.webharvest.annotation.Definition;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * Converter from JSON to XML
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value="json-to-xml", validAttributes="tag")
public class JsonToXmlPlugin extends WebHarvestPlugin {

    private final static String ATTR_ROOT_TAG_NAME = "tag";

    @Override
    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        try {
            return new NodeVariable(XML.toString(
                    new JSONObject(executeBody(context).toString()),
                    evaluateAttribute(ATTR_ROOT_TAG_NAME, context)));
        } catch (JSONException e) {
            throw new PluginException("Error converting JSON to XML: " + e.getMessage());
        }
    }

}