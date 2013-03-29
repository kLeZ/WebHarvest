package org.webharvest.runtime.processors;

import org.webharvest.definition.IElementDef;
import org.webharvest.ioc.InjectorHelper;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.Monitor.Guard;

/**
 * {@link AbstractProcessorDecorator} implementation which decorates
 * {@link Processor#run(Scraper, DynamicScopeContext)} method in the way that it
 * enters to the {@link Monitor} using {@link Guard} verifying that processing
 * is not paused. Otherwise it wait until {@link Guard} allows to continue
 * processing.
 *
 * {@link Monitor} and {@link Guard} instances are retrieved using
 * {@link InjectorHelper}.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <TDef>
 *            type of definition of decorated processor
 */
public final class RunningStatusController<TDef extends IElementDef> extends
        AbstractProcessorDecorator<TDef> {

    private final Monitor monitor;
    private final Monitor.Guard runningGuard;

    /**
     * Default class constructor which accepts {@link Processor} instance which
     * is going to be decorated by this class.
     *
     * @param decoratedProcessor
     *            an instance of {@link Processor} which is going to be
     *            decorated; must be not {@code null}.
     */
    public RunningStatusController(final Processor<TDef> decoratedProcessor) {
        super(decoratedProcessor);
        this.monitor = InjectorHelper.getInjector().getInstance(Monitor.class);
        this.runningGuard = InjectorHelper.getInjector().getInstance(
                Monitor.Guard.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Variable run(final DynamicScopeContext context) throws InterruptedException {
        try {
            monitor.enterWhen(runningGuard);
        } finally {
            monitor.leave();
        }
        return this.decoratedProcessor.run(context);
    }

}
