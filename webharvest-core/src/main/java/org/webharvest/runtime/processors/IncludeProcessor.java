/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

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

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime.processors;

import static org.webharvest.WHConstants.XMLNS_CORE;
import static org.webharvest.WHConstants.XMLNS_CORE_10;

import java.io.IOException;

import org.webharvest.annotation.Definition;
import org.webharvest.definition.Config;
import org.webharvest.definition.ConfigFactory;
import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.ConfigSource.Location;
import org.webharvest.definition.IncludeDef;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.NestedContextFactory;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;

import com.google.inject.Inject;

/**
 * Include processor.
 */
//TODO Add unit test
//TODO Add javadoc
@Autoscanned
@TargetNamespace({ XMLNS_CORE, XMLNS_CORE_10 })
@Definition(value = "include", validAttributes = { "id", "path" },
        requiredAttributes = "path", definitionClass = IncludeDef.class)
public class IncludeProcessor extends AbstractProcessor<IncludeDef> {

    @Inject
    private ConfigFactory configFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    // TODO Missing unit test
    public Variable execute(DynamicScopeContext context) throws InterruptedException {

        final String path = BaseTemplater.evaluateToString(elementDef.getPath(),
                null, context);

        this.setProperty("Path", path);

        final Config config = includeConfig(context, path);

        // TODO rbala Unify processors execution with Scraper (remove duplicate code)!
        ProcessorResolver.createProcessor(config.getElementDef()).run(
                NestedContextFactory.create(context));

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        return EmptyVariable.INSTANCE;
    }

    private Config includeConfig(final DynamicScopeContext context,
            final String path) {
        try {
            final ConfigSource configSource = context.getConfig().
                    getConfigSource().include(new Location() {

                public String toString() {
                    return path;
                }

            });

            final Config config = configFactory.create(configSource);
            config.reload();

            return config;
        } catch (IOException e) {
            // FIXME rbala What type of exception should we throw in case of problems with ConfigResource initialization
            // FIXME rbala What type of exception should we throw in case of problems with Config initialization
            throw new RuntimeException(e);
        }
    }


}