package org.webharvest.gui.ioc;

import org.webharvest.ioc.ScrapingScope;
import org.webharvest.runtime.ExceptionHandlingScraperWrapper;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.WebScraper;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Guice module responsible for overriding binding rule for {@link WebScraper}
 * defined in core's modules. This module defines binding rules which provide
 * {@link ExceptionHandlingScraperWrapper} as default implementation of
 * {@link WebScraper} instead of wrapped {@link WebScraper}'s implementation
 * provided by core's modules.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class GuiModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(WebScraper.class).annotatedWith(Names.named("default"))
                .to(Scraper.class).in(ScrapingScope.class);
        bind(WebScraper.class).to(ExceptionHandlingScraperWrapper.class).in(
                ScrapingScope.class);
    }

}
