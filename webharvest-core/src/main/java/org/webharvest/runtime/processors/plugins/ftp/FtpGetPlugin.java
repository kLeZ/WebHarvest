package org.webharvest.runtime.processors.plugins.ftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Ftp Get plugin - can be used only inside ftp plugin for retrieving file from remote directory.
 */
public class FtpGetPlugin extends WebHarvestPlugin {

    public String getName() {
        return "ftp-get";
    }

    public Variable executePlugin(DynamicScopeContext context) {
        FtpPlugin ftpPlugin = (FtpPlugin) getParentProcessor();
        if (ftpPlugin != null) {
            FTPClient ftpClient = ftpPlugin.getFtpClient();

            String path = CommonUtil.nvl( evaluateAttribute("path", context), "" );

            setProperty("Path", path);

            try {
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                boolean succ = ftpClient.retrieveFile(path, byteOutputStream);
                byteOutputStream.close();
                if (!succ) {
                    throw new FtpPluginException("Cannot retrieve file \"" + path + "\" from FTP server!");
                }
                byte[] bytes = byteOutputStream.toByteArray();
                return new NodeVariable(bytes);
            } catch (IOException e) {
                throw new FtpPluginException(e);
            }
        } else {
            throw new FtpPluginException("Cannot use ftp get plugin out of ftp plugin context!");
        }
    }

    public String[] getValidAttributes() {
        return new String[] {"path"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"path"};
    }

    public boolean hasBody() {
        return false;
    }
}