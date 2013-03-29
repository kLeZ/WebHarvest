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

package org.webharvest.definition;

import java.io.IOException;

import org.webharvest.utils.HasReader;

/**
 * Represents source of XML configuration object. The configuration can be
 * loaded from different sources like database, remote web server or just plain
 * old file system.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see HasReader
 */
public interface ConfigSource extends HasReader {

    /**
     * Configuration source undefined location.
     */
    Location UNDEFINED_LOCATION = new Location() {
    };

    /**
     * Gets the reference to physical location of configuration. (eg. URL or
     * file path).
     *
     * @return location of configuration.
     */
    Location getLocation();

    /**
     * Includes specified {@link Location} from current configuration if
     * possible.
     * Included configuration is not merged but instead returned as a
     * reference to new {@link ConfigSource}.
     * Inclusion is smart enough to decide if the current configuration
     * source points to a file system or remote web server location
     * to determine the base for always contextual location that has
     * been specified to include.
     *
     * @param location
     *            reference to {@link Location} to include.
     * @return Included instance of {@link ConfigSource} or throws
     *         {@link IllegalStateException} in case of unrecognized
     *         {@link Location} type.
     * @throws IOException
     *             In case of any problems with instantiation of included
     *             {@link ConfigSource}
     */
    ConfigSource include(Location location) throws IOException;

    /**
     * Just a marker interface to indicate the actual type of location eg. file,
     * url or any other
     *
     * @author Robert Bala
     * @since 2.1.0-SNAPSHOT
     * @version %I%, %G%
     */
    interface Location {

    }

}
