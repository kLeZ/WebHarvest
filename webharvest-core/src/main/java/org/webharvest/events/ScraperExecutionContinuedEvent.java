package org.webharvest.events;

import org.webharvest.Harvester;

public final class ScraperExecutionContinuedEvent implements HarvesterEvent {

    private final Harvester harvester;

    public ScraperExecutionContinuedEvent(final Harvester harvester) {
        this.harvester = harvester;
    }

    @Override
    public Harvester getHarvester() {
        return harvester;
    }

}
