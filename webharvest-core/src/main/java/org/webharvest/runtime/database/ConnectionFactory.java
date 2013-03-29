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

/**
 * Factory responsible for creating SQL {@link Connection}s basis on the
 * provided parameters. {@link ConnectionFactory} encapsulates all connection
 * configuration details under the hood, allowing to switch between particular
 * implementations transparently.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface ConnectionFactory {

    /**
     * Returns SQL {@link Connection} instance available under the given name
     * (for example under JNDI name within Java EE container). If no
     * {@link Connection} has been bound to the provided name, then
     * {@link DatabaseException} containing root cause is thrown.
     *
     * @param name
     *            name to which connection is bound
     * @return ready for use {@link Connection} instance bound to the given
     *         name, never {@code null}
     */
    Connection getConnection(String name);

    /**
     * Returns SQL {@link Connection} instance configured according to provided
     * parameters.
     *
     * @param driver
     *            fully qualified name of the database driver; mandatory, must
     *            not be {@code null}
     * @param url
     *            JDBC connection url; mandatory, must not be {@code null}
     * @param username
     *            database username
     * @param password
     *            database password
     * @return ready for use {@link Connection} instance
     */
    Connection getConnection(String driver, String url, String username,
            String password);
}
