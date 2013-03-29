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

package org.webharvest.utils;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.google.common.base.Predicate;

/**
 * {@link ClassPathScanner} interface implementation using
 * <a href="http://code.google.com/p/reflections/">Reflections</a> library under
 * the hood.
 *
 * @see ClassPathScanner
 *
 * @author Piotr Dyraga
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
public final class ClassPathScannerImpl implements ClassPathScanner {

    private final String packageName;

    /**
     * Constructor accepting name of the java package. Components under this
     * package and its subpackages are going to be scanned.
     *
     * @param packageName
     *            target package name; mandatory, must not be {@code null}
     */
    public ClassPathScannerImpl(final String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package name must not be null");
        }
        this.packageName = packageName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class< ? >> getTypesAnnotatedWith(
            final Class< ? extends Annotation> annotation) {
        final Predicate<String> packageFilter =
            new FilterBuilder().include(FilterBuilder.prefix(packageName));
        final Predicate<String> annotationFilter =
            new FilterBuilder().include(annotation.getCanonicalName());
        final Scanner annotationScanner =
            new TypeAnnotationsScanner().filterResultsBy(annotationFilter);

        final ConfigurationBuilder builder = new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forPackage(packageName,
                    ClasspathHelper.classLoaders()))
            .filterInputsBy(packageFilter)
            .setScanners(annotationScanner);

        final Reflections reflections = new Reflections(builder);
        return reflections.getTypesAnnotatedWith(annotation);
    }
}
