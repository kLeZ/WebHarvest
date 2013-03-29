package org.webharvest.runtime.processors.plugins.mail;

import org.webharvest.exception.BaseException;

/**
 * Runtime exception for MailPlugin
 */
public class MailPluginException extends BaseException {

    public MailPluginException(String message) {
        super(message);
    }

    public MailPluginException(Throwable cause) {
        super(cause);
    }

    public MailPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}