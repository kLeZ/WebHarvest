package org.webharvest;

/**
 * Interface to be implemented by any object that wishes to be notified of
 * scraping scope possibly it runs in.
 * Currently it is the only solution to receive notification about scope's
 * state changes.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
// FIXME rbala What will happen to an instance created in a scope? How can we
// unregister it?
public interface ScrapingAware {

    /**
     * Receive notification about new scope initialized for particular
     * {@link Harvester} object. This notification is sent just after the scope
     * has been created, but usually before any bean is created as a part of it.
     *
     * @param harvester
     *            the reference to {@link Harvester} for which the scope has
     *            been created.
     */
    void onBeforeScraping(Harvester harvester);

    /**
     * Receive notification about the scope for particular {@link Harvester}
     * that is about to be tore down. This notification is sent just before
     * destroying the scope and its all associated beans.
     *
     * @param harvester
     *            the reference to {@link Harvester} for which the scope is
     *            about to be tore down.
     */
    void onAfterScraping(Harvester harvester);

}
