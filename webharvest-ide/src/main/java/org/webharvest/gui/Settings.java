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
package org.webharvest.gui;

import org.webharvest.TransformationException;
import org.webharvest.Transformer;
import org.webharvest.WHConstants;
import org.webharvest.definition.DefinitionResolver;
import org.webharvest.definition.validation.SchemaResolver;
import org.webharvest.definition.validation.SchemaResolverPostProcessor;
import org.webharvest.definition.validation.SchemaSource;
import org.webharvest.definition.validation.TransformerPair;
import org.webharvest.definition.validation.URIToSchemaSourceTransformer;
import org.webharvest.exception.PluginException;
import org.webharvest.gui.settings.validation.FilePathToURITransformer;
import org.webharvest.gui.settings.validation.XmlSchemaDTO;
import org.webharvest.utils.CommonUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author Vladimir Nikic
 * Date: Apr 27, 2007
 */
//TODO This class should be refactored, because its complexity is too high.
public class Settings implements Serializable, SchemaResolverPostProcessor {

    private static final String CONFIG_FILE_PATH_OLD = System.getProperty("java.io.tmpdir") + "/webharvest.config";
    private static final String CONFIG_FILE_PATH = System.getProperty("java.io.tmpdir") + "/webharvest.properties";
    private static final int MAX_RECENT_FILES = 20;

    private String workingPath = System.getProperty("java.io.tmpdir");
    private String fileCharset = "UTF-8";
    private boolean isProxyEnabled;
    private String proxyServer;
    private int proxyPort = -1;
    private boolean isProxyAuthEnabled;
    private String proxyUserename;
    private String proxyPassword;
    private boolean isNtlmAuthEnabled;
    private String ntlmHost;
    private String ntlmDomain;

    private boolean isShowHierarchyByDefault = true;
    private boolean isShowLogByDefault = true;
    private boolean isShowLineNumbersByDefault = true;

    // specify if processors are located in source while configuration is running
    private boolean isDynamicConfigLocate = true;

    //
    //  Wed Feb 20 19:23:56 2013 -- Scott R. Turner
    //
    //  Mute the INFO messages in the log?
    //
    private boolean isMuteLog = false;

    // specify if info is displayed on execution finish
    private boolean isShowFinishDialog = true;

    // array of plugins
    private PluginInfo plugins[] = {};

    // array of XML schemas
    private XmlSchemaDTO xmlSchemas[] = {};

    // list of recently open files
    private List recentFiles = new LinkedList();

    private DefinitionResolver definitionResolver = DefinitionResolver.INSTANCE;

    private final Transformer<String, SchemaSource> schemaTransformer =
        new TransformerPair<String, URI, SchemaSource>(
                new FilePathToURITransformer(),
                new URIToSchemaSourceTransformer());

    public Settings() {
        try {
            readFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            GuiUtils.showErrorMessage("Error while reading programs settings: " + e.getMessage());
        }
    }

    public boolean isProxyAuthEnabled() {
        return isProxyAuthEnabled;
    }

    public void setProxyAuthEnabled(boolean proxyAuthEnabled) {
        isProxyAuthEnabled = proxyAuthEnabled;
    }

    public boolean isProxyEnabled() {
        return isProxyEnabled;
    }

