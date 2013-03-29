package org.webharvest.runtime;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.util.ReflectionUtils;

import com.google.common.util.concurrent.Monitor;

public class EventBasedStatusHolderTest {

    private EventBasedStatusHolder holder;

    @BeforeMethod
    public void setUp() throws Exception {
        holder = new EventBasedStatusHolder(new Monitor());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        holder = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullMonitor() throws Exception {
        new EventBasedStatusHolder(null);
    }

    @Test
    public void pause() throws Exception {
        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.RUNNING);

        //event is unneeded in unit test
        holder.pause(null);

        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.PAUSED);
    }

    @Test
    public void resume() throws Exception {
        ReflectionUtils.setFieldValue(holder,
                EventBasedStatusHolder.class.getDeclaredField("status"),
                ScraperState.PAUSED);

        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.PAUSED);

        //event is unneeded in unit test
        holder.resume(null);

        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.RUNNING);
    }

    @Test
    public void stop() throws Exception {
        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.RUNNING);

        //event is unneeded in unit test
        holder.stop(null);

        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.STOPPED);
    }

    @Test
    public void exit() throws Exception {
        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.RUNNING);

        //event is unneeded in unit test
        holder.exit(null);

        assertEquals("Unexpected status.", holder.getStatus(),
                ScraperState.EXIT);
    }


}
