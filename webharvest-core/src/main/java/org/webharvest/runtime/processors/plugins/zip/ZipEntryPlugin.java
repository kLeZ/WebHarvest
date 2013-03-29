package org.webharvest.runtime.processors.plugins.zip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Zip entry plugin - can be used only inside zip plugin.
 */
public class ZipEntryPlugin extends WebHarvestPlugin {

    public String getName() {
        return "zip-entry";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        ZipPlugin zipPlugin = (ZipPlugin) getParentProcessor();
        if (zipPlugin != null) {
            String name = evaluateAttribute("name", context);
            if (CommonUtil.isEmptyString(name)) {
                throw new ZipPluginException("Name of zip entry cannot be empty!");
            }
            String charset = evaluateAttribute("charset", context);
            if (CommonUtil.isEmptyString(charset)) {
                charset = context.getCharset();
            }

            ZipOutputStream zipOutStream = zipPlugin.getZipOutStream();
            Variable bodyResult = executeBody(context);
            try {
                zipOutStream.putNextEntry(new ZipEntry(name));
                zipOutStream.write(bodyResult.toBinary(charset));
                zipOutStream.closeEntry();
            } catch (IOException e) {
                throw new ZipPluginException(e);
            }
        } else {
            throw new ZipPluginException("Cannot use zip entry plugin out of zip plugin context!");
        }
        return EmptyVariable.INSTANCE;
    }

    public String[] getValidAttributes() {
        return new String[] {"name", "charset"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"name"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("charset".equalsIgnoreCase(attributeName)) {
            Set<String> charsetKeys = Charset.availableCharsets().keySet();
            return new ArrayList<String>(charsetKeys).toArray(new String[charsetKeys.size()]);
        }
        return null;
    }

}