    public void setProxyEnabled(boolean proxyEnabled) {
        isProxyEnabled = proxyEnabled;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    public String getProxyUserename() {
        return proxyUserename;
    }

    public void setProxyUserename(String proxyUserename) {
        this.proxyUserename = proxyUserename;
    }

    public boolean isNtlmAuthEnabled() {
        return isNtlmAuthEnabled;
    }

    public void setNtlmAuthEnabled(boolean ntlmAuthEnabled) {
        isNtlmAuthEnabled = ntlmAuthEnabled;
    }

    public String getNtlmDomain() {
        return ntlmDomain;
    }

    public void setNtlmDomain(String ntlmDomain) {
        this.ntlmDomain = ntlmDomain;
    }

    public String getNtlmHost() {
        return ntlmHost;
    }

    public void setNtlmHost(String ntlmHost) {
        this.ntlmHost = ntlmHost;
    }

    public String getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(String workingPath) {
        this.workingPath = workingPath;
    }

    public String getFileCharset() {
        return fileCharset;
    }

    public void setFileCharset(String fileCharset) {
        this.fileCharset = fileCharset;
    }

    public boolean isDynamicConfigLocate() {
        return this.isDynamicConfigLocate;
    }

    public void setDynamicConfigLocate(boolean dynamicConfigLocate) {
        isDynamicConfigLocate = dynamicConfigLocate;
    }

    public boolean isMuteLog() {
        return this.isMuteLog;
    }

    public void setMuteLog(boolean muteLog) {
        isMuteLog = muteLog;
        //
        //  Wed Feb 20 19:26:54 2013 -- Scott R. Turner
        //
        //  When mute log is set (or unset) we need to adjust the log level accordingly.
        //
        if (muteLog) {
            LogManager.getRootLogger().setLevel(Level.WARN);
        } else {
            LogManager.getRootLogger().setLevel(Level.INFO);
        };
    }

    public boolean isShowFinishDialog() {
        return isShowFinishDialog;
    }

    public void setShowFinishDialog(boolean showFinishDialog) {
        isShowFinishDialog = showFinishDialog;
    }

    public boolean isShowHierarchyByDefault() {
        return isShowHierarchyByDefault;
    }

    public void setShowHierarchyByDefault(boolean showHierarchyByDefault) {
        isShowHierarchyByDefault = showHierarchyByDefault;
    }

    public boolean isShowLogByDefault() {
        return isShowLogByDefault;
    }

    public void setShowLogByDefault(boolean showLogByDefault) {
        isShowLogByDefault = showLogByDefault;
    }

    public boolean isShowLineNumbersByDefault() {
        return isShowLineNumbersByDefault;
    }

    public void setShowLineNumbersByDefault(boolean showLineNumbersByDefault) {
        isShowLineNumbersByDefault = showLineNumbersByDefault;
    }

    public PluginInfo[] getPlugins() {
        return plugins;
    }

    public void setPlugins(PluginInfo[] plugins) {
        this.plugins = plugins;
    }

    /**
     * Returns array of {@link XmlSchemaDTO}s read for the properties file.
     *
     * @return array of {@link XmlSchemaDTO}s read for the properties file.
     */
    public XmlSchemaDTO[] getXmlSchemas() {
        return xmlSchemas;
    }

    /**
     * Sets array of {@link XmlSchemaDTO}s defining XML schemas for custom
     * plugins.
     *
     * @param xmlSchemas
     *            array of {@link XmlSchemaDTO}s; must be not-{@code null}
     */
    public void setXmlSchemas(final XmlSchemaDTO[] xmlSchemas) {
        if (xmlSchemas == null) {
            throw new IllegalArgumentException(
                    "Array of XmlSchemaDTOs must not be null.");
        }
        this.xmlSchemas = xmlSchemas;
    }

    public List getRecentFiles() {
        return recentFiles;
    }

    public void addRecentFile(String filePath) {
        int index = CommonUtil.findValueInCollection(recentFiles, filePath);
        if (index >= 0) {
            recentFiles.remove(index);
        }
        recentFiles.add(0, filePath);

        int recentFilesCount = recentFiles.size();
        if (recentFilesCount > MAX_RECENT_FILES) {
            recentFiles.remove(recentFilesCount - 1);
        }
    }

    private void writeString(ObjectOutputStream out, String s) throws IOException {
        if (s != null) {
            out.writeInt(s.getBytes().length);
            out.writeBytes(s);
        } else {
            out.writeInt(0);
        }
    }

    private String readString(ObjectInputStream in, String defaultValue) throws IOException {
        try {
            byte[] bytes = new byte[in.readInt()];
            in.read(bytes);
            return new String(bytes);
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private boolean readBoolean(ObjectInputStream in, boolean defaultValue) throws IOException {
        try {
            return in.readBoolean();
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private int readInt(ObjectInputStream in, int defaultValue) throws IOException {
        try {
            return in.readInt();
        } catch (IOException e) {
            return defaultValue;
        }
    }

    private Properties readProperties() {
        Properties props = new Properties();

        props.setProperty("workpath", String.valueOf(workingPath));
        props.setProperty("proxyenabled", String.valueOf(isProxyEnabled));
        props.setProperty("proxyserver", String.valueOf(proxyServer));
        props.setProperty("proxyport", String.valueOf(proxyPort));
        props.setProperty("proxyauthenabled", String.valueOf(isProxyAuthEnabled));
        props.setProperty("proxyusername", String.valueOf(proxyUserename));
        props.setProperty("proxypassword", String.valueOf(proxyPassword));
        props.setProperty("ntlmauthenabled", String.valueOf(isNtlmAuthEnabled));
        props.setProperty("ntlmhost", String.valueOf(ntlmHost));
        props.setProperty("ntlmdomain", String.valueOf(ntlmDomain));

        props.setProperty("showhierarchy", String.valueOf(isShowHierarchyByDefault));
        props.setProperty("showlog", String.valueOf(isShowLogByDefault));
        props.setProperty("showlinenums", String.valueOf(isShowLineNumbersByDefault));
        props.setProperty("dynlocate", String.valueOf(isDynamicConfigLocate));
        props.setProperty("mutelog", String.valueOf(isMuteLog));
        props.setProperty("filecharset", String.valueOf(fileCharset));
        props.setProperty("showfinish", String.valueOf(isShowFinishDialog));

        props.setProperty("plugin.count", String.valueOf(plugins.length));
        for (int i = 0; i < plugins.length; i++) {
            props.setProperty("plugin" + i + ".class", String.valueOf(plugins[i].getClassName()));
            props.setProperty("plugin" + i + ".uri", String.valueOf(plugins[i].getUri()));
        }

        final XmlSchemaDTO[] schemas = getXmlSchemas();
        props.setProperty("schema.count", String.valueOf(schemas.length));
        for (int i = 0; i < schemas.length; i++) {
            props.setProperty("schema" + i + ".location",
                    String.valueOf(schemas[i].getLocation()));
        }

        props.setProperty("recentfiles.count", String.valueOf(recentFiles.size()));
        for (int i = 0; i < recentFiles.size(); i++) {
            props.setProperty("recentfile" + i, String.valueOf(recentFiles.get(i)));
        }

        return props;
    }

    private void loadFromProperties(Properties props) {
        if (props.containsKey("workpath")) {
            workingPath = props.getProperty("workpath");
        }

        if (props.containsKey("proxyenabled")) {
            isProxyEnabled = CommonUtil.getBooleanValue(props.getProperty("proxyenabled"), false);
        }
        if (props.containsKey("proxyserver")) {
            proxyServer = props.getProperty("proxyserver");
        }
        if (props.containsKey("proxyport")) {
            proxyPort = CommonUtil.getIntValue(props.getProperty("proxyport"), 0);
        }
        if (props.containsKey("proxyauthenabled")) {
            isProxyAuthEnabled = CommonUtil.getBooleanValue(props.getProperty("proxyauthenabled"), false);
        }
        if (props.containsKey("proxyusername")) {
            proxyUserename = props.getProperty("proxyusername");
        }
        if (props.containsKey("proxypassword")) {
            proxyPassword = props.getProperty("proxypassword");
        }
        if (props.containsKey("ntlmauthenabled")) {
            isNtlmAuthEnabled = CommonUtil.getBooleanValue(props.getProperty("ntlmauthenabled"), false);
        }
        if (props.containsKey("ntlmhost")) {
            ntlmHost = props.getProperty("ntlmhost");
        }
        if (props.containsKey("ntlmdomain")) {
            ntlmDomain = props.getProperty("ntlmdomain");
        }

        if (props.containsKey("showhierarchy")) {
            isShowHierarchyByDefault = CommonUtil.getBooleanValue(props.getProperty("showhierarchy"), true);
        }
        if (props.containsKey("showlog")) {
            isShowLogByDefault = CommonUtil.getBooleanValue(props.getProperty("showlog"), true);
        }
        if (props.containsKey("showlinenums")) {
            isShowLineNumbersByDefault = CommonUtil.getBooleanValue(props.getProperty("showlinenums"), true);
        }
        if (props.containsKey("dynlocate")) {
            isDynamicConfigLocate = CommonUtil.getBooleanValue(props.getProperty("dynlocate"), false);
        }
        if (props.containsKey("mutelog")) {
            isMuteLog = CommonUtil.getBooleanValue(props.getProperty("mutelog"), false);
        }
        if (props.containsKey("filecharset")) {
            fileCharset = props.getProperty("filecharset");
        }
        if (props.containsKey("showfinish")) {
            isShowFinishDialog = CommonUtil.getBooleanValue(props.getProperty("showfinish"), true);
        }

        int pluginCount = CommonUtil.getIntValue(props.getProperty("plugin.count"), 0);
        plugins = new PluginInfo[pluginCount];
        for (int i = 0; i < pluginCount; i++) {
            plugins[i] = new PluginInfo(props.getProperty("plugin" + i + ".class"), props.getProperty("plugin" + i + ".uri"), null);
            try {
                definitionResolver.registerPlugin(plugins[i].getClassName(), plugins[i].getUri());
            } catch (PluginException e) {
                e.printStackTrace();
            }
        }

        // load from properties file information about XML schemas
        final int schemaCount =
            CommonUtil.getIntValue(props.getProperty("schema.count"), 0);
        final XmlSchemaDTO[] schemas = new XmlSchemaDTO[schemaCount];
        for (int i = 0; i < schemaCount; i++) {
            schemas[i] =
                new XmlSchemaDTO(props.getProperty("schema" + i + ".location"));
        }
        setXmlSchemas(schemas);

        int recentFileCount = CommonUtil.getIntValue(props.getProperty("recentfiles.count"), 0);
        recentFiles.clear();
        for (int i = 0; i < recentFileCount; i++) {
            recentFiles.add(props.getProperty("recentfile" + i));
        }
    }

    /**
     * Serialization read.
     *
     * @param in
     * @throws IOException
     */
    private void readObject_old(ObjectInputStream in) throws IOException {
        workingPath = readString(in, workingPath);

        isProxyEnabled = readBoolean(in, isProxyEnabled);
        proxyServer = readString(in, proxyServer);
        proxyPort = readInt(in, proxyPort);
        isProxyAuthEnabled = readBoolean(in, isProxyAuthEnabled);
        proxyUserename = readString(in, proxyUserename);
        proxyPassword = readString(in, proxyPassword);
        isNtlmAuthEnabled = readBoolean(in, isNtlmAuthEnabled);
        ntlmHost = readString(in, ntlmHost);
        ntlmDomain = readString(in, ntlmDomain);

        isShowHierarchyByDefault = readBoolean(in, isShowHierarchyByDefault);
        isShowLogByDefault = readBoolean(in, isShowLogByDefault);
        isShowLineNumbersByDefault = readBoolean(in, isShowLineNumbersByDefault);
        isDynamicConfigLocate = readBoolean(in, isDynamicConfigLocate);
        isMuteLog = readBoolean(in, isMuteLog);

        fileCharset = readString(in, fileCharset);

        isShowFinishDialog = readBoolean(in, isShowFinishDialog);

        int pluginsCount = readInt(in, 0);
        plugins = new PluginInfo[pluginsCount];
        for (int i = 0; i < pluginsCount; i++) {
            plugins[i] = new PluginInfo(readString(in, ""), WHConstants.XMLNS_CORE_10, null);
            try {
                definitionResolver.registerPlugin(plugins[i].getClassName(), WHConstants.XMLNS_CORE_10);
            } catch (PluginException e) {
                e.printStackTrace();
            }
        }

        int recentFilesCount = readInt(in, 0);
        recentFiles.clear();
        for (int i = 0; i < recentFilesCount; i++) {
            recentFiles.add(readString(in, ""));
        }
    }

    private void readFromFile() throws IOException {
        File configFile = new File(CONFIG_FILE_PATH);
        // try first to read from the properties file
        if (configFile.exists()) {
            Properties props = new Properties();
            props.load(new FileInputStream(CONFIG_FILE_PATH));
            loadFromProperties(props);
        } else {
            // if properties file doesn't exist, try to read from old formatted binary file
            configFile = new File(CONFIG_FILE_PATH_OLD);
            if (configFile.exists()) {
                FileInputStream fis = new FileInputStream(configFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                readObject_old(ois);
            }
        }
    }

    public void writeToFile() throws IOException {
        readProperties().store(new FileOutputStream(CONFIG_FILE_PATH), null);
    }

    public void writeSilentlyToFile() {
        try {
            writeToFile();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcess(final SchemaResolver resolver) {
        if (resolver == null) {
            throw new IllegalArgumentException(
                    "SchemaResolver must not be null.");
        }
        for (final XmlSchemaDTO schema : xmlSchemas) {
            try {
                resolver.registerSchemaSource(
                        schemaTransformer.transform(schema.getLocation()));
            } catch (final TransformationException e) {
                throw new RuntimeException(e);
            }
        }
    }

}