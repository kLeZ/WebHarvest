package org.webharvest.definition.validation;

/**
 * Factory class providing instances of {@link SchemaResolver} and
 * {@link SchemaFactory} which actually are the same instance of
 * {@link SchemaFactoryImpl}.
 *
 * @see SchemaFactoryImpl#INSTANCE
 *
 * @author Maciej Czapiewski (mc5122)
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class SchemaComponentFactory {

    /**
     * Returns an instance of {@link SchemaResolver}.
     *
     * @return an instance of {@link SchemaResolver}
     */
    public static SchemaResolver getSchemaResolver() {
        return SchemaFactoryImpl.INSTANCE;
    }

    /**
     * Returns an instance of {@link SchemaFactory}.
     *
     * @return an instance of {@link SchemaFactory}.
     */
    public static SchemaFactory getSchemaFactory() {
        return SchemaFactoryImpl.INSTANCE;
    }

}
