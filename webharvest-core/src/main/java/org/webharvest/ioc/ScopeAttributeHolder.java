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

package org.webharvest.ioc;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an object that implements {@link AttributeHolder} and is intended
 * to serve purpose as container for Guice scope's beans.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see AttributeHolder
 */
public final class ScopeAttributeHolder implements AttributeHolder {

    private final Map<Object, Object> attributes =
            new HashMap<Object, Object>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(final Object key) {
        return attributes.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAttribute(final Object key) {
        return attributes.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAttribute(final Object key, final Object value) {
        attributes.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttributeLock() {
        return this.attributes;
    }

}
