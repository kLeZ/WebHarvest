/*  Copyright (c) 2006-2008, Vladimir Nikic
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

import java.util.Map;

import org.webharvest.definition.WebHarvestPluginDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.Assert;
import org.webharvest.utils.CommonUtil;

/**
 * Base for all user-defined plugins.
 */
public abstract class WebHarvestPlugin extends AbstractProcessor<WebHarvestPluginDef> {

    /**
     * {@inheritDoc}
     */
    @Override
    public final Variable execute(DynamicScopeContext context) throws InterruptedException {
        // pre processing
        final Variable variable = executePlugin(context);
        Assert.notNull(variable, "Plugin {0} returned 'null' instead of 'empty'", getClass().getName());
        // post processing
        return variable;
    }

    /**
     * Mathod that actually executes processor. Since one instance of this class may
     * be used for multiple executions, creator of plugin is responsible for initiating
     * local variables at the beginning of this method.
     *
     * @deprecated Use execute(...) instead
     * @param context
     * @return Instance of variable as result of execution.
     */
    @Deprecated
    public abstract Variable executePlugin(DynamicScopeContext context) throws InterruptedException;

    /**
     * @return Map of attributes of this plugin
     */
    @Deprecated
    // FIXME We can use definition object directly
    protected Map<String, String> getAttributes() {
        return elementDef.getAttributes();
    }

    /**
     * @return Map of attributes of this plugin
     */
    private Map<String, String> getAttributes(String uri) {
        return elementDef.getAttributes(uri);
    }

    /**
     * @param attName Name of attribute
     * @param scraper
     * @return Value of specified attribute, or null if attribute doesn't exist
     */
    private String evaluateAttribute(String attName, String uri, DynamicScopeContext context) {
        return BaseTemplater.evaluateToString(getAttributes(uri).get(attName), null, context);
    }

    protected String evaluateAttribute(String attName, DynamicScopeContext context) {
        return evaluateAttribute(attName, elementDef.getUri(), context);
    }

    protected boolean evaluateAttributeAsBoolean(String attName, boolean defaultValue, DynamicScopeContext context) {
        return CommonUtil.getBooleanValue(evaluateAttribute(attName, elementDef.getUri(), context), defaultValue);
    }

    protected int evaluateAttributeAsInteger(String attName, int defaultValue, DynamicScopeContext context) {
        return CommonUtil.getIntValue(evaluateAttribute(attName, elementDef.getUri(), context), defaultValue);
    }

    protected double evaluateAttributeAsDouble(String attName, double defaultValue, DynamicScopeContext context) {
        return CommonUtil.getDoubleValue(evaluateAttribute(attName, elementDef.getUri(), context), defaultValue);
    }

    /**
     * Executes body of plugin processor
     *
     *
     * @param scraper
     * @param context
     * @return Instance of Variable
     */
    protected Variable executeBody(DynamicScopeContext context) throws InterruptedException {
        return new BodyProcessor.Builder(elementDef).setParentProcessor(this).
            build().execute(context);
    }

}