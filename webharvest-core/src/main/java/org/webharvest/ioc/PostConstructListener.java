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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.google.inject.ConfigurationException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * {@link TypeListener} implementation enabling Guice support for JSR-250
 * {@code @PostConstruct} annotation.
 *
 * Typically, this listener should be installed in the Guice module in the
 * following way:
 *
 * <pre>
 * public class MyModule extends AbstractModule {
 *   protected void configure() {
 *     // (...)
 *     bindListener(Matchers.any(), new PostConstructListener());
 *     // (...)
 *   }
 * }
 * </pre>
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class PostConstructListener implements TypeListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public <I> void hear(final TypeLiteral<I> type,
            final TypeEncounter<I> encounter) {

        final List<Method> hooks = getPostConstructHooks(type.getRawType());

        if (!hooks.isEmpty()) {
            for (final Method hook : hooks) {
                encounter.register(new Invoker<I>(hook));
            }
        }
    }

    private List<Method> getPostConstructHooks(final Class< ? > clazz) {
        final List<Method> methods = new LinkedList<Method>();
        for (Method m : clazz.getMethods()) {
            if (m.isAnnotationPresent(PostConstruct.class)) {
                methods.add(m);
            }
        }
        return methods;
    }

    private class Invoker<I> implements InjectionListener<I> {

        private final Method method;

        public Invoker(final Method method) {
            this.method = method;
        }

        @Override
        public void afterInjection(final I injectee) {
            try {
                method.invoke(injectee);
            } catch (IllegalAccessException cause) {
                throw new ConfigurationException(Arrays.asList(new Message(
                        cause, "Misplaced @PostConstruct annotation")));
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException) {
                    // @PostConstruct methods should not declare checked
                    // exceptions, so in case of exception during @PostConstruct
                    // annotation it should be handled here
                    throw (RuntimeException) e.getCause();
                }
                throw new ConfigurationException(Arrays.asList(new Message(
                        e.getCause(),
                        "@PostConstruct method thrown checked exception")));
            }
        }
    }
}
