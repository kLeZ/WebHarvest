package org.webharvest.runtime.processors.plugins.ftp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.net.ftp.FTPClient;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Ftp Put plugin - can be used only inside ftp plugin for storing file to remote directory.
 */
public class FtpPutPlugin extends WebHarvestPlugin {

    public String getName() {
        return "ftp-put";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        FtpPlugin ftpPlugin = (FtpPlugin) getParentProcessor();
        if (ftpPlugin != null) {
            FTPClient ftpClient = ftpPlugin.getFtpClient();

            String path = CommonUtil.nvl( evaluateAttribute("path", context), "" );
            String charset = CommonUtil.nvl( evaluateAttribute("charset", context), "" );
            if (CommonUtil.isEmptyString(charset)) {
                charset = context.getCharset();
            }

            setProperty("Path", path);
            setProperty("Charset", charset);

            Variable body = executeBody(context);

            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(body.toBinary(charset));
                boolean succ = ftpClient.storeFile(path, stream);
                stream.close();
                if (!succ) {
                    throw new FtpPluginException("Cannot store file \"" + path + "\" to FTP server!");
                }
            } catch (IOException e) {
                throw new FtpPluginException(e);
            }
        } else {
            throw new FtpPluginException("Cannot use ftp put plugin out of ftp plugin context!");
        }

        return EmptyVariable.INSTANCE;
    }

    public String[] getValidAttributes() {
        return new String[] {"path", "charset"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"path"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("charset".equalsIgnoreCase(attributeName)) {
            Set<String> charsetKeys = Charset.availableCharsets().keySet();
            return new ArrayList<String>(charsetKeys).toArray(new String[charsetKeys.size()]);
        }
        return null;
    }

}