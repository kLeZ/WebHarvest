package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;
import org.webharvest.ioc.InjectorHelper;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperState;
import org.webharvest.runtime.StatusHolder;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * {@link AbstractProcessorDecorator} implementation which decorates
 * {@link Processor#run(Scraper, DynamicScopeContext)} method in the way that if
 * status of the processing is 'stopped' or 'exit' then returns
 * {@link EmptyVariable#INSTANCE} otherwise it delegates invocation to decorated
 * {@link Processor}.
 *
 * {@link StatusHolder} instances are retrieved using {@link InjectorHelper}.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <TDef>
 *            type of definition of decorated processor
 */
public final class StoppedOrExitedProcessor<TDef extends IElementDef> extends
        AbstractProcessorDecorator<TDef> {

    private final StatusHolder statusHolder;

    /**
     * Default class constructor which accepts {@link Processor} instance which
     * is going to be decorated by this class.
     *
     * @param decoratedProcessor
     *            an instance of {@link Processor} which is going to be
     *            decorated; must be not {@code null}.
     */
    public StoppedOrExitedProcessor(final Processor<TDef> decoratedProcessor) {
        super(decoratedProcessor);
        statusHolder = InjectorHelper.getInjector().getInstance(
                StatusHolder.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable run(final DynamicScopeContext context)
            throws InterruptedException {
        final ScraperState status = statusHolder.getStatus();

        if (status == ScraperState.STOPPED
                || status == ScraperState.EXIT) {
            return EmptyVariable.INSTANCE;
        }

        return decoratedProcessor.run(context);
    }

}
