package org.webharvest.definition.validation;

import javax.xml.validation.Schema;

/**
 * Factory creating an instance of {@link Schema} which is a base of XML
 * validation process.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface SchemaFactory {

    /**
     * Returns an instance of {@link Schema} which should be used to validate a
     * XML file.
     *
     * @return specially prepared instance of {@link Schema}.
     */
    Schema getSchema();
}
