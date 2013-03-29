package org.webharvest.gui.settings.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.webharvest.definition.validation.SchemaComponentFactory;
import org.webharvest.definition.validation.SchemaResolver;
import org.webharvest.gui.Settings;
import org.webharvest.gui.settings.SettingsAware;
import org.webharvest.gui.settings.validation.XmlSchemasView.Presenter;

/**
 * Default implementation of MVP's {@link XmlSchemasView.Presenter} interface
 * which also realizes {@link SettingsAware} interface in order to
 * react on {@link Settings} load/update action.
 *
 * @see XmlSchemasView.Presenter
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public final class XmlSchemasPresenter implements Presenter, SettingsAware {

    private Set<XmlSchemaDTO> schemaDTOs = new HashSet<XmlSchemaDTO>();

    private final SchemaResolver schemaResolver =
        SchemaComponentFactory.getSchemaResolver();

    private final XmlSchemasView view;

    /**
     * Presenter's constructor accepting not-{@code null} reference to the
     * instance of {@link XmlSchemasView} with which this presenter is intended
     * to cooperate (MVP design pattern).
     *
     * @param view
     *            not-{@code null} reference to the {@link XmlSchemasView}
     *            instance.
     */
    public XmlSchemasPresenter(final XmlSchemasView view) {
        if (view == null) {
            throw new IllegalArgumentException("View must not be null.");
        }
        this.view = view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerSchema(final XmlSchemaDTO schema) {
        if (schema == null) {
            throw new IllegalArgumentException("DTO must not be null.");
        }
        if (schemaDTOs.add(schema)) {
            view.addToList(schema);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterSchema(final XmlSchemaDTO schema) {
        if (schema == null) {
            throw new IllegalArgumentException("DTO must not be null.");
        }
        if (schemaDTOs.remove(schema)) {
            view.removeFromList(schema);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoad(final Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings must not be null.");
        }
        final Set<XmlSchemaDTO> schemas = new LinkedHashSet<XmlSchemaDTO>(
                Arrays.asList(settings.getXmlSchemas()));
        for (XmlSchemaDTO schema : schemas) {
            registerSchema(schema);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(final Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings must not be null.");
        }
        settings.setXmlSchemas(schemaDTOs.toArray(new XmlSchemaDTO[]{}));
        schemaResolver.refresh();
    }

}
