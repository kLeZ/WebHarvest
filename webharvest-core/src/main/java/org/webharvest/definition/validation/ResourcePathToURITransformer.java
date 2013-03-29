package org.webharvest.definition.validation;

import java.net.URI;

import org.webharvest.TransformationException;
import org.webharvest.Transformer;

/**
 * An implementation of {@link Transformer} interface which supports
 * transformation from given resource name (resource path as string) to its
 * {@link URI}.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class ResourcePathToURITransformer implements
        Transformer<String, URI> {

    /**
     * {@inheritDoc}
     */
    @Override
    public URI transform(final String resourcePath)
            throws TransformationException {
        if (resourcePath == null) {
            throw new IllegalArgumentException(
                    "Path to the resource must not be null.");
        }
        try {
            return getClass().getResource(resourcePath).toURI();
        } catch (final Throwable cause) {
            throw new TransformationException(cause);
        }
    }

}
