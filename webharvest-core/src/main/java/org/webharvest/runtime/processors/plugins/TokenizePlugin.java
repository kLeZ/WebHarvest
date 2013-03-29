package org.webharvest.runtime.processors.plugins;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Support for database operations.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition("tokenize")
public class TokenizePlugin extends WebHarvestPlugin {

    public String getName() {
        return "tokenize";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        String delimiters = evaluateAttribute("delimiters", context);
        if ( delimiters == null || "".equals(delimiters) ) {
            delimiters = "\n\r";
        }
        boolean trimTokens = evaluateAttributeAsBoolean("trimtokens", true, context);
        boolean allowWmptyTokens = evaluateAttributeAsBoolean("allowemptytokens", false, context);
        String text =  executeBody(context).toString();

        this.setProperty("Delimiters", delimiters);
        this.setProperty("Trim tokens", trimTokens);
        this.setProperty("Allow empty tokens", allowWmptyTokens);

        String tokens[] = CommonUtil.tokenize(text, delimiters, trimTokens, allowWmptyTokens);

        ListVariable listVariable = new ListVariable();
        for (String token: tokens) {
            listVariable.addVariable(new NodeVariable(token));
        }

        return listVariable;
    }

    public String[] getValidAttributes() {
        return new String[] {
                "delimiters",
                "trimtokens",
                "allowemptytokens"
        };
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("trimtokens".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        } else if ("allowemptytokens".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }

}