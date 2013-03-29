package org.webharvest.definition.validation;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

/**
 * An POJO object which contains XML schema's {@link Source}. It also
 * instantiates XML schema's {@link Source} in the default constructor using
 * specified {@link InputStream} and system identifier of the source.
 *
 * It implements {@link Object#toString()}, {@link Object#hashCode()} and
 * {@link Object#equals(Object)} methods so it could be used e.g. as element of
 * collections.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class SchemaSource {

    private final Source source;

    /**
     * Default class constructors accepting not {@code null} {@link InputSteam}
     * and system identifier of the XML schema source. If any of parameters is
     * {@code null}, then {@link IllegalArgumentException} is thrown.
     *
     * @param inputStream
     *            reference to {@link InputStream} of the source
     * @param systemId
     *            an identifier of the source which should be retrieved as
     *            string of schema's {@link java.net.URI}.
     */
    public SchemaSource(final InputStream inputStream, final String systemId) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Source must not be null.");
        }
        if (systemId == null) {
            throw new IllegalArgumentException("System ID must not be null.");
        }
        final InputSource inputSource = new InputSource(inputStream);
        inputSource.setSystemId(systemId);
        this.source = new SAXSource(inputSource);
    }

    /**
     * Returns an instance of created XML schema's {@link Source}. This
     * {@link Source} is created from specified {@link InputStream} and contains
     * appropriate system identifier - see
     * {@link SchemaSource#SchemaSource(InputStream, String)}.
     *
     * @return an instance of XML schema's {@link Source}.
     */
    public Source getSource() {
        return source;
    }

    /**
     * Returns system identifier of the XML schema's {@link Source}.
     */
    @Override
    public String toString() {
        return source.getSystemId().toString();
    }

    /**
     * Returns hash code of system identifier of the schema's {@link Source}.
     */
    @Override
    public int hashCode() {
        return source.getSystemId().hashCode();
    }

    /**
     * Equals system identifiers of {@link SchemaSource} specified as parameter
     * and this {@link SchemaSource}.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final SchemaSource other = (SchemaSource) obj;
        if (other.getSource() == null) {
            return false;
        } else if (!source.getSystemId().equals(other.source.getSystemId())) {
            return false;
        }

        return true;
    }

}
