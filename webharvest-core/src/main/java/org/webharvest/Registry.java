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

import java.util.Set;

/**
 * Generic registry interface following 'registry' design pattern.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 *
 * @param <K>
 *            type of key under which values are bound
 * @param <V>
 *            type of bound values
 */
public interface Registry<K, V> {

    /**
     * Lookups value bound under the given name. If no value is bound, then
     * {@code null} is returned.
     *
     * @param name
     *            name under which value is bound
     * @return found value or {@code null} if none bound
     */
    V lookup(K name);

    /**
     * Binds value under the given name.
     *
     * @param name
     *            name under which value is going to be bound
     * @param value
     *            bound value
     * @throws AlreadyBoundException
     *             thrown if the given name is already bound to some value
     */
    void bind(K name, V value) throws AlreadyBoundException;

    /**
     * Unbinds name and the associated value from the registry.
     *
     * @param name
     *            name to be unbound
     */
    void unbind(K name);

    /**
     * Lists names of all values bound.
     *
     * @return {@link Set} of all names registered; empty {@link Set} if
     *         registry is empty
     */
    Set<K> listBound();
}
