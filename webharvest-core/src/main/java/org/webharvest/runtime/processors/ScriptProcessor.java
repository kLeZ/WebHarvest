/*  Copyright (c) 2006-2007, Vladimir Nikic
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
package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.ScriptDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import com.google.inject.Inject;

/**
 * Script processor - executes script defined in the body.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "script", validAttributes = { "id", "language", "return" },
        definitionClass = ScriptDef.class)
public class ScriptProcessor extends AbstractProcessor<ScriptDef> {

    @Inject
    private ScriptEngineFactory scriptEngineFactory;

    public Variable execute(DynamicScopeContext context) throws InterruptedException {
        String sourceCode = getBodyTextContent(elementDef, context).toString();

        // For backward compatibility with ver.2b1 only!
        final String returnExpression = elementDef.getReturnExpression();
        if (returnExpression != null) {
            sourceCode += ("; " + BaseTemplater.evaluateToString(returnExpression, null, context));
        }

        final ScriptSource scriptSource = new ScriptSource(sourceCode,
                evaluateScriptingLanguage(context));
        return CommonUtil.createVariable(scriptEngineFactory
                .getEngine(scriptSource).evaluate(context, scriptSource));
    }

    private ScriptingLanguage evaluateScriptingLanguage(
            final DynamicScopeContext context) {
        ScriptingLanguage language = ScriptingLanguage.recognize(
                BaseTemplater.evaluateToString(elementDef.getLanguage(),
                        null, context));
        return language != null ? language : context.getScriptingLanguage();
    }
}