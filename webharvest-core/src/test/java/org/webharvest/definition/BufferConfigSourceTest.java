package org.webharvest.definition;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.definition.ConfigLocationVisitor.VisitableLocation;

public class BufferConfigSourceTest extends UnitilsTestNG {

    private static final String CONTENT = "blah, blah";

    @RegularMock
    private ConfigLocationVisitor mockVisitor;

    @RegularMock
    private VisitableLocation mockVisitableLocation;

    private BufferConfigSource source;

    @BeforeMethod
    public void setUp() throws Exception {
        source = new BufferConfigSource(CONTENT);
    }

    @AfterMethod
    public void testTearDown() {
        source = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCreateWithoutContent() {
        new BufferConfigSource(null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCreateWithoutContentButWithLocation() {
        new BufferConfigSource(null, ConfigSource.UNDEFINED_LOCATION);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testCreateWithContentButWithoutLocation() {
        new BufferConfigSource(CONTENT, null);
    }

    @Test
    public void testGetLocation() {
        assertNotNull(source.getLocation());
        assertEquals(source.getLocation(), ConfigSource.UNDEFINED_LOCATION);
    }

    @Test
    public void testGetReader() throws IOException {
        final Reader reader = source.getReader();
        assertNotNull(reader);
        assertTrue((reader instanceof StringReader));
    }

    @Test
    public void testVisitWithUnvisitableLocation() throws IOException {
        EasyMockUnitils.replay();
        source.visit(mockVisitor);
    }

    @Test
    public void testVisitWithVisitableLocation() throws IOException {
        mockVisitableLocation.accept(mockVisitor);
        expectLastCall();
        EasyMockUnitils.replay();
        new BufferConfigSource(CONTENT,
                mockVisitableLocation).visit(mockVisitor);
    }

}
