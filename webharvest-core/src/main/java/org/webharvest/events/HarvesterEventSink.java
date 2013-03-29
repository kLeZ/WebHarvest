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

package org.webharvest.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.Harvester;
import org.webharvest.Registry;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Implementation of {@link EventSink} intended to guarantee scraping scope's
 * events delivery. Published {@link HarvesterEvent} is routed to appropriate
 * scope's {@link EventBus} that is associated with it. The scope resolution
 * takes place on {@link Registry} that binds {@link Harvester} scope's with
 * instance of {@link EventBus} which lives in this scope. In case it is not
 * possible to particular {@link EventBus} (it has not been registered yet or
 * for whatever reason) then the {@link IllegalStateException} is thrown. For
 * multi-threded environment the appropriate type of {@link Registry} needs to
 * be injected.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see EventSink
 * @see Registry
 * @see Harvester
 * @see EventBus
 */
public final class HarvesterEventSink implements EventSink {

    private static final Logger LOG = LoggerFactory
            .getLogger(HarvesterEventSink.class);

    private final Registry<Harvester, EventBus> registry;

    /**
     * Default class constructor expecting {@link Registry} to be injected
     * possibly by IoC container.
     *
     * @param registry
     *            reference to storage of {@link Harvester} and {@link EventBus}
     *            assocaitions.
     */
    @Inject
    public HarvesterEventSink(final Registry<Harvester, EventBus> registry) {
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends HarvesterEvent> void publish(final E event) {
        lookup(event).post(event);
        LOG.debug("Posted event [{}])", event);
    }

    private <E extends HarvesterEvent> EventBus lookup(final E event) {
        if (event == null) {
            throw new IllegalArgumentException("Event is requried");
        }
        final EventBus eventBus = registry.lookup(event.getHarvester());
        if (eventBus == null) {
            throw new IllegalStateException("Cound not find event bus");
        }

        return eventBus;
    }

}
