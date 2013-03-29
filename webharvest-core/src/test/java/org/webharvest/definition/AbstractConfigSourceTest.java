package org.webharvest.definition;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

import org.webharvest.definition.ConfigLocationVisitor.VisitableLocation;
import org.webharvest.definition.URLConfigSource.URLLocation;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.definition.ConfigSource.Location;

public class AbstractConfigSourceTest extends UnitilsTestNG {

    private MockConfigSource source;

    @RegularMock
    private VisitableLocation mockLocation;

    @BeforeMethod
    public void setUp() throws Exception {
        source = new MockConfigSource();
    }

    @AfterMethod
    public void testTearDown() {
        source = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testIncludeWithoutLocation() throws IOException {
        source.include(null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testIncludeWithDefaultLocation() throws IOException {
        source.include(ConfigSource.UNDEFINED_LOCATION);
    }

    @Test
    public void testIncludeWithURLLocation() throws Exception {
        source.setLocation(new URLLocation(new URL("http://sf.net")));
        final ConfigSource included = source.include(
                new Location() {

                    @Override
                    public String toString() {
                        return "/home/";
                    }

                });
        assertNotNull(included);
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void testIncludeWithUnsupportedLocation() throws Exception {
        mockLocation.accept(isA(IncludeVisitor.class));
        expectLastCall();
        source.setLocation(mockLocation);
        EasyMockUnitils.replay();
        source.include(
                new Location() {

                    @Override
                    public String toString() {
                        return "/home/";
                    }

                });
    }

    private class MockConfigSource extends AbstractConfigSource {

        private VisitableLocation location;

        @Override
        public Location getLocation() {
            return location;
        }

        public void setLocation(final VisitableLocation location) {
            this.location = location;
        }

        @Override
        public Reader getReader() throws IOException {
            return null;
        }

        @Override
        protected void visit(ConfigLocationVisitor visitor) throws IOException {
            location.accept(visitor);
        }

    }

}
