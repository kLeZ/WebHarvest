package org.webharvest.ioc;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Guice {@link TypeListener} implementation that is responsible for
 * registration of objects managed by Guice in singleton {@link EventBus}.
 * Each newly created object is treated as potential event subscriber.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see TypeListener
 * @see EventBus
 */
public final class EventBusTypeListener implements TypeListener {

    private static final Logger LOG = LoggerFactory.
        getLogger(EventBusTypeListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public <I> void hear(final TypeLiteral<I> typeLiteral,
             final TypeEncounter<I> typeEncounter) {
        // TODO rbala Refactor as soon as possible. Ugly code (the whole method)
        boolean qualifies = false;
        for (final Method method : typeLiteral.getRawType().
                getDeclaredMethods()) {
            // If one of the class's methods is annotated to receive events then
            // register it to event bus
            qualifies = method.isAnnotationPresent(Subscribe.class);
            if (qualifies) {
                break;
            }

        }
        if (qualifies) {
            typeEncounter.register(new InjectionListener<I>() {
                public void afterInjection(final I i) {
                    InjectorHelper.getInjector().
                        getInstance(EventBus.class).register(i);
                    LOG.info("Subscribed to events {}", i);
                }
            });
        }
     }

}
