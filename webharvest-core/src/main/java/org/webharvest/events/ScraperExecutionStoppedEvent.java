package org.webharvest.events;

import org.webharvest.Harvester;

/**
 * Event informing that the execution of {@link Harvester} has been stopped. It
 * implements {@link HarvesterEvent} to be able to be fired from outside of
 * scraping scope.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ScraperExecutionStoppedEvent implements HarvesterEvent {

    private final Harvester harvester;

    /**
     * Constructs {@link ScraperExecutionStoppedEvent} accepting reference to
     * {@link Harvester} which execution has been stopped.
     *
     * @param harvester
     *            {@link Harvester} which execution has been stopped
     */
    public ScraperExecutionStoppedEvent(final Harvester harvester) {
        if (harvester == null) {
            throw new IllegalArgumentException("Harvester is mandatory.");
        }
        this.harvester = harvester;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Harvester getHarvester() {
        return harvester;
    }

}
