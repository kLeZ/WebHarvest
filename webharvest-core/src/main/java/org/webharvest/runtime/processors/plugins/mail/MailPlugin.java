package org.webharvest.runtime.processors.plugins.mail;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;

import javax.activation.DataSource;

import org.apache.commons.mail.ByteArrayDataSource;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Mail sending processor.
 */
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition("mail")
public class MailPlugin extends WebHarvestPlugin {

    public static DataSource createDataSourceOfVariable(Variable variable, String charset, String mimeType) throws IOException {
        if (variable != null) {
            byte[] bytes = variable.toBinary(charset);
            return new ByteArrayDataSource(bytes, mimeType);
        }
        return null;
    }

    private Email email = null;
    private int attachmentCounter = 0;

    public String getName() {
        return "mail";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        email = null;

        boolean isHtml = "html".equalsIgnoreCase(evaluateAttribute("type", context));
        if (isHtml) {
            email = new HtmlEmail();
        } else {
            email = new SimpleEmail();
        }

        email.setHostName( evaluateAttribute("smtp-host", context) );
        email.setSmtpPort( evaluateAttributeAsInteger("smtp-port", 25, context) );

        try {
            email.setFrom( evaluateAttribute("from", context) );
        } catch (EmailException e) {
            throw new MailPluginException("Invalid \"from\" email address!", e);
        }

        for ( String replyTo:  CommonUtil.tokenize(evaluateAttribute("reply-to", context), ",") ) {
            try {
                email.addReplyTo(replyTo);
            } catch (EmailException e) {
                throw new MailPluginException("Invalid \"reply-to\" email address!", e);
            }
        }
        for ( String to:  CommonUtil.tokenize(evaluateAttribute("to", context), ",") ) {
            try {
                email.addTo(to);
            } catch (EmailException e) {
                throw new MailPluginException("Invalid \"to\" email address!", e);
            }
        }
        for ( String cc:  CommonUtil.tokenize(evaluateAttribute("cc", context), ",") ) {
            try {
                email.addCc(cc);
            } catch (EmailException e) {
                throw new MailPluginException("Invalid \"cc\" email address!", e);
            }
        }
        for ( String bcc:  CommonUtil.tokenize(evaluateAttribute("bcc", context), ",") ) {
            try {
                email.addBcc(bcc);
            } catch (EmailException e) {
                throw new MailPluginException("Invalid \"bcc\" email address!", e);
            }
        }

        email.setSubject( evaluateAttribute("subject", context) );

        String username = evaluateAttribute("username", context);
        String password = evaluateAttribute("password", context);
        if ( !CommonUtil.isEmptyString(username) ) {
            email.setAuthentication(username, password);
        }

        String security = evaluateAttribute("security", context);
        if ("tls".equals(security)) {
            email.setTLS(true);
        } else if ("ssl".equals(security)) {
            email.setSSL(true);
        }

        String charset = evaluateAttribute("charset", context);
        if (CommonUtil.isEmptyString(charset)) {
            charset = context.getCharset();
        }
        email.setCharset(charset);

        if (isHtml) {
            HtmlEmail htmlEmail = (HtmlEmail) email;
            String htmlContent = executeBody(context).toString();
            try {
                htmlEmail.setHtmlMsg(htmlContent);
            } catch (EmailException e) {
                throw new MailPluginException(e);
            }
        } else {
            try {
                email.setMsg(executeBody(context).toString());
            } catch (EmailException e) {
                throw new MailPluginException(e);
            }
        }

        try {
            email.send();
        } catch (EmailException e) {
            throw new MailPluginException(e);
        }

        email = null;

        return EmptyVariable.INSTANCE;
    }

    public String[] getValidAttributes() {
        return new String[] {
                "smtp-host",
                "smtp-port",
                "type",
                "from",
                "reply-to",
                "to",
                "cc",
                "bcc",
                "subject",
                "charset",
                "username",
                "password",
                "security"
        };
    }

    public String[] getRequiredAttributes() {
        return new String[] {"smtp-host", "from", "to"};
    }

    public String[] getValidSubprocessors() {
        return null;
    }

    public String[] getRequiredSubprocessors() {
        return null;
    }

    public Class[] getDependantProcessors() {
        return new Class[] {
            MailAttachPlugin.class,
        };
    }

    public Email getEmail() {
        return email;
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("type".equalsIgnoreCase(attributeName)) {
            return new String[] {"html", "text"};
        } else if ("charset".equalsIgnoreCase(attributeName)) {
            Set<String> charsetKeys = Charset.availableCharsets().keySet();
            return new ArrayList<String>(charsetKeys).toArray(new String[charsetKeys.size()]);
        } else if ("security".equalsIgnoreCase(attributeName)) {
            return new String[] {"ssl", "tls", "none"};
        }
        return null;
    }

    protected String getNextAttachmentName() {
        attachmentCounter++;
        return "Attachment " + attachmentCounter;
    }

}