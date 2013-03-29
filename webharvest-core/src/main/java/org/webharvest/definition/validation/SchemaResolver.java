package org.webharvest.definition.validation;

/**
 * Interface to be implemented by the web harvest XML schema sources resolvers.
 * Facilitates configuration of resolvers, allowing to register resolver post
 * processors and to register instances of {@link SchemaSource}s.
 *
 * @see SchemaResolverPostProcessor
 * @see SchemaSource
 *
 * @author Maciej Czapiewski (mc5122)
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface SchemaResolver {

    /**
     * Adds new {@link SchemaResolverPostProcessor} which is going to be applied
     * on the current resolver instance on the resolver refresh.
     *
     * @param postProcessor
     *            new instance of {@link SchemaResolverPostProcessor}; must not
     *            be {code null}
     */
    void addPostProcessor(SchemaResolverPostProcessor postProcessor);

    /**
     * Refresh the current resolver instance, causing all XML schema sources to
     * be reinitialized. Also, all previously registered
     * {@link SchemaResolverPostProcessor}s are invoked.
     */
    void refresh();

    /**
     * Register provided {@link SchemaSource} which cannot be {@code null}
     *
     * @param schemaSource
     *            an instance of {@link SchemaSource} which is going to be
     *            registered
     */
    void registerSchemaSource(SchemaSource schemaSource);

}
