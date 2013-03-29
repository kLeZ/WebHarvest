package org.webharvest.gui.settings.validation;

import java.io.File;
import java.net.URI;

import org.webharvest.TransformationException;
import org.webharvest.Transformer;

/**
 * An implementation of {@link Transformer} interface which supports
 * transformation path to XML schema from given location to its {@link URI}.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class FilePathToURITransformer
        implements Transformer<String, URI> {

    /**
     * {@inheritDoc}
     */
    @Override
    public URI transform(final String location) throws TransformationException {
        if (location == null) {
            throw new IllegalArgumentException("Location must not be null.");
        }
        return new File(location).toURI();
    }

}
