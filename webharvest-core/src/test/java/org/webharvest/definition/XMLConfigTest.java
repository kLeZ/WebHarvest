package org.webharvest.definition;

import static org.easymock.EasyMock.*;
import static org.testng.AssertJUnit.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.testng.annotations.*;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.WHConstants;

public class XMLConfigTest extends UnitilsTestNG {

    private static final String BASIC_CONFIG_SKELETON = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
        "<config xmlns=\"" + WHConstants.XMLNS_CORE + "\"\n" +
        "\t\txmlns:var=\"" + WHConstants.XMLNS_VAR + "\"\n" +
        "\t\txmlns:p=\"" + WHConstants.XMLNS_PARAM + "\">\n" +
        "\t\n" +
        "</config>";

    @RegularMock
    private ConfigSource mockConfigSource;

    private XMLConfig config;

    private Reader mockReader;

    @BeforeMethod
    public void setUp() {
        mockReader = new StringReader(BASIC_CONFIG_SKELETON);
        config = new XMLConfig(mockConfigSource);
    }

    @AfterMethod
    public void tearDown() {
        config = null;
        mockReader = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorWithoutSource() {
        new XMLConfig(null);
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void testGetElementDefWithoutLoadedConfiguration() {
        config.getElementDef();
    }

    @Test
    public void testGetConfigSource() {
        assertSame(config.getConfigSource(), mockConfigSource);
    }

    @Test
    public void testLoad() throws IOException {
        expect(mockConfigSource.getReader()).andReturn(mockReader);
        EasyMockUnitils.replay();
        config.reload();
        final IElementDef def = config.getElementDef();
        assertNotNull(def);
    }

}
