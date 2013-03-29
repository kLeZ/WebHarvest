package org.webharvest.runtime;

/**
 * Component responsible for providing information about current status of
 * being processed configuration.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface StatusHolder {

    /**
     * Returns current status of being processed configuration.
     *
     * @return current status of being processed configuration.
     */
    ScraperState getStatus();

}
