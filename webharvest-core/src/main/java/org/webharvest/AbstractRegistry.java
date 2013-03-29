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
import java.util.Set;

/**
 * Abstract class implementing {@link Registry} interface. In most cases, in
 * order to introduce {@link Registry} implementation it is enough to inherit
 * from this class.
 *
 * @see Registry
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public abstract class AbstractRegistry<K, V> implements Registry<K, V> {

    private final Map<K, V> storage = new HashMap<K, V>();

    /**
     * {@inheritDoc}
     */
    @Override
    public V lookup(K name) {
        return storage.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(K name, V value) throws AlreadyBoundException {
        if (storage.containsKey(name)) {
            throw new AlreadyBoundException(name + "is already bound");
        }
        storage.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbind(K name) {
        storage.remove(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> listBound() {
        return storage.keySet();
    }
}
