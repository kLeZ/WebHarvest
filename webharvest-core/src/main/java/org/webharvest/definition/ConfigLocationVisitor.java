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

import org.webharvest.definition.ConfigSource.Location;
import org.webharvest.definition.FileConfigSource.FileLocation;
import org.webharvest.definition.URLConfigSource.URLLocation;

/**
 * Represents object implementing Visitor patter. Provides polymorphic
 * handler methods on visitable location objects that implements
 * {@link VisitableLocation}.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see FileLocation
 * @see URLLocation
 */
public interface ConfigLocationVisitor {

    /**
     * Takes certain action on {@link FileLocation}.
     *
     * @param location
     *            reference to {@link VisitableLocation} object.
     * @throws IOException
     *             in case of any problem associated with visitor's action taken
     *             on {@link VisitableLocation} object.
     */
    void visit(FileLocation location) throws IOException;

    /**
     * Takes certain action on {@link URLLocation}.
     *
     * @param location
     *            reference to {@link VisitableLocation} object.
     * @throws IOException
     *             in case of any problem associated with visitor's action taken
     *             on {@link VisitableLocation} object.
     */
    void visit(URLLocation location) throws IOException;

    /**
     * Extended version of {@link Location} interface that closely cooperate
     * with {@link ConfigLocationVisitor} in order to take special action on
     * certain {@link Location} instance of which we haev lost a type.
     *
     * @author Robert Bala
     * @since 2.1.0-SNAPSHOT
     * @version %I%, %G%
     * @see Location
     */
    interface VisitableLocation extends Location {

        /**
         * Accepts {@link ConfigLocationVisitor}
         *
         * @param visitor
         *            reference of {@link ConfigLocationVisitor} to visit.
         * @throws IOException
         *             in case of any problem associated with visitor's action
         *             taken on {@link VisitableLocation} object.
         */
        void accept(ConfigLocationVisitor visitor) throws IOException;

    }

}
