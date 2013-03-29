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

/**
 * Cache containing semi-persistent, key-value mappings. Mapping is available in
 * cache until it is manually evicted. {@link Cache} implementations are
 * typically available as singleton instances.
 *
 * @param <K>
 *            type of cache mapping key
 * @param <V>
 *            type of values stored in cache
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface Cache<K, V> {

    /**
     * Associates the specified value with the specified key in {@link Cache}.
     * If {@link Cache} previously contained a mapping for the specified key,
     * the old value is replaced by the new one.
     *
     * @param key
     *            key under which value will be stored in cache
     * @param value
     *            value to be stored in cache
     */
    void put(K key, V value);

    /**
     * Returns {@link true} if {@link Cache} contains object identified by the
     * given key.
     *
     * @param key
     *            key of the object being looked up in the {@link Cache}
     * @return {@link true} if cache contains object identified by the given
     *         key; {@link false} is returned otherwise
     */
    boolean contains(K key);

    /**
     * Returns value associated with the specified key. If for the given key, no
     * value is currently cached, then {@link null} is returned.
     *
     * @param key
     *            key of the object being looked up in the {@link Cache}
     * @return object associated with the specified key, or {@link null} if
     *         there is no such object
     */
    V lookup(K key);

    /**
     * Discard value cached under the specified key.
     *
     * @param key
     *            key of the cache binding which is going to be invalidated.
     */
    void invalidate(K key);
}
