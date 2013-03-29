package org.webharvest.gui.settings;

import org.webharvest.gui.Settings;

/**
 * Component according to the Observer design pattern informs all registered
 * {@link SettingsAware} instances (observers) about loading or updating
 * {@link Settings} action.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface SettingsManager {

    /**
     * Registers given {@link SettingsAware} instance in order to inform it
     * about settings loading/updating action.
     *
     * @param settingsAware
     *            an instance of {@link SettingsAware} which is going to be
     *            registered
     */
    void addSettingsAware(SettingsAware settingsAware);

    /**
     * Passes to all registered {@link SettingsAware} instances information
     * about loading {@link Settings}.
     *
     * @param settings
     *            an instance of {@link Settings} which is going to be passed to
     *            observers
     */
    void loadSettings(Settings settings);

    /**
     * Passes to all registered {@link SettingsAware} instances information that
     * {@link Settings} should be updated.
     *
     * @param settings
     *            an instance of {@link Settings} which is going to be passed to
     *            observers
     */
    void updateSettings(Settings settings);

}
