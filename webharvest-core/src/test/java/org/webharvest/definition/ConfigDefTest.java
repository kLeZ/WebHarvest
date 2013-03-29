package org.webharvest.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.webharvest.runtime.scripting.ScriptingLanguage;

public class ConfigDefTest {

    private static final String CHARSET_ATTR = "charset";
    private static final String LANGUAGE_ATTR = "scriptlang";

    private static final String CHARSET = "ISO-8859-2";
    private static final String LANGUAGE = "groovy";

    private XmlNode nodeMock;

    @BeforeMethod
    public void setUp() {
        //constructor's parameters are not important
        nodeMock = new XmlNode(null, null, null, null);
    }

    @AfterMethod
    public void tearDown() {
        nodeMock = null;
    }


    @Test
    public void getCharset() {
        nodeMock.addAttribute(CHARSET_ATTR, null, CHARSET);

        final String charset = new ConfigDef(nodeMock, null).getCharset();
        assertNotNull("Charset is null.", charset);
        assertEquals("Unexpected charset.", CHARSET, charset);
    }

    @Test
    public void getCharsetIfEmptyAttribute() {
        final String charset = new ConfigDef(nodeMock, null).getCharset();
        assertNotNull("Default charset is null.", charset);
        assertEquals("Unexpected default charset.", "UTF-8", charset);
    }

    @Test
    public void getScriptingLanguage() {
        nodeMock.addAttribute(LANGUAGE_ATTR, null, LANGUAGE);

        final ScriptingLanguage lang = new ConfigDef(nodeMock, null).
            getScriptingLanguage();
        assertNotNull("Scripting language is null.", lang);
        assertEquals("Unexpected scripting language.",
                ScriptingLanguage.GROOVY, lang);
    }

    @Test
    public void getScriptingLanguageIfNullLanguage() {
        final ScriptingLanguage lang = new ConfigDef(nodeMock, null).
            getScriptingLanguage();
        assertNotNull("Default scripting language is null.", lang);
        assertEquals("Unexpected default scripting language.",
                ScriptingLanguage.BEANSHELL, lang);
    }

}
