package org.webharvest.runtime.scripting.jsr;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;
import static org.unitils.mock.ArgumentMatchers.same;
import static org.unitils.mock.ArgumentMatchers.eq;

import java.util.HashSet;
import java.util.Set;

import javax.script.ScriptException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.scripting.ScriptSource;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.KeyValuePair;

public class JSRScriptEngineAdapterTest extends UnitilsTestNG {

    private static final String SCRIPT = "def name = 'Dummy script';";

    private ScriptSource scriptSource;

    private Mock<DynamicScopeContext> mockContext;

    private Mock<javax.script.ScriptEngine> adaptee;

    private JSRScriptEngineAdapter adapter;

    @BeforeMethod
    public void setUp() {
        this.scriptSource = new ScriptSource(SCRIPT, ScriptingLanguage.GROOVY);
        this.adapter = new JSRScriptEngineAdapter(adaptee.getMock());
    }

    @AfterMethod
    public void tearDown() {
        this.scriptSource = null;
        this.adapter = null;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullAdapteeNotAllowed() {
        new JSRScriptEngineAdapter(null);
    }

    @Test
    public void isDelegateInvoked() throws Exception {
        final Object adapteeResult = new Object();
        mockContext.returns(new HashSet<KeyValuePair<Variable>>().iterator())
            .iterator();
        adaptee.returns(adapteeResult).eval(same(SCRIPT));

        final Object result = adapter.evaluate(mockContext.getMock(),
                scriptSource);

        mockContext.assertInvoked().iterator();
        adaptee.assertInvoked().eval(same(SCRIPT));
        assertSame("Unexpected script result", adapteeResult, result);
    }

    @Test
    public void areContextVariablesCopied() {
        final Variable value1 = new NodeVariable("var1value");
        final Variable value2 = new NodeVariable("var2value");

        final Set<KeyValuePair<Variable>> variables =
            new HashSet<KeyValuePair<Variable>>();
        variables.add(new KeyValuePair<Variable>("var1name", value1));
        variables.add(new KeyValuePair<Variable>("var2name", value2));

        mockContext.returns(variables.iterator()).iterator();

        adapter.evaluate(mockContext.getMock(), scriptSource);

        mockContext.assertInvoked().iterator();
        adaptee.assertInvoked().put(eq("var1name"), same(value1));
        adaptee.assertInvoked().put(eq("var2name"), same(value2));
    }

    @Test
    public void evaluateInCaseOfException() throws Exception {
        mockContext.returns(new HashSet<KeyValuePair<Variable>>().iterator())
            .iterator();
        adaptee.raises(new ScriptException("test")).eval((String) null);

        try {
            adapter.evaluate(mockContext.getMock(), scriptSource);
            fail("ScriptException expected");
        } catch (org.webharvest.exception.ScriptException e) {
            // ok, it's expected
        }
        adaptee.assertInvoked().eval(same(SCRIPT));
    }
}
