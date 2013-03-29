package org.webharvest.runtime;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.easymock.EasyMock.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.unitils.util.ReflectionUtils;

import com.google.common.util.concurrent.Monitor;

public class RunningStatusGuardTest extends UnitilsTestNG {

    @RegularMock
    private StatusHolder mockHolder;

    private RunningStatusGuard guard;

    @BeforeMethod
    public void setUp() throws Exception {
        guard = new RunningStatusGuard(new Monitor());

        ReflectionUtils.setFieldValue(guard,
                RunningStatusGuard.class.getDeclaredField("statusHolder"),
                mockHolder);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        guard = null;
    }

    @Test
    public void isSatisfiedIfRunningStatus() throws Exception {
        expect(mockHolder.getStatus()).andReturn(ScraperState.RUNNING);

        EasyMockUnitils.replay();

        final boolean result = guard.isSatisfied();
        assertTrue("Unexpected result.", result);
    }

    @Test
    public void isSatisfiedIfOtherStatus() throws Exception {
        expect(mockHolder.getStatus()).andReturn(ScraperState.PAUSED);

        EasyMockUnitils.replay();

        final boolean result = guard.isSatisfied();
        assertFalse("Unexpected result.", result);
    }
}
