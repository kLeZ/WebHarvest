/*
 Copyright (c) 2006-2012 the original author or authors.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */

package org.webharvest.runtime;

import java.io.IOException;

import org.webharvest.Harvester;
import org.webharvest.definition.Config;
import org.webharvest.ioc.ContextFactory;
import org.webharvest.ioc.Scraping;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * Default implementation of {@link Harvester} interface aimed to perform data
 * extraction from remote websites. Its worth to note that web scraping may be
 * against the terms of use of some websites in rather unclear manner. Current
 * implementation serves purpose of a kind of facade for {@link WebScraper}
 * instance that is obtained during invocation of
 * {@link #execute(org.webharvest.Harvester.ContextInitCallback)} method. Class
 * constructors are Guice aware and the dependencies are ment to be
 * automatically created. Unfortunately since the instance of this class is
 * produced by Guice dynamic factory the new instance will get injected its
 * dependencies but itself can not be a dependency to other container objects.
 * When instantiated with a help of Guice it provides additional behavior
 * introduced with {@link Scraping} annotated
 * {@link #execute(org.webharvest.Harvester.ContextInitCallback)} method. In
 * such situation invocation of this method automatically creates execution
 * scope so new instance of {@link WebScraper} object (and most of its
 * dependencies) lives within it.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see Harvester
 * @see WebScraper
 * @see Config
 */
// FIXME rbala Can not be final as we put an @Scraping annotation on this
public class ScrapingHarvester implements Harvester {

    private final Provider<WebScraper> scraperProvider;

    private final Config config;

    private final ContextFactory contextFactory;

    /**
     * Class constructor expecting Guice
     * {@link WebScraper} provider, {@link DynamicScopeContext} factory and
     * {@link Config} as already loaded configuration.
     *
     * @param scraperProvider
     *            the {@link WebScraper} provider.

     * @param contextFactory
     *            reference to a {@link DynamicScopeContext} factory.
     * @param config
     *            reference to the configuration {@link Config}.
     * @throws IOException
     */
    @Inject
    public ScrapingHarvester(final Provider<WebScraper> scraperProvider,
            final ContextFactory contextFactory,
            @Assisted final Config config) {
        this.scraperProvider = scraperProvider;
        this.contextFactory = contextFactory;
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Scraping
    public DynamicScopeContext execute(final ContextInitCallback callback) {
        final WebScraper scraper = scraperProvider.get();

        final DynamicScopeContext context =
                contextFactory.create(config);
        callback.onSuccess(context);

        scraper.execute(context);

        return context;
    }

}
