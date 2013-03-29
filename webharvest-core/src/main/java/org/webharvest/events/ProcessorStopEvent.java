package org.webharvest.events;

import java.util.Map;

import org.webharvest.runtime.processors.Processor;

/**
 * Event informing that the specified {@link Processor} has successfully
 * finished its work.
 *
 * @see ProcessorStopEvent
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ProcessorStopEvent {

    private final Processor processor;

    private final Map properties;

    /**
     * Default class constructor which accepts not {@code null} reference to
     * {@link Processor} and its properties.
     *
     * @param processor
     *            reference to {@link Processor} which has been stopped; must
     *            not be null
     * @param properties
     *            all properties which has been completed by the
     *            {@link Processor}; must not be null
     */
    public ProcessorStopEvent(final Processor processor, final Map properties) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor is mandatory.");
        }
        if (properties == null) {
            throw new IllegalArgumentException("Properties are required.");
        }
        this.processor = processor;
        this.properties = properties;
    }

    /**
     * Returns reference to {@link Processor} which has been stopped.
     *
     * @return reference to {@link Processor} which has been stopped.
     */
    public Processor getProcessor() {
        return processor;
    }

    /**
     * Returns properties of the {@link Processor} which has been stopped.
     *
     * @return properties of the {@link Processor} which has been stopped.
     */
    public Map getProperties() {
        return properties;
    }

}
