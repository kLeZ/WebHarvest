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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.annotation.Definition;
import org.webharvest.annotation.ElementInfoFactory;
import org.webharvest.runtime.processors.AbstractProcessor;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.processors.plugins.Autoscanned;
import org.webharvest.runtime.processors.plugins.TargetNamespace;
import org.webharvest.utils.ClassPathScanner;
import org.webharvest.utils.ClassPathScannerImpl;

/**
 * {@link ResolverPostProcessor} implementation capable of scanning
 * Java class path looking for {@link Autoscanned} web harvest plugins.
 * Found {@link Autoscanned} plugins declaring {@link TargetNamespace} are
 * registered in the post processed {@link ConfigurableResolver}.
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class AnnotatedPluginsPostProcessor implements
        ResolverPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            AnnotatedPluginsPostProcessor.class);

    private final ClassPathScanner scanner;

    /**
     * Constructs {@link AnnotatedPluginsPostProcessor} accepting name of java
     * package which is going to be scanned for the web harvest plugins.
     * Provided package and all its subpackages are scanned for plugins
     * annotated with {@link Autoscanned}.
     *
     * @param packageName
     *            name of package to be scanned
     */
    public AnnotatedPluginsPostProcessor(final String packageName) {
        this.scanner = new ClassPathScannerImpl(packageName);
    }

    /**
     * Post process provided {@link ConfigurableResolver} by registering all
     * {@link Autoscanned} web harvest plugins available on classpath under
     * package (and its subpackages) declared in the post processor's
     * constructor.
     *
     * @param resolver
     *            post processed {@link ConfigurableResolver} instance
     */
    @Override
    public void postProcess(final ConfigurableResolver resolver) {
        for (Class< ? extends WebHarvestPlugin > plugin : scanPlugins()) {
            registerPlugin(plugin, resolver);
        }
    }

    /**
     * Registers web harvest plugin in the resolver under namespace declared in
     * the {@link TargetNamespace} annotation.
     *
     * @param plugin
     *            plugin which is going to be registered
     * @param resolver
     *            resolver in which plugin is going to be registered
     */
    private void registerPlugin(
            final Class< ? extends WebHarvestPlugin > plugin,
            final ConfigurableResolver resolver) {
        final TargetNamespace targetNamespace = plugin.getAnnotation(
                TargetNamespace.class);
        final ElementInfo elementInfo =
                ElementInfoFactory.getElementInfo(plugin);

        for (String namespace : targetNamespace.value()) {
            LOGGER.info("Registering plugin {} under namespace {}",
                    plugin.getCanonicalName(), namespace);
            resolver.registerPlugin(elementInfo, namespace);
        }
    }

    /**
     * Scans class path looking for plugins annotated with {@link Autoscanned}.
     * Candidate plugins must also declare target namespace using
     * {@link TargetNamespace} annotation.
     *
     * @return web harvest plugins eligible for registration
     */
    private Set<Class< ? extends WebHarvestPlugin >> scanPlugins() {
        final Set<Class< ? >> candidates = scanner
                .getTypesAnnotatedWith(Autoscanned.class);
        return filterCandidates(candidates);
    }

    /**
     * Filters provided {@link Set} of candidates according to the following
     * criteria:
     * <ul>
     * <li>type must declare {@link TargetNamespace} annotation</li>
     * <li>type must be a subtype of {@link WebHarvestPlugin}</li>
     * </ul>
     * Only candidates matching criteria are returned.
     *
     * @param candidates
     *            {@link Set} of candidates to be filtered
     * @return {@link Set} of candidates matching filtering criteria
     */
    private Set<Class< ? extends WebHarvestPlugin >> filterCandidates(
            final Set<Class< ? >> candidates) {
        final Predicate validCandidatePredicate = PredicateUtils.andPredicate(
                // TODO Use Processor interface instead
                new SubtypePredicate(AbstractProcessor.class),
                PredicateUtils.andPredicate(
                        new AnnotatedTypePredicate(TargetNamespace.class),
                        new AnnotatedTypePredicate(Definition.class)));

        final Set<Class< ? extends WebHarvestPlugin >> validCandidates =
            new HashSet<Class < ? extends WebHarvestPlugin >>();
        final Set<Class< ? >> rejectedCandidates = new HashSet<Class < ? >>();

        CollectionUtils.select(candidates, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                boolean isValid = validCandidatePredicate.evaluate(object);
                if (!isValid) {
                    rejectedCandidates.add((Class< ? >) object);
                }
                return isValid;
            }
        }, validCandidates);

        for (Class < ? > rejected : rejectedCandidates) {
            LOGGER.info("{} is not a valid candidate. Skipping!",
                    rejected.getCanonicalName());
        }

        return validCandidates;
    }

    /**
     * {@link Predicate} satisfied when super type provided in constructor
     * is assignable from the currently evaluated type.
     */
    private class SubtypePredicate implements Predicate {
        private final Class< ? > superType;

        public SubtypePredicate(final Class < ? > superType) {
            this.superType = superType;
        }

        @Override
        public boolean evaluate(final Object object) {
            return superType.isAssignableFrom((Class< ? >) object);
        }
    }

    /**
     * {@link Predicate} satisfied when type is annotated with the annotation
     * provided in {@link AnnotatedTypePredicate}'s constructor.
     */
    private class AnnotatedTypePredicate implements Predicate {
        private final Class< ? extends Annotation > annotation;

        public AnnotatedTypePredicate(
                final Class< ? extends Annotation > annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean evaluate(final Object object) {
            return ((Class< ? >) object).getAnnotation(annotation) != null;
        }
    }
}
