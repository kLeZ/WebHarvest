package org.webharvest;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Multithreaded test is declared inside separate class class to overcome
 * unitils's 'before method' interceptor problems.
 * (We have to use 'before class' interceptor for multithreaded tests,
 * otherwise we can get unexpected results, since the cache reference may
 * change between test method invocations)
 */
public class ThreadLocalCacheMTest {

    private ThreadLocalCache<String, Object> cache;

    @BeforeClass
    public void setUp() {
        this.cache = new ThreadLocalCache<String, Object>();
    }

    @Test(invocationCount = 100, threadPoolSize = 10)
    public void cacheWorksLocallyPerThread() throws Exception {
        final Long cachedObject = new Long(Thread.currentThread().getId());
        cache.put("mykey", cachedObject);
        assertTrue("Cache should contain previously stored object",
                cache.contains("mykey"));
        assertSame("Cache should be able to lookup previously stored object",
                cachedObject, cache.lookup("mykey"));
    }
}
