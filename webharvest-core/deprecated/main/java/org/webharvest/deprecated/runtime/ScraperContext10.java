/*
 Copyright (c) 2006-2007, Vladimir Nikic
 All rights reserved.

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

 You can contact Vladimir Nikic by sending e-mail to
 nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
 subject line.
 */

package org.webharvest.deprecated.runtime;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.definition.Config;
import org.webharvest.definition.FunctionDef;
import org.webharvest.exception.VariableException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.variables.ScriptingVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.VariableName;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;
import org.webharvest.utils.Stack;
import org.webharvest.utils.SystemUtilities;

import com.google.inject.Inject;

@Deprecated
public class ScraperContext10 implements DynamicScopeContext {

    private static final Logger LOG = LoggerFactory.getLogger(Scraper.class);

    private static final String CALLER_PREFIX = "caller";

    @Inject private HttpClientManager httpClientManager;

    private Stack<Map<String, Variable>> stack = new Stack<Map<String, Variable>>();

    // map of function definitions
    @Deprecated
    private final Map<String, FunctionDef> functionDefs = new HashMap<String, FunctionDef>();

    @Deprecated
    private ScriptingLanguage scriptingLanguage;

    @Deprecated
    private String charset;

    private Config config;

    public ScraperContext10() {
        this.stack.push(new HashMap<String, Variable>());
        LOG.warn("You are using the DEPRECATED scraper configuration version. We urge you to migrate to a newer one! Please visit http://web-harvest.sourceforge.net/release.php for details.");
    }

    @PostConstruct
    public void initContext() {
        setLocalVar("sys", new ScriptingVariable(new SystemUtilities(this)));
        setLocalVar("http", new ScriptingVariable(
                httpClientManager.getHttpInfo()));
    }

    @Override
    public Variable getVar(String name) {
        // TODO rbala Currently used only for the sake of validation.
        new VariableName(name);
        int level = 0;
        while (name.startsWith(CALLER_PREFIX, level * CALLER_PREFIX.length())) {
            level++;
        }

        final List<Map<String, Variable>> mapList = stack.getList();
        if (mapList.size() > level) {
            return mapList.get(mapList.size() - 1 - level).get(name.substring(level * CALLER_PREFIX.length()));
        } else {
            throw new VariableException(MessageFormat.format("Too many ''caller.'' prefixes in the variable name ''{0}''", name));
        }
    }

    @Override
    public void setLocalVar(String key, Variable value) {
        // TODO rbala Currently used only for the sake of validation.
        new VariableName(key);
        stack.peek().put(key, value);
    }

    @Override
    public <R> R executeWithinNewContext(Callable<R> callable) throws InterruptedException {
        try {
            // No new contexts.
            // Just execute...
            return callable.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Variable replaceExistingVar(String name, Variable variable) {
        final Variable oldVar = getVar(name);
        setLocalVar(name, variable);
        return oldVar;
    }

    @Override
    public boolean containsVar(String name) {
        return getVar(name) != null;
    }

    public <R> R executeFunctionCall(Callable<R> callable) throws InterruptedException {
        // Here the context shifts.
        try {
            stack.push(new HashMap<String, Variable>());
            initContext();

            return callable.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            stack.pop();
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Iterator<KeyValuePair<Variable>> iterator() {
        return IteratorUtils.transformedIterator(stack.peek().entrySet().iterator(), new Transformer() {
            @Override
            public Object transform(Object input) {
                final Map.Entry<String, Variable> entry = (Map.Entry<String, Variable>) input;
                return new KeyValuePair<Variable>(entry.getKey(), entry.getValue());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void setLocalVar(final String name, final Object value) {
        setLocalVar(name, CommonUtil.createVariable(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public void setLocalVar(final Map<String, Object> map) {
        // FIXME rbala code moved from Scraper object. Refactore ASAP
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                setLocalVar(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public FunctionDef getFunctionDef(final String name) {
        return functionDefs.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void addFunctionDef(final FunctionDef funcDef) {
        functionDefs.put(funcDef.getName(), funcDef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public ScriptingLanguage getScriptingLanguage() {
        return scriptingLanguage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void setScriptingLanguage(final ScriptingLanguage language) {
        this.scriptingLanguage = language;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCharset() {
        return charset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCharset(final String charset) {
        this.charset = charset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    @Deprecated
    public void setConfig(final Config config) {
        this.config = config;
    }

}
