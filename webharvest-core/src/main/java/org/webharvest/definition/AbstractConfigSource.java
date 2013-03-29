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

/**
 * Base class for other more specialized implementations of {@link ConfigSource}
 * interface. Provides implementation of
 * {@link #include(org.webharvest.definition.ConfigSource.Location)} method.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see ConfigSource
 */
public abstract class AbstractConfigSource implements ConfigSource {

    /**
     * {@inheritDoc}
     */
    @Override
    public final ConfigSource include(final Location location)
            throws IOException {
        if ((location == null) || location == ConfigSource.UNDEFINED_LOCATION) {
            throw new IllegalArgumentException("Unknown location");
        }
        final IncludeVisitor visitor = new IncludeVisitor(location.toString());
        visit(visitor);
        final ConfigSource configSource = visitor.getConfigSource();
        if (configSource == null) {
            throw new IllegalStateException("Unsupported location type");
        }
        return configSource;
    }

    /**
     * Depending on owned {@link Location} allows the ancestor class to accept
     * intercepted {@link ConfigLocationVisitor}.
     *
     * @param visitor
     *            reference to {@link ConfigLocationVisitor}
     * @throws IOException
     *             in case of any problem with inclusion of {@link ConfigSource}
     *             .
     */
    protected abstract void visit(ConfigLocationVisitor visitor)
            throws IOException;

}
