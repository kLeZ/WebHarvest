package org.webharvest.runtime;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;

import java.io.IOException;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.Harvester;
import org.webharvest.definition.Config;
import org.webharvest.ioc.ContextFactory;

import com.google.inject.Provider;

public class ScrapingHarvesterTest extends UnitilsTestNG {

    @RegularMock
    private Provider<WebScraper> mockScraperProvider;

    @RegularMock
    private WebScraper mockScraper;

    @RegularMock
    private Harvester.ContextInitCallback mockInitCallback;

    @RegularMock
    private DynamicScopeContext mockContext;

    @RegularMock
    private ContextFactory mockContextFactory;

    @RegularMock
    private Config mockConfig;

    @Test
    public void testConstructor() {
        new ScrapingHarvester(mockScraperProvider,
                mockContextFactory, mockConfig);
    }

    @Test
    public void testExecute() throws IOException {
        expect(mockScraperProvider.get()).andReturn(mockScraper);
        expect(mockContextFactory.create(mockConfig)).andReturn(mockContext);
        mockInitCallback.onSuccess(mockContext);
        expectLastCall();
        mockScraper.execute(mockContext);
        expectLastCall();
        EasyMockUnitils.replay();
        final Harvester harvester = new ScrapingHarvester(mockScraperProvider,
                mockContextFactory, mockConfig);
        final DynamicScopeContext context = harvester.execute(mockInitCallback);
        assertNotNull(context);
        assertSame(mockContext, context);
    }

}
