/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.definition;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.webharvest.AlreadyBoundException;
import org.webharvest.annotation.ElementInfoFactory;
import org.webharvest.exception.ConfigurationException;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.processors.ConstantProcessor;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.utils.Assert;
import org.webharvest.utils.ClassLoaderUtil;

/**
 * Class contains information and logic to validate and crate definition classes for
 * parsed xml nodes from Web-Harvest configurations.
 *
 * @author Vladimir Nikic
 * @author Robert Bala
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DefinitionResolver extends AbstractRefreshableResolver {

    /**
     * Singleton instance reference.
     */
    public static final DefinitionResolver INSTANCE;

    static {
        INSTANCE = new DefinitionResolver();
    }

    private final static class PluginClassKey {

        private PluginClassKey(String className, String uri) {
            this.className = className;
            this.uri = uri;
        }

        final String className;
        final String uri;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PluginClassKey that = (PluginClassKey) o;
            return className.equals(that.className) && uri.equals(that.uri);

        }

        @Override
        public int hashCode() {
            return 31 * className.hashCode() + uri.hashCode();
        }
    }

    // map containing pairs (class name, plugin element name)
    // of externally registered plugins
    private Map<PluginClassKey, ElementName> externalPlugins =
        new LinkedHashMap<PluginClassKey, ElementName>();

    // map of external plugin dependencies
    private Map<ElementName, Class[]> externalPluginDependencies =
        new HashMap<ElementName, Class[]>();


    private DefinitionResolver() {
        // TODO Remove along with deprecated code
        addPostProcessor(new AnnotatedPluginsPostProcessor(
            "org.webharvest.deprecated.runtime.processors"));

        addPostProcessor(new AnnotatedPluginsPostProcessor(
            "org.webharvest.runtime.processors"));

        refresh();
    }

    @Deprecated
    private void registerPlugin(final Class pluginClass,
            final Class<? extends IElementDef> definitionClass,
            final boolean isInternalPlugin, final String... uris) {
        Assert.notNull(pluginClass);
        try {
            final Object pluginObj = pluginClass.newInstance();
            if (!(pluginObj instanceof WebHarvestPlugin)) {
                throw new PluginException("Plugin class \"" + pluginClass.getName() + "\" does not extend WebHarvestPlugin class!");
            }
            final WebHarvestPlugin plugin = (WebHarvestPlugin) pluginObj;
            for (String uri : uris) {
                registerPlugin(ElementInfoFactory.getElementInfo(pluginClass), uri);
            }
        } catch (InstantiationException e) {
            throw new PluginException("Error instantiating plugin class \"" + pluginClass.getName() + "\": " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new PluginException("Error instantiating plugin class \"" + pluginClass.getName() + "\": " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPlugin(final ElementInfo elementInfo,
            final String namespace) {
        if (elementInfo == null) {
            throw new IllegalArgumentException("ElementInfo is required");
        }
        if (namespace == null) {
            throw new IllegalArgumentException("Namespace is requried");
        }
        final ElementName pluginElementName = new ElementName(elementInfo.getName(), namespace);
        try {
            getElementsRegistry().bind(pluginElementName, elementInfo);
        } catch (AlreadyBoundException e) {
            throw new PluginException("Plugin \"" + pluginElementName + "\" is already registered!");
        }

        for (final Class subClass : elementInfo.getDependantProcessors()) {
            registerPlugin(ElementInfoFactory.getElementInfo(subClass), namespace);
        }

        if (!elementInfo.isInternal()) {
            externalPlugins.put(new PluginClassKey(elementInfo.getProcessorClass().getName(), namespace), pluginElementName);
        }
        externalPluginDependencies.put(pluginElementName, elementInfo.getDependantProcessors());
    }

    @Deprecated
    public void registerPlugin(String className, String uri) throws PluginException {
        registerPlugin(ClassLoaderUtil.getPluginClass(className), WebHarvestPluginDef.class, false, uri);
    }

    private void unregisterPlugin(Class pluginClass, String uri) {
        if (pluginClass != null) {
            unregisterPlugin(pluginClass.getName(), uri);
        }
    }

    public void unregisterPlugin(String className, String uri) {
        final PluginClassKey key = new PluginClassKey(className, uri);
        // only external plugins can be unregistered
        if (externalPlugins.containsKey(key)) {
            final ElementName pluginElementName = externalPlugins.get(key);
            getElementsRegistry().unbind(pluginElementName);
            externalPlugins.remove(key);

            // unregister dependant classes as well
            Class[] dependantClasses = externalPluginDependencies.get(pluginElementName);
            externalPluginDependencies.remove(pluginElementName);
            if (dependantClasses != null) {
                for (Class c : dependantClasses) {
                    unregisterPlugin(c, uri);
                }
            }
        }
    }

    public boolean isPluginRegistered(String className, String uri) {
        return externalPlugins.containsKey(new PluginClassKey(className, uri));
    }

    @Deprecated
    public boolean isPluginRegistered(Class pluginClass, String uri) {
        return pluginClass != null && isPluginRegistered(pluginClass.getName(), uri);
    }

    /**
     * Returns names of all known elements.
     */
    public Set<ElementName> getElementNames() {
        return getElementsRegistry().listBound();
    }

    /**
     * @param name Name of the element
     * @param uri  URI of the element
     * @return Instance of ElementInfo class for the specified element name,
     *         or null if no element is defined.
     */
    public ElementInfo getElementInfo(String name, String uri) {
        return getElementsRegistry().lookup(new ElementName(name, uri));
    }

    /**
     * Creates proper element definition instance based on given xml node
     * from input configuration.
     *
     * @param node node
     * @return Instance of IElementDef, or exception is thrown if cannot find
     *         appropriate element definition.
     */
    public IElementDef createElementDefinition(XmlNode node) {
        final String nodeName = node.getName();
        final String nodeUri = node.getUri();

        final ElementInfo elementInfo = getElementInfo(nodeName, nodeUri);
        if (elementInfo == null || elementInfo.getDefinitionClass() == null) {
            throw new ConfigurationException("Unexpected configuration element (URI, name): (" + nodeUri + "," + nodeName + ")!");
        }

        //FIXME: use a better construction than this as soon as possible
        try {
            final AbstractElementDef elementDef = (AbstractElementDef) elementInfo.getDefinitionClass().
                getConstructor(XmlNode.class, Class.class).
                newInstance(node, elementInfo.getProcessorClass());

            for (final Object element : node.getElementList()) {
                elementDef.add(toElementDef(element));
            }

            return elementDef;
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof ConfigurationException) {
                throw (ConfigurationException) cause;
            }
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        } catch (InstantiationException e) {
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Cannot create class instance: " +
                    elementInfo.getDefinitionClass() + "!", e);
        }
    }

    private IElementDef toElementDef(final Object subject) {
        if (subject instanceof XmlNode) {
            // TODO Use a proxy instead of real definition
            return createElementDefinition((XmlNode) subject);
        } else {
            // TODO Use a proxy instead of real definition
            return new ConstantDef(subject.toString(), ConstantProcessor.class);
        }
    }

}