package org.webharvest.annotation;

import org.webharvest.definition.ElementInfo;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.utils.CommonUtil;

/**
 * Temporary helper class responsible for instantiation of {@link ElementInfo}
 * from {@link Definition} annotation that is put on {@link WebHarvestPlugin}
 * that is about to be registered. Helper methods
 * {@link #getTagDesc(Definition)} and {@link #getAttributeDesc(Definition)}
 * have been moved from {@link WebHarvestPlugin} class almost without
 * modification.
 *
 * @author Robert Bala
 * @since 2.1.0-SNAPSHOT
 * @version %I%, %G%
 */
@Deprecated
// TODO Removed when finished with processors refactoring
public final class ElementInfoFactory {

    /**
     * Class constructor preventing instantiation.
     */
    private ElementInfoFactory() {
        // Do nothing constructor
    }

    /**
     * Creates new {@link ElementInfo} object from processor class that is about
     * to be registered.
     *
     * @param processorClass
     *            plugin/processor class to register.
     * @return
     */
    public static ElementInfo getElementInfo(
            final Class<? extends WebHarvestPlugin> processorClass) {
        if (processorClass == null) {
            throw new IllegalArgumentException("Processor is required");
        }

        // No need to check if annotation is set (thows NullPointerException
        // when missing)
        final Definition definition = processorClass
                .getAnnotation(Definition.class);

        final String name = definition.value();

        if (!CommonUtil.isValidXmlIdentifier(name)) {
            throw new PluginException("Plugin class \""
                    + processorClass.getName()
                    + "\" does not define valid name!");
        }

        return new ElementInfo(definition.value(),
                definition.definitionClass(), processorClass,
                getTagDesc(definition), getAttributeDesc(definition),
                definition.internal(), definition.dependantProcessors());
    }

    private static String getTagDesc(final Definition definition) {
        if (!definition.body()) {
            return "";
        }

        final String[] validSubprocessors = definition.validSubprocessors();
        if (validSubprocessors.length < 1) {
            return null;
        }

        final String requiredTags[] = definition.requiredSubprocessors();

        final StringBuilder result = new StringBuilder();
        for (String subProcessor : validSubprocessors) {
            if (result.length() != 0) {
                result.append(',');
            }
            if (CommonUtil
                    .existsInStringArray(requiredTags, subProcessor, true)) {
                result.append('!');
            }
            result.append(subProcessor);
        }
        return result.toString();
    }

    private static String getAttributeDesc(final Definition definition) {
        final String[] validAtts = definition.validAttributes();
        if (validAtts.length < 1) {
            return "id";
        }

        final String requiredAtts[] = definition.requiredAttributes();

        final StringBuilder result = new StringBuilder("id,");
        for (String attr : validAtts) {
            if (CommonUtil.existsInStringArray(requiredAtts, attr, true)) {
                result.append('!');
            }
            result.append(attr);
            result.append(",");
        }
        return result.toString();
    }

}
