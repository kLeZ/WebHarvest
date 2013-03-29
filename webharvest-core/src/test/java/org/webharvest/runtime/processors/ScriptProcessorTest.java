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

package org.webharvest.runtime.processors;

import static org.webharvest.runtime.scripting.ScriptingLanguage.GROOVY;
import static org.webharvest.runtime.scripting.ScriptingLanguage.JAVASCRIPT;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.mock.Mock;
import org.unitils.mock.core.MockObject;
import org.unitils.reflectionassert.ReflectionAssert;
import org.webharvest.UnitilsTestNGExtension;
import org.webharvest.definition.XmlNodeTestUtils;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.variables.NodeVariable;

/**
 * Created by IntelliJ IDEA.
 * User: awajda
 * Date: Sep 27, 2010
 * Time: 12:14:12 AM
 */
public class ScriptProcessorTest extends UnitilsTestNGExtension {

    ScraperContext context;
    Mock<ScriptEngine> engineMock;

    @BeforeMethod
    public void before() {
        context = new ScraperContext();
        context.setScriptingLanguage(JAVASCRIPT); // default scripting language
        scriptEngineFactoryMock.returns(engineMock.getMock()).getEngine(null);
    }

    @Test
    public void testExecute_defaultLang() throws Exception {
        engineMock.returns("my val").evaluate(context, null);

        final Processor processor =
            ProcessorTestUtils.processor(
                    XmlNodeTestUtils.createXmlNode("" +
                    "<script><![CDATA[ some script here ]]></script>",
                    XmlNodeTestUtils.NAMESPACE_10));

        ReflectionAssert.assertReflectionEquals(
                new NodeVariable("my val"),
                processor.run(context));

        scriptEngineFactoryMock.assertInvoked().
                getEngine(new ScriptSource("some script here", JAVASCRIPT));
    }

    @Test
    public void testExecute_specifiedLang() throws Exception {
        engineMock.returns("my val").evaluate(context, null);

        final Processor processor =
            ProcessorTestUtils.processor(
                    XmlNodeTestUtils.createXmlNode("" +
                    "<script language='groovy'><![CDATA[ some script here ]]></script>",
                    XmlNodeTestUtils.NAMESPACE_10));

        ReflectionAssert.assertReflectionEquals(
                new NodeVariable("my val"),
                processor.run(context));

        scriptEngineFactoryMock.assertInvoked().
                getEngine(new ScriptSource("some script here", GROOVY));
    }

    @Test
    public void testExecute_langGroovy() throws Exception {
        engineMock.returns("my val").evaluate(context, null);


        final Processor processor =
            ProcessorTestUtils.processor(
                    XmlNodeTestUtils.createXmlNode("" +
                    "<script language='groovy'><![CDATA[ some script here ]]></script>",
                    XmlNodeTestUtils.NAMESPACE_10));

        ReflectionAssert.assertReflectionEquals(
                new NodeVariable("my val"),
                processor.run(context));

        scriptEngineFactoryMock.assertInvoked().
                getEngine(new ScriptSource("some script here", GROOVY));
    }

    @Test
    public void testExecute_BACKWARD_COMPAT_with_ver2b1() throws Exception {
        final Mock<ScriptEngine> templaterMock = new MockObject<ScriptEngine>(
                "Templater mock", ScriptEngine.class, this);

        scriptEngineFactoryMock.
                returns(templaterMock.getMock()).
                getEngine(new ScriptSource("myReturnExpr", JAVASCRIPT));

        templaterMock.returns("my return code").evaluate(context, null);

        ProcessorTestUtils.processor(XmlNodeTestUtils.createXmlNode("" +
                "<script return='${myReturnExpr}'><![CDATA[ some script here ]]></script>",
                XmlNodeTestUtils.NAMESPACE_10)).run(context);

        scriptEngineFactoryMock.assertInvoked().
                getEngine(new ScriptSource("some script here; my return code",
                        JAVASCRIPT));
    }

}
