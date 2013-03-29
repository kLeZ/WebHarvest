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

import org.apache.commons.collections.CollectionUtils;
import org.webharvest.WHConstants;
import org.webharvest.definition.DefinitionResolver;
import org.webharvest.exception.PluginException;
import org.webharvest.gui.component.*;
import org.webharvest.gui.settings.SettingsManager;
import org.webharvest.gui.settings.SimpleSettingsManager;
import org.webharvest.gui.settings.db.DatabaseDriversPanel;
import org.webharvest.gui.settings.db.DatabaseDriversPresenter;
import org.webharvest.gui.settings.validation.XmlSchemasPanel;
import org.webharvest.gui.settings.validation.XmlSchemasPresenter;
import org.webharvest.utils.CommonUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class SettingsDialog extends CommonDialog implements ChangeListener {

    private DefinitionResolver definitionResolver = DefinitionResolver.INSTANCE;

    private SettingsManager settingsManager = new SimpleSettingsManager();

    /**
     * List model implementation for the list of plugins.
     */
    private class PluginListModel extends DefaultListModel {

        public void addElement(PluginInfo pluginInfo, boolean throwErrIfRegistered) {
            if (addPlugin(pluginInfo, throwErrIfRegistered)) {
                super.addElement(pluginInfo);
            }
        }

        public boolean setElement(PluginInfo pluginInfo, int index) {
            if (addPlugin(pluginInfo, true)) {
                super.setElementAt(pluginInfo, index);
                fireContentsChanged(this, index, index);
                return true;
            }
            return false;
        }

        private boolean addPlugin(PluginInfo pluginInfo, boolean throwErrIfRegistered) {
            final String className = pluginInfo.getClassName();
            final String uri = pluginInfo.getUri();

            if (CommonUtil.isEmptyString(className) || CommonUtil.isEmptyString(uri)) {
                GuiUtils.showError(SettingsDialog.this, "Full plugin class name and namespace URI must be specified!");
                return false;
            }

            // check if it already exists in the list
            int size = getSize();
            for (int i = 0; i < size; i++) {
                PluginInfo item = (PluginInfo) get(i);
                if (item != null && item.equals(pluginInfo)) {
                    if (SettingsDialog.this.isVisible()) {
                        GuiUtils.showError(SettingsDialog.this, "Plugin is already added to the list!");
                    }
                    return false;
                }
            }

            String errorMessage = null;

            final boolean isAlreadyRegistered = definitionResolver.isPluginRegistered(className, uri);
            if (!isAlreadyRegistered || throwErrIfRegistered) {
                try {
                    if (isAlreadyRegistered) {
                        definitionResolver.unregisterPlugin(className, uri);
                    }
                    definitionResolver.registerPlugin(className, uri);
                } catch (PluginException e) {
                    errorMessage = e.getMessage();
                }
            }

            pluginInfo.setErrorMessage(errorMessage);

            return true;
        }
    }

    /**
     * Cell renderer for the list of plugins. It displays label with plugin class name
     * and OK or ERROR icon telling if plugin is registered successfully or not. If
     * plugin registration failed, tooltip is defined with error message for the label.
     */
    private class PluginListCellRenderer extends JLabel implements ListCellRenderer {

        private PluginListCellRenderer() {
            setOpaque(true);
            setPreferredSize(new Dimension(1, 18));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            PluginInfo item = (PluginInfo) value;
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setText(item.getClassName());
            setIcon(item.isValid() ? ResourceManager.VALID_ICON : ResourceManager.INVALID_ICON);
            setToolTipText(item.getErrorMessage());
            return this;
        }
    }

    // Ide instance where this dialog belongs.
    private Ide ide;

    // settings fields
    private JTextField workingPathField;
    private JComboBox fileCharsetComboBox;
    private JTextField proxyServerField;
    private JTextField proxyPortField;
    private JTextField proxyUsernameField;
    private JTextField proxyPasswordField;
    private JTextField ntlmHostField;
    private JTextField ntlmDomainField;
    private JCheckBox proxyEnabledCheckBox;
    private JCheckBox proxyAuthEnabledCheckBox;
    private JCheckBox ntlmEnabledCheckBox;

    private JLabel proxyUsernameLabel;
    private JLabel proxyPasswordLabel;
    private JLabel proxyPortLabel;
    private JLabel proxyServerLabel;
    private JLabel ntlmHostLabel;
    private JLabel ntlmDomainLabel;

    private JCheckBox showHierarchyByDefaultCheckBox;
    private JCheckBox showLogByDefaultCheckBox;
    private JCheckBox showLineNumbersByDefaultCheckBox;
    private JCheckBox dynamicConfigLocateCheckBox;
    private JCheckBox muteLogCheckBox;
    private JCheckBox showFinishDialogCheckBox;

    private JButton pluginUpdateButton;
    private JButton pluginRemoveButton;
    private PluginListModel pluginListModel;
    private JList pluginsList;
    private JTextField pluginClassNameField;
    private JTextField pluginNamespaceUriField;


    private final JFileChooser pathChooser = new JFileChooser();

    public SettingsDialog(Ide ide) throws HeadlessException {
        super("Settings");
        this.ide = ide;
        this.setResizable(false);

        pathChooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.exists() && f.isDirectory();
            }

            public String getDescription() {
                return "All directories";
            }
        });
        pathChooser.setMultiSelectionEnabled(false);
        pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        createGUI();
    }

    private void createGUI() {
        Container contentPane = this.getContentPane();

        JPanel generalPanel = new JPanel(new GridBagLayout());
        generalPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        contentPane.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 5, 2, 5);

        workingPathField = new FixedSizeTextField(250, -1);

        Map<String, Charset> charsetsMap = Charset.availableCharsets();
        Vector<String> allSupportedCharsets = new Vector<String>(charsetsMap.keySet());
        fileCharsetComboBox = new WHComboBox(allSupportedCharsets);

        proxyServerField = new FixedSizeTextField(250, -1);
        proxyPortField = new FixedSizeTextField(250, -1);
        proxyUsernameField = new FixedSizeTextField(250, -1);
        proxyPasswordField = new FixedSizeTextField(250, -1);
        ntlmHostField = new FixedSizeTextField(250, -1);
        ntlmDomainField = new FixedSizeTextField(250, -1);

        proxyEnabledCheckBox = new WHCheckBox("Proxy server enabled");
        proxyEnabledCheckBox.addChangeListener(this);
        proxyAuthEnabledCheckBox = new WHCheckBox("Proxy authentication enabled");
        proxyAuthEnabledCheckBox.addChangeListener(this);
        ntlmEnabledCheckBox = new WHCheckBox("Use NTLM authentication scheme");
        ntlmEnabledCheckBox.addChangeListener(this);

        constraints.gridx = 0;
        constraints.gridy = 0;
        generalPanel.add(new JLabel("Output path"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pathPanel.add(workingPathField);
        JButton chooseDirButton = new SmallButton("...") {
            public Dimension getPreferredSize() {
                return new Dimension(30, workingPathField.getHeight());
            }
        };
        chooseDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = pathChooser.showOpenDialog(SettingsDialog.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = pathChooser.getSelectedFile();
                    if (selectedDir != null) {
                        workingPathField.setText(selectedDir.getAbsolutePath());
                    }
                }
            }
        });
        pathPanel.add(chooseDirButton);
        generalPanel.add(pathPanel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        generalPanel.add(new JLabel("File encoding"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        generalPanel.add(fileCharsetComboBox, constraints);


        constraints.gridx = 0;
        constraints.gridy = 2;
        generalPanel.add(proxyEnabledCheckBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        proxyServerLabel = new JLabel("Proxy server");
        generalPanel.add(proxyServerLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        generalPanel.add(proxyServerField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        proxyPortLabel = new JLabel("Proxy port (blank is default)");
        generalPanel.add(proxyPortLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        generalPanel.add(proxyPortField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        generalPanel.add(proxyAuthEnabledCheckBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 6;
        proxyUsernameLabel = new JLabel("Proxy username");
        generalPanel.add(proxyUsernameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 6;
        generalPanel.add(proxyUsernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 7;
        proxyPasswordLabel = new JLabel("Proxy password");
        generalPanel.add(proxyPasswordLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 7;
        generalPanel.add(proxyPasswordField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 8;
        generalPanel.add(ntlmEnabledCheckBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 9;
        ntlmHostLabel = new JLabel("NT host");
        generalPanel.add(ntlmHostLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 9;
        generalPanel.add(ntlmHostField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 10;
        ntlmDomainLabel = new JLabel("NT domain");
        generalPanel.add(ntlmDomainLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 10;
        generalPanel.add(ntlmDomainField, constraints);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(createOkButton());
        buttonPanel.add(createCancelButton());

        JPanel viewPanel = new JPanel();
        viewPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
        viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.PAGE_AXIS));
        this.showHierarchyByDefaultCheckBox = new WHCheckBox("Show hierarchy panel by default");
        this.showLogByDefaultCheckBox = new WHCheckBox("Show log panel by default");
        this.showLineNumbersByDefaultCheckBox = new WHCheckBox("Show line numbers by default");
        this.dynamicConfigLocateCheckBox = new WHCheckBox("Dynamically locate processors in runtime");
        //
        //  Wed Feb 20 19:21:29 2013 -- Scott R. Turner
        //
        //  Add a new option for muting the log.
        //
        this.muteLogCheckBox = new WHCheckBox("Mute INFO messages in log window");
        this.showFinishDialogCheckBox = new WHCheckBox("Show info/error dialog when execution finishes");

        viewPanel.add(this.showHierarchyByDefaultCheckBox);
        viewPanel.add(this.showLogByDefaultCheckBox);
        viewPanel.add(this.showLineNumbersByDefaultCheckBox);
        viewPanel.add(this.dynamicConfigLocateCheckBox);
        viewPanel.add(this.muteLogCheckBox);
        viewPanel.add(this.showFinishDialogCheckBox);

        JPanel pluginsPanel = new JPanel(new BorderLayout(5, 0));
        JPanel pluginButtonsPanel = new JPanel();
        SpringLayout springLayout = new SpringLayout();
        pluginButtonsPanel.setLayout(springLayout);
        pluginButtonsPanel.setBorder(new EmptyBorder(3, 0, 3, 3));
        pluginButtonsPanel.setPreferredSize(new Dimension(116, 1));

        final JButton pluginAddButton = new FixedSizeButton("Add plugin", 110, 22);
        pluginAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String className = pluginClassNameField.getText().trim();
                String namespaceUri = pluginNamespaceUriField.getText().trim();
                if (CommonUtil.isEmptyString(namespaceUri)) {
                    namespaceUri = WHConstants.XMLNS_CORE;
                }
                if (className != null) {
                    pluginListModel.addElement(new PluginInfo(className, namespaceUri, null), true);
                    pluginsList.setSelectedIndex(pluginListModel.size() - 1);
                }
            }
        });
        pluginUpdateButton = new FixedSizeButton("Update plugin", 110, 22);
        pluginUpdateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = pluginsList.getSelectedIndex();
                if (index >= 0) {
                    PluginInfo oldPluginInfo = (PluginInfo) pluginsList.getSelectedValue();
                    PluginInfo pluginInfo = new PluginInfo(pluginClassNameField.getText().trim(), pluginNamespaceUriField.getText().trim(), null);
                    if (!pluginInfo.equals(oldPluginInfo)) {
                        boolean isSet = pluginListModel.setElement(pluginInfo, index);
                        if (isSet) {
                            definitionResolver.unregisterPlugin(oldPluginInfo.getClassName(), pluginInfo.getUri());
                            definitionResolver.registerPlugin(pluginInfo.getClassName(), pluginInfo.getUri());
                        }
                    }
                }
            }
        });
        pluginRemoveButton = new FixedSizeButton("Remove plugin", 110, 22);
        pluginRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = pluginsList.getSelectedIndex();
                if (index >= 0) {
                    definitionResolver.unregisterPlugin(pluginsList.getSelectedValue().toString(), pluginNamespaceUriField.getText().trim());
                    pluginListModel.remove(index);
                    pluginsList.setSelectedIndex(Math.min(index, pluginListModel.size() - 1));
                }
            }
        });

        pluginButtonsPanel.add(pluginAddButton);
        pluginButtonsPanel.add(pluginUpdateButton);
        pluginButtonsPanel.add(pluginRemoveButton);

        springLayout.putConstraint(SpringLayout.NORTH, pluginUpdateButton, 5, SpringLayout.SOUTH, pluginAddButton);
        springLayout.putConstraint(SpringLayout.NORTH, pluginRemoveButton, 5, SpringLayout.SOUTH, pluginUpdateButton);

        pluginsPanel.add(pluginButtonsPanel, BorderLayout.EAST);
        JPanel pluginsListPanel = new JPanel(new BorderLayout(5, 5));
        pluginsListPanel.setBorder(new EmptyBorder(3, 3, 3, 3));

        pluginListModel = new PluginListModel();
        pluginsList = new WHList(pluginListModel);
        pluginsList.setCellRenderer(new PluginListCellRenderer());
        pluginsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateControls();
            }
        });

        pluginsListPanel.add(new WHScrollPane(pluginsList), BorderLayout.CENTER);
        pluginsPanel.add(pluginsListPanel, BorderLayout.CENTER);

        JPanel pluginEditPanel = new JPanel();
        pluginEditPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        pluginEditPanel.setLayout(new BoxLayout(pluginEditPanel, BoxLayout.Y_AXIS));
        final JLabel fullClassNameLabel = new JLabel("Full class name:");
        final JLabel nsLabel = new JLabel("Namespace URI:");
        GuiUtils.fixComponentWidths(new Component[]{fullClassNameLabel, nsLabel});
        this.pluginClassNameField = new JTextField("", 40);
        this.pluginNamespaceUriField = new JTextField("", 40);
        pluginEditPanel.add(GuiUtils.createLineOfComponents(new Component[]{fullClassNameLabel, pluginClassNameField}));
        pluginEditPanel.add(GuiUtils.createLineOfComponents(new Component[]{nsLabel, pluginNamespaceUriField}));

        pluginsPanel.add(pluginEditPanel, BorderLayout.NORTH);

        tabbedPane.addTab("General", null, generalPanel, null);
        tabbedPane.addTab("View", null, viewPanel, null);
        tabbedPane.addTab("Plugins", null, pluginsPanel, null);
        tabbedPane.addTab("XML schemas", null, createXmlSchemasPanel(), null);
        tabbedPane.addTab("DB drivers", null, createDBDriversPanel(), null);

        contentPane.add(tabbedPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        updateControls();

        this.pack();
    }

    // FIXME: we need more versatile (IoC-like) solution
    private JPanel createDBDriversPanel() {
        final DatabaseDriversPanel panel = new DatabaseDriversPanel();
        final DatabaseDriversPresenter presenter =
            new DatabaseDriversPresenter(panel);
        panel.setPresenter(presenter);

        return panel;
    }

    private JPanel createXmlSchemasPanel() {
        final XmlSchemasPanel panel = new XmlSchemasPanel();
        final XmlSchemasPresenter presenter = new XmlSchemasPresenter(panel);
        panel.setPresenter(presenter);

        settingsManager.addSettingsAware(presenter);

        return panel;
    }

    private void fillValues() {
        Settings settings = ide.getSettings();

        workingPathField.setText(settings.getWorkingPath());
        fileCharsetComboBox.setSelectedItem(settings.getFileCharset());
        proxyServerField.setText(settings.getProxyServer());
        proxyPortField.setText(settings.getProxyPort() > 0 ? "" + settings.getProxyPort() : "");
        proxyUsernameField.setText(settings.getProxyUserename());
        proxyPasswordField.setText(settings.getProxyPassword());
        proxyEnabledCheckBox.setSelected(settings.isProxyEnabled());
        proxyAuthEnabledCheckBox.setSelected(settings.isProxyAuthEnabled());

        ntlmEnabledCheckBox.setSelected(settings.isNtlmAuthEnabled());
        ntlmHostField.setText(settings.getNtlmHost());
        ntlmDomainField.setText(settings.getNtlmDomain());

        showHierarchyByDefaultCheckBox.setSelected(settings.isShowHierarchyByDefault());
        showLogByDefaultCheckBox.setSelected(settings.isShowLogByDefault());
        showLineNumbersByDefaultCheckBox.setSelected(settings.isShowLineNumbersByDefault());
        dynamicConfigLocateCheckBox.setSelected(settings.isDynamicConfigLocate());
        muteLogCheckBox.setSelected(settings.isMuteLog());
        showFinishDialogCheckBox.setSelected(settings.isShowFinishDialog());

        pluginListModel.clear();
        PluginInfo[] plugins = settings.getPlugins();
        for (PluginInfo plugin : plugins) {
            pluginListModel.addElement(plugin, false);
        }

        settingsManager.loadSettings(settings);
    }

    @SuppressWarnings({"unchecked"})
    private void undoPlugins() {
        Set<PluginInfo> pluginSet = new HashSet<PluginInfo>(Arrays.asList(ide.getSettings().getPlugins()));
        Set<PluginInfo> listSet = new HashSet<PluginInfo>();

        // unregister plugins registered during this settings session
        int count = pluginListModel.getSize();
        for (int i = 0; i < count; i++) {
            PluginInfo item = (PluginInfo) pluginListModel.get(i);
            listSet.add(item);
            if (item.isValid() && !pluginSet.contains(item)) {
                definitionResolver.unregisterPlugin(item.getClassName(), item.getUri());
            }
        }

        // register plugins unregistered during this setting session
        for (PluginInfo currPlugin : (Iterable<? extends PluginInfo>) CollectionUtils.subtract(pluginSet, listSet)) {
            if (!definitionResolver.isPluginRegistered(currPlugin.getClassName(), currPlugin.getUri())) {
                try {
                    definitionResolver.registerPlugin(currPlugin.getClassName(), currPlugin.getUri());
                } catch (PluginException e) {
                    // do nothing - ignore
                }
            }
        }
    }

    public void setVisible(boolean b) {
        if (b) {
            fillValues();
        } else {
            undoPlugins();
        }
        super.setVisible(b);
    }

    private void define() {
        Settings settings = this.ide.getSettings();

        settings.setWorkingPath(this.workingPathField.getText());
        settings.setFileCharset(this.fileCharsetComboBox.getSelectedItem().toString());
        settings.setProxyServer(this.proxyServerField.getText());

        int port = -1;
        try {
            port = Integer.parseInt(this.proxyPortField.getText());
        } catch (NumberFormatException ignored) {
        }
        settings.setProxyPort(port);

        settings.setProxyUserename(this.proxyUsernameField.getText());
        settings.setProxyPassword(this.proxyPasswordField.getText());

        settings.setProxyEnabled(this.proxyEnabledCheckBox.isSelected());
        settings.setProxyAuthEnabled(this.proxyAuthEnabledCheckBox.isSelected());

        settings.setNtlmAuthEnabled(this.ntlmEnabledCheckBox.isSelected());
        settings.setNtlmHost(this.ntlmHostField.getText());
        settings.setNtlmDomain(this.ntlmDomainField.getText());

        settings.setShowHierarchyByDefault(this.showHierarchyByDefaultCheckBox.isSelected());
        settings.setShowLogByDefault(this.showLogByDefaultCheckBox.isSelected());
        settings.setShowLineNumbersByDefault(this.showLineNumbersByDefaultCheckBox.isSelected());
        settings.setDynamicConfigLocate(this.dynamicConfigLocateCheckBox.isSelected());
        settings.setMuteLog(this.muteLogCheckBox.isSelected());
        settings.setShowFinishDialog(this.showFinishDialogCheckBox.isSelected());

        int pluginCount = pluginListModel.getSize();
        PluginInfo plugins[] = new PluginInfo[pluginCount];
        for (int i = 0; i < pluginCount; i++) {
            plugins[i] = (PluginInfo) pluginListModel.get(i);
        }
        settings.setPlugins(plugins);

        settingsManager.updateSettings(settings);

        try {
            settings.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
            GuiUtils.showErrorMessage("Error saving programs settings: " + e.getMessage());
        }

        updateControls();

        setVisible(false);
    }

    /**
     * Enable/disable controls depending on setting values.
     */
    private void updateControls() {
        boolean isProxyEnabled = this.proxyEnabledCheckBox.isSelected();
        boolean isProxyAuthEnabled = this.proxyAuthEnabledCheckBox.isSelected();
        boolean isNtlmAuthEnabled = this.ntlmEnabledCheckBox.isSelected();

        this.proxyServerLabel.setEnabled(isProxyEnabled);
        this.proxyServerField.setEnabled(isProxyEnabled);
        this.proxyPortLabel.setEnabled(isProxyEnabled);
        this.proxyPortField.setEnabled(isProxyEnabled);

        this.proxyAuthEnabledCheckBox.setEnabled(isProxyEnabled);

        this.proxyUsernameLabel.setEnabled(isProxyEnabled && isProxyAuthEnabled);
        this.proxyUsernameField.setEnabled(isProxyEnabled && isProxyAuthEnabled);
        this.proxyPasswordLabel.setEnabled(isProxyEnabled && isProxyAuthEnabled);
        this.proxyPasswordField.setEnabled(isProxyEnabled && isProxyAuthEnabled);

        this.ntlmEnabledCheckBox.setEnabled(isProxyEnabled && isProxyAuthEnabled);

        this.ntlmHostLabel.setEnabled(isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled);
        this.ntlmHostField.setEnabled(isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled);
        this.ntlmDomainLabel.setEnabled(isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled);
        this.ntlmDomainField.setEnabled(isProxyEnabled && isProxyAuthEnabled && isNtlmAuthEnabled);

        int selectedPluginIndex = pluginsList.getSelectedIndex();
        pluginUpdateButton.setEnabled(selectedPluginIndex >= 0);
        pluginRemoveButton.setEnabled(selectedPluginIndex >= 0);
        if (selectedPluginIndex >= 0 && selectedPluginIndex < pluginListModel.getSize()) {
            PluginInfo pluginItem = (PluginInfo) pluginListModel.get(selectedPluginIndex);
            pluginClassNameField.setText(pluginItem.getClassName());
            pluginNamespaceUriField.setText(CommonUtil.nvl(pluginItem.getUri(), WHConstants.XMLNS_CORE));
        } else {
            pluginClassNameField.setText("");
            pluginNamespaceUriField.setText(WHConstants.XMLNS_CORE);
        }
    }

    public void stateChanged(ChangeEvent e) {
        updateControls();
    }

    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                define();
            }
        };
        rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


        return rootPane;
    }

    protected void onOk() {
        define();
    }

}