package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
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
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "body", validAttributes = "id")
// TODO Rename to BodyProcessor when finally got rid of miserable BoddyProcessor
public final class RealBodyProcessor extends WebHarvestPlugin {

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable executePlugin(final DynamicScopeContext context)
            throws InterruptedException {
        // Do nothing
        return EmptyVariable.INSTANCE;
    }

}
