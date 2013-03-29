package org.webharvest.events;

/**
 * Event informing that the execution of configuration has exited.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ScraperExecutionExitEvent {

    private final String message;

    /**
     * Constructs {@link ScraperExecutionExitEvent} accepting cause of of
     * execution's exit.
     *
     * @param message
     *            cause of execution's exit; must not be null.
     */
    public ScraperExecutionExitEvent(final String message) {
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null.");
        }
        this.message = message;
    }

    /**
     * Returns cause of execution's exit.
     *
     * @return cause of execution's exit.
     */
    public String getMessage() {
        return message;
    }
}
