package org.webharvest.runtime.web;

import static org.testng.AssertJUnit.assertEquals;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.web.HttpClientManager.ProxySettings;

public class HttpClientManagerTest {

    private static final String CHARSET = "utf-8";

    private HttpClientManager manager;

    @BeforeMethod
    public void setUp() {
        this.manager = new HttpClientManager(ProxySettings.NO_PROXY_SET);
    }

    @AfterMethod
    public void tearDown() {
        this.manager = null;
    }

    @Test
    public void createGetMethodNoHttpParams() {
        final HttpGet get = manager.createGetMethod("http://sourceforge.net/",
                new HashMap<String, HttpParamInfo>(), CHARSET, false);

        assertEquals("Unexpected URI created",
                "http://sourceforge.net/", get.getURI().toString());
    }

    @Test
    public void createGetMethodWithHttpParams() {
        final Map<String, HttpParamInfo> params =
            new LinkedHashMap<String, HttpParamInfo>();
        params.put("param1", new HttpParamInfo("param1", false, null, null,
                new NodeVariable("param1Value")));
        params.put("param2", new HttpParamInfo("param2", false, null, null,
                new NodeVariable("param2Value")));

        final HttpGet get = manager.createGetMethod("http://sourceforge.net/",
                params, CHARSET, false);

        assertEquals("Unexpected URI created",
                "http://sourceforge.net/?param1=param1Value&param2=param2Value",
                get.getURI().toString());
    }
}
