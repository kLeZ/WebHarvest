package org.webharvest.runtime.database;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DefaultDriverManagerTest {

    private static final String JAR_DRIVER_LOCATION =
        "src/test/resources/MockDBDriver.jar";
    private static final String JAR_DRIVER_CLASS = "MockJarDriver";
    private static final String JAR_DRIVER_URL_ACCEPTED =
        "jdbc:dummydb:jarhost";

    private final DefaultDriverManager manager = DefaultDriverManager.INSTANCE;

    @BeforeMethod
    public void setUp() {
        manager.addDriverResource(new File(JAR_DRIVER_LOCATION).toURI());

    }

    @AfterMethod
    public void tearDown() {
        manager.removeDriverResource(new File(JAR_DRIVER_LOCATION).toURI());
    }

    /**
     * Tests if driver available on classpath ({@link MockClasspathDriver})
     * can be successfully registered by manager.
     */
    @Test
    public void registerDriverFromClassPath() throws Exception {
        manager.registerDriver(MockClasspathDriver.class.getName());
        assertNotNull("Driver has not been registered",
                DriverManager.getDriver(MockClasspathDriver.URL_ACCEPTED));
    }

    /**
     * Tests if in case of registration of driver not available on classpath
     * nor in any of previously added resources,
     * {@link ClassNotFoundException} is thrown.
     */
    @Test(expectedExceptions = ClassNotFoundException.class)
    public void doNotRegisterNotExistingDriver() throws Exception {
        manager.registerDriver("not.existing.Driver123");
    }

    /**
     * Tests if driver accessible in the one of previously added resources
     * (but not on the classpath!) can be successfully registered.
     */
    @Test
    public void registerDriverFromJAR() throws Exception {
        manager.registerDriver(JAR_DRIVER_CLASS);
        assertNotNull("Driver has not been registered",
                DriverManager.getDriver(JAR_DRIVER_URL_ACCEPTED));
    }

    /**
     * Tests if resources are properly removed - if resource has been removed,
     * {@link ClassNotFoundException} should be thrown in case of instantiation
     * of driver located in this resource.
     */
    @Test
    public void unregisteredResourceNoLongerAccessible() {
          manager.removeDriverResource(new File(JAR_DRIVER_LOCATION).toURI());
          try {
              manager.registerDriver(JAR_DRIVER_CLASS);
              fail("ClassNotFoundException expected");
          } catch (ClassNotFoundException e) {
              // ok, it's expected
          }
    }

    /**
     * Mock {@link Driver} implementation. It is available on test's classpath.
     */
    static class MockClasspathDriver extends AbstractMockedDriver {

        public static String URL_ACCEPTED = "jdbc:dummydb:myhost";

        @Override
        protected String getURLAccepted() {
            return URL_ACCEPTED;
        }

        @Override
        protected Connection doConnect(final String url, final Properties info)
                throws SQLException {
            throw new UnsupportedOperationException("TEST MOCK");
        }
    }
}
