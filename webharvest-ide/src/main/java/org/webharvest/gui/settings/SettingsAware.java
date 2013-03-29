package org.webharvest.gui.settings;

import org.webharvest.gui.Settings;

/**
 * Component is responsible for loading and updating appropriate part of global
 * {@link Settings}. It reacts when {@link Settings} are loaded or updated e.g.
 * by configuration panel.
 *
 * @see Settings
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface SettingsAware {

    /**
     * Retrieves from given {@link Settings} information designed for this
     * component.
     *
     * @param settings
     *            reference to global {@link Settings}
     */
    void onLoad(Settings settings);

    /**
     * Sets into given {@link Settings} information modified by this component.
     *
     * @param settings
     *            reference to global {@link Settings}
     */
    void onUpdate(Settings settings);

}
