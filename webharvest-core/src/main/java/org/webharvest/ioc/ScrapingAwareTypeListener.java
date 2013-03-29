package org.webharvest.ioc;

import org.webharvest.ScrapingAware;
import org.webharvest.ioc.ScrapingInterceptor.ScrapingAwareHelper;

import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Implementation of {@link TypeListener} that is responsible for registration
 * of detected {@link ScrapingAware}. If such an object is detected it is
 * automatically added as a listener to {@link ScrapingAwareHelper}.
 * Unfortunately listeners are only registered. No reverse process takes place
 * so be aware of prospective side effects.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see TypeListener
 * @see ScrapingAware
 * @see ScrapingAwareHelper
 */
public final class ScrapingAwareTypeListener implements TypeListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public <I> void hear(final TypeLiteral<I> type,
            final TypeEncounter<I> encounter) {
        final Provider<ScrapingAwareHelper> provider = encounter
                .getProvider(ScrapingAwareHelper.class);
        encounter.register(new InjectionListener<I>() {

            @Override
            public void afterInjection(I i) {
                if (i instanceof ScrapingAware) {
                    provider.get().addListener((ScrapingAware) i);
                }
            }
        });
    }

}
