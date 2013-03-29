package org.webharvest.definition.validation;

/**
 * Allows for custom modification of the web harvest XML schema sources
 * resolvers. Can be installed in the {@link SchemaResolver} and triggered on
 * the resolver refresh.
 *
 * @see SchemaResolver
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface SchemaResolverPostProcessor {

    /**
     * Triggered on the {@link SchemaResolver} refresh. Allows for
     * {@link SchemaResolver} customization.
     *
     * @param resolver
     *            current {@link SchemaResolver} instance, never {@code null}
     */
    void postProcess(SchemaResolver resolver);

}
