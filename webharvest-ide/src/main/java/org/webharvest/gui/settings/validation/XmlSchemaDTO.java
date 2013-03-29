package org.webharvest.gui.settings.validation;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Data Transfer Object representing XML schema.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class XmlSchemaDTO {

    private final String location;

    /**
     * DTO constructor accepting parameter indicating location of XML schema.
     *
     * @param location
     *            XML schema's location; mandatory, must not be {@code null}
     */
    public XmlSchemaDTO(final String location) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("Location is mandatory");
        }
        this.location = location;
    }

    /**
     * Returns XML schema's location.
     *
     * @return XML schema's location
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XmlSchemaDTO)) {
            return false;
        }
        final XmlSchemaDTO other = (XmlSchemaDTO) obj;

        return new EqualsBuilder()
            .append(this.location, other.location)
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(this.location)
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append(FilenameUtils.getName(this.location)).append(' ')
                .append('(').append(this.location).append(')').toString();
    }

}
