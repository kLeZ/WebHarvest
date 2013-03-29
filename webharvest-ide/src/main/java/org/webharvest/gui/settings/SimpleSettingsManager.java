package org.webharvest.gui.settings;

import java.util.HashSet;
import java.util.Set;

import org.webharvest.gui.Settings;

/**
 * An implementation of {@link SettingsManager} representing simple class
 * supporting {@link SettingsAware} instances registering and informing them
 * about load/update action on the global {@link Settings}.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class SimpleSettingsManager implements SettingsManager {

    private Set<SettingsAware> settingsAwareSet = new HashSet<SettingsAware>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSettingsAware(final SettingsAware settingsAware) {
        settingsAwareSet.add(settingsAware);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettings(final Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings must be not null.");
        }
        for (SettingsAware settingsAware: settingsAwareSet) {
            settingsAware.onLoad(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSettings(final Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings must be not null.");
        }
        for (SettingsAware settingsAware: settingsAwareSet) {
            settingsAware.onUpdate(settings);
        }
    }

}
