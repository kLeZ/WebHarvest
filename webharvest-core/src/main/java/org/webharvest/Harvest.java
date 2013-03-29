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

package org.webharvest;

import java.io.IOException;
import java.net.URL;

import org.webharvest.definition.ConfigSource;
import org.webharvest.events.EventHandler;
import org.webharvest.events.HarvesterEvent;

/**
 * Web-Harvest application facade that provides control over creation of
 * scraping processors {@link Harvester} and dispatching of scraping events.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see Harvester
 * @see HarvestLoadCallback
 * @see URL
 * @see EventHandler
 * @see HarvesterEvent
 */
public interface Harvest {

    /**
     * Creates new {@link Harvester} object with configuration with
     * configuration represented by {@link ConfigSource} object.
     * Throws {@link IOException} in case of any problems with reading
     * configuration from remote location.
     *
     * @param source the URL from where to get the configuration.
     * @param callback
     * @return new {@link Harvester} instance.
     * @throws IOException problems with reading the configuration.
     */
    Harvester getHarvester(ConfigSource source, HarvestLoadCallback callback)
            throws IOException;

    /**
     * Register event handler. Registered handler will receive events from
     * different scraping scopes.
     *
     * @param handler
     *            reference to {@link EventHandler}.
     */
    void addEventHandler(EventHandler<?> handler);

    /**
     * Post an event with hope it will be routed to target scope. An event
     * object needs to implement or be an ancestor of {@link HarvesterEvent}.
     * Event is not propagated to all current scopes. Instead it is sent to a
     * scope associated withurl event's {@link Harvester}.
     *
     * @param <E>
     *            Type of {@link HarvesterEvent} event.
     * @param event
     *            event to publish.
     */
    <E extends HarvesterEvent> void postEvent(E event);

}
