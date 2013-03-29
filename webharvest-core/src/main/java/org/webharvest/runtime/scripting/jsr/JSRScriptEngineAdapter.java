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

package org.webharvest.runtime.scripting.jsr;

import org.webharvest.exception.ScriptException;

import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.variables.ScriptingVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;

/**
 * Adapter design pattern implementation. Adapts provided
 * {@link javax.script.ScriptEngine} representing JSR-223 script engine to the
 * {@link org.webharvest.runtime.scripting.ScriptEngine} interface. This adapter
 * is universal for all scripting language implementations supporting JSR-223
 * specification.
 * <p/>
 * It is important to bear in mind that this implementation is not thread-safe,
 * that is, {@link javax.script.ScriptEngine} being adapted can not be shared
 * between multiple threads. Bindings from scraper's {@link DynamicScopeContext}
 * are copied directly to the {@link ScriptEngine}. This is performance
 * trade-off, since currently creation of brand new
 * {@link javax.script.ScriptContext} instances each time script is evaluated is
 * quite expensive.
 *
 * @see ScriptEngine
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class JSRScriptEngineAdapter implements ScriptEngine {

    private javax.script.ScriptEngine adaptee;

    /**
     * {@link JSRScriptEngineAdapter} constructor accepting reference to the
     * not-{@code null} {@link javax.script.ScriptEngine} delegate.
     *
     * @param adaptee
     *            adaptee reference; mandatory, must not be {@code null}
     */
    public JSRScriptEngineAdapter(final javax.script.ScriptEngine adaptee) {
        if (adaptee == null) {
            throw new IllegalArgumentException(
                    "Adaptee engine must not be null");
        }
        this.adaptee = adaptee;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluate(final DynamicScopeContext context,
            final ScriptSource script) {
        try {
            copyVariables(context);
            return adaptee.eval(script.getSourceCode());
        } catch (javax.script.ScriptException e) {
            throw new ScriptException(e);
        }
    }

    private void copyVariables(final DynamicScopeContext context) {
        for (KeyValuePair<Variable> pair : context) {
            final Variable value = pair.getValue();

            // FIXME: The inline condition below has been moved from the
            // first web harvest implementation of script engines
            // (org.webharvest.runtime.scripting.ScriptEngine abstract class);
            // It was required to place it here for backward compatibility,
            // however it would be neat if we just had value.getWrappedObject()
            // invocation; this way we could use in scripts wrapped objects
            // directly instead of manually unwrapping them...
            adaptee.put(pair.getKey(), (value instanceof ScriptingVariable)
                            ? value.getWrappedObject() : value);
        }
    }
}
