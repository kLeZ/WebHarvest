package org.webharvest.runtime.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Abstract SQL {@link Driver} mock. Facilitates testing classes using
 * {@link java.sql.DriverManager} static methods (not possible to mock).
 */
public abstract class AbstractMockedDriver implements Driver {

    /**
     * Returns SQL URL accepted by driver.
     */
    protected abstract String getURLAccepted();

    /**
     * Handles connection logic. This method is invoked only, if driver
     * agreed to accept URL proposed by the driver manager.
     */
    protected abstract Connection doConnect(String url, Properties info)
            throws SQLException;

    /**
     * {@inheritDoc}
     */
    @Override
    public final Connection connect(final String url,
            final Properties info) throws SQLException {
        if (getURLAccepted().equals(url)) {
            return doConnect(url, info);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean acceptsURL(final String url) throws SQLException {
        return getURLAccepted().equals(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url,
            final Properties info) throws SQLException {
        throw new UnsupportedOperationException("TEST MOCK");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException("TEST MOCK");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException("TEST MOCK");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean jdbcCompliant() {
        throw new UnsupportedOperationException("TEST MOCK");
    }

    // added in JDK7 Driver interface, we can't add @Override because
    // it would break build in JDK6 ;/
    public Logger getParentLogger() {
        throw new UnsupportedOperationException("TEST MOCK");
    }

}
