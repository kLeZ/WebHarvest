package org.webharvest.events;

import static org.easymock.EasyMock.*;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.Harvester;
import org.webharvest.Registry;

import com.google.common.eventbus.EventBus;

public class HarvesterEventSinkTest extends UnitilsTestNG {

    @RegularMock
    private HarvesterEvent mockEvent;

    @RegularMock
    private Harvester mockHarvester;

    @RegularMock
    private Registry<Harvester, EventBus> mockRegistry;

    // FIXME rbala Unsure if it is a right way to mock classes
    private EventBus mockEventBus;

    private HarvesterEventSink eventSink;

    @BeforeMethod
    public void setUp() {
        eventSink = new HarvesterEventSink(mockRegistry);
        mockEventBus = new EventBus();
    }

    @AfterMethod
    public void tearDown() {
        eventSink = null;
        mockEventBus = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testPublishWithoutEvent() {
        eventSink.publish(null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testPublishForNotRegisteredHarvester() {
        expect(mockEvent.getHarvester()).andReturn(mockHarvester);
        expect(mockRegistry.lookup(mockHarvester)).andReturn(null);
        EasyMockUnitils.replay();
        eventSink.publish(mockEvent);
    }

    @Test
    public void testPublishForRegisteredHarvester() {
        expect(mockEvent.getHarvester()).andReturn(mockHarvester);
        expect(mockRegistry.lookup(mockHarvester)).andReturn(mockEventBus);
        EasyMockUnitils.replay();
        eventSink.publish(mockEvent);
    }

}
