package org.webharvest.ioc;

import static org.easymock.EasyMock.*;

import org.aopalliance.intercept.MethodInvocation;
import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.inject.annotation.InjectIntoStatic;
import org.webharvest.Harvester;
import org.webharvest.runtime.web.HttpClientManager.ProxySettings;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ScrapingInterceptorTest extends UnitilsTestNG {

    @RegularMock
    private MethodInvocation mockInvocation;

    @RegularMock
    private Harvester mockHarvester;

    // FIXME rbala I'm not a great fun of this but mocking the Guice does not have sense either!
    @InjectIntoStatic(target = InjectorHelper.class, property = "injector")
    private Injector mockInjector = Guice.createInjector(
            new ScraperModule("."),
            new HttpModule(ProxySettings.NO_PROXY_SET));

    private ScrapingInterceptor interceptor;

    @BeforeMethod
    public void setUp() {
        interceptor = new ScrapingInterceptor();
    }

    @AfterMethod
    public void tearDown() {
        interceptor = null;
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void testInvokeInCaseOfNotHarvester() throws Throwable {
        expect(mockInvocation.getThis()).andReturn(this);
        EasyMockUnitils.replay();
        interceptor.invoke(mockInvocation);
    }

    @Test
    public void testInvoke() throws Throwable {
        expect(mockInvocation.getThis()).andReturn(mockHarvester);
        expect(mockInvocation.proceed()).andReturn(null);
        EasyMockUnitils.replay();
        interceptor.invoke(mockInvocation);
    }

}
