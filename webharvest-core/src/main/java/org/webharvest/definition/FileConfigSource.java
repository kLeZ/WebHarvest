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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.webharvest.definition.ConfigLocationVisitor.VisitableLocation;
import org.webharvest.utils.HasReader;

/**
 * Implementation of {@link ConfigSource} that uses a file system as
 * source of XML configurations.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see ConfigSource
 * @see File
 * @see Location
 */
public final class FileConfigSource extends AbstractConfigSource {

    private final FileLocation location;

    /**
     * Class constructor expecting {@link File} as configuration source.
     *
     * @param file configuration source
     */
    public FileConfigSource(final File file) {
        this(new FileLocation(file));
    }

    /**
     * Class constructor accepting {@link FileLocation} as configuration source.
     *
     * @param location configuration source
     */
    FileConfigSource(final FileLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("Location is requried");
        }
        this.location = location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reader getReader() throws IOException {
        return location.getReader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VisitableLocation getLocation() {
        return location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void visit(final ConfigLocationVisitor visitor)
            throws IOException {
        location.accept(visitor);
    }

    /**
     * Helper class representing wrapper for {@link File} object that is adapted
     * to {@link Location} and {@link HasReader} interfaces.
     *
     * @author Robert Bala
     * @since 2.1.0-SNAPSHOT
     * @version %I%, %G%
     * @see VisitableLocation
     */
    // TODO Move to its own file
    static final class FileLocation implements VisitableLocation, HasReader {

        private final File file;

        /**
         * Default class constructor expecting reference to {@link File}.
         *
         * @param file reference to {@link File}.
         */
        public FileLocation(final File file) {
            if (file == null) {
                throw new IllegalArgumentException("Configuration file is "
                        + "required");
            }
            this.file = file;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Reader getReader() throws IOException {
            return new FileReader(file);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return file.getAbsolutePath();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(final ConfigLocationVisitor visitor)
                throws IOException {
            visitor.visit(this);
        }

    }

}
