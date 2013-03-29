package org.webharvest.runtime.scripting;

import org.testng.annotations.Test;

public class ScriptSourceTest {

    private static final String SCRIPT_SOURCE = "i = 1";

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullLanguageIsNotAllowed() {
        new ScriptSource(SCRIPT_SOURCE, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void nullScriptSourceIsNotAllowed() {
        new ScriptSource(null, ScriptingLanguage.GROOVY);
    }
}
