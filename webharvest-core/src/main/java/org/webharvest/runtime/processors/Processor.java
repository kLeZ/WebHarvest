package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.variables.Variable;

// TODO Add javadoc
public interface Processor<TDef extends IElementDef> {

    /**
     * Sets appropriate element definition to the processor.
     *
     * @deprecated Find better solution to create processor in valid state
     * @param elementDef
     *            the element definition
     */
    @Deprecated
    void setElementDef(TDef elementDef);

    TDef getElementDef();

    // TODO Add javadoc
    Variable run(DynamicScopeContext context) throws InterruptedException;

    /**
     * Returns reference to parent {@link Processor} of this processor.
     */
    Processor getParentProcessor();

    /**
     * Sets reference to parent {@link Processor}.
     */
    void setParentProcessor(Processor parentProcessor);

    /**
     * Returns information how deep in processors hierarchy is this
     * {@link Processor}. Value of the lowest level is {@code 1}.
     */
    int getRunningLevel();

}
