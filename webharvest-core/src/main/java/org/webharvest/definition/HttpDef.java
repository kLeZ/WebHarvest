/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.definition;

import java.util.concurrent.TimeUnit;

import org.webharvest.runtime.processors.Processor;
import org.webharvest.utils.CommonUtil;

/**
 * Definition of HTTP processor.
 */
public class HttpDef extends WebHarvestPluginDef {

    public static final String DEFAULT_METHOD = "get";
    public static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private static final String DEFAULT_RETRY_ATTEMPTS = Integer.toString(5);
    private static final String DEFAULT_RETRY_DELAY = Long.toString(TimeUnit.SECONDS.toMillis(10));
    private static final String DEFAULT_RETRY_DELAY_FACTOR = Double.toString(2);

    private String method;
    private String contentType;
    private String url;
    private String charset;
    private String username;
    private String password;
    private String cookiePolicy;
    private String followRedirects;
    private String ignoreResponseBody;

    private String retryAttempts;
    private String retryDelay;
    private String retryDelayFactor;

    public HttpDef(XmlNode xmlNode, Class<? extends Processor> processorClass) {
        super(xmlNode, processorClass);

        this.method = CommonUtil.nvl(xmlNode.getAttribute("method"), DEFAULT_METHOD);
        this.contentType = CommonUtil.nvl(xmlNode.getAttribute("content-type"), DEFAULT_CONTENT_TYPE);
        this.url = xmlNode.getAttribute("url");
        this.charset = xmlNode.getAttribute("charset");
        this.username = xmlNode.getAttribute("username");
        this.password = xmlNode.getAttribute("password");
        this.cookiePolicy = xmlNode.getAttribute("cookie-policy");
        this.followRedirects = xmlNode.getAttribute("follow-redirects");
        this.ignoreResponseBody = xmlNode.getAttribute("ignore-response-body");

        this.retryAttempts = CommonUtil.nvl(xmlNode.getAttribute("retry-attempts"), DEFAULT_RETRY_ATTEMPTS);
        this.retryDelay = CommonUtil.nvl(xmlNode.getAttribute("retry-delay"), DEFAULT_RETRY_DELAY);
        this.retryDelayFactor = CommonUtil.nvl(xmlNode.getAttribute("retry-delay-factor"), DEFAULT_RETRY_DELAY_FACTOR);
    }

    public String getMethod() {
        return method;
    }

    public String getFollowRedirects() {
        return followRedirects;
    }

    public String getContentType() {
        return contentType;
    }

    public String getUrl() {
        return url;
    }

    public String getCharset() {
        return charset;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getCookiePolicy() {
        return cookiePolicy;
    }

    public String getShortElementName() {
        return "http";
    }

    public String getRetryAttempts() {
        return retryAttempts;
    }

    public String getRetryDelay() {
        return retryDelay;
    }

    public String getRetryDelayFactor() {
        return retryDelayFactor;
    }

    public String getIgnoreResponseBody() {
        return ignoreResponseBody;
    }
}