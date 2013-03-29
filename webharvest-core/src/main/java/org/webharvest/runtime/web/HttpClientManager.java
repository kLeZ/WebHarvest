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
package org.webharvest.runtime.web;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.SetCookie;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import com.google.inject.Inject;

/**
 * HTTP client functionality.
 */
public class HttpClientManager {

    private static final Logger LOG =
        LoggerFactory.getLogger(HttpClientManager.class);

    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1";

//    static {
//        // registers default handling for https
//        Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), 443));
//    }

    private final DefaultHttpClient client;
    private final HttpInfo httpInfo;

    @Inject
    public HttpClientManager(final ProxySettings proxySettings) {
        this.client = new DefaultHttpClient();
        this.httpInfo = new HttpInfo(client);

        client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, new EasySSLProtocolSocketFactory()));
        client.getParams().setBooleanParameter("http.protocol.allow-circular-redirects", true);

        proxySettings.apply(this.client);
    }

    public void setCookiePolicy(String cookiePolicy) {
        if (StringUtils.isBlank(cookiePolicy) || "browser".equalsIgnoreCase(cookiePolicy)) {
            HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.BROWSER_COMPATIBILITY);
            // http://hc.apache.org/httpclient-3.x/cookies.html
            client.getParams().setParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, Boolean.TRUE);

        } else if ("ignore".equalsIgnoreCase(cookiePolicy)) {
            HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.IGNORE_COOKIES);

        } else if ("netscape".equalsIgnoreCase(cookiePolicy)) {
            HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.NETSCAPE);

        } else if ("rfc_2109".equalsIgnoreCase(cookiePolicy)) {
            HttpClientParams.setCookiePolicy(client.getParams(), CookiePolicy.RFC_2109);

        } else {
            HttpClientParams.setCookiePolicy(client.getParams(), cookiePolicy);
        }
    }

    public HttpResponseWrapper execute(
            String methodType,
            Boolean followRedirects,
            String contentType,
            String url,
            String charset,
            String username,
            String password,
            Variable bodyContent, Map<String, HttpParamInfo> params,
            Map<String, String> httpHeaderMap, int retryAttempts, long retryDelay, double retryDelayFactor) throws InterruptedException, UnsupportedEncodingException {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }

        url = CommonUtil.encodeUrl(url, charset);

        // if username and password are specified, define new credentials for authenticaton
        if (username != null && password != null) {
            try {
                URL urlObj = new URL(url);
                client.getCredentialsProvider().setCredentials(
                        new AuthScope(urlObj.getHost(), urlObj.getPort()),
                        new UsernamePasswordCredentials(username, password)
                );
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        fixCookiesWithoutExpirationDate(client.getCookieStore());

        HttpRequestBase method;
        if ("post".equalsIgnoreCase(methodType)) {
            method = createPostMethod(url, params, contentType, charset, bodyContent);
        } else {
            method = createGetMethod(url, params, charset, followRedirects);
        }

        boolean isUserAgentSpecified = false;

        // define request headers, if any exist
        if (httpHeaderMap != null) {
            for (Object o : httpHeaderMap.keySet()) {
                String headerName = (String) o;
                if ("User-Agent".equalsIgnoreCase(headerName)) {
                    isUserAgentSpecified = true;
                }
                String headerValue = (String) httpHeaderMap.get(headerName);
                method.addHeader(headerName, headerValue);
            }
        }

        if (!isUserAgentSpecified) {
            identifyAsDefaultBrowser(method);
        }

        HttpResponseWrapper responseWrapper = null;
        try {
            responseWrapper = doExecute(url, method, followRedirects, retryAttempts, retryDelay, retryDelayFactor);
            return responseWrapper;
        } finally {
            if (responseWrapper == null) {
                // i.e. an exception need thrown
                method.releaseConnection();
            }
        }
    }

    private void fixCookiesWithoutExpirationDate(CookieStore clientState) {
        // If cookie expiry date is not specified in the response, HttpClient 3.1 doesn't send it back.
        // This leads to inability to login to some sites, being always redirected to login page.
        // Workaround here is to set cookies with null expiry dates to the current date plus 1 day
        // ( patched by heysteveo - https://sourceforge.net/projects/web-harvest/forums/forum/591299/topic/4372223 post #10 )
        // todo: remove this method if HttpClient 4.x fixes the problem
        final List<Cookie> cookies = clientState.getCookies();
        if (cookies != null && cookies.size() > 0) {
            final Calendar defaultExpirationDate = Calendar.getInstance();
            defaultExpirationDate.setTime(new Date());
            defaultExpirationDate.add(Calendar.DAY_OF_MONTH, 1);
            for (Cookie cookie : cookies) {
                if (cookie.getExpiryDate() == null) {
                	((SetCookie) cookie).setExpiryDate(defaultExpirationDate.getTime());
                }
            }
        }
    }

    private HttpResponseWrapper doExecute(String url, HttpRequestBase request, Boolean followRedirects,
                                          int retryAttempts, long retryDelay, double retryDelayFactor) throws InterruptedException {

        int attemptsRemain = retryAttempts;
        HttpResponse response = null;

        do {
            boolean wasException = false;
            try {
            	response = executeFollowingRedirects(request, url, followRedirects);
            } catch (IOException e) {
                if (attemptsRemain == 0) {
                    throw new org.webharvest.exception.HttpException("IO error during HTTP execution for URL: " + url, e);
                }
                wasException = true;
                LOG.warn("Exception occurred during executing HTTP method {}: {}", request.getMethod(), e.getMessage());
            }

            if (!wasException
                    && response.getStatusLine().getStatusCode() != HttpStatus.SC_BAD_GATEWAY
                    && response.getStatusLine().getStatusCode() != HttpStatus.SC_SERVICE_UNAVAILABLE
                    && response.getStatusLine().getStatusCode() != HttpStatus.SC_GATEWAY_TIMEOUT
                    && response.getStatusLine().getStatusCode() != 509 /*Bandwidth Limit Exceeded (Apache bw/limited extension)*/) {
                // success.
                break;
            }
            if (attemptsRemain == 0) {
                throw new org.webharvest.exception.HttpException("HTTP Status: " + response.getStatusLine().getStatusCode() + ", Url: " + url);
            }

            final long delayBeforeRetry = (long) (retryDelay * (Math.pow(retryDelayFactor, retryAttempts - attemptsRemain)));

            LOG.warn("HTTP Status: {}; URL: [{}]; Waiting for {} second(s) before retrying (attempt {} of {})...", new Object[]{
            		response.getStatusLine(), url, MILLISECONDS.toSeconds(delayBeforeRetry), retryAttempts - attemptsRemain + 1, retryAttempts});

            Thread.sleep(delayBeforeRetry);
            attemptsRemain--;
        } while (true);

        final HttpResponseWrapper httpResponseWrapper = new HttpResponseWrapper(request, response);
        // updates HTTP info with response's details
        this.httpInfo.setResponse(httpResponseWrapper);
        return httpResponseWrapper;
    }

    private HttpResponse executeFollowingRedirects(HttpRequestBase method, String url, Boolean followRedirects) throws IOException {
        final HttpResponse response = client.execute(method);
        // POST method is not redirected automatically, so it's on our responsibility then.
        if (BooleanUtils.isTrue(followRedirects)
                && ((response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) ||
                (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY) ||
                (response.getStatusLine().getStatusCode() == HttpStatus.SC_SEE_OTHER) ||
                (response.getStatusLine().getStatusCode() == HttpStatus.SC_TEMPORARY_REDIRECT))) {
            final Header header = method.getFirstHeader("location");
            if (header != null) {
                final String nextURI = header.getValue();
                if (!CommonUtil.isEmptyString(nextURI)) {
                    method.releaseConnection();
                    final HttpGet nextMethod = new HttpGet(CommonUtil.fullUrl(url, nextURI));
                    identifyAsDefaultBrowser(nextMethod);
                    return client.execute(nextMethod);
                }
            }
        }
        return response;
    }

    /**
     * Defines "User-Agent" HTTP header.
     *
     * @param method
     */
    private void identifyAsDefaultBrowser(HttpRequestBase method) {
        method.addHeader("User-Agent", DEFAULT_USER_AGENT);
    }

    private HttpRequestBase createPostMethod(String url, Map<String, HttpParamInfo> params, String contentType, String charset, Variable bodyContent)
            throws UnsupportedEncodingException {
        HttpPost method = new HttpPost(url);

        int filenameIndex = 1;
        if (params != null) {
            if ("multipart/form-data".equals(contentType)) {
                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (Map.Entry<String, HttpParamInfo> entry : params.entrySet()) {
                    String name = entry.getKey();
                    HttpParamInfo httpParamInfo = entry.getValue();
                    Variable value = httpParamInfo.getValue();

                    if (httpParamInfo.isFile()) {
                        String filename = httpParamInfo.getFileName();
                        if (CommonUtil.isEmptyString(filename)) {
                            filename = "uploadedfile_" + filenameIndex;
                            filenameIndex++;
                        }
                        String paramContentType = httpParamInfo.getContentType();
                        if (CommonUtil.isEmptyString(paramContentType)) {
                            paramContentType = null;
                        }

                        byte[] bytes = value.toBinary(charset);
                        entity.addPart(httpParamInfo.getName(), new ByteArrayBody(bytes, paramContentType, filename));
                    } else {
                        entity.addPart(name, new StringBody(CommonUtil.nvl(value, ""), Charset.forName(charset)));
                    }
                }
                method.setEntity(entity);

            } else if (StringUtils.startsWith(contentType, "text/") || StringUtils.startsWith(contentType, "application/xml")) {
                method.setEntity(new StringEntity(bodyContent.toString(charset), contentType));

            } else {
                List<NameValuePair> parameters = new ArrayList<NameValuePair>();
                for (Map.Entry<String, HttpParamInfo> entry : params.entrySet()) {
                    String name = entry.getKey();
                    String value = entry.getValue().getValue().toString();
                    parameters.add(new BasicNameValuePair(name, value));
                }
                method.setEntity(new UrlEncodedFormEntity(parameters));
            }
        }

        return method;
    }

    // FIXME: package protected for testing. This is not perfect solution
    // - we need to refactor entire HttpClientManager class (splitting it into
    // multiple classes/interfaces)
    HttpGet createGetMethod(String url, Map<String, HttpParamInfo> params,
            String charset, Boolean followRedirects) {
        if (params != null) {
            final StringBuilder urlParamsBuilder = new StringBuilder();
            final Iterator<Entry<String, HttpParamInfo>> iterator =
                params.entrySet().iterator();

            while (iterator.hasNext()) {
                final Entry<String, HttpParamInfo> entry = iterator.next();
                final HttpParamInfo httpParamInfo = entry.getValue();

                try {
                    urlParamsBuilder.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(CommonUtil.nvl(
                                httpParamInfo.getValue(), ""), charset));
                } catch (UnsupportedEncodingException e) {
                    throw new org.webharvest.exception.HttpException("Charset "
                            + charset + " is not supported!", e);
                }
                if (iterator.hasNext()) {
                    urlParamsBuilder.append("&");
                }
            }

            if (urlParamsBuilder.length() != 0) {
                final String urlParams = urlParamsBuilder.toString();
                if (url.indexOf("?") < 0) {
                    url += "?" + urlParams;
                } else if (url.endsWith("&")) {
                    url += urlParams;
                } else {
                    url += "&" + urlParams;
                }
            }
        }

        final HttpGet method = new HttpGet(url);
        method.getParams().setParameter("http.protocol.handle-redirects", followRedirects);
        return method;
    }

    public HttpInfo getHttpInfo() {
        return httpInfo;
    }

    public HttpClient getHttpClient() {
        return client;
    }

    // ProxySettings class and its inner Builder class encapsulates logic
    // previously distributed across entire application;
    // Now, we have one class responsible for performing proxy configuration
    // - it's still too complex, but far better than the previous approach,
    // when proxy configuration logic was shared between HttpClientManager,
    // CommandLine or ConfigPanel...
    public static final class ProxySettings {

        public static final ProxySettings NO_PROXY_SET =
            new ProxySettings(null);

        private final HttpHost proxyHost;
        private Credentials proxyCredentials;

        private ProxySettings(final HttpHost proxyHost) {
            this.proxyHost = proxyHost;
        }

        private void setProxyCredentials(final Credentials credentials) {
            this.proxyCredentials = credentials;
        }

        void apply(final DefaultHttpClient httpClient) {
            if (this == NO_PROXY_SET) {
                return;
            }
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
            if (proxyCredentials != null)
                httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, proxyCredentials);
        }

        public static final class Builder {
            private final String proxyHost;
            private int proxyPort = -1;

            // proxy credentials
            private String proxyUsername;
            private String proxyPassword;
            private String proxyNTHost;
            private String proxyNTDomain;

            public Builder(final String proxyHost) {
                if (proxyHost == null || "".equals(proxyHost)) {
                    throw new IllegalArgumentException(
                            "Proxy host is required");
                }
                this.proxyHost = proxyHost;
            }

            public Builder setProxyPort(int proxyPort) {
                this.proxyPort = proxyPort;
                return this;
            }

            public Builder setProxyCredentialsUsername(final String username) {
                this.proxyUsername = username;
                return this;
            }

            public Builder setProxyCredentialsPassword(final String password) {
                this.proxyPassword = password;
                return this;
            }

            public Builder setProxyCredentialsNTHost(final String proxyNTHost) {
                this.proxyNTHost = proxyNTHost;
                return this;
            }

            public Builder setProxyCredentialsNTDomain(
                    final String proxyNTDomain) {
                this.proxyNTDomain = proxyNTDomain;
                return this;
            }

            public ProxySettings build() {
                final ProxySettings settings = new ProxySettings(
                        new HttpHost(proxyHost, proxyPort));

                if (proxyUsername != null) {
                    settings.setProxyCredentials(createCredentials());
                }

                return settings;
            }

            private Credentials createCredentials() {
                return (proxyNTHost == null || proxyNTDomain == null
                        || "".equals(proxyNTHost.trim()) || "".equals(proxyNTDomain.trim())) ?
                            new UsernamePasswordCredentials(proxyUsername, proxyPassword) :
                            new NTCredentials(proxyUsername, proxyPassword, proxyNTHost, proxyNTDomain);
            }
        }
    }
}