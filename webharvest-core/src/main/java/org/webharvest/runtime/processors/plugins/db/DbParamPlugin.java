package org.webharvest.runtime.processors.plugins.db;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * DB param plugin - can be used only inside database plugin.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "db-param", validAttributes = { "type" })
public final class DbParamPlugin extends WebHarvestPlugin {

    public String getName() {
        return "db-param";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        Processor processor = getParentProcessor();
        if (processor != null) {
            AbstractDatabasePlugin databasePlugin = (AbstractDatabasePlugin) processor;
            String type = evaluateAttribute("type", context);
            Variable body = executeBody(context);
            if (CommonUtil.isEmptyString(type)) {
                type = "text";
                if ( body.getWrappedObject() instanceof byte[] ) {
                    type = "binary";
                } else if (body instanceof ListVariable) {
                    ListVariable list = (ListVariable) body;
                    if (list.toList().size() == 1 && list.get(0).getWrappedObject() instanceof byte[]) {
                        type = "binary";
                    }
                }
            }
            databasePlugin.addDbParam(body, type);
            return new NodeVariable("?");
        } else {
            throw new PluginException("Cannot use db-param attach plugin out of database plugin context!");
        }
    }

    public String[] getValidAttributes() {
        return new String[] {"type"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("type".equalsIgnoreCase(attributeName)) {
            return new String[] {"int", "long", "double", "text", "binary"};
        }
        return null;
    }


}