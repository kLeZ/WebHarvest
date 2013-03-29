package org.webharvest.events;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

public class ScraperExecutionExitEventTest {

    private static final String message = "Cookie monster";

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullMessage() {
        new ScraperExecutionExitEvent(null);
    }

    @Test
    public void getMessage() {
        final ScraperExecutionExitEvent event =
            new ScraperExecutionExitEvent(message);

        final String result = event.getMessage();
        assertNotNull("Message is null.", result);
        assertSame("Unexpected message.", message, result);
    }

}
