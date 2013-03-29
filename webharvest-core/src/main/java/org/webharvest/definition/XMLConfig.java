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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Implementation of {@link ConfigSource} capable to work with XML
 * based configurations.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 * @see Config
 * @see ConfigSource
 * @see IElementDef
 */
public final class XMLConfig implements Config {

    private final ConfigSource configSource;

    private ElementDefProxy elementDef;

    /**
     * Default class constructor specifying {@link ConfigSource} object
     * as configuration resource (from which it will be obtained).
     *
     * @param configSource reference to configuration resource.
     */
    @Inject
    public XMLConfig(@Assisted final ConfigSource configSource) {
        if (configSource == null) {
            throw new IllegalArgumentException("ConfigSource is required");
        }
        this.configSource = configSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigSource getConfigSource() {
        return configSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IElementDef getElementDef() {
        if (elementDef == null) {
            throw new IllegalStateException("No configuration source provided");
        }

        return elementDef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public String getNamespaceURI() {
        if (elementDef == null) {
            throw new IllegalStateException("No configuration source provided");
        }

        return elementDef.getNamespaceURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reload() {
        this.elementDef = XmlParser.parse(configSource);
    }

}
