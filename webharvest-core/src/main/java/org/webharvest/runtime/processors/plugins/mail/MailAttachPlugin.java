package org.webharvest.runtime.processors.plugins.mail;

import java.io.IOException;

import javax.activation.DataSource;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.webharvest.WHConstants;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

/**
 * Mail attachment plugin - can be used only inside mail plugin.
 */
public class MailAttachPlugin extends WebHarvestPlugin {

    public String getName() {
        return "mail-attach";
    }

    public Variable executePlugin(DynamicScopeContext context) throws InterruptedException {
        Processor processor = getParentProcessor();
        if (processor != null) {
            MailPlugin mailPlugin = (MailPlugin) processor;
            Email email = mailPlugin.getEmail();
            if (email instanceof HtmlEmail) {
                String attachmentName = evaluateAttribute("name", context);
                if (CommonUtil.isEmptyString(attachmentName)) {
                    attachmentName = mailPlugin.getNextAttachmentName();
                }
                String mimeType = evaluateAttribute("mimetype", context);
                boolean isInline = evaluateAttributeAsBoolean("inline", false, context);
                HtmlEmail htmlEmail = (HtmlEmail) email;
                Variable bodyVar = executeBody(context);
                try {
                    if (CommonUtil.isEmptyString(mimeType)) {
                        mimeType = isInline ? "image/jpeg" : "application/octet-stream";
                    }
                    DataSource dataSource = MailPlugin.createDataSourceOfVariable(bodyVar, context.getCharset(), mimeType);
                    String cid = htmlEmail.embed(dataSource, attachmentName);
                    return isInline ? new NodeVariable("cid:" + cid) : EmptyVariable.INSTANCE;
                } catch (IOException e) {
                    throw new MailPluginException(e);
                } catch (EmailException e) {
                    throw new MailPluginException(e);
                }
            } else {
                throw new MailPluginException("Cannot use mail attach plugin if mail type is not html!");
            }
        } else {
            throw new MailPluginException("Cannot use mail attach plugin out of mail plugin context!");
        }
    }

    public String[] getValidAttributes() {
        return new String[]{"name", "mimetype", "inline"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("mimetype".equalsIgnoreCase(attributeName)) {
            return WHConstants.MIME_TYPES;
        } else if ("inline".equalsIgnoreCase(attributeName)) {
            return new String[]{"true", "false"};
        }
        return null;
    }


}