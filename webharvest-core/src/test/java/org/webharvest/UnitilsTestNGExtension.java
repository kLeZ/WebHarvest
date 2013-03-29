package org.webharvest;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.webharvest.ioc.DebugFileLogger;
import org.webharvest.ioc.InjectorHelper;
import org.webharvest.runtime.StatusHolder;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.templaters.BaseTemplater;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Monitor;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Extension of {@link UnitilsTestNG} which before each test method inject
 * appropriate mocks such as {@link Injector} mock and
 * {@link ScriptEngineFactory} mock into static fields in {@link InjectorHelper}
 * and {@link BaseTemplater} classes.
 *
 * Each test class verifying WebHarvest's processor or plugin should extend this
 * class.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public abstract class UnitilsTestNGExtension extends UnitilsTestNG {

    protected Mock<ScriptEngineFactory> scriptEngineFactoryMock;
    protected Mock<Logger> loggerMock;
    protected Mock<StatusHolder> holderMock;

    /**
     * Put mocks into static fields of InjectorHelper and BaseTemplater classes.
     * It must be done in this way, because unit tests work out of Guice IoC
     * which injects to these classes appropriate objects.
     */
    @BeforeMethod
    protected void injectStaticFields() throws Exception {

        // Puts Injector's mock into InjectorHelper class
        final Field injectorField = InjectorHelper.class
                .getDeclaredField("injector");
        injectorField.setAccessible(true);
        injectorField.set(null, createTestInjector());

        // Puts ScriptEngineFactory's mock into BaseTemplater class
        final Field templaterField = BaseTemplater.class
                .getDeclaredField("scriptEngineFactory");
        templaterField.setAccessible(true);
        templaterField.set(null, getScriptEngineFactory());

    }

    /**
     * Helper method returning {@link ScriptEngineFactory} instance which by
     * default is a mock of this interface. This method could be overridden.
     *
     * @return {@link ScriptEngineFactory} instance
     */
    protected ScriptEngineFactory getScriptEngineFactory() {
        return scriptEngineFactoryMock.getMock();
    }

    /**
     * Helper method returning instance of {@link Injector} created for
     * {@link TestModule}.
     *
     * @return instance of {@link Injector} created for {@link TestModule}.
     */
    private Injector createTestInjector() {
        return Guice.createInjector(new TestModule());
    }

    /*
     * Guice module designed for processor tests.
     *
     * In the future this class could be split on smaller modules which should
     * be specified by annotation marking test class e.g.
     *
     * @GuiceModules({ ComponentsTestModule.class, ServicesTestModule.class })
     */
    private class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            // AbstractProcessor dependecies
            bind(EventBus.class).in(Singleton.class);
            bind(Logger.class).annotatedWith(DebugFileLogger.class).toInstance(
                    loggerMock.getMock());

            // ScriptProcessor dependecies
            bind(ScriptEngineFactory.class)
                    .toInstance(getScriptEngineFactory());

            // Processor's decorators dependencies
            bind(Monitor.class).in(Singleton.class);
            bind(StatusHolder.class).toInstance(holderMock.getMock());
        }

        @Inject
        @Provides
        public Monitor.Guard getMonitorGuard(final Monitor monitor) {
            return new Monitor.Guard(monitor) {
                @Override
                public boolean isSatisfied() {
                    return true;
                }
            };
        }

    }

}
