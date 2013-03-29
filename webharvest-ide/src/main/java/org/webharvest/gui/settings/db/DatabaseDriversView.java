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

/**
 * MVP View interface responsible for management of database drivers. It is
 * capable of adding and removing drivers used by Web Harvest during processing
 * database read/write operations.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface DatabaseDriversView {

    /**
     * Adds provided database driver to displayed list of drivers.
     *
     * @param driver
     *            not-{@code null} database driver DTO
     */
    void addToList(DatabaseDriverDTO driver);

    /**
     * Removes driver from the displayed list.
     *
     * @param driver
     *            driver which is going to be removed from the list; must not be
     *            {@code null}
     */
    void removeFromList(DatabaseDriverDTO driver);

    /**
     * Sets the view MVP's presenter reference.
     *
     * @param presenter
     *            reference to the view's presenter instance.
     */
    void setPresenter(Presenter presenter);

    /**
     * MVP presenter interface working with {@link DatabaseDriversView}.
     */
    public interface Presenter {

        /**
         * Registers database driver represented by the provided DTO. When this
         * method is completed, web harvest is fully capable of execution of
         * database queries using driver registered.
         *
         * @param driver
         *            DTO representing database driver which is going to be
         *            registered; must not be {@code null}
         */
        void registerDriver(DatabaseDriverDTO driver);

        /**
         * Unregisters database driver represented by the provided DTO. When
         * this method is completed, web harvest can no longer execute database
         * queries using this driver.
         *
         * @param driver
         *            DTO representing database driver which is going to be
         *            unregistered; must not be {@code null}
         */
        void unregisterDriver(DatabaseDriverDTO driver);
    }
}
