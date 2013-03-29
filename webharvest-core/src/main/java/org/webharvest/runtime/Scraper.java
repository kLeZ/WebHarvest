/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

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

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.events.ScraperExecutionStartEvent;
import org.webharvest.events.ScraperExecutionStoppedEvent;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.processors.ProcessorResolver;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Basic runtime class.
 */
public class Scraper implements WebScraper {

    private static final Logger LOG = LoggerFactory.getLogger(Scraper.class);

    @Inject
    private EventBus eventBus;

    public void execute(final DynamicScopeContext context) {
        long startTime = System.currentTimeMillis();

        // inform all listeners that execution is just about to start
        eventBus.post(new ScraperExecutionStartEvent(this));

        try {
            final Processor processor = ProcessorResolver.createProcessor(
                    context.getConfig().getElementDef());
            if (processor != null) {
                processor.run(context);
            }
        } catch (InterruptedException e) {
            informListenersAboutError(e);
            Thread.currentThread().interrupt();
        }

        // inform all listeners that execution is finished
        eventBus.post(new ScraperExecutionEndEvent(this,
                System.currentTimeMillis() - startTime));
    }

    /**
     * Logs information that Scraper's execution has been stopped.
     *
     * @param event
     *            an instance of {@link ScraperExecutionStoppedEvent}
     */
    @Subscribe
    public void onExecutionStopped(final ScraperExecutionStoppedEvent event) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Configuration stopped!");
        }
    }

    /**
     * Logs information about time of Scraper's execution on
     * {@link ScraperExecutionEndEvent}.
     *
     * @param event
     *            an instance of {@link ScraperExecutionEndEvent}
     */
    @Subscribe
    public void onExecutionFinished(final ScraperExecutionEndEvent event) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Configuration executed in {} ms.",
                    event.getExecutionTime());
        }
    }

    /**
     * Inform all scraper listeners that an error has occured during scraper execution.
     */
    public void informListenersAboutError(Exception e) {
        // inform al listeners that execution is continued
        eventBus.post(new ScraperExecutionErrorEvent(e));
    }

}
