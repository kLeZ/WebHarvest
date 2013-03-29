package org.webharvest.definition.validation;

import java.net.URI;

import org.webharvest.TransformationException;
import org.webharvest.Transformer;

/**
 * An implementation of {@link Transformer} interface which supports
 * transformation from given resource {@link URI} to appropriate instance of
 * {@link SchemaSource}.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class URIToSchemaSourceTransformer implements
        Transformer<URI, SchemaSource> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SchemaSource transform(final URI uri)
            throws TransformationException {
        if (uri == null) {
            throw new IllegalArgumentException("URI must not be null.");
        }
        try {
            return new SchemaSource(uri.toURL().openStream(), uri.toString());
        } catch (final Throwable cause) {
            throw new TransformationException(cause);
        }
    }

}
