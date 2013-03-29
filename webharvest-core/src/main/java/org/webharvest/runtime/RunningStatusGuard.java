package org.webharvest.runtime;

import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.Monitor.Guard;
import com.google.inject.Inject;

/**
 * Implementation of {@link Guard} verifying that current status of
 * configuration's processing is 'running'.
 *
 * @see RunningStatusController
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class RunningStatusGuard extends Guard {

    @Inject
    private StatusHolder statusHolder;

    /**
     * Default class constructor which accepts reference to {@link Monitor}.
     *
     * @param monitor
     *            reference to {@link Monitor}; must not be {@code null}
     */
    @Inject
    public RunningStatusGuard(final Monitor monitor) {
        super(monitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSatisfied() {
        return statusHolder.getStatus() == ScraperState.RUNNING;
    }

}
