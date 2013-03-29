package org.webharvest.definition.validation;

import static org.easymock.EasyMock.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.tools.ant.filters.StringInputStream;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.TransformationException;
import org.webharvest.Transformer;

public class SchemaResourcesPostProcessorTest extends UnitilsTestNG {

    private static final String RESOURCE1 = "resource1";
    private static final String RESOURCE2 = "resource2";
    private static final String INCORRECT_RESOURCE = "incorrect";

    @RegularMock
    private Transformer<String, SchemaSource> mockTransformer;
    @RegularMock
    private SchemaResolver mockResolver;
    @RegularMock
    private Logger mockLogger;

    private SchemaResourcesPostProcessor<String> postProcessor;

    @BeforeMethod
    public void setUp() throws Exception {
        final Field field =
            SchemaResourcesPostProcessor.class.getDeclaredField("LOG");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, mockLogger);

        postProcessor = new SchemaResourcesPostProcessor<String>(
                mockTransformer, RESOURCE1, RESOURCE2);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        postProcessor = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorIfNullTransformer() {
        new SchemaResourcesPostProcessor<String>(null, RESOURCE1, RESOURCE2);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testConstructorIfNullResourceArray() {
        new SchemaResourcesPostProcessor<String>(mockTransformer, null);
    }

    @Test
    public void testProcessIfFirstResourceIsIncorrect() throws Exception {
        final SchemaSource source =
            new SchemaSource(new StringInputStream(RESOURCE2), RESOURCE2);
        postProcessor = new SchemaResourcesPostProcessor<String>(
                mockTransformer, INCORRECT_RESOURCE, RESOURCE2);

        expect(mockTransformer.transform(same(INCORRECT_RESOURCE))).andThrow(
                new TransformationException(new RuntimeException()));
        mockLogger.error("Transformation of {} resource failed.",
                INCORRECT_RESOURCE);
        expectLastCall();
        expect(mockTransformer.transform(same(RESOURCE2))).andReturn(source);
        mockResolver.registerSchemaSource(same(source));
        expectLastCall();

        EasyMockUnitils.replay();

        postProcessor.postProcess(mockResolver);
    }

    @Test
    public void testProcess() throws Exception {
        final SchemaSource source1 =
            new SchemaSource(new StringInputStream(RESOURCE1), RESOURCE1);
        final SchemaSource source2 =
            new SchemaSource(new StringInputStream(RESOURCE2), RESOURCE2);

        expect(mockTransformer.transform(same(RESOURCE1))).andReturn(source1);
        mockResolver.registerSchemaSource(same(source1));
        expectLastCall();
        expect(mockTransformer.transform(same(RESOURCE2))).andReturn(source2);
        mockResolver.registerSchemaSource(same(source2));
        expectLastCall();

        EasyMockUnitils.replay();

        postProcessor.postProcess(mockResolver);
    }
}
