package org.webharvest.runtime.scripting.jsr;

import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.scripting.ScriptingLanguage;

// We can't do more as testing if engines for all scripting languages declared
// in web harvest documentation as supported are accessible...
public class JSRScriptEngineFactoryTest {

    private JSRScriptEngineFactory factory;

    @BeforeMethod
    public void setUp() {
        this.factory = new JSRScriptEngineFactory();
    }

    @AfterMethod
    public void tearDown() {
        this.factory = null;
    }

    @Test
    public void getJSRBeanshellEngine() {
        final ScriptEngine engine = factory.getEngine(
                new ScriptSource("a = 2", ScriptingLanguage.BEANSHELL));
        assertNotNull("Null script engine", engine);
    }

    @Test
    public void getGroovyEngine() {
        final ScriptEngine engine = factory.getEngine(new ScriptSource(
                "def name = 'mashup'", ScriptingLanguage.GROOVY));
        assertNotNull("Null script engine", engine);
    }

    @Test
    public void getJavascriptEngine() {
        final ScriptEngine engine = factory.getEngine(new ScriptSource(
                "var x = 123", ScriptingLanguage.JAVASCRIPT));
        assertNotNull("Null script engine", engine);
    }
}
