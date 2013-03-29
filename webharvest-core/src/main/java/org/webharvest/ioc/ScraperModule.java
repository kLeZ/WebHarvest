package org.webharvest.ioc;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.AbstractRegistry;
import org.webharvest.Harvest;
import org.webharvest.Harvester;
import org.webharvest.LockedRegistry;
import org.webharvest.Registry;
import org.webharvest.ScrapingAware;
import org.webharvest.WHConstants;
import org.webharvest.definition.BufferConfigSource;
import org.webharvest.definition.Config;
import org.webharvest.definition.ConfigFactory;
import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.ConfigSourceFactory;
import org.webharvest.definition.FileConfigSource;
import org.webharvest.definition.URLConfigSource;
import org.webharvest.definition.XMLConfig;
import org.webharvest.deprecated.runtime.ScraperContext10;
import org.webharvest.events.DefaultHandlerHolder;
import org.webharvest.events.EventSink;
import org.webharvest.events.HandlerHolder;
import org.webharvest.events.HarvesterEventSink;
import org.webharvest.ioc.ScrapingInterceptor.ScrapingAwareHelper;
import org.webharvest.runtime.DefaultHarvest;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.EventBasedStatusHolder;
import org.webharvest.runtime.RunningStatusGuard;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.ScrapingHarvester;
import org.webharvest.runtime.StatusHolder;
import org.webharvest.runtime.WebScraper;
import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.database.JNDIConnectionFactory;
import org.webharvest.runtime.database.StandaloneConnectionPool;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.jsr.JSRScriptEngineFactory;
import org.webharvest.runtime.templaters.BaseTemplater;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.Monitor;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

// TODO Add javadoc
// TODO Add unit test
public final class ScraperModule extends AbstractModule {

    private static final ScraperScope SCRAPER_SCOPE = new ScraperScope();

    private final String workingDir;

    // TODO Add documentation
    // TODO Add unit test
    // FIXME rbala I'm not convinced this is good idea
    public ScraperModule(final String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(ScrapingAwareHelper.class).toInstance(new ScrapingAwareHelper());
        bindListener(Matchers.any(), new PostConstructListener());

        // FIXME rbala AbstractRegistry is actually not an abstract (no abstract methods)
        bind(new TypeLiteral<Registry<Harvester, EventBus>>() {}).
                toInstance(new LockedRegistry<Harvester, EventBus>(
                        new AbstractRegistry<Harvester, EventBus>() { }));

        bindConstant().annotatedWith(WorkingDir.class).to(workingDir);

        bindScope(ScrapingScope.class, SCRAPER_SCOPE);
        // Make our scope instance injectable
        bind(ScraperScope.class).toInstance(SCRAPER_SCOPE);

        bindListener(TypeMatchers.subclassesOf(ScrapingAware.class),
                new ScrapingAwareTypeListener());

        bind(EventBus.class).in(ScrapingScope.class);
        bindListener(Matchers.any(), new EventBusTypeListener());
        bind(EventSink.class).to(HarvesterEventSink.class).in(Singleton.class);

        requestStaticInjection(InjectorHelper.class);

        bind(ConnectionFactory.class).to(StandaloneConnectionPool.class).in(
                ScrapingScope.class);
        bind(WebScraper.class).to(Scraper.class).in(ScrapingScope.class);

        bind(AttributeHolder.class).to(ScopeAttributeHolder.class);

        bind(Harvest.class).to(DefaultHarvest.class).in(Singleton.class);
        bind(HandlerHolder.class).to(DefaultHandlerHolder.class).in(
                Singleton.class);

        install(new FactoryModuleBuilder().implement(Config.class,
                XMLConfig.class).build(ConfigFactory.class));

        install(new FactoryModuleBuilder().implement(Harvester.class,
                ScrapingHarvester.class).build(HarvesterFactory.class));

        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Scraping.class),
                new ScrapingInterceptor());

        bindDBConnectionFactory();
        bindScraperContext();
        bindDebugFileLogger();
        bindStatusHolder();

        // FIXME rbala Moved from ConfigModule
        bind(ScriptEngineFactory.class).to(JSRScriptEngineFactory.class).in(
                Singleton.class);
        requestStaticInjection(BaseTemplater.class);
        bind(RuntimeConfig.class).in(Singleton.class);
    }

    protected void bindDBConnectionFactory() {
        bind(ConnectionFactory.class).annotatedWith(Names.named("standalone"))
           .to(StandaloneConnectionPool.class).in(ScrapingScope.class);
        bind(ConnectionFactory.class).annotatedWith(Names.named("jndi"))
           .to(JNDIConnectionFactory.class).in(Singleton.class);
    }

    protected void bindScraperContext() {
        bind(ContextFactory.class).toInstance(new ContextFactory() {

            @Inject private Provider<ScraperContext> newProvider;
            @Inject private Provider<ScraperContext10> oldProvider;

            @Override
            public DynamicScopeContext create(final Config config) {
                final DynamicScopeContext context = WHConstants.XMLNS_CORE_10.equals(config.getNamespaceURI())
                    ? oldProvider.get() : newProvider.get();
                // TODO rbala So far this should be enough. Find better option to pass configuration when instantiating context
                context.setConfig(config);

                return context;
            }
        });
    }

    protected void bindDebugFileLogger() {
        bind(Logger.class).annotatedWith(DebugFileLogger.class).toInstance(
                LoggerFactory.getLogger(DebugFileLogger.NAME));
    }

    protected void bindStatusHolder() {
        bind(Monitor.class).in(ScrapingScope.class);
        bind(StatusHolder.class).to(EventBasedStatusHolder.class)
            .in(ScrapingScope.class);
        bind(Monitor.Guard.class).to(RunningStatusGuard.class)
            .in(ScrapingScope.class);
    }

    // FIXME rbala Plain old approach to guice's factory binding. Please consider it as temporary workaround. Can stay longer then actual work on 2.1 version.
    // TODO rbala Untested code
    @Provides
    ConfigSourceFactory getConfigSourceFactory() {
        return new ConfigSourceFactory() {

            @Override
            public ConfigSource create(final URL source) {
                return new URLConfigSource(source);
            }

            @Override
            public ConfigSource create(final File source) {
                return new FileConfigSource(source);
            }

            @Override
            public ConfigSource create(final String source) {
                return new BufferConfigSource(source);
            }

        };
    }

}
