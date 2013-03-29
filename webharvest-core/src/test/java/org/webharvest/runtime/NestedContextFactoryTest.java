package org.webharvest.runtime;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.same;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.runtime.scripting.ScriptingLanguage;

public class NestedContextFactoryTest extends UnitilsTestNG {

    @RegularMock
    private DynamicScopeContext mockDelegate;

    // instance obtained from factory (under the test)
    private DynamicScopeContext nestedContext;

    @BeforeMethod
    public void setUp() {
        this.nestedContext = NestedContextFactory.create(mockDelegate);
    }

    @AfterMethod
    public void tearDown() {
        this.nestedContext = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void doesNotAcceptNullParentContext() {
        NestedContextFactory.create(null);
    }

    @Test
    public void delegateNotShadowedCalls() {
        final String varName = "varName";
        final String varValue = "var Value 123";

        mockDelegate.setLocalVar(same(varName), same(varValue));
        expectLastCall();

        EasyMockUnitils.replay();
        nestedContext.setLocalVar(varName, varValue);
    }

    @Test
    public void shadowCharsetOperations() {
        EasyMockUnitils.replay();

        assertNull("Expected null for not set charset",
                nestedContext.getCharset());
        nestedContext.setCharset("UTF-8");
        assertEquals("Unexpected charset", "UTF-8", nestedContext.getCharset());
    }

    @Test
    public void shadowScriptingLanguageOperations() {
        EasyMockUnitils.replay();

        assertNull("Expected null for not set scripting language",
                nestedContext.getScriptingLanguage());
        nestedContext.setScriptingLanguage(ScriptingLanguage.JAVASCRIPT);
        assertEquals("Unexpected scripting language",
                ScriptingLanguage.JAVASCRIPT,
                nestedContext.getScriptingLanguage());
    }
}
