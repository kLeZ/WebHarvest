package org.webharvest.ioc;

import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;

import com.google.inject.OutOfScopeException;

public class AttributeHolderScopeTest extends UnitilsTestNG {

    private AttributeHolderScope<AttributeHolder> scope;

    @RegularMock
    private AttributeHolder mockHolder;

    @RegularMock
    private AttributeHolder mockOtherHolder;

    @BeforeMethod
    public void setUp() {
        this.scope = new AttributeHolderScope<AttributeHolder>();
    }

    @AfterMethod
    public void tearDown() {
        this.scope = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void cannotEnterWithNullHolder() {
        scope.enter(null);
    }

    @Test(expectedExceptions = OutOfScopeException.class)
    public void cannotExitWhenNotInScope() {
        scope.exit();
    }

    @Test(expectedExceptions = OutOfScopeException.class)
    public void cannotGetHolderWhenNotInScope() {
        scope.get();
    }

    @Test
    public void enterScopeOnce() {
        EasyMockUnitils.replay();
        scope.enter(mockHolder);
        assertSame("Unexpected scope's attribute holder",
                mockHolder, scope.get());
        scope.exit();
    }

    @Test
    public void supportsNestedScopes() {
        EasyMockUnitils.replay();
        scope.enter(mockHolder);
        assertSame("Unexpected scope's attribute holder",
                mockHolder, scope.get());
        scope.enter(mockOtherHolder);
        assertSame("Unexpected scope's attribute holder",
                mockOtherHolder, scope.get());
        scope.exit();
        assertSame("Unexpected scope's attribute holder",
                mockHolder, scope.get());
        scope.exit();
    }
}
