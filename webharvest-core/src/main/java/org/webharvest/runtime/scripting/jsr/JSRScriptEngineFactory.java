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

import javax.script.ScriptEngineManager;

import org.webharvest.Cache;
import org.webharvest.ThreadLocalCache;
import org.webharvest.exception.ConfigurationException;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.scripting.ScriptingLanguage;

/**
 * {@link ScriptEngineFactory} implementation that creates script engines based
 * on JSR-223 specification. Under the hood, factory uses
 * {@link ScriptEngineManager}, so all available scripting language
 * implementations supporting JSR-223 spec should be detected automatically.
 *
 * @see ScriptEngine
 * @see ScriptEngineFactory
 *
 * @author Michał Amerek
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class JSRScriptEngineFactory implements ScriptEngineFactory {

    /**
     * Despite of declarations, some script engines are not really thread safe
     * (for example bsh script engine storing stateful interpreter instance
     * as a class field). To overcome this problem we have to cache script
     * engines per thread. Storing script engines 'globally' may cause
     * serious issues in the multithreaded environment.
     */
    private final Cache<ScriptingLanguage, javax.script.ScriptEngine> cache =
        new ThreadLocalCache<ScriptingLanguage, javax.script.ScriptEngine>();

    private final ScriptEngineManager manager = new ScriptEngineManager();

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptEngine getEngine(final ScriptSource scriptSource) {
        return new JSRScriptEngineAdapter(getJSRScriptEngine(scriptSource));
    }

    private javax.script.ScriptEngine getJSRScriptEngine(
            final ScriptSource scriptSource) {
        final ScriptingLanguage scriptingLanguage = scriptSource.getLanguage();

        javax.script.ScriptEngine scriptEngine =
            cache.lookup(scriptingLanguage);
        if (scriptEngine != null) {
            return scriptEngine;
        }

        scriptEngine = createJSRScriptEngine(scriptingLanguage);
        cache.put(scriptingLanguage, scriptEngine);
        return scriptEngine;
    }

    /**
     * Creates {@link ScriptEngine} with usage of {@link ScriptEngineManager}.
     * Strongly depends on JSR 223 script engine providers. If no engine found
     * for the given {@code engineType} using
     * {@link ScriptEngineManager#getEngineByName(String)} then
     * {@link ConfigurationException} is thrown indicating possibly missing
     * engine provider.
     */
    private javax.script.ScriptEngine createJSRScriptEngine(
            final ScriptingLanguage scriptingLanguage) {
        final String engineType = scriptingLanguage.name().toLowerCase();
        final javax.script.ScriptEngine engine = manager.getEngineByName(
                engineType);

        if (engine == null) {
            throw new ConfigurationException("No script engine found for "
                    + "name: '" + engineType + "'; Possibly missing provider");
        }
        return engine;
    }
}
