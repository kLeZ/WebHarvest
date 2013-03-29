package org.webharvest.gui.settings;

import static org.easymock.EasyMock.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.easymock.EasyMockUnitils;
import org.unitils.easymock.annotation.RegularMock;
import org.webharvest.gui.Settings;

public class SimpleSettingsManagerTest extends UnitilsTestNG {

    @RegularMock
    private SettingsAware mockSettingsAware;

    private SimpleSettingsManager manager;

    @BeforeMethod
    public void setUp() {
        manager = new SimpleSettingsManager();
        manager.addSettingsAware(mockSettingsAware);
    }

    @AfterMethod
    public void tearDown() {
        manager = null;
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testLoadIfNullSettings() {
        manager.loadSettings(null);
    }

    @Test
    public void testLoad() {
        final Settings settings = new Settings();

        mockSettingsAware.onLoad(same(settings));
        expectLastCall();

        EasyMockUnitils.replay();

        manager.loadSettings(settings);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testUpdateIfNullSettings() {
        manager.updateSettings(null);
    }

    @Test
    public void testUpdate() {
        final Settings settings = new Settings();

        mockSettingsAware.onUpdate(same(settings));
        expectLastCall();

        EasyMockUnitils.replay();

        manager.updateSettings(settings);
    }

}
