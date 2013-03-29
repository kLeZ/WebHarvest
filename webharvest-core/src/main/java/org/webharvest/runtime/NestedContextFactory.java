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

package org.webharvest.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Factory capable of creating nested {@link DynamicScopeContext} instances.
 * Nested context inherits some properties from the parent context and overrides
 * some other properties. Changes of properties being overridden should not be
 * propagated to the parent context. That is, if the nested context declares
 * another charset or scripting language as a default one, defaults of parent
 * context should remain unchanged.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class NestedContextFactory {

    /**
     * Properties being overwritten by the nested (child) context. Changes
     * of these properties affecting nested context are not propagated
     * to the parent.
     */
    private static final String[] SHADED_PROPERTIES = {
        "charset", "scriptingLanguage"
    };

    private NestedContextFactory() { }

    /**
     * Creates new instance of {@link DynamicScopeContext}. Newly created
     * context is nested in the provided parent context (please consult class
     * javadoc for more details).
     *
     * @param parent
     *            parent context reference (must not be {@code null})
     * @return new instance of {@link DynamicScopeContext} nested in the
     *         provided parent context.
     */
    public static DynamicScopeContext create(final DynamicScopeContext parent) {
        if (parent == null) {
            throw new IllegalArgumentException(
                    "Parent context must not be null");
        }

        return (DynamicScopeContext) Proxy.newProxyInstance(
                NestedContextFactory.class.getClassLoader(),
                new Class< ? >[] {DynamicScopeContext.class},
                new PropertyShadeHandler(parent, SHADED_PROPERTIES));
    }

    private static class PropertyShadeHandler
            implements InvocationHandler {

        private final Map<String, Object> values =
            new HashMap<String, Object>();

        private final List<String> shadedProperties;
        private final DynamicScopeContext delegate;

        public PropertyShadeHandler(final DynamicScopeContext delegate,
                final String... shadedProperties) {
            this.delegate = delegate;

            this.shadedProperties = new LinkedList<String>();
            for (String property : shadedProperties) {
                this.shadedProperties.add(property.toLowerCase());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object invoke(final Object proxy,
                final Method method, final Object[] args) throws Throwable {
            if (isGetter(method) && isShaded(method)) {
                return values.get(getPropertyName(method));
            }
            if (isSetter(method) && isShaded(method)) {
                values.put(getPropertyName(method), args[0]);
                return null;
            }

            return method.invoke(delegate, args);
        }

        private boolean isGetter(final Method method) {
            return method.getName().startsWith("get");
        }

        private boolean isSetter(final Method method) {
            return method.getName().startsWith("set");
        }

        private boolean isShaded(final Method method) {
            return shadedProperties.contains(getPropertyName(method));
        }

        private String getPropertyName(final Method method) {
            return method.getName().substring(3).toLowerCase();
        }
    }
}
