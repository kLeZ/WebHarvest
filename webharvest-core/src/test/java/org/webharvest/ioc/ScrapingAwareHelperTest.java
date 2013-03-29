package org.webharvest.ioc;

import static org.easymock.EasyMock.*;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.Harvester;
import org.webharvest.ScrapingAware;
import org.webharvest.ioc.ScrapingInterceptor.ScrapingAwareHelper;

public class ScrapingAwareHelperTest extends UnitilsTestNG {

    @RegularMock
    private Harvester mockHarvester;

    @RegularMock
    private ScrapingAware mockListener;

    private ScrapingAwareHelper helper;

    @BeforeMethod
    public void setUp() {
        helper = new ScrapingAwareHelper();
    }

    @AfterMethod
    public void tearDown() {
        helper = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testAddListenerWithoutListener() {
        helper.addListener(null);
    }

    @Test
    public void testOnBeforeScraping() {
        mockListener.onBeforeScraping(mockHarvester);
        expectLastCall();
        EasyMockUnitils.replay();
        helper.addListener(mockListener);
        helper.onBeforeScraping(mockHarvester);
    }

    @Test
    public void testOnAfterScraping() {
        mockListener.onAfterScraping(mockHarvester);
        expectLastCall();
        EasyMockUnitils.replay();
        helper.addListener(mockListener);
        helper.onAfterScraping(mockHarvester);
    }


}
