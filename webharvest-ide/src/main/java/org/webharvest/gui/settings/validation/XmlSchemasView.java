package org.webharvest.gui.settings.validation;

import java.util.Set;

/**
 * MVP View interface responsible for management of XML schemas. It is capable
 * of adding and removing schemas used by Web Harvest during configuration's
 * validation.
 *
 * @author mczapiewski
 * @since 2.1-SNAPSHOT
 * @version %I%, %G%
 */
public interface XmlSchemasView {

    /**
     * Adds given XML schema to displayed list of schemas.
     *
     * @param schema
     *            not-{@code null} XML schema DTO
     */
    void addToList(XmlSchemaDTO schema);

    /**
     * Removes XML schema from the displayed list.
     *
     * @param schema
     *            XML schema which is going to be removed from the list; must
     *            not be {@code null}
     */
    void removeFromList(XmlSchemaDTO schema);

    /**
     * Sets the view MVP's presenter reference.
     *
     * @param presenter
     *            reference to the view's presenter instance.
     */
    void setPresenter(Presenter presenter);

    /**
     * MVP presenter interface working with {@link XmlSchemasView}.
     */
    public interface Presenter {

        /**
         * Registers XML schema represented by the provided DTO. When this
         * method is completed, web harvest is fully capable of validation of
         * configuration using schema registered.
         *
         * @param schema
         *            DTO representing XML schema which is going to be
         *            registered; must not be {@code null}
         */
        void registerSchema(XmlSchemaDTO schema);

        /**
         * Unregisters XML schema represented by the provided DTO. When this
         * method is completed, web harvest can no longer validate configuration
         * using this XML schema.
         *
         * @param schema
         *            DTO representing XML schema which is going to be
         *            unregistered; must not be {@code null}
         */
        void unregisterSchema(XmlSchemaDTO schema);

    }
}
