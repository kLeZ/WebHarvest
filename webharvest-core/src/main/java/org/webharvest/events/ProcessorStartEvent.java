package org.webharvest.events;

import org.webharvest.runtime.processors.Processor;

/**
 * Event informing that the specified {@link Processor} has been started.
 *
 * @see ProcessorStopEvent
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ProcessorStartEvent {

    private final Processor processor;

    /**
     * Default class constructor which accepts not {@code null} reference to
     * {@link Processor}.
     *
     * @param processor
     *            reference to {@link Processor} which has been started; must
     *            not be null.
     */
    public ProcessorStartEvent(final Processor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor is mandatory.");
        }
        this.processor = processor;
    }

    /**
     * Returns reference to {@link Processor} which has been started.
     *
     * @return reference to {@link Processor} which has been started.
     */
    public Processor getProcessor() {
        return processor;
    }

}
