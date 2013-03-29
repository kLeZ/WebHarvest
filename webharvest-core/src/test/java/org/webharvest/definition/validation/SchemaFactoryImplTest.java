package org.webharvest.definition.validation;

import static org.testng.AssertJUnit.assertFalse;

import javax.xml.validation.Schema;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.mock.Mock;

public class SchemaFactoryImplTest extends UnitilsTestNG {

    private Mock<SchemaResolverPostProcessor> postProcessor;

    private SchemaFactoryImpl schemaFactory;

    @BeforeMethod
    public void setUp() {
        schemaFactory = SchemaFactoryImpl.INSTANCE;
    }

    @AfterMethod
    public void tearDown() {
        schemaFactory = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testAddNullPostProcessor() {
        schemaFactory.addPostProcessor(null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testRegisterNullSchemaSource() {
        schemaFactory.registerSchemaSource(null);
    }

    @Test
    public void postProcessorExecutedOnRefresh() {
        schemaFactory.addPostProcessor(postProcessor.getMock());

        schemaFactory.refresh();

        postProcessor.assertInvoked().postProcess(schemaFactory);
    }

    @Test
    public void createsNewSchemaOnRefresh() {
        final Schema previous = schemaFactory.getSchema();
        schemaFactory.refresh();
        final Schema current = schemaFactory.getSchema();

        assertFalse("New instance of schema expected", previous == current);
    }

}
