/*
 Copyright (c) 2006-2012 the original author or authors.

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
 */

package org.webharvest;

import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.WebScraper;

/**
 * Represents scraping session object that is associated with particular
 * configuration and can be executed multiple times. Execution takes place in
 * isolated sandbox which is a scraping scope. All objects created in a scope
 * have limited life-cycle only to this particular scope and are considered to
 * be destroyed when the scope is torn down.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see DynamicScopeContext
 */
public interface Harvester {

    /**
     * Execute scraping session in its own scope. Accepts
     * {@link ContextInitCallback} that receives notification about new context
     * object shortly before it is used.
     *
     * @param callback
     *            scraping session initialization callback.
     * @return reference to the context object that stores results of execution.
     *         Is is expected to be the same instance that is passed on to the
     *         {@link ContextInitCallback} before execution.
     */
    DynamicScopeContext execute(ContextInitCallback callback);

    /**
     * Context initialization callback that is invoked for all newly created
     * context objects shortly before of scraping session. This creates
     * opportunity to set attributes that are taken into account in scraping
     * configuration.
     *
     * @author Robert Bala
     * @since 2.1.0-SNAPSHOT
     * @version %I%, %G%
     * @see DynamicScopeContext
     */
    interface ContextInitCallback {

        /**
         * Adjust newly created {@link DynamicScopeContext} object shortly
         * before it is used to launch scraping session.
         *
         * @param context
         *            new scrpaer's context object.
         */
        void onSuccess(DynamicScopeContext context);

    }
}
