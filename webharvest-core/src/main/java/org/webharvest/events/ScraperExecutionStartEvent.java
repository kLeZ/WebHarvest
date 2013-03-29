package org.webharvest.events;

import org.webharvest.runtime.WebScraper;

public final class ScraperExecutionStartEvent {

    private final WebScraper scraper;

    public ScraperExecutionStartEvent(final WebScraper scraper) {
        this.scraper = scraper;
    }

    public WebScraper getScraper() {
        return scraper;
    }

}
