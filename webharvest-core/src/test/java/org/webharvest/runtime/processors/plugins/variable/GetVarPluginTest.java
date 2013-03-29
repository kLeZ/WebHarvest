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

package org.webharvest.runtime.processors.plugins.variable;

import static java.util.Arrays.asList;
import static org.webharvest.runtime.processors.plugins.PluginTestUtils.createPlugin;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.mock.Mock;
import org.unitils.reflectionassert.ReflectionAssert;
import org.webharvest.UnitilsTestNGExtension;
import org.webharvest.definition.XmlNodeTestUtils;
import org.webharvest.exception.VariableException;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.scripting.jsr.JSRScriptEngineFactory;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;

public class GetVarPluginTest extends UnitilsTestNGExtension {

    Mock<ScraperContext> contextMock;

    @BeforeMethod
    public void before() {
        contextMock.returns(ScriptingLanguage.GROOVY).getScriptingLanguage();
    }

    @Test
    public void testExecutePlugin() throws Exception {
        contextMock.returns(new NodeVariable(123)).getVar("x");

        ReflectionAssert.assertReflectionEquals(
                new NodeVariable(123),
                createPlugin(XmlNodeTestUtils.createXmlNode("<get var='x'/>",
                        XmlNodeTestUtils.NAMESPACE_21), GetVarPlugin.class).executePlugin(contextMock.getMock()));

        contextMock.assertInvoked().getVar("x");
    }

    @Test
    public void testExecutePlugin_null() throws Exception {
        contextMock.returns(EmptyVariable.INSTANCE).getVar("empty");

        Assert.assertSame(
                createPlugin(XmlNodeTestUtils.createXmlNode("<get var='empty'/>",
                        XmlNodeTestUtils.NAMESPACE_21), GetVarPlugin.class).executePlugin(contextMock.getMock()),
                EmptyVariable.INSTANCE);

        contextMock.assertInvoked().getVar("empty");
    }

    @Test
    public void testExecutePlugin_templateAsVarName() throws Exception {
        final NodeVariable v123 = new NodeVariable(123);

        contextMock.
                returns(v123).
                getVar("x7");
        contextMock.
                returns(asList(new KeyValuePair<Variable>("x7", v123)).iterator()).
                iterator();

        Assert.assertEquals(
                createPlugin(XmlNodeTestUtils.createXmlNode("<get var='x${5+2}'/>",
                        XmlNodeTestUtils.NAMESPACE_21), GetVarPlugin.class).executePlugin(contextMock.getMock()),
                v123);

        contextMock.assertInvoked().getVar("x7");
    }

    @Test(expectedExceptions = VariableException.class)
    public void testExecutePlugin_notDefined() throws Exception {
        createPlugin(XmlNodeTestUtils.createXmlNode("<get var='not defined'/>",
                XmlNodeTestUtils.NAMESPACE_21), GetVarPlugin.class).executePlugin(contextMock.getMock());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptEngineFactory getScriptEngineFactory() {
        return new JSRScriptEngineFactory();
    }

}
