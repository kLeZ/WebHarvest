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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.XQueryDef;
import org.webharvest.definition.XQueryExternalParamDef;
import org.webharvest.exception.ScraperXQueryException;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;
import org.webharvest.utils.XmlUtil;

import com.google.inject.Inject;

/**
 * XQuery processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "xquery", validAttributes = { "id" },
        validSubprocessors = { "xq-param", "xq-expression" },
        requiredSubprocessors = "xq-expression",
        definitionClass = XQueryDef.class)
public class XQueryProcessor extends AbstractProcessor<XQueryDef> {

    public static final Set<String> ALLOWED_PARAM_TYPES = new TreeSet<String>();
    public static String DEFAULT_PARAM_TYPE = "node()";

    // initialize set of allowed parameter types

    static {
        ALLOWED_PARAM_TYPES.add("node()");
        ALLOWED_PARAM_TYPES.add("node()*");
        ALLOWED_PARAM_TYPES.add("integer");
        ALLOWED_PARAM_TYPES.add("integer*");
        ALLOWED_PARAM_TYPES.add("long");
        ALLOWED_PARAM_TYPES.add("long*");
        ALLOWED_PARAM_TYPES.add("float");
        ALLOWED_PARAM_TYPES.add("float*");
        ALLOWED_PARAM_TYPES.add("double");
        ALLOWED_PARAM_TYPES.add("double*");
        ALLOWED_PARAM_TYPES.add("boolean");
        ALLOWED_PARAM_TYPES.add("boolean*");
        ALLOWED_PARAM_TYPES.add("string");
        ALLOWED_PARAM_TYPES.add("string*");
    }

    @Inject
    private RuntimeConfig runtimeConfig;

    public Variable execute(DynamicScopeContext context) throws InterruptedException {
        IElementDef xqueryElementDef = elementDef.getXqDef();
        Variable xq = getBodyTextContent(xqueryElementDef, context, true);
        debug(xqueryElementDef, context, xq);

        String xqExpression = xq.toString().trim();
        XQueryExternalParamDef[] externalParamDefs = elementDef.getExternalParamDefs();

        final StaticQueryContext sqc = runtimeConfig.getStaticQueryContext();
        final Configuration config = sqc.getConfiguration();

        try {
            final XQueryExpression exp = runtimeConfig.getXQueryExpressionPool().getCompiledExpression(xqExpression);
            final DynamicQueryContext dynamicContext = new DynamicQueryContext(config);

            // define external parameters
            for (XQueryExternalParamDef externalParamDef : externalParamDefs) {
                String externalParamName = BaseTemplater.evaluateToString(externalParamDef.getName(), null, context);
                String externalParamType = BaseTemplater.evaluateToString(externalParamDef.getType(), null, context);
                if (externalParamType == null) {
                    externalParamType = DEFAULT_PARAM_TYPE;
                }

                // check if param type is one of allowed
                if (!ALLOWED_PARAM_TYPES.contains(externalParamType)) {
                    throw new ScraperXQueryException("Type " + externalParamType + " is not allowed. Use one of " + ALLOWED_PARAM_TYPES.toString());
                }

                if (externalParamType.endsWith("*")) {
                    BodyProcessor bodyProcessor =
                        new BodyProcessor.Builder(externalParamDef).
                            setParentProcessor(this).build();
                    bodyProcessor.setProperty("Name", externalParamName);
                    bodyProcessor.setProperty("Type", externalParamType);
                    Variable variable = bodyProcessor.run(context);
                    debug(externalParamDef, context, variable);

                    List<Object> paramList = new ArrayList<Object>();
                    for (Object o : variable.toList()) {
                        Variable currVar = (Variable) o;
                        paramList.add(castSimpleValue(externalParamType, currVar, sqc));
                    }

                    dynamicContext.setParameter(externalParamName, paramList);
                } else {
                    KeyValuePair props[] = {new KeyValuePair<String>("Name", externalParamName), new KeyValuePair<String>("Type", externalParamType)};
                    Variable var = getBodyTextContent(externalParamDef, context, true, props);

                    debug(externalParamDef, context, var);

                    Object value = castSimpleValue(externalParamType, var, sqc);
                    dynamicContext.setParameter(externalParamName, value);
                }
            }

            return XmlUtil.createListOfXmlNodes(exp, dynamicContext);
        } catch (XPathException e) {
            throw new ScraperXQueryException("Error executing XQuery expression (XQuery = [" + xqExpression + "])!", e);
        }
    }

    /**
     * For the specified type, value and static query context, returns proper Java typed value.
     *
     * @param type
     * @param value
     * @param sqc
     * @return
     * @throws XPathException
     */
    private Object castSimpleValue(String type, Variable value, StaticQueryContext sqc) throws XPathException {
        type = type.toLowerCase();

        if (type.startsWith("node()")) {
            StringReader reader = new StringReader(value.toString());
            return sqc.buildDocument(new StreamSource(reader));
        } else if (type.startsWith("integer")) {
            return new Integer(value.toString().trim());
        } else if (type.startsWith("long")) {
            return new Long(value.toString().trim());
        } else if (type.startsWith("float")) {
            return new Float(value.toString().trim());
        } else if (type.startsWith("double")) {
            return new Double(value.toString().trim());
        } else if (type.startsWith("boolean")) {
            return CommonUtil.isBooleanTrue(value.toString()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            return value.toString();
        }
    }

}