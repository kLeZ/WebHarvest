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

package org.webharvest.runtime.database;

import java.net.URI;

/**
 * {@link DriverManager} allows to register database drivers placed in the
 * arbitrary locations. Database driver being registered must be available on
 * classpath or in any of previously added driver resources.
 * <p/>
 * If no resources has been added to {@link DefaultDriverManager} (as it is in
 * most cases when web harvest is embedded into enterprise application running
 * within container), all drivers being registered must be accessible within
 * container's default class loader.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public interface DriverManager {

    /**
     * Adds resource containing database driver (in most cases it is a JAR
     * file).
     *
     * @param location
     *            location of the resource; must not be {@code null}
     */
    void addDriverResource(URI location);

    /**
     * Removes previously added database driver resource.
     *
     * @param location
     *            location of resource which is going to be removed; must not be
     *            {@code null}
     */
    void removeDriverResource(URI location);

    /**
     * Registers driver with the provided class name. Driver must be already
     * accessible on classpath or in any of previously added resources.
     *
     * @param driverClass
     *            fully qualified name of the driver's class.
     *
     * @throws ClassNotFoundException
     *             thrown if the provided driver's class can not be found.
     */
    void registerDriver(String driverClass) throws ClassNotFoundException;
}
