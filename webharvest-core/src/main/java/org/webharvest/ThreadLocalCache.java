/*
 Copyright (c) 2006-2012 the original author or authors.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
*/

package org.webharvest;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Cache} implementation based on {@link ThreadLocal}, that is, allowing
 * each thread to have separate cache bindings. It is especially useful for
 * caching non thread-safe objects. Since it is possible, that the application
 * will need to use different {@link ThreadLocalCache} instances, for example
 * one cache for script engines and another cache for database connections, we
 * do not enforce this class to be singleton. However, it is mandatory to have
 * only one instance of {@link ThreadLocalCache} per cache usage context (e.g.
 * single instance for caching script engines and another, single instance for
 * caching database connections) - usually, it is a job of IoC container.
 *
 * @see Cache
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class ThreadLocalCache<K, V> implements Cache<K, V> {

    private final ThreadLocal<Map<K, V>> storage;

    public ThreadLocalCache() {
        this.storage = new ThreadLocal<Map<K, V>>() {
            protected java.util.Map<K, V> initialValue() {
                return new HashMap<K, V>();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final K key) {
        return storage.get().containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final K key, final V value) {
        storage.get().put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V lookup(final K key) {
        return storage.get().get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invalidate(final K key) {
        storage.get().remove(key);
    }
}
