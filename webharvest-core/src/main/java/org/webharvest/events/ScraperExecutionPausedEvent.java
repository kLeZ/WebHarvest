package org.webharvest.events;

import org.webharvest.Harvester;

public final class ScraperExecutionPausedEvent implements HarvesterEvent{

    private final Harvester harvester;

    public ScraperExecutionPausedEvent(final Harvester harvester) {
        this.harvester = harvester;
    }

    @Override
    public Harvester getHarvester() {
        return harvester;
    }

}
