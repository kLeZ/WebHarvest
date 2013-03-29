package org.webharvest.definition.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Implementation of {@link SchemaFactory} and {@link SchemaResolver} interfaces
 * which realizes Abstract Factory and Singleton design patterns. It support
 * multiple refresh operations. Each time, resolver refresh operation is
 * invoked, new {@link Set} of {@link SchemaSource}s is created, all previously
 * registered {@link SchemaResolverPostProcessor}s are triggered and finally
 * using {@link XMLConstants#W3C_XML_SCHEMA_NS_URI} schema language new instance
 * of {@link Schema} is created.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class SchemaFactoryImpl implements SchemaFactory, SchemaResolver {

    private static final Logger LOG = LoggerFactory
            .getLogger(SchemaFactoryImpl.class);

    /**
     * Reference to singleton instance.
     */
    public static final SchemaFactoryImpl INSTANCE;

    /**
     * Static block instantiating {@link SchemaFactoryImpl}.
     */
    static {
        INSTANCE = new SchemaFactoryImpl();
    }

    private final javax.xml.validation.SchemaFactory schemaFactory =
        javax.xml.validation.SchemaFactory.newInstance(
                XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private final List<SchemaResolverPostProcessor> postProcessors =
        new ArrayList<SchemaResolverPostProcessor>();

    /** Synchronization monitor for the "refresh" operation */
    private final Object refreshMonitor = new Object();

    private Set<SchemaSource> schemaSources;
    private Schema schema;

    /**
     * Private class constructor which is a required part of Singleton design
     * pattern.
     */
    private SchemaFactoryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerSchemaSource(final SchemaSource schemaSource) {
        if (schemaSource == null) {
            throw new IllegalArgumentException(
                    "Schema source must not be null.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Register schema source for system id {}", schemaSource
                    .getSource().getSystemId());
        }
        schemaSources.add(schemaSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPostProcessor(
            final SchemaResolverPostProcessor postProcessor) {
        if (postProcessor == null) {
            throw new IllegalArgumentException(
                    "PostProcessor must not be null.");
        }
        postProcessors.add(postProcessor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return schema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        synchronized (this.refreshMonitor) {
            schemaSources = new HashSet<SchemaSource>();
            invokePostProcessors();
            createSchema();
        }
    }

    /**
     * Helper method invoking all post processors.
     */
    private void invokePostProcessors() {
        for (SchemaResolverPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcess(this);
        }
    }

    /**
     * Helper method creating new {@link Schema} instance for schema sources
     * resolved by post processors. If this operation failed a
     * {@link RuntimeException} is thrown.
     */
    private void createSchema() {
        try {
            schema = schemaFactory.newSchema(
                    getSources().toArray(new Source[] {}));
        } catch (SAXException e) {
        	LOG.error(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method transforming schema sources resolved by post processors to
     * {@link Source}s and returning them.
     *
     * @return {@link Source}s retrieved from schema sources which has been
     *         resolved by post processors
     */
    private Set<Source> getSources() {
        final Set<Source> sources = new HashSet<Source>();
        for (SchemaSource schemaSource : schemaSources) {
            sources.add(schemaSource.getSource());
        }
        return sources;
    }

}
