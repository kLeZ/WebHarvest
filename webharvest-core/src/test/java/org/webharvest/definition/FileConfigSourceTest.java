package org.webharvest.definition;

import static org.testng.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.webharvest.definition.FileConfigSource.FileLocation;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.definition.ConfigSource.Location;

public class FileConfigSourceTest extends UnitilsTestNG {

    private File configFile;

    private FileConfigSource source;

    @RegularMock
    private ConfigLocationVisitor mockVisitor;

    @BeforeMethod
    public void setUp() throws Exception {
        configFile = File.createTempFile("foo", "tmp");
        source = new FileConfigSource(configFile);
    }

    @AfterMethod
    public void testTearDown() {
        source = null;
        configFile.delete();
        configFile = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCreateWithoutFile() {
        new FileConfigSource((File) null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCreateWithoutLocation() {
        new FileConfigSource((FileLocation) null);
    }

    @Test
    public void testGetLocation() {
        final Location location = source.getLocation();
        assertNotNull(location);
        assertEquals(location.toString(), configFile.getAbsolutePath());
    }

    @Test
    public void testGetReader() throws IOException {
        final Reader reader = source.getReader();
        assertNotNull(reader);
        assertTrue((reader instanceof FileReader));
    }

    @Test
    public void testVisitWithVisitableLocation() throws IOException {
        final FileLocation mockLocation = new FileLocation(configFile);
        mockVisitor.visit(mockLocation);
        EasyMockUnitils.replay();
        new FileConfigSource(mockLocation).visit(mockVisitor);
    }

}
