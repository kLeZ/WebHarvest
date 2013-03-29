package org.webharvest.runtime.processors;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.lang.reflect.Field;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.mock.Mock;
import org.webharvest.ioc.InjectorHelper;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.ScraperState;
import org.webharvest.runtime.StatusHolder;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;

import com.google.inject.Injector;

public class StoppedOrExitedProcessorTest extends UnitilsTestNG {

    @RegularMock
    private StatusHolder mockStatusHolder;

    @RegularMock
    private DynamicScopeContext mockContext;

    @RegularMock
    private Processor mockProcessor;

    private Mock<Injector> mockInjector;

    private StoppedOrExitedProcessor processor;

    @BeforeMethod
    public void setUp() throws Exception {
        mockInjector.returns(mockStatusHolder).getInstance(StatusHolder.class);

        // Puts Injector's mock into InjectorHelper class
        final Field injectorField = InjectorHelper.class
                .getDeclaredField("injector");
        injectorField.setAccessible(true);
        injectorField.set(null, mockInjector.getMock());

        processor = new StoppedOrExitedProcessor(mockProcessor);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        processor = null;
    }


    @Test
    public void runIfStoppedStatus() throws Exception {
        expect(mockStatusHolder.getStatus())
            .andReturn(ScraperState.STOPPED);

        EasyMockUnitils.replay();

        final Variable result = processor.run(mockContext);
        assertNotNull("Result variable is null.", result);
        assertEquals("Unexpected variable.", result, EmptyVariable.INSTANCE);
    }

    @Test
    public void runIfExitStatus() throws Exception {
        expect(mockStatusHolder.getStatus())
            .andReturn(ScraperState.EXIT);

        EasyMockUnitils.replay();

        final Variable result = processor.run(mockContext);
        assertNotNull("Result variable is null.", result);
        assertEquals("Unexpected variable.", result, EmptyVariable.INSTANCE);
    }

    @Test
    public void run() throws Exception {
        final Variable var = EmptyVariable.INSTANCE;

        expect(mockStatusHolder.getStatus())
            .andReturn(ScraperState.RUNNING);

        expect(mockProcessor.run(eq(mockContext)))
            .andReturn(var);

        EasyMockUnitils.replay();

        final Variable result = processor.run(mockContext);
        assertNotNull("Result variable is null.", result);
        assertEquals("Unexpected variable.", result, var);
    }

}
