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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.XPathDef;
import org.webharvest.exception.ScraperXPathException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.XmlUtil;

import com.google.inject.Inject;

/**
 * XQuery processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "xpath", validAttributes = { "id", "expression", "v:*" },
        definitionClass = XPathDef.class )
public class XPathProcessor extends AbstractProcessor<XPathDef> {

    @Inject
    private RuntimeConfig runtimeConfig;

    public Variable execute(DynamicScopeContext context) throws InterruptedException {
        Variable xml = getBodyTextContent(elementDef, context);
        String expression = BaseTemplater.evaluateToString(elementDef.getExpression(), null, context);
        if (expression != null) {
            this.setProperty("Expression", expression);
        }

        Map<String, String> varMap = elementDef.getVariableMap();
        Map<String, String> evaluatedVarMap = new HashMap<String, String>();
        for ( Map.Entry<String, String> attEntry: varMap.entrySet() ) {
            String varName = attEntry.getKey();
            String varValue = BaseTemplater.evaluateToString(attEntry.getValue(), null, context);
            evaluatedVarMap.put(varName, varValue);
            this.setProperty(varName, varValue);
        }

        String xpathExpression = null;

        try {
            StaticQueryContext sqc = runtimeConfig.getStaticQueryContext();
            Configuration config = sqc.getConfiguration();

            DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
            StringReader reader = new StringReader(xml.toString());

            dynamicContext.setContextItem(sqc.buildDocument(new StreamSource(reader)));

            for ( Map.Entry<String, String> attEntry: evaluatedVarMap.entrySet() ) {
                String varName = attEntry.getKey();
                xpathExpression = attEntry.getValue();
                XQueryExpression exp = runtimeConfig.getXQueryExpressionPool().getCompiledExpression(xpathExpression);
                ListVariable xpathResult = XmlUtil.createListOfXmlNodes(exp, dynamicContext);
                context.setLocalVar(varName, xpathResult);
            }

            if (expression != null) {
                xpathExpression = expression;
                XQueryExpression exp = runtimeConfig.getXQueryExpressionPool().getCompiledExpression(xpathExpression);
                return XmlUtil.createListOfXmlNodes(exp, dynamicContext);
            } else {
                return EmptyVariable.INSTANCE;
            }
        } catch (XPathException e) {
            throw new ScraperXPathException("Error parsing XPath expression (XPath = [" + xpathExpression + "])!", e);
        }
    }

}