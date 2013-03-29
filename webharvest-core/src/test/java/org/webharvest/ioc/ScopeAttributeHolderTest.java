package org.webharvest.ioc;

import static org.testng.AssertJUnit.*;

import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;

public class ScopeAttributeHolderTest extends UnitilsTestNG {

    private static final String KEY = "key";

    private static final String VALUE = "value";

    private ScopeAttributeHolder holder;

    @BeforeMethod
    public void setUp() {
        holder = new ScopeAttributeHolder();
    }

    @AfterMethod
    public void tearDown() {
        holder = null;
    }

    @Test
    public void testWriteReadOperations() {
        assertFalse(holder.hasAttribute(KEY));
        holder.putAttribute(KEY, VALUE);
        assertTrue(holder.hasAttribute(KEY));
        final Object value = holder.getAttribute(KEY);
        assertNotNull(value);
        assertSame(VALUE, value);
        final Map<Object, Object> attributes =
            (Map<Object, Object>) holder.getAttributeLock();
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsValue(VALUE));
    }


}
