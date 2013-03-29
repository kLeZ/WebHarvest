package org.webharvest.definition.validation;

import static org.testng.AssertJUnit.assertNotNull;
import java.net.URI;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.webharvest.TransformationException;

public class ResourcePathToURITransformerTest extends UnitilsTestNG {

    private ResourcePathToURITransformer transformer;

    @BeforeMethod
    public void setUp() {
        transformer = new ResourcePathToURITransformer();
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
        transformer.transform("/incorrect/path");
    }

    @Test
    public void testTransform() throws Exception {
        final URI uri = transformer.transform("/mockSchema.xsd");
        assertNotNull("Returned URI is null.", uri);
    }


}
