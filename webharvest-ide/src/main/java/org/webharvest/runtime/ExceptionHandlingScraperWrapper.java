package org.webharvest.runtime;

import javax.inject.Named;

import org.webharvest.events.ScraperExecutionErrorEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * {@link WebScraper} implementation which is a wrapper for default
 * {@link WebScraper} realization. It handles all {@link RuntimeException}s by
 * sending {@link ScraperExecutionErrorEvent} over {@link EventBus}.
 * 
 * It should be used only in Web-Harvest IDE.
 * 
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ExceptionHandlingScraperWrapper implements WebScraper {

	private final WebScraper webScraper;
	private final EventBus eventBus;

	/**
	 * Default class constructor which accepts references to default
	 * {@link WebScraper} instance and to {@link EventBus}.
	 * 
	 * @param webScraper
	 *            reference to default {@link WebScraper} instance; must be not
	 *            {@code null}.
	 * @param eventBus
	 *            reference to {@link EventBus}; must be not null.
	 */
	@Inject
	public ExceptionHandlingScraperWrapper(
			final @Named("default") WebScraper webScraper,
			final EventBus eventBus) {
		if (webScraper == null) {
			throw new IllegalArgumentException("WebScraper is mandatory.");
		}
		if (eventBus == null) {
			throw new IllegalArgumentException("EventBus is mandatory.");
		}
		this.webScraper = webScraper;
		this.eventBus = eventBus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(final DynamicScopeContext context) {
		try {
			webScraper.execute(context);
		} catch (RuntimeException e) {
			eventBus.post(new ScraperExecutionErrorEvent(e));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void informListenersAboutError(final Exception e) {
		webScraper.informListenersAboutError(e);
	}

}
