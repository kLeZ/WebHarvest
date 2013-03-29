package org.webharvest.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.webharvest.Harvest;
import org.webharvest.Harvester;
import org.webharvest.Harvester.ContextInitCallback;
import org.webharvest.Registry;
import org.webharvest.ioc.HttpModule;
import org.webharvest.ioc.ScraperModule;
import org.webharvest.ioc.Scraping;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.WebScraper;
import org.webharvest.runtime.web.HttpClientManager.ProxySettings;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class HandlerHolderIntegrationTest extends UnitilsTestNG {

    private static final Logger LOG =
        LoggerFactory.getLogger(HandlerHolderIntegrationTest.class);

    private Injector injector;

    private Harvest harvest;

    @BeforeClass
    public void setUp() {
        injector = Guice.createInjector(new ScraperModule("."), new HttpModule(
                ProxySettings.NO_PROXY_SET));
        harvest = injector.getInstance(Harvest.class);
        harvest.addEventHandler(new EventHandler<DummyEvent>() {

            @Override
            @Subscribe
            public void handle(final DummyEvent event) {
                LOG.info("Received event from thred [{}]", event.getId());
            }

        });
    }

    @AfterClass
    public void tearDown() {
        harvest = null;
        injector = null;
    }

    @Test(threadPoolSize = 5, invocationCount = 100)
    public void test() {
        injector.getInstance(MockHarvester.class).execute(
                new ContextInitCallback() {

                    @Override
                    public void onSuccess(final DynamicScopeContext context) {
                        // Do nothing
                    }

                });
    }

    public static class MockHarvester implements Harvester {

        private final Registry<Harvester, EventBus> registry;


        @Inject
        public MockHarvester(final Registry<Harvester, EventBus> registry) {
            this.registry = registry;
        }

        @Override
        @Scraping
        public DynamicScopeContext execute(final ContextInitCallback callback) {
            registry.lookup(this).post(
                    new DummyEvent(Thread.currentThread().getId()));

            return null;
        }

    }

    public static final class DummyEvent {

        private final long id;

        public DummyEvent(final long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

    }

}
