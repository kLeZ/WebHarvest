package org.webharvest.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.scripting.ScriptingLanguage;

/**
 * Web Harvest's definition of the {@code <config>} element.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ConfigDef extends WebHarvestPluginDef {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final ScriptingLanguage DEFAULT_SCRIPTING_LANGUAGE =
        ScriptingLanguage.BEANSHELL;

    private String charset;
    private ScriptingLanguage scriptingLanguage;

    /**
     * Default class constructor which obtains from given {@link XmlNode}
     * attributes such as charset and scripting language. If any of these
     * attributes is empty or has not been specified the defaults will be used.
     *
     * @param xmlNode
     *            reference to {@link XmlNode}
     * @param pluginClass
     *            class of the processor/plugin
     */
    public ConfigDef(final XmlNode xmlNode,
            final Class<? extends Processor> pluginClass) {
        super(xmlNode, pluginClass);

        this.charset = StringUtils.defaultIfEmpty(
                xmlNode.getAttribute("charset"), DEFAULT_CHARSET);
        this.scriptingLanguage = (ScriptingLanguage) ObjectUtils
                .defaultIfNull(ScriptingLanguage.recognize(xmlNode
                        .getAttribute("scriptlang")),
                        DEFAULT_SCRIPTING_LANGUAGE);
    }

    /**
     * Returns default charset for current configuration.
     *
     * @return default charset for current configuration
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Returns default {@link ScriptingLanguage} for current configuration.
     *
     * @return default {@link ScriptingLanguage} for current configuration
     */
    public ScriptingLanguage getScriptingLanguage() {
        return scriptingLanguage;
    }

}
