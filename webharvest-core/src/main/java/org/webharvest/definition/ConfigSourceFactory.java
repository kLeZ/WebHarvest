package org.webharvest.definition;

import java.io.File;
import java.net.URL;

/**
 * Represents factory object capable to instantiate {@link ConfigSource}
 * objects either from {@link URL}, {@link File} or just raw XML configuration.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see ConfigSource
 * @see File
 * @see URL
 */
public interface ConfigSourceFactory {

    /**
     * Create new instance of {@link ConfigSource} from {@link URL}.
     *
     * @param source configuration source.
     * @return new instance of {@link ConfigSource}.
     */
    ConfigSource create(URL source);

    /**
     * Create new instance of {@link ConfigSource} from {@link File}.
     *
     * @param source configuration source.
     * @return new instance of {@link ConfigSource}.
     */
    ConfigSource create(File source);

    /**
     * Create new instance of {@link ConfigSource} from raw XML content.
     *
     * @param source configuration source.
     * @return new instance of {@link ConfigSource}.
     */
    ConfigSource create(String source);

}
