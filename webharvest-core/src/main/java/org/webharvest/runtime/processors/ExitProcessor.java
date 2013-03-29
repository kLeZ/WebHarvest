package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.ExitDef;
import org.webharvest.events.ScraperExecutionExitEvent;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Exit processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "exit", validAttributes = { "id", "condition", "message" },
        definitionClass = ExitDef.class)
public class ExitProcessor extends AbstractProcessor<ExitDef> {

    @Inject
    private EventBus eventBus;

    public Variable execute(DynamicScopeContext context) {
        String condition = BaseTemplater.evaluateToString(elementDef.getCondition(), null, context);
        if (condition == null || "".equals(condition)) {
            condition = "true";
        }

        if (CommonUtil.isBooleanTrue(condition)) {
            String message = BaseTemplater.evaluateToString(elementDef.getMessage(), null, context);
            if (message == null) {
                message = "";
            }
            eventBus.post(new ScraperExecutionExitEvent(message));
            LOG.info("Configuration exited: {}", message);
        }

        return EmptyVariable.INSTANCE;
    }

}
