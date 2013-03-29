package org.webharvest.definition.validation;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;

public class SchemaComponentFactoryTest extends UnitilsTestNG {

    @Test
    public void testGetSchemaResolver() {
        final SchemaResolver resolver =
            SchemaComponentFactory.getSchemaResolver();

        assertNotNull("Returned schema resolver is null.", resolver);
        assertSame("Unexpected schema resolver.", SchemaFactoryImpl.INSTANCE,
                resolver);
    }

    @Test
    public void testGetSchemaFactory() {
        final SchemaFactory factory =
            SchemaComponentFactory.getSchemaFactory();

        assertNotNull("Returned schema factory is null.", factory);
        assertSame("Unexpected schema factory.", SchemaFactoryImpl.INSTANCE,
                factory);
    }

}
