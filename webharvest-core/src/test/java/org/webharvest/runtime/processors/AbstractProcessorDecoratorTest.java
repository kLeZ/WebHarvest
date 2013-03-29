package org.webharvest.runtime.processors;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.same;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.definition.IElementDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.variables.Variable;

public class AbstractProcessorDecoratorTest extends UnitilsTestNG {

    @RegularMock
    private IElementDef mockElement;

    @RegularMock
    private Processor mockProcessor;

    private MockDecorator decorator;

    @BeforeMethod
    public void setUp() throws Exception {
        decorator = new MockDecorator(mockProcessor);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        decorator = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void constructorIfNullProcessor() {
        new MockDecorator(null);
    }

    @Test
    public void setElementDef() {
        mockProcessor.setElementDef(same(mockElement));
        expectLastCall();

        EasyMockUnitils.replay();

        decorator.setElementDef(mockElement);
    }

    @Test
    public void getElementDef() {
        expect(mockProcessor.getElementDef()).andReturn(mockElement);

        EasyMockUnitils.replay();

        final IElementDef element = decorator.getElementDef();
        assertNotNull("Returned element is null.", element);
        assertSame("Unexpected element.", mockElement, element);
    }

    @Test
    public void getParentProcessor() {
        expect(mockProcessor.getParentProcessor()).andReturn(mockProcessor);

        EasyMockUnitils.replay();

        final Processor parent = decorator.getParentProcessor();
        assertNotNull("Parent processor is null.", parent);
        assertSame("Unexpected parent processor.", mockProcessor, parent);
    }

    @Test
    public void setParentProcessor() {
        mockProcessor.setParentProcessor(same(mockProcessor));
        expectLastCall();

        EasyMockUnitils.replay();

        decorator.setParentProcessor(mockProcessor);
    }

    @Test
    public void getRunningLevel() {
        expect(mockProcessor.getRunningLevel()).andReturn(2);

        EasyMockUnitils.replay();

        final int level = decorator.getRunningLevel();
        assertEquals("Unexpected element.", 2, level);
    }


    private class MockDecorator extends AbstractProcessorDecorator {

        public MockDecorator(final Processor decoratedProcessor) {
            super(decoratedProcessor);
        }

        @Override
        public Variable run(DynamicScopeContext context)
                throws InterruptedException {
            return null;
        }

    }
}
