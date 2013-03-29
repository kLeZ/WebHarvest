/*
 Copyright (c) 2006-2007, Vladimir Nikic
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

package org.webharvest.deprecated.runtime;

import static org.testng.AssertJUnit.assertNotNull;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.mock.Mock;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.runtime.web.HttpClientManager.ProxySettings;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.SystemUtilities;

import java.util.concurrent.Callable;

public class ScraperContext10Test extends UnitilsTestNG {

    @TestedObject
    private ScraperContext10 context;

    @InjectInto(property = "httpClientManager")
    private HttpClientManager httpClientManager;

    @BeforeMethod
    public void before() {
        this.context = new ScraperContext10();
        this.httpClientManager = new HttpClientManager(
                ProxySettings.NO_PROXY_SET);
    }

    @AfterMethod
    public void after() {
        this.context = null;
        this.httpClientManager = null;
    }

    @Test
    public void initializesHttpVar() {
        context.initContext(); // @PostConstruct annotated
        assertNotNull("Expected not null 'http' variable",
                context.getVar("http"));
    }

    @Test
    public void initializesSysVar() {
        context.initContext(); // @PostConstruct annotated
        assertNotNull("Expected not null 'sys' variable",
                context.getVar("sys"));
    }

    @Test
    public void testGetVar() throws Exception {
        this.context.initContext(); // @PostConstruct annotated

        // not existing var
        Assert.assertNull(context.getVar("x"));

        // local var
        context.setLocalVar("x", CommonUtil.createVariable(123));
        Assert.assertEquals(context.getVar("x").toInt(), 123);

        // sub-context. 1st level
        context.executeFunctionCall(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Assert.assertNull(context.getVar("x"));
                Assert.assertEquals(context.getVar("callerx").toInt(), 123);

                // inner variables should be accessible at any level
                Assert.assertNotNull(context.getVar("sys"));
                Assert.assertNotNull(context.getVar("http"));

                // sub-context. 2st level
                return context.executeFunctionCall(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Assert.assertNull(context.getVar("x"));
                        Assert.assertNull(context.getVar("callerx"));
                        Assert.assertEquals(context.getVar("callercallerx").toInt(), 123);

                        // inner variables should be accessible at any level
                        Assert.assertNotNull(context.getVar("sys"));
                        Assert.assertNotNull(context.getVar("http"));
                        return null;
                    }
                });
            }
        });
    }
}
