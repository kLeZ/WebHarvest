package org.webharvest;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;

public class LockedRegistryTest extends UnitilsTestNG  {

    private static final String VALUE = "value";

    private Registry<String, String> delegate;

    private LockedRegistry<String, String> decorator;

    private AtomicInteger atomicCounter;

    @BeforeClass
    public void setUp() {
        delegate = new RegistryMock();
        decorator = new LockedRegistry<String, String>(delegate);
        atomicCounter = new AtomicInteger();
    }

    @AfterClass
    public void tearDown() {
        decorator = null;
        delegate = null;
        atomicCounter = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorWithNullDelegate() {
         new LockedRegistry<String, String>(null);
    }

    @Test(threadPoolSize = 100, invocationCount = 100)
    public void test() throws Exception {
        final String key = Integer.toString(atomicCounter.incrementAndGet());
        decorator.bind(key, VALUE);
        final String value = decorator.lookup(key);
        assertNotNull("Null value", value);
        assertSame("Unexpected value", VALUE, value);
        decorator.unbind(key);
        final Set<String> entries = decorator.listBound();
        assertNotNull("Null bound", entries);
        assertFalse("Found previously unbound value", entries.contains(key));
    }

    private final class RegistryMock implements Registry<String, String> {

        private final Map<String, String> storage =
            new HashMap<String, String>();

        @Override
        public String lookup(final String name) {
            return storage.get(name);
        }

        @Override
        public void bind(final String name, final String value)
                throws AlreadyBoundException {
            storage.put(name, value);
        }

        @Override
        public void unbind(final String name) {
            storage.remove(name);
        }

        @Override
        public Set<String> listBound() {
            return new HashSet<String>(storage.keySet());
        }

    }

}
