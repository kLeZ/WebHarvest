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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.AbstractRegistry;
import org.webharvest.AlreadyBoundException;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.exception.DatabaseException;

import com.google.common.eventbus.Subscribe;

/**
 * Default, standalone {@link ConnectionFactory} interface implementation. It
 * provides very simple connection pooling - {@link Connection} created once for
 * the given driver, url and username is reused later. While this will work fine
 * with the short-living object lifecycle (as it is in case of {@link Scraper}),
 * it is discouraged to use {@link StandaloneConnectionPool} as an
 * application-wide singleton having long lifecycle.
 * <p/>
 * This implementation is not thread-safe and does not provide advanced
 * connection pooling features. It can not execute interval connection
 * validation, does not support connection retrieval and so forth. Additionally,
 * in case of having too long lifecycle it is vulnerable for connection
 * timeouts. For production deployments you should certainly use another
 * {@link ConnectionFactory} implementation.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class StandaloneConnectionPool implements ConnectionFactory {

    private static final Logger LOG = LoggerFactory.getLogger(
            StandaloneConnectionPool.class);

    private final ConnectionsRegistry registry = new ConnectionsRegistry();

    private final DriverManager driverManager = DefaultDriverManager.INSTANCE;

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection(final String driver, final String url,
            final String username, final String password) {

        final String connectionKey = getConnectionKey(driver, url, username);
        ConnectionProxy connection = registry.lookup(connectionKey);
        if (connection == null) {
            connection = createConnectionProxy(createNewConnection(
                    driver, url, username, password));
            try {
                registry.bind(connectionKey, connection);
            } catch (AlreadyBoundException e) {
                throw new RuntimeException(e); // not possible in the single
            }                                  // threaded environment
        }

        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection(final String name) {
        throw new UnsupportedOperationException(
                "Does not support retrieval of connection by name");
    }

    /**
     * Reacts on the end of configuration execution releasing all pooled
     * database connections.
     *
     * @param event
     *            {@link ScraperExecutionEndEvent} occurred
     */
    @Subscribe
    public void onExecutionEnd(final ScraperExecutionEndEvent event) {
        System.out.println("POOL: " + this);
        releaseAllConnections();
    }

    /**
     * Reacts on the configuration execution error releasing all pooled database
     * connections.
     *
     * @param event
     *            {@link ScraperExecutionErrorEvent} occurred
     */
    @Subscribe
    public void onExecutionError(final ScraperExecutionErrorEvent event) {
        releaseAllConnections();
    }

    private Connection createNewConnection(final String driver,
            final String url, final String username, final String password) {
        try {
            LOG.info("Creating new database connection for url: {}", url);
            driverManager.registerDriver(driver);
            return java.sql.DriverManager.getConnection(
                    url, username, password);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException(e);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private String getConnectionKey(final String driver, final String url,
            final String username) {
        return new StringBuilder(driver)
            .append(url)
            .append(username)
            .toString();
    }

    private void releaseAllConnections() {
        LOG.info("Releasing all database connections...");
        for (String key : registry.listBound()) {
            try {
                registry.lookup(key).getTargetConnection().close();
            } catch (SQLException e) {
                LOG.warn("Exception occurred during database connection "
                        + "closing", e);
            }
        }
        LOG.info("Connections released.");
    }

    private ConnectionProxy createConnectionProxy(final Connection delegate) {
        return (ConnectionProxy) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class< ? >[] {ConnectionProxy.class},
                new ConnectionProxyInvocationHandler(delegate));
    }

    /**
     * Registry binding string-based key with proxied database
     * {@link Connection} instance.
     */
    private static final class ConnectionsRegistry extends
            AbstractRegistry<String, ConnectionProxy> {
    }

    /**
     * Dynamic proxy {@link InvocationHandler}. Suppress calls to
     * {@link Connection#close()} method as well as implements
     * {@link ConnectionProxy#getTargetConnection()} method returning proxied
     * delegate instance.
     * <p/>
     * Connection close is suppressed to make it possible to reuse existing
     * connections and because only the pool itself should decide when to close
     * the connection.
     */
    private static final class ConnectionProxyInvocationHandler implements
            InvocationHandler {

        private final Connection delegate;

        public ConnectionProxyInvocationHandler(final Connection delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object invoke(final Object proxy, final Method method,
                final Object[] args) throws Throwable {

            if (ConnectionProxy.class.getMethod("getTargetConnection")
                    .equals(method)) {
                return this.delegate;
            }

            if (Connection.class.getMethod("close")
                    .equals(method)) {
                return null;
            }
            return method.invoke(delegate, args);
        }
    }
}
