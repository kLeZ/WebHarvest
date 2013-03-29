package org.webharvest.events;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.runtime.processors.Processor;

public class ProcessorStopEventTest extends UnitilsTestNG {

    @RegularMock
    private Processor mockProcessor;
    private Map mockProperties;

    private ProcessorStopEvent event;

    @BeforeMethod
    public void setUp() {
        mockProperties = new HashMap();

        event = new ProcessorStopEvent(mockProcessor, mockProperties);
    }

    @AfterMethod
    public void tearDown() {
        mockProperties = null;

        event = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullProcessor() {
        new ProcessorStopEvent(null, mockProperties);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullProperties() {
        new ProcessorStopEvent(mockProcessor, null);
    }

    @Test
    public void getProcessor() {
        final Processor processor = event.getProcessor();
        assertNotNull("Processor is null.", processor);
        assertSame("Unexpected processor.", mockProcessor, processor);
    }

    @Test
    public void getProperties() {
        final Map properties = event.getProperties();
        assertNotNull("Properties is null.", properties);
        assertSame("Unexpected properties.", mockProperties, properties);
    }

}
