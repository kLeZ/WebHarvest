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

import java.util.Stack;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * A {@code Scope} that uses an {@link AttributeHolder} as the backing store for
 * its scoped objects. {@link AttributeHolderScope} supports nested scopes, that
 * is, client code can enter scope as many times as required. Nested scopes are
 * separated from each other.
 *
 * Based on work of Matthias Treydte <waldheinz at gmail.com>.
 *
 * @author Robert Bala
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public class AttributeHolderScope<AHT extends AttributeHolder>
        implements Scope, Provider<AHT> {

    private final ThreadLocal<Stack<AHT>> holder =
        new ThreadLocal<Stack<AHT>>() {
            protected java.util.Stack<AHT> initialValue() {
                return new Stack<AHT>();
            };
    };

    /**
     * Lets the current {@code Thread} enter this {@code Scope}.
     *
     * @param holder
     *            the {@link AttributeHolder} instance for the {@code Scope}
     */
    public void enter(final AHT holder) {
        if (holder == null) {
            throw new IllegalArgumentException();
        }

        this.holder.get().add(holder);
    }

    /**
     * Lets the current {@code Thread} leave this {@code Scope}.
     *
     * @throws OutOfScopeException if the current thread is not in
     *      this {@code Scope}
     */
    public void exit() throws OutOfScopeException {
        assertInScope();
        this.holder.get().pop();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@docRoot}
     * @throws OutOfScopeException if the current thread is not in
     *      this {@code Scope}
     */
    @Override
    public AHT get() throws OutOfScopeException {
        assertInScope();
        return this.holder.get().peek();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final <T> Provider<T> scope(
            final Key<T> key, final Provider<T> outer) {

        return new Provider<T>() {

            @Override
            public T get() {
                assertInScope();

                final AttributeHolder ah = holder.get().peek();

                synchronized (ah.getAttributeLock()) {
                    T current = (T) ah.getAttribute(key);

                    if ((current == null) && !ah.hasAttribute(key)) {
                        current = outer.get();
                        ah.putAttribute(key, current);
                    }

                    return current;
                }
            }

            @Override
            public String toString() {
                return "Provider [scope=" + //NOI18N
                        AttributeHolderScope.this.getClass().getSimpleName() +
                        ", outer=" + outer.toString() + "]"; //NOI18N
            }

        };
    }

    private void assertInScope() throws OutOfScopeException {
        if (holder.get().isEmpty()) {
            throw new OutOfScopeException("not in "
                    + getClass().getSimpleName()); //NOI18N
        }
    }

}
