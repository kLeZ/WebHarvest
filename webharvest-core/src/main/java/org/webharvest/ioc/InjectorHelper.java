package org.webharvest.ioc;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Guice static injector helper. This class is handy when it comes to get
 * reference to Guice's {@link Injector} for a class that is not managed or
 * instantiated by Guice (eg. legacy code or Guice module type listener
 * classes). Please do not overuse it as its a kind of trick rather then good
 * approach to codding style.
 *
 * <pre>
 * public class MyModule extends AbstractModule {
 *   protected void configure() {
 *     // (...)
 *     requestStaticInjection(InjectorHelper.class);
 *     // (...)
 *   }
 * }
 * </pre>
 *
 * @author Robert Bala
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @see Injector
 */
public final class InjectorHelper {

    @Inject
    private static Injector injector;

    /**
     * Class do nothing constructor preventing instantiation.
     */
    private InjectorHelper() {
        // Do nothing constructor
    }

    /**
     * Gets reference to the current {@link Injector}.
     *
     * @return reference to Guice's or {@code null} if the helper has not been
     *         installed properly.
     */
    public static Injector getInjector() {
        return injector;
    }

}
