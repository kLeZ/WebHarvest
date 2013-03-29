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
 * Converter from XML to JSON
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition("xml-to-json")
public class XmlToJsonPlugin extends WebHarvestPlugin {

    public String getName() {
        return "xml-to-json";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        Variable body = executeBody(context);
        try {
            JSONObject jsonObject = XML.toJSONObject(body.toString());
            return new NodeVariable(jsonObject.toString());
        } catch (JSONException e) {
            throw new PluginException("Error converting XML to JSON: " + e.getMessage());
        }
    }

}