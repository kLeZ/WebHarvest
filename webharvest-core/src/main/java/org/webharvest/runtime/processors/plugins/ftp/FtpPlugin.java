package org.webharvest.runtime.processors.plugins.ftp;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * FTP processor
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition("ftp")
public class FtpPlugin extends WebHarvestPlugin {

    FTPClient ftpClient;

    public String getName() {
        return "ftp";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        String server = CommonUtil.nvl(evaluateAttribute("server", context), "");
        int port = evaluateAttributeAsInteger("port", 21, context);
        String username = CommonUtil.nvl(evaluateAttribute("username", context), "");
        String password = CommonUtil.nvl(evaluateAttribute("password", context), "");
        String account = CommonUtil.nvl(evaluateAttribute("account", context), "");
        String remoteDir = CommonUtil.nvl(evaluateAttribute("remotedir", context), "");

        setProperty("Server", server);
        setProperty("Port", port);
        setProperty("Username", username);
        setProperty("Password", password);
        setProperty("Account", account);
        setProperty("Remote Dir", remoteDir);

        ftpClient = new FTPClient();

        try {
            int reply;
            ftpClient.connect(server, port);
            reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new FtpPluginException("FTP server refused connection!");
            }

            if (CommonUtil.isEmptyString(account)) {
                ftpClient.login(username, password);
            } else {
                ftpClient.login(username, password, account);
            }

            if (!CommonUtil.isEmptyString(remoteDir)) {
                ftpClient.changeWorkingDirectory(remoteDir);
            }

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            return executeBody(context);
        } catch (IOException e) {
            throw new FtpPluginException(e);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                    LOG.warn(ioe.getMessage(), ioe);
                }
            }
            ftpClient = null;
        }
    }

    public String[] getValidAttributes() {
        return new String[] {"server", "port", "username", "password", "account", "remotedir"};
    }

    public String[] getRequiredAttributes() {
        return new String[] {"server", "username", "password"};
    }

    public Class[] getDependantProcessors() {
        return new Class[] {
            FtpListPlugin.class,
            FtpGetPlugin.class,
            FtpPutPlugin.class,
            FtpDelPlugin.class,
            FtpMkdirPlugin.class,
            FtpRmdirPlugin.class
        };
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

}