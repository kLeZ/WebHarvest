package org.webharvest.runtime.database;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.easymock.classextension.EasyMock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.exception.DatabaseException;

public class StandaloneConnectionPoolIntegrationTest extends UnitilsTestNG {

    private StandaloneConnectionPool pool;

    @BeforeMethod
    public void setUp() {
        this.pool = new StandaloneConnectionPool();
    }

    @AfterMethod
    public void tearDown() {
        this.pool = null;
    }

    @Test
    public void createsBrandNewConnection() {
        final Connection connection = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);
        assertNotNull("Null connection", connection);
    }

    @Test
    public void connectionIsPooled() {
        final Connection connection1 = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);
        final Connection connection2 = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);
        final Connection connection3 = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);

        final Connection connection4 = pool.getConnection(
                MockDriver2.class.getName(),
                MockDriver2.URL, MockDriver2.USER, MockDriver2.PASSWORD);

        assertTrue("Connection is not pooled",
                connection1 == connection2
                && connection2 == connection3);
        assertFalse("Connections are not distinguished propely",
                connection1 == connection4);
    }

    @Test(expectedExceptions = DatabaseException.class)
    public void throwsExceptionForMissingDriver() {
        pool.getConnection("not.existing.Driver",
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);
    }

    @Test
    public void releaseConnectionOnExecutionError() throws Exception {
        final Connection connection1 = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);
        final Connection connection2 = pool.getConnection(
                MockDriver2.class.getName(),
                MockDriver2.URL, MockDriver2.USER, MockDriver2.PASSWORD);

        ((ConnectionProxy) connection1).getTargetConnection().close();
        EasyMock.expectLastCall();
        ((ConnectionProxy) connection2).getTargetConnection().close();
        EasyMock.expectLastCall();

        EasyMockUnitils.replay();
        pool.onExecutionError(new ScraperExecutionErrorEvent(null));
        EasyMockUnitils.verify();
    }

    @Test
    public void releaseConnectionOnExecutionEnd() throws Exception {
        final Connection connection1 = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);
        final Connection connection2 = pool.getConnection(
                MockDriver2.class.getName(),
                MockDriver2.URL, MockDriver2.USER, MockDriver2.PASSWORD);

        ((ConnectionProxy) connection1).getTargetConnection().close();
        EasyMock.expectLastCall();
        ((ConnectionProxy) connection2).getTargetConnection().close();
        EasyMock.expectLastCall();

        EasyMockUnitils.replay();
        pool.onExecutionEnd(new ScraperExecutionEndEvent(null, 0));
        EasyMockUnitils.verify();
    }

    @Test
    public void tryToReleaseAllConnections() throws Exception {
        final Connection connection1 = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);
        final Connection connection2 = pool.getConnection(
                MockDriver2.class.getName(),
                MockDriver2.URL, MockDriver2.USER, MockDriver2.PASSWORD);

        ((ConnectionProxy) connection1).getTargetConnection().close();
        EasyMock.expectLastCall().andThrow(new SQLException("TEST"));
        ((ConnectionProxy) connection2).getTargetConnection().close();
        EasyMock.expectLastCall();

        EasyMockUnitils.replay();
        pool.onExecutionEnd(new ScraperExecutionEndEvent(null, 0));
        EasyMockUnitils.verify();
    }

    @Test
    public void connectionCloseIsSuppresed() throws Exception {
        final Connection connection = pool.getConnection(
                MockDriver.class.getName(),
                MockDriver.URL, MockDriver.USER, MockDriver.PASSWORD);

        EasyMockUnitils.replay();
        connection.close();
        EasyMockUnitils.verify();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void doesNotSupportRetrievalByName() throws Exception {
        pool.getConnection("jdbc/myConnHook");
    }

    static class MockDriver extends AbstractMockedDriver {

        static final String URL = "jdbc:my://naughty:3306/url";
        static final String USER = "whuser";
        static final String PASSWORD = "somepassword";

        @Override
        protected Connection doConnect(final String url, final Properties info)
                throws SQLException {
            assertEquals("Unexpected database url", URL, url);
            assertEquals("Unexpected database username",
                    USER, info.get("user"));
            assertEquals("Unexpected database password",
                    PASSWORD, info.get("password"));
            return EasyMockUnitils.createRegularMock(Connection.class);
        }

        @Override
        protected String getURLAccepted() {
            return URL;
        }
    }

    static class MockDriver2 extends AbstractMockedDriver {
        static final String URL = "jdbc:another2://url:3306/database2";
        static final String USER = "whuser";
        static final String PASSWORD = "somepassword";

        @Override
        protected Connection doConnect(final String url, final Properties info)
                throws SQLException {
            assertEquals("Unexpected database url", URL, url);
            assertEquals("Unexpected database username",
                    USER, info.get("user"));
            assertEquals("Unexpected database password",
                    PASSWORD, info.get("password"));
            return EasyMockUnitils.createRegularMock(Connection.class);
        }

        @Override
        protected String getURLAccepted() {
            return URL;
        }
    }
}
