package org.webharvest.ioc;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.inject.annotation.InjectIntoStatic;

import com.google.inject.Injector;

public class InjectorHelperTest extends UnitilsTestNG {

    @RegularMock
    @InjectIntoStatic(target = InjectorHelper.class, property = "injector")
    private Injector mockInjector;

    @Test
    public void testGetInjector() {
        final Injector injector = InjectorHelper.getInjector();
        assertNotNull(injector);
        assertSame(mockInjector, injector);
    }

}
