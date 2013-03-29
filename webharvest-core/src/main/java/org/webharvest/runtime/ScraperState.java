package org.webharvest.runtime;

/**
 * An enum containing all available Scraper's states.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public enum ScraperState {
    /**
     * Scraper is ready to use.
     */
    READY,
    /**
     * Scraper is running.
     */
    RUNNING,
    /**
     * Scraper has been paused.
     */
    PAUSED,
    /**
     * Scraper has successfully finished its work.
     */
    FINISHED,
    /**
     * Scraper has been stopped by the user.
     */
    STOPPED,
    /**
     * Scraper has finished its work with error.
     */
    ERROR,
    /**
     * Scraper's execution has been stopped by exit command.
     */
    EXIT,
    /**
     * Unknown state of the Scraper.
     */
    @Deprecated
    UNKNOWN
}
