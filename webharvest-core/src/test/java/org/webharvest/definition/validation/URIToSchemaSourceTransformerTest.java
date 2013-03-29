package org.webharvest.definition.validation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import java.net.URI;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.webharvest.TransformationException;

public class URIToSchemaSourceTransformerTest extends UnitilsTestNG {

    private URIToSchemaSourceTransformer transformer;

    @BeforeMethod
    public void setUp() {
        transformer = new URIToSchemaSourceTransformer();
    }

    @AfterMethod
    public void tearDown() {
        transformer = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testTransformIfNullPath() throws Exception {
        transformer.transform(null);
    }

    @Test(expectedExceptions=TransformationException.class)
    public void testTransformIfIncorrectPath() throws Exception {
        transformer.transform(new URI("/incorrect/path"));
    }

    @Test
    public void testTransform() throws Exception {
        final URI uri = getClass().getResource("/mockSchema.xsd").toURI();
        final SchemaSource source = transformer.transform(uri);
        assertNotNull("Returned schema source is null.", source);
        assertEquals("Unexpected schema source.", uri.toString(),
                source.toString());
    }


}
