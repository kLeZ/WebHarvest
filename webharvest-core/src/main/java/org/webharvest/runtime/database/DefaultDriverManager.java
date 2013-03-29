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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.exception.DatabaseException;

/**
 * Default implementation of the {@link DriverManger} interface. Allows to load
 * database drivers from the arbitrary location. Database driver being
 * registered must be available on classpath or in any of previously added
 * driver resources.
 * <p/>
 * If no resources has been added to {@link DefaultDriverManager} (as it is in
 * most cases when web harvest is embedded into enterprise application running
 * within container), all drivers being registered must be accessible within
 * container's default class loader.
 *
 * @see DefaultDriverManager
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public enum DefaultDriverManager implements DriverManager {

    /**
     * Singleton instance reference
     */
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(
            DefaultDriverManager.class);

    private Set<URI> driverResources = new LinkedHashSet<URI>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDriverResource(final URI location) {
        this.driverResources.add(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDriverResource(final URI location) {
        this.driverResources.remove(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDriver(final String driverClassName)
            throws ClassNotFoundException {
        final Driver driver = createDriver(driverClassName);
        try {
            java.sql.DriverManager.registerDriver(createDriverProxy(driver));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Creates dynamic proxy for the provided {@link Driver} instance. Proxy
     * delegates all operations to the wrapped {@link Driver} instance.
     * Proxying is required since {@link Driver} is created using custom,
     * {@link DefaultDriverManager}'s internal class loader.
     */
    private Driver createDriverProxy(final Driver delegate) {
        return (Driver) Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class< ? >[] {Driver.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy,
                            final Method method, final Object[] args)
                            throws Throwable {
                        return method.invoke(delegate, args);
                    }
                });
    }

    /**
     * For the given class name creates SQL {@link Driver} instance.
     * Driver's class must be available on classpath or in any of previously
     * added resources.
     *
     * @see {@link #addDriverResource(URI)}
     */
    private Driver createDriver(final String className)
            throws ClassNotFoundException {
        final ClassLoader classLoader = URLClassLoader.newInstance(
                toURLs(this.driverResources),
                this.getClass().getClassLoader());

        final Class< ? > clazz = Class.forName(className, true, classLoader);
        try {
            return clazz.asSubclass(Driver.class).newInstance();
        } catch (InstantiationException e) {
            throw new DatabaseException(e);
        } catch (IllegalAccessException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Converts provided set of {@link URI}s into the array of {@link URL}
     * objects required to create new instance of {@link URLClassLoader}.
     * {@link DefaultDriverManager} stores set of {@link URI}s instead of
     * {@link URL}s because of the broken {@link URL#equals(Object)} method
     * (it is blocking operation).
     */
    private URL[] toURLs(final Set<URI> uris) {
        final List<URL> urls = new LinkedList<URL>();
        for (URI resource : uris) {
            try {
                urls.add(resource.toURL());
            } catch (MalformedURLException e) {
                LOG.warn("Cannot retrieve driver resource URL", e);
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }
}
