package org.webharvest.runtime;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertNotNull;
import static org.easymock.EasyMock.*;

import org.easymock.IAnswer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.mock.Mock;
import org.webharvest.events.ScraperExecutionErrorEvent;

import com.google.common.eventbus.EventBus;

public class ExceptionHandlingScraperWrapperTest extends UnitilsTestNG {

    @RegularMock
    private EventBus eventBus;

    @RegularMock
    private WebScraper webScraper;

    private Mock<DynamicScopeContext> mockContext;

    private ExceptionHandlingScraperWrapper wrapper;

    @BeforeMethod
    public void setUp() throws Exception {
        wrapper = new ExceptionHandlingScraperWrapper(webScraper, eventBus);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        wrapper = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullWebScraper() {
        new ExceptionHandlingScraperWrapper(null, eventBus);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullEventBus() {
        new ExceptionHandlingScraperWrapper(webScraper, null);
    }

    @Test
    public void execute() {
        final DynamicScopeContext context = mockContext.getMock();

        webScraper.execute(same(context));
        expectLastCall();

        EasyMockUnitils.replay();

        wrapper.execute(context);
    }

    @Test
    public void executeIfRuntimeException() {
        final Capture capture = new Capture();
        final RuntimeException exception = new RuntimeException();
        final DynamicScopeContext context = mockContext.getMock();

        webScraper.execute(same(context));
        expectLastCall().andThrow(exception);

        eventBus.post(isA(ScraperExecutionErrorEvent.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                capture.setCaptured(getCurrentArguments()[0]);
                return null;
            }
        });

        EasyMockUnitils.replay();

        wrapper.execute(context);

        final ScraperExecutionErrorEvent event =
            capture.<ScraperExecutionErrorEvent>getCaptured();
        assertNotNull("Event is null.", event);
        final Exception ex = event.getException();
        assertNotNull("Exception from event is null.", ex);
        assertSame("Unexpected exception.", exception, ex);
    }



    // TODO mczapiewski Duplicate code with SpringAwareTypeListener
    private final class Capture {

        private Object captured;

        @SuppressWarnings("unchecked")
        public <T> T getCaptured() {
            return (T) captured;
        }

        private void setCaptured(final Object captured) {
            this.captured = captured;
        }

    }

}
