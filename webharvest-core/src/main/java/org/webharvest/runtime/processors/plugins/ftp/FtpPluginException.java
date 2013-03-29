package org.webharvest.runtime.processors.plugins.ftp;

import org.webharvest.exception.BaseException;

/**
 * Runtime exception for FtpPlugin
 */
public class FtpPluginException extends BaseException {

    public FtpPluginException() {
    }

    public FtpPluginException(String message) {
        super(message);
    }

    public FtpPluginException(Throwable cause) {
        super(cause);
    }

    public FtpPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}