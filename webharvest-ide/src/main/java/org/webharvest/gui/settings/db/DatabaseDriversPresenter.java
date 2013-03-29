/*
 Copyright (c) 2006-2012 the original author or authors.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
   copyright notice, this list of conditions and the
   following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

 * The name of Web-Harvest may not be used to endorse or promote
   products derived from this software without specific prior
   written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
*/

package org.webharvest.gui.settings.db;

import java.io.File;

import org.webharvest.runtime.database.DefaultDriverManager;
import org.webharvest.runtime.database.DriverManager;

/**
 * Default implementation of MVP's {@link DatabaseDriversView.Presenter}
 * interface.
 *
 * @see DatabaseDriversView.Presenter
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class DatabaseDriversPresenter implements
        DatabaseDriversView.Presenter {

    private final DriverManager driverManager = DefaultDriverManager.INSTANCE;

    private final DatabaseDriversView view;

    /**
     * Presenter's constructor accepting not-{@code null} reference to the
     * instance of {@link DatabaseDriversView} with which this presenter is
     * intended to cooperate (MVP design pattern).
     *
     * @param view
     *            not-{@code null} reference to the {@link DatabaseDriversView}
     *            instance.
     */
    public DatabaseDriversPresenter(final DatabaseDriversView view) {
        if (view == null) {
            throw new IllegalArgumentException("View must not be null");
        }
        this.view = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDriver(final DatabaseDriverDTO driver) {
        driverManager.addDriverResource(new File(driver.getLocation()).toURI());
        view.addToList(driver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterDriver(final DatabaseDriverDTO driver) {
        driverManager.removeDriverResource(
                new File(driver.getLocation()).toURI());
        view.removeFromList(driver);
    }
}
