package org.webharvest.events;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.runtime.processors.Processor;

public class ProcessorStartEventTest extends UnitilsTestNG {

    @RegularMock
    private Processor mockProcessor;

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullProcessor() {
        new ProcessorStartEvent(null);
    }

    @Test
    public void getProcessor() {
        final ProcessorStartEvent event =
            new ProcessorStartEvent(mockProcessor);

        final Processor processor = event.getProcessor();
        assertNotNull("Processor is null.", processor);
        assertSame("Unexpected processor.", mockProcessor, processor);
    }

}
