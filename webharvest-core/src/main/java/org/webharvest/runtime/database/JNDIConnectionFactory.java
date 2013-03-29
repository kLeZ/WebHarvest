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

package org.webharvest.runtime.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.webharvest.exception.DatabaseException;

/**
 * {@link ConnectionFactory} implementation supporting JNDI {@link DataSource}
 * lookups. It is recommended, production-ready {@link ConnectionFactory}
 * implementation. Allows for integration with c3p0, DBCP or other well-known
 * connection pools.
 *
 * @see ConnectionFactory
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public class JNDIConnectionFactory implements ConnectionFactory {

    /**
     * Environment-related component's binding subtree used as default by JavaEE
     * container.
     */
    public static final String CONTAINER_PREFIX = "java:comp/env/";

    private final Context context;

    /**
     * Instantiates {@link JNDIConnectionFactory}, obtaining reference to
     * the JNDI {@link Context}. If no reference to {@link Context} can be
     * obtained, {@link NamingException} is thrown.
     */
    public JNDIConnectionFactory() {
        try {
            this.context = createContext();
        } catch (NamingException e) {
            throw new DatabaseException("Cannot obtain JNDI resource", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection(final String name) {
        try {
            final DataSource ds = (DataSource) context.lookup(
                    convertIntoJndiName(name));
            return ds.getConnection();
        } catch (NamingException e) {
            throw new DatabaseException("Cannot obtain JNDI resource", e);
        } catch (SQLException e) {
            throw new DatabaseException("Cannot open connection", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection(final String driver, final String url,
            final String username, final String password) {
        throw new UnsupportedOperationException();
    }

    protected Context createContext() throws NamingException {
        return new InitialContext();
    }

    /**
     * Converts provided name to conform java enterprise JNDI naming policy. If
     * provided name is fully-qualified JNDI binding, it is returned unchanged.
     * Otherwise, when provided name does not fulfill JNDI naming policy, it is
     * prefixed with environment-related component's binding subtree
     * ({@code java:comp/env}).
     */
    protected String convertIntoJndiName(final String name) {
        if (!name.startsWith(CONTAINER_PREFIX) && name.indexOf(':') == -1) {
            return CONTAINER_PREFIX + name;
        }
        return name;
    }
}
