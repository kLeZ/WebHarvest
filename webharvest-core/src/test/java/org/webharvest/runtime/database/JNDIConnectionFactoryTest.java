package org.webharvest.runtime.database;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.webharvest.exception.DatabaseException;

public class JNDIConnectionFactoryTest extends UnitilsTestNG {

    private JNDIConnectionFactory factory;

    private Context mockContext;

    private DataSource mockDataSource;

    private Connection mockConnection;

    @BeforeMethod
    public void setUp() {
        this.mockDataSource = EasyMockUnitils.createMock(DataSource.class);
        this.mockConnection = EasyMockUnitils.createMock(Connection.class);
        this.mockContext = EasyMockUnitils.createRegularMock(Context.class);

        this.factory = new JNDIConnectionFactory() {
            protected Context createContext()
                    throws javax.naming.NamingException {
                return mockContext;
            };
        };
    }

    @AfterMethod
    public void tearDown() {
        this.mockDataSource = null;
        this.mockConnection = null;
        this.mockContext = null;
        this.factory = null;
    }

    @Test
    public void getConnectionForExistingHook() throws Exception {
        expect(mockContext.lookup(eq("java:comp/env/hook")))
            .andReturn(mockDataSource);
        expect(mockDataSource.getConnection()).andReturn(mockConnection);

        EasyMockUnitils.replay();
        final Connection connection = factory.getConnection("hook");
        assertSame("Unexpected connection returned",
                mockConnection, connection);
    }

    @Test
    public void getConnectionWhenMissingHook() throws Exception {
        expect(mockContext.lookup(eq("java:comp/env/notexisting"))).andThrow(
                new NamingException("TEST"));

        EasyMockUnitils.replay();
        try {
            factory.getConnection("notexisting");
            fail("DatabaseException expected");
        } catch (DatabaseException e) {
            // ok, expected
        }
    }

    @Test
    public void getConnectionWhenDatasourceFails() throws Exception {
        expect(mockContext.lookup(eq("java:comp/env/jdbc/hook")))
            .andReturn(mockDataSource);
        expect(mockDataSource.getConnection()).andThrow(
                new SQLException("TEST"));

        EasyMockUnitils.replay();
        try {
            factory.getConnection("jdbc/hook");
            fail("DatabaseException expected");
        } catch (DatabaseException e) {
            // ok, expected
        }
    }

    @Test
    public void getConnectionForFullyQualifiedHookName() throws Exception {
        expect(mockContext.lookup(eq("java:env/db/book"))).andReturn(
                mockDataSource);
        expect(mockDataSource.getConnection()).andReturn(mockConnection);

        EasyMockUnitils.replay();
        final Connection connection = factory.getConnection("java:env/db/book");
        assertSame("Unexpected connection returned",
                mockConnection, connection);
    }

    @Test(expectedExceptions = DatabaseException.class)
    public void doNotCreateFactoryWhenContextFails() {
        new JNDIConnectionFactory() {
            protected Context createContext()
                    throws javax.naming.NamingException {
                throw new NamingException("TEST");
            };
        };
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void createConnectionForParametersUnsupported() throws Exception {
        factory.getConnection("com.my.Driver", "jdbc:my://naughty:3306/url",
                null, null);
    }
}
