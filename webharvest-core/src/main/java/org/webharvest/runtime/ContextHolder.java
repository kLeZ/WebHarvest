package org.webharvest.runtime;

/**
 * Component responsible for providing current {@link DynamicScopeContext} which
 * is used by the Scraper.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface ContextHolder {

    /**
     * Returns current {@link DynamicScopeContext} which is used by the Scraper.
     *
     * @return current {@link DynamicScopeContext} which is used by the Scraper.
     */
    DynamicScopeContext getContext();

}
