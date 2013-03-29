package org.webharvest.gui.settings.validation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FilePathToURITransformerTest {

    private FilePathToURITransformer transformer;

    @BeforeMethod
    public void setUp() {
        transformer = new FilePathToURITransformer();
    }

    @AfterMethod
    public void tearDown() {
        transformer = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testTransformationIfNullLocation() throws Exception {
        transformer.transform(null);
    }

    @Test
    public void testTransformation() throws Exception {
        final URL url = getClass().getResource("/empty.xsd");
        final String location = FileUtils.toFile(url).getAbsolutePath();

        final URI uri = transformer.transform(location);
        assertNotNull("Result URI is null.", uri);
        assertEquals("Unexpected URI.", url.toURI(), uri);
    }

}
