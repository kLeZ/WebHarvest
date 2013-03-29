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

package org.webharvest.definition;

import org.webharvest.runtime.processors.WebHarvestPlugin;

/**
 * Interface to be implemented by the web harvest configuration elements
 * definition resolvers. Facilitates configuration of resolvers, allowing to
 * register resolver post processors and to register custom web harvest plugins.
 *
 * @author Piotr Dyraga
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface ConfigurableResolver {

    /**
     * Adds new {@link ResolverPostProcessor} which is going to be applied on
     * the current resolver instance on the resolver refresh.
     *
     * @param postProcessor
     *            new instance of {@link ResolverPostProcessor}; must not be
     *            {code null}
     */
    void addPostProcessor(ResolverPostProcessor postProcessor);

    /**
     * Refresh the current resolver instance, causing all configuration element
     * definitions to be reinitialized. Also, all previously registered
     * {@link ResolverPostProcessor}s are invoked.
     */
    void refresh();

    /**
     * Register provided {@link WebHarvestPlugin} based on information provided
     * by specified {@link ElementInfo}.
     *
     * @param elementInfo
     *            web harvest plugin definition needed to succeed registration.
     * @param namespace
     *            XML namespace under which plugin is going to be registered.
     */
    void registerPlugin(ElementInfo elementInfo, String namespace);

}
