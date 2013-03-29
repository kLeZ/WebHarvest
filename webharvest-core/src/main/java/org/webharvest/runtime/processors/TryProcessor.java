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

import java.util.concurrent.Callable;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.webharvest.annotation.Definition;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.TryDef;
import org.webharvest.exception.BaseException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * OnError processor - sets .
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "try", validAttributes = { "id" },
        validSubprocessors = { "body", "catch" },
        requiredAttributes = { "body", "catch" }, definitionClass = TryDef.class )
public class TryProcessor extends AbstractProcessor<TryDef> {

    public Variable execute(final DynamicScopeContext context) throws InterruptedException {
        try {
            IElementDef tryBodyDef = elementDef.getTryBodyDef();
            Variable result = new BodyProcessor.Builder(tryBodyDef).
                setParentProcessor(this).build().run(context);
            debug(tryBodyDef, context, result);

            return result;
        } catch (final BaseException e) {
            if (Thread.interrupted()) {
                Thread.currentThread().interrupt();
                final int interruptedExceptionIndexInChain = ExceptionUtils.indexOfType(e, InterruptedException.class);
                throw (interruptedExceptionIndexInChain < 0)
                        ? new InterruptedException(e.getMessage())
                        : (InterruptedException) ExceptionUtils.getThrowableList(e).get(interruptedExceptionIndexInChain);
            }
            //
            //  Fri Feb 22 17:32:06 2013 -- Scott R. Turner
            //
            //  Make this a "warning" level log message so that it shows up even
            //  when the INFO messages are muted.
            //
            LOG.warn("Exception caught with try processor: {}", e.getMessage());

            return context.executeWithinNewContext(new Callable<Variable>() {
                @Override
                public Variable call() throws Exception {
                    //
                    //  Fri Feb 22 17:32:37 2013 -- Scott R. Turner
                    //
                    //  Changed _error to error to match 2.1 variable name requirements.
                    //  This is dubious code, since it might overwrite an "error" variable
                    //  provided by the user.
                    //
                    context.setLocalVar("error", CommonUtil.createVariable(e));
                    final IElementDef catchValueDef = elementDef.getCatchValueDef();
                    final Variable res =
                        new BodyProcessor.Builder(catchValueDef).build().
                            run(context);
                    debug(catchValueDef, context, res);
                    return res;
                }
            });
        }
    }

}