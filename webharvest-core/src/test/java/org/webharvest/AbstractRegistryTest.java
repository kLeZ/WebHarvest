package org.webharvest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AbstractRegistryTest {

    private static final String KEY = "my registry key";
    private static final String VALUE = "my registry value";

    private class MockRegistry extends AbstractRegistry<String, String> {
    }

    private MockRegistry registry;

    @BeforeMethod
    public void setUp() {
        this.registry = new MockRegistry();
    }

    @Test
    public void lookupBound() throws Exception {
        registry.bind(KEY, VALUE);
        final String result = registry.lookup(KEY);
        assertSame("Unexpected value found", VALUE, result);
    }

    @Test
    public void lookupNotBound() {
        final String result = registry.lookup(KEY);
        assertNull("Expectected value not to be bound", result);
    }

    @Test
    public void lookupUnbound() throws Exception {
        registry.bind(KEY, VALUE);
        registry.unbind(KEY);
        final String result = registry.lookup(KEY);
        assertNull("Expectected value not to be bound", result);
    }

    @Test(expectedExceptions = AlreadyBoundException.class)
    public void doNotBoundTwice() throws Exception {
        registry.bind(KEY, VALUE);
        registry.bind(KEY, "another value 123!@#");
    }

    @Test
    public void listAllBound() throws Exception {
        registry.bind(KEY, VALUE);
        registry.bind("anotherkey", "anothervalue");

        final Set<String> expected = new HashSet<String>(Arrays.asList(
                KEY, "anotherkey"));
        final Set<String> result = registry.listBound();

        assertEquals("Unexpected bound list", expected, result);
    }
}
