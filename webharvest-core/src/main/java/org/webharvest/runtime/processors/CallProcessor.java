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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.CallDef;
import org.webharvest.definition.FunctionDef;
import org.webharvest.exception.BaseException;
import org.webharvest.exception.FunctionException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

/**
 * Function call processor.
 */
/**
 * Function call processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE })
@Definition(value = "call", validAttributes = { "id", "name" },
        requiredAttributes = "name", definitionClass = CallDef.class)
public class CallProcessor extends AbstractProcessor<CallDef> {

    private Map<String, Variable> functionParams =
        new HashMap<String, Variable>();
    private Variable functionResult = new NodeVariable("");

    public Variable execute(final DynamicScopeContext context) throws InterruptedException {
        String functionName = BaseTemplater.evaluateToString(elementDef.getName(), null, context);
        final FunctionDef functionDef = context.getFunctionDef(functionName);

        this.setProperty("Name", functionName);

        if (functionDef == null) {
            throw new FunctionException("Function \"" + functionName + "\" is undefined!");
        }

        // executes body of call processor
        new BodyProcessor.Builder(elementDef).setParentProcessor(this).
            build().execute(context);

        doCall(context, new Callable<Object>() {

            @Override
            public Object call() throws InterruptedException {
                for (Map.Entry<String, Variable> entry : functionParams.entrySet()) {
                    context.setLocalVar(entry.getKey(), entry.getValue());
                }

                // executes body of function using new context
                new BodyProcessor.Builder(functionDef).
                    setParentProcessor(CallProcessor.this).build().
                        execute(context);
                return null;
            }
        });

        return functionResult;
    }

    protected void doCall(DynamicScopeContext context, Callable<Object> callable) throws InterruptedException {
        try {
            callable.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(e);
        }
    }

    public void setFunctionResult(Variable result) {
        this.functionResult = result;
    }

    /**
     * Adds parameter of function which is going to call.
     *
     * @param name
     *            name of the parameter
     * @param value
     *            value of the parameter
     */
    public void addFunctionParam(final String name, final Variable value) {
        functionParams.put(name, value);
    }

}