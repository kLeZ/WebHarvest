package org.webharvest.runtime;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.definition.Config;
import org.webharvest.definition.ConfigFactory;
import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.IElementDef;
import org.webharvest.events.EventHandler;
import org.webharvest.events.EventSink;
import org.webharvest.events.HandlerHolder;
import org.webharvest.events.HarvesterEvent;
import org.webharvest.ioc.HarvesterFactory;

public class DefaultHarvestTest extends UnitilsTestNG {

    @RegularMock
    private HarvesterFactory mockFactory;

    @RegularMock
    private HandlerHolder mockHandlerHolder;

    @RegularMock
    private HarvestLoadCallback mockLoadCallback;

    @RegularMock
    private EventSink mockEventSink;

    @RegularMock
    private HarvesterEvent mockEvent;

    @RegularMock
    private Harvester mockHarvester;

    @RegularMock
    private ConfigSource mockConfigSource;

    @RegularMock
    private ConfigFactory mockConfigFactory;

    @RegularMock
    private Config mockConfig;

    @RegularMock
    private IElementDef mockElementDef;

    private DefaultHarvest harvest;

    @BeforeMethod
    public void setUp() {
        harvest = new DefaultHarvest(mockConfigFactory, mockFactory,
                mockHandlerHolder, mockEventSink);
    }

    @AfterMethod
    public void tearDown() {
        harvest = null;
    }

    @Test
    public void testGetHarvester() throws IOException {
        final List<IElementDef> emptyList = Collections.emptyList();
        expect(mockConfigFactory.create(mockConfigSource)).andReturn(mockConfig);
        expect(mockFactory.create(mockConfig)).
            andReturn(mockHarvester);
        mockConfig.reload();
        expectLastCall();
        expect(mockConfig.getElementDef()).andReturn(mockElementDef);
        expect(mockElementDef.getElementDefs()).andReturn(emptyList);
        mockLoadCallback.onSuccess(emptyList);
        expectLastCall();
        EasyMockUnitils.replay();
        final Harvester harvester = harvest.getHarvester(mockConfigSource,
                mockLoadCallback);
        assertNotNull(harvester);
        assertSame(mockHarvester, harvester);
    }

    @Test
    public void testAddEventHandler() {
        final EventHandler<HarvesterEvent> handler =
                new EventHandler<HarvesterEvent>() {

                    @Override
                    public void handle(final HarvesterEvent event) {
                        // Do nothing
                    }

                };
        mockHandlerHolder.register(handler);
        expectLastCall();
        EasyMockUnitils.replay();
        harvest.addEventHandler(handler);
    }

    @Test
    public void testPostEvent() {
        mockEventSink.publish(mockEvent);
        EasyMockUnitils.replay();
        harvest.postEvent(mockEvent);
    }

}
