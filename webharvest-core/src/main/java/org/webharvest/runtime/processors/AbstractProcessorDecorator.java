package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;

/**
 * Abstract implementation of {@link Processor} interface which realizes
 * Decorator design pattern and overrides almost all {@link Processor}'s methods
 * (without #run() method) in the default way. It means that invocation of
 * {@link Processor}'s method is delegated to decorated processor.
 *
 * This class could be really helpful when only #run() method should be
 * decorated in special way.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <TDef>
 *            type of definition of decorated processor
 */
public abstract class AbstractProcessorDecorator<TDef extends IElementDef>
        implements Processor<TDef> {

    protected final Processor<TDef> decoratedProcessor;

    public AbstractProcessorDecorator(final Processor<TDef> decoratedProcessor) {
        if (decoratedProcessor == null) {
            throw new IllegalArgumentException(
                    "Decorated processor is required.");
        }
        this.decoratedProcessor = decoratedProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setElementDef(final TDef elementDef) {
        this.decoratedProcessor.setElementDef(elementDef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TDef getElementDef() {
        return this.decoratedProcessor.getElementDef();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Processor getParentProcessor() {
        return this.decoratedProcessor.getParentProcessor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParentProcessor(final Processor parentProcessor) {
        this.decoratedProcessor.setParentProcessor(parentProcessor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRunningLevel() {
        return this.decoratedProcessor.getRunningLevel();
    }

}
