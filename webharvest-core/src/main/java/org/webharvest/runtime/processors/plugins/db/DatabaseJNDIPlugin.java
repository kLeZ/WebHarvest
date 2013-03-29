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

package org.webharvest.runtime.processors.plugins.db;

import java.sql.Connection;

import org.webharvest.annotation.Definition;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.database.ConnectionFactory;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Web Harvest plugin supporting database operations within JNDI - enabled
 * environment. Database connection is obtained from
 * {@link org.webharvest.runtime.database.ConnectionFactory}, thus, its
 * implementation must be JNDI-aware. {@link DatabaseJNDIPlugin} allows for
 * execution of database queries in the same way as the standard
 * {@link DatabasePlugin}, but also allows for integration with some external
 * connection pool (c3p0, dbcp and so forth).
 *
 * @see DatabaseJNDIPlugin
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
@Autoscanned
@TargetNamespace({ "http://web-harvest.sourceforge.net/schema/2.1/jndi" })
@Definition(value = "database", validAttributes = {
        DatabaseJNDIPlugin.JNDI_NAME_ATTRIBUTE })
public final class DatabaseJNDIPlugin extends AbstractDatabasePlugin {

    /**
     * Name of XML attribute representing JNDI hook name.
     */
    public static final String JNDI_NAME_ATTRIBUTE = "name";

    @Inject
    @Named("jndi")
    private ConnectionFactory connectionFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Connection obtainConnection(final DynamicScopeContext context) {
        final String jndiHook = evaluateAttribute(JNDI_NAME_ATTRIBUTE, context);
        return connectionFactory.getConnection(jndiHook);
    }
}
