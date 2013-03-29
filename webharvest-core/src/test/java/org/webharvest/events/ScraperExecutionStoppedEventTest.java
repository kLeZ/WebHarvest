package org.webharvest.events;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.Harvester;

public class ScraperExecutionStoppedEventTest extends UnitilsTestNG {

    @RegularMock
    private Harvester mockHarvester;

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullHarvester() {
        new ScraperExecutionStoppedEvent(null);
    }

    @Test
    public void getHavester() {
        final ScraperExecutionStoppedEvent event =
            new ScraperExecutionStoppedEvent(mockHarvester);

        final Harvester harvester = event.getHarvester();
        assertNotNull("Harvester is null.", harvester);
        assertSame("Unexpected harvester.", mockHarvester, harvester);
    }

}
