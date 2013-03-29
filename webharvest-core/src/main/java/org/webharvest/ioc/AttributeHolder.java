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

import java.util.Set;

/**
 * Implementors of this interface can serve as the backing store for
 * Objects that are scoped within an (subclass of) {@link AttributeHolderScope}.
 * Based on work of Matthias Treydte <waldheinz at gmail.com>.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface AttributeHolder {

    /**
     * Extracts the {@code Object} memorized for the specified key from this
     * {@code AttributeHolder}.
     *
     * @param key the identifier for the attribute to extract
     * @return the {@code Object} stored for the specified key, or {@code null}
     *      if either the {@code null} value was stored for this key or there
     *      is no attribute stored for the key
     * @see #hasAttribute(java.lang.Object) to discriminate the two reasons
     *      this method may return {@code null}
     */
    public Object getAttribute(Object key);

    /**
     * Decides if this {@code AttributeHolder} has an association for the
     * specified key.
     *
     * @param key the key to check if it's known to this {@code AttributeHolder}
     * @return if this key is known
     */
    public boolean hasAttribute(Object key);

    /**
     * Stores a new value in this {@code AttributeHolder}.
     *
     * @param key the key to identify the new attribute
     * @param value the new attribute
     */
    public void putAttribute(Object key, Object value);

    /**
     * Returns an object on which to lock when access to multiple methods of
     * the {@code AttributeHolder} are to be made atomic.
     *
     * @return the {@code Object} to synchronize on
     */
    public Object getAttributeLock();

}
