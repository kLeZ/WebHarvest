package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.ConfigDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
// TODO Add unit test
// TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "config", validAttributes = { "charset", "scriptlang" },
        definitionClass = ConfigDef.class)
public final class ConfigProcessor extends AbstractProcessor<ConfigDef> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Variable execute(final DynamicScopeContext context)
            throws InterruptedException {

        context.setCharset(getElementDef().getCharset());
        context.setScriptingLanguage(getElementDef().getScriptingLanguage());

        // FIXME: It should be done by BodyProcessor (or some other component
        // while we'll annihilate BodyProcessor). However, due to current
        // BodyProcessor implementation, every processor is executed within new
        // context. If we had new context at this level, variables defined
        // within 'config' element body would not be available in the scraper
        // context at the end of its execution.
        for (IElementDef elementDef : getElementDef().getOperationDefs()) {
            ProcessorResolver.createProcessor(elementDef).run(context);
        }

        return EmptyVariable.INSTANCE;
    }

}
