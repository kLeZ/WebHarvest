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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.webharvest.utils.KeyValuePair;

/**
 * Class defines http server response.
 */
public class HttpResponseWrapper {

    private String charset;
    private String mimeType;
    private KeyValuePair<String> headers[];
    private int statusCode;
    private String statusText;
    private HttpRequestBase request;
    private HttpResponse response;

    /**
     * Constructor - defines response result based on specified HttpMethodBase instance.
     *
     * @param method Http method object
     */
    @SuppressWarnings({"unchecked"})
    public HttpResponseWrapper(HttpRequestBase request, HttpResponse response) {
        this.request = request;
        this.response = response;

        Header[] headerArray = response.getAllHeaders();
        if (headerArray != null) {
            headers = new KeyValuePair[headerArray.length];
            for (int i = 0; i < headerArray.length; i++) {
                String currName = headerArray[i].getName();
                String currValue = headerArray[i].getValue();
                headers[i] = new KeyValuePair<String>(currName, currValue);
                if ("content-type".equalsIgnoreCase(currName)) {
                    int index = currValue.indexOf(';');
                    this.mimeType = index > 0 ? currValue.substring(0, index) : currValue;
                }
            }
        }

        ContentType contentType = ContentType.getOrDefault(response.getEntity());
        this.charset = contentType.getCharset().name();
        this.statusCode = response.getStatusLine().getStatusCode();
        this.statusText = response.getStatusLine().getReasonPhrase();

    }

    public long getContentLength() {
        return response.getEntity().getContentLength();
    }

    public String getCharset() {
        return this.charset;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    /**
     * @return byte array
     * @deprecated Left only for backward compatibility. Use {@link #readBodyAsArray()} or {@link #getBodyAsInputStream()}
     */
    @Deprecated
    public byte[] getBody() {
        return readBodyAsArray();
    }

    public byte[] readBodyAsArray() {
        try {
            InputStream input = response.getEntity().getContent();
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = input.read(buffer)) != -1)
            {
                output.write(buffer, 0, bytesRead);
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getBodyAsInputStream() throws IllegalStateException, IOException {
        // TODO: Fix this so a real stream is returned. DO NOT CACHE THE ENTIRE RESPONSE IN THE MEMORY!
        return response.getEntity().getContent();
    }

    public void close() {
    	request.releaseConnection();
    }

    public KeyValuePair<String>[] getHeaders() {
        return this.headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

}