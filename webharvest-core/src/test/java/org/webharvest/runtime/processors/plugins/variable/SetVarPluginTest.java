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

import static org.webharvest.runtime.processors.plugins.PluginTestUtils.createPlugin;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.webharvest.UnitilsTestNGExtension;
import org.webharvest.definition.XmlNodeTestUtils;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngineFactory;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.scripting.jsr.JSRScriptEngineFactory;
import org.webharvest.runtime.variables.NodeVariable;

@SuppressWarnings({"unchecked"})
public class SetVarPluginTest extends UnitilsTestNGExtension {

    ScraperContext context;

    @BeforeMethod
    public void before() {
        context = new ScraperContext();
        context.setScriptingLanguage(ScriptingLanguage.GROOVY);
    }

    @Test
    public void testExecutePlugin_valueAsAttr() throws Exception {
        context.setLocalVar("name", new NodeVariable("World"));

        createPlugin(XmlNodeTestUtils.createXmlNode(
                "<set var='greetings' value='Hello, ${name}!'/>",
                XmlNodeTestUtils.NAMESPACE_21),
                SetVarPlugin.class).executePlugin(context);

        ReflectionAssert.assertReflectionEquals(
                new NodeVariable("Hello, World!"),
                context.getVar("greetings"));
    }

    @Test
    public void testExecutePlugin_valueAsBody() throws Exception {
        context.setLocalVar("name", new NodeVariable("World"));
        createPlugin(XmlNodeTestUtils.createXmlNode(
                "<set var='greetings'><template>Hello, ${name}!</template></set>",
                XmlNodeTestUtils.NAMESPACE_21),
                SetVarPlugin.class).executePlugin(context);
        ReflectionAssert.assertReflectionEquals(
                new NodeVariable("Hello, World!"),
                context.getVar("greetings"));
    }

    @Test
    public void testExecutePlugin_bodyIgnoredWhenAttrSpecified() throws Exception {
        createPlugin(XmlNodeTestUtils.createXmlNode(
                "<set var='x' value='actual'>ignored</set>",
                XmlNodeTestUtils.NAMESPACE_21),
                SetVarPlugin.class).executePlugin(context);

        ReflectionAssert.assertReflectionEquals(
                new NodeVariable("actual"),
                context.getVar("x"));
    }

    @Test
    public void testExecutePlugin_replace() throws Exception {
        context.setLocalVar("x", new NodeVariable("old"));

        createPlugin(XmlNodeTestUtils.createXmlNode(
                "<set var='x' value='new'/>",
                XmlNodeTestUtils.NAMESPACE_21),
                SetVarPlugin.class).executePlugin(context);

        ReflectionAssert.assertReflectionEquals(
                new NodeVariable("new"),
                context.getVar("x"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptEngineFactory getScriptEngineFactory() {
        return new JSRScriptEngineFactory();
    }

}
