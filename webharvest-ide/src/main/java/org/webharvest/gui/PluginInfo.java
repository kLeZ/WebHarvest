package org.webharvest.gui;

/**
 * Class definining elements in list of plugins. Each item has name (fully qualified class name)
 * and optional error message telling why plugin cannot be registered.
 */
public class PluginInfo {

    private String className;
    private String uri;
    private String errorMessage;

    public PluginInfo(String className, String uri, String errorMessage) {
        this.className = className;
        this.uri = uri;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return errorMessage == null;
    }

    public String getClassName() {
        return className;
    }

    public String getUri() {
        return uri;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String toString() {
        return className;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PluginInfo) {
            PluginInfo pluginInfo = (PluginInfo) obj;
            return className != null && className.equals(pluginInfo.className) &&
                   uri != null && uri.equals(pluginInfo.uri);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return className != null ? className.hashCode() : 0;
    }
}