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

package org.webharvest.runtime;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import org.webharvest.definition.Config;
import org.webharvest.definition.FunctionDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 20, 2010
 * Time: 9:32:52 PM
 */
public interface DynamicScopeContext extends Iterable<KeyValuePair<Variable>> {

    Variable getVar(String name);

    void setLocalVar(String name, Variable value);

    void setLocalVar(String name, Object value);

    void setLocalVar(Map<String, Object> map);

    <R> R executeWithinNewContext(Callable<R> callable) throws InterruptedException;

    Variable replaceExistingVar(String name, Variable variable);

    boolean containsVar(String name);

    @Deprecated
    FunctionDef getFunctionDef(String name);

    @Deprecated
    void addFunctionDef(FunctionDef funcDef);

    @Deprecated
    ScriptingLanguage getScriptingLanguage();

    @Deprecated
    void setScriptingLanguage(final ScriptingLanguage language);

    /**
     * Returns default configuration's charset.
     *
     * @return default configuration's charset.
     */
    String getCharset();

    /**
     * Sets default configuration's charset.
     *
     * @param charset
     *            new default configuration's charset
     */
    void setCharset(String charset);

    Config getConfig();

    @Deprecated
    void setConfig(Config config);

}
