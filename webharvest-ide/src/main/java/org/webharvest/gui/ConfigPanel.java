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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.webharvest.Harvest;
import org.webharvest.HarvestLoadCallback;
import org.webharvest.Harvester;
import org.webharvest.WHConstants;
import org.webharvest.definition.ConfigSource;
import org.webharvest.definition.ConstantDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.events.EventHandler;
import org.webharvest.events.ProcessorStartEvent;
import org.webharvest.events.ProcessorStopEvent;
import org.webharvest.events.ScraperExecutionContinuedEvent;
import org.webharvest.events.ScraperExecutionEndEvent;
import org.webharvest.events.ScraperExecutionErrorEvent;
import org.webharvest.events.ScraperExecutionExitEvent;
import org.webharvest.events.ScraperExecutionPausedEvent;
import org.webharvest.events.ScraperExecutionStartEvent;
import org.webharvest.events.ScraperExecutionStoppedEvent;
import org.webharvest.gui.component.MenuElements;
import org.webharvest.gui.component.ProportionalSplitPane;
import org.webharvest.gui.component.WHPopupMenu;
import org.webharvest.gui.ioc.GuiModule;
import org.webharvest.ioc.HttpModule;
import org.webharvest.ioc.ScraperModule;
import org.webharvest.runtime.ContextHolder;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperState;
import org.webharvest.runtime.processors.AbstractProcessor;
import org.webharvest.runtime.processors.Processor;
import org.webharvest.runtime.web.HttpClientManager.ProxySettings;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.util.Modules;

/**
 * Single panel containing XML configuration.
 * It is part of multiple-document interface where several such instances may exist at
 * the same time.
 */
public class ConfigPanel extends JPanel implements TreeSelectionListener, CaretListener, ContextHolder {

    private static final String VIEW_RESULT_AS_TEXT = "View result as text";
    private static final String VIEW_RESULT_AS_XML = "View result as XML";
    private static final String VIEW_RESULT_AS_HTML = "View result as HTML";
    private static final String VIEW_RESULT_AS_IMAGE = "View result as image";
    private static final String VIEW_RESULT_AS_LIST = "View result as list";

    // basic skeleton for new opened configuration
    private static final String BASIC_CONFIG_SKELETON = "" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" +
            "<config xmlns=\"" + WHConstants.XMLNS_CORE + "\"\n" +
            "\t\txmlns:var=\"" + WHConstants.XMLNS_VAR + "\"\n" +
            "\t\txmlns:p=\"" + WHConstants.XMLNS_PARAM + "\">\n" +
            "\t\n" +
            "</config>";

    private static Logger LOG = Logger.getLogger(ConfigPanel.class);

    static {
        LOG.addAppender(TextAreaAppender.INSTANCE);
        Logger.getLogger(Scraper.class).addAppender(TextAreaAppender.INSTANCE);
        Logger.getLogger(AbstractProcessor.class).addAppender(TextAreaAppender.INSTANCE);
        // TODO Register all loggers we want to trace messages in IDE log panel
    }

    private XmlEditorScrollPane xmlEditorScrollPane;

    /**
     * Action listener for view menu items
     */
    private class ViewerActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();

            int viewType = ViewerFrame.TEXT_VIEW;
            if (VIEW_RESULT_AS_HTML.equalsIgnoreCase(actionCommand)) {
                viewType = ViewerFrame.HTML_VIEW;
            } else if (VIEW_RESULT_AS_IMAGE.equalsIgnoreCase(actionCommand)) {
                viewType = ViewerFrame.IMAGE_VIEW;
            } else if (VIEW_RESULT_AS_LIST.equalsIgnoreCase(actionCommand)) {
                viewType = ViewerFrame.LIST_VIEW;
            } else if (VIEW_RESULT_AS_XML.equalsIgnoreCase(actionCommand)) {
                viewType = ViewerFrame.XML_VIEW;
            }

            DefaultMutableTreeNode treeNode;

            TreePath path = tree.getSelectionPath();
            if (path != null) {
                treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (treeNode != null) {
                    Object userObject = treeNode.getUserObject();
                    if (userObject instanceof TreeNodeInfo) {
                        TreeNodeInfo treeNodeInfo = (TreeNodeInfo) userObject;
                        Map properties = treeNodeInfo.getProperties();
                        Object value = properties == null ? null : properties.get(WHConstants.VALUE_PROPERTY_NAME);
                        final ViewerFrame viewerFrame = new ViewerFrame(ConfigPanel.this, WHConstants.VALUE_PROPERTY_NAME, value, treeNodeInfo, viewType);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                viewerFrame.setVisible(true);
                                viewerFrame.toFront();
                            }
                        });
                    }
                }
            }
        }
    }

    private ConfigDocument configDocument;

    // initial configuration parameters
    private Map initParams = null;

    private Ide ide;

    private DefaultMutableTreeNode topNode;
    private DefaultTreeModel treeModel;
    private TreeNodeInfo selectedNodeInfo;
    private JTextArea logTextArea;
    // Indexed by source line number for particular element def
    private Map<Integer, TreeNodeInfo> nodeInfos = new Hashtable<Integer, TreeNodeInfo>();
    private NodeRenderer nodeRenderer = new NodeRenderer();

    private JSplitPane bottomSplitter;
    private JSplitPane leftSplitter;
    private JSplitPane leftView;
    private JScrollPane bottomView;
    private int leftDividerLocation = 0;
    private int bottomDividerLocation = 0;

    private XmlTextPane xmlPane;
    private JTree tree;
    private PropertiesGrid propertiesGrid;

    // tree popup menu items
    private JMenuItem textViewMenuItem;
    private JMenuItem xmlViewMenuItem;
    private JMenuItem htmlViewMenuItem;
    private JMenuItem imageViewMenuItem;
    private JMenuItem listViewMenuItem;

    // Log area popup menu items
    private JMenuItem logSelectAllMenuItem;
    private JMenuItem logClearAllMenuItem;

    private Harvest harvest;

    private Harvester harvester;

    //TODO: ConfigPanel should not hold reference to DynamicScopeContext, but
    //firstly ViewerFrame must be well designed in order to do not required
    //Scraper's context.
    private DynamicScopeContext scraperContext;

    //TODO: ConfigPanel should not hold Scraper's state, but firstly components
    //of IDE must be well designed and react correctly on events.
    private ScraperState scraperState = ScraperState.READY;

    /**
     * Constructor of the panel - initializes parent Ide instance and name of the document.
     *
     * @param ide
     * @param name
     */
    public ConfigPanel(final Ide ide, String name) {
        super(new BorderLayout());

        this.ide = ide;

        this.topNode = new DefaultMutableTreeNode();
        this.treeModel = new DefaultTreeModel(this.topNode);

        this.tree = new JTree(this.treeModel);
        this.tree.setRootVisible(false);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(this.tree);
        this.tree.setCellRenderer(this.nodeRenderer);
        tree.setShowsRootHandles(true);
        this.tree.addTreeSelectionListener(this);

        // defines pop menu for the tree
        final JPopupMenu treePopupMenu = new WHPopupMenu();
        JMenuItem menuItem = new MenuElements.MenuItem("Locate in source");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    locateInSource((DefaultMutableTreeNode) path.getLastPathComponent(), false);
                    try {
                        int startPos = xmlPane.getCaretPosition();
                        String content = xmlPane.getDocument().getText(0, xmlPane.getDocument().getLength());
                        if (content != null && content.length() > startPos) {
                            int closingIndex = content.indexOf('>', startPos);
                            if (closingIndex > startPos) {
                                xmlPane.select(startPos, closingIndex + 1);
                            }
                        }
                    } catch (BadLocationException e1) {
                        //todo: swallow exception?
                    }
                    xmlPane.requestFocus();
                }
            }
        });
        treePopupMenu.add(menuItem);

        treePopupMenu.addSeparator();

        ViewerActionListener viewContentActionListener = new ViewerActionListener();

        textViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_TEXT);
        textViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(textViewMenuItem);

        xmlViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_XML);
        xmlViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(xmlViewMenuItem);

        htmlViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_HTML);
        htmlViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(htmlViewMenuItem);

        imageViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_IMAGE);
        imageViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(imageViewMenuItem);

        listViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_LIST);
        listViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(listViewMenuItem);

        treePopupMenu.setOpaque(true);
        treePopupMenu.setLightWeightPopupEnabled(true);

        this.tree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        tree.setSelectionPath(path);
                    }
                    treePopupMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane treeView = new JScrollPane(this.tree);
        treeView.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Create the XML editor pane.
        this.xmlPane = new XmlTextPane();
        this.xmlPane.addCaretListener(this);

        final AutoCompleter autoCompleter = new AutoCompleter(this.xmlPane);
        this.xmlPane.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                        autoCompleter.autoComplete();
                    }
                }
            }
        });

        xmlPane.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    xmlPane.setLastClickPoint(e.getPoint());
                    ide.getEditorPopupMenu().show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        });

        this.configDocument = loadConfigDocument(name);

        this.xmlEditorScrollPane = new XmlEditorScrollPane(this.xmlPane, this.ide.getSettings().isShowLineNumbersByDefault());

        this.propertiesGrid = new PropertiesGrid(this);
        JScrollPane propertiesView = new JScrollPane(propertiesGrid);
        propertiesView.setBorder(new EmptyBorder(0, 0, 0, 0));
        propertiesView.getViewport().setBackground(Color.white);
        this.leftView = new ProportionalSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.leftView.setResizeWeight(0.8d);
        this.leftView.setBorder(null);
        this.leftView.setTopComponent(treeView);
        this.leftView.setBottomComponent(propertiesView);
        this.leftView.setDividerLocation(0.8d);
        this.leftView.setDividerSize(WHConstants.SPLITTER_WIDTH);

        //Add the scroll panes to a split pane.
        leftSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplitter.setBorder(null);
        leftSplitter.setLeftComponent(leftView);
        leftSplitter.setRightComponent(this.xmlEditorScrollPane);
        leftSplitter.setDividerSize(WHConstants.SPLITTER_WIDTH);

        leftSplitter.setDividerLocation(250);

        prepareLogArea();

        bottomSplitter = new ProportionalSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplitter.setResizeWeight(1.0d);
        bottomSplitter.setBorder(null);
        bottomSplitter.setTopComponent(leftSplitter);
        bottomSplitter.setDividerSize(WHConstants.SPLITTER_WIDTH);
        bottomView = new JScrollPane(logTextArea);
        bottomView.setBorder(new EmptyBorder(0, 0, 0, 0));
        bottomSplitter.setBottomComponent(this.bottomView);
        bottomSplitter.setDividerLocation(0.8d);


        this.add(bottomSplitter, BorderLayout.CENTER);

        if (!ide.getSettings().isShowHierarchyByDefault()) {
            showHierarchy();
        }

        if (!ide.getSettings().isShowLogByDefault()) {
            showLog();
        }

        updateControls();
    }

    private Harvest createHarvest() {
        // FIXME rbala although temporary solution it is duplicated (CommandLine)
        this.harvest = Guice.createInjector(Modules.override(
                        new ScraperModule(ide.getSettings().getWorkingPath())).
                            with(new GuiModule()),
                    new HttpModule(loadProxySettings()))
                .getInstance(Harvest.class);
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ScraperExecutionStartEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ScraperExecutionStartEvent event) {
                ConfigPanel.this.scraperState = ScraperState.RUNNING;
                ConfigPanel.this.onExecutionStart();
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ScraperExecutionPausedEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ScraperExecutionPausedEvent event) {
                ConfigPanel.this.scraperState = ScraperState.PAUSED;
                ConfigPanel.this.onExecutionPaused();
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ScraperExecutionContinuedEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ScraperExecutionContinuedEvent event) {
                ConfigPanel.this.scraperState = ScraperState.RUNNING;
                ConfigPanel.this.onExecutionContinued();
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ScraperExecutionEndEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ScraperExecutionEndEvent event) {
                //TODO This condition has been moved from Scraper class, but
                //finally it should not be here.
                if (ConfigPanel.this.scraperState == ScraperState.RUNNING) {
                    ConfigPanel.this.scraperState = ScraperState.FINISHED;
                    ConfigPanel.this.onExecutionFinished();
                }
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ScraperExecutionStoppedEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ScraperExecutionStoppedEvent event) {
                ConfigPanel.this.scraperState = ScraperState.STOPPED;
                ConfigPanel.this.onExecutionStopped();
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ScraperExecutionExitEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ScraperExecutionExitEvent event) {
                ConfigPanel.this.scraperState = ScraperState.EXIT;
                ConfigPanel.this.onExecutionExit(event.getMessage());
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ScraperExecutionErrorEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ScraperExecutionErrorEvent event) {
                ConfigPanel.this.scraperState = ScraperState.ERROR;
                ConfigPanel.this.onExecutionError(event.getException());
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ProcessorStartEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ProcessorStartEvent event) {
                ConfigPanel.this.onNewProcessorExecution(event.getProcessor());
            }

        });
        // TODO rbala Possibly bind with Guice when finally created Swing module
        harvest.addEventHandler(new EventHandler<ProcessorStopEvent>() {

            @Override
            @Subscribe
            // TODO rbala Get rid of @Subscribe annotation
            public void handle(final ProcessorStopEvent event) {
                ConfigPanel.this.onProcessorExecutionFinished(
                        event.getProcessor(), event.getProperties());
            }
        });

        return harvest;
    }

    private ConfigDocument loadConfigDocument(String name) {
        // creates document for this configuration panel
        final ConfigDocument configDocument = new ConfigDocument(this, name);

        // initialize document content
        try {
            configDocument.load(BASIC_CONFIG_SKELETON);
        } catch (IOException e) {
            GuiUtils.showErrorMessage(e.getMessage());
        }
        return configDocument;
    }

    private void prepareLogArea() {
        logTextArea = new JTextArea();
        logTextArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        logTextArea.setEditable(false);

        // defines pop menu for the log area
        final JPopupMenu logPopupMenu = new WHPopupMenu();

        logSelectAllMenuItem = new MenuElements.MenuItem("Select All");
        logSelectAllMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.requestFocus();
                logTextArea.selectAll();
            }
        });
        logPopupMenu.add(logSelectAllMenuItem);

        logClearAllMenuItem = new MenuElements.MenuItem("Clear All");
        logClearAllMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.setText("");
            }
        });
        logPopupMenu.add(logClearAllMenuItem);

        logTextArea.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    String text = logTextArea.getText();
                    logClearAllMenuItem.setEnabled(text != null && !"".equals(text));
                    logSelectAllMenuItem.setEnabled(text != null && !"".equals(text));
                    logPopupMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Occures whenever caret position is changed inside editor
     *
     * @param e
     */
    public void caretUpdate(CaretEvent e) {
        ide.updateGUI();
    }

    private void updateControls() {
        boolean viewAllowed = false;

        if (this.harvester != null) {
            viewAllowed = (this.scraperState != null) && (this.scraperState != ScraperState.READY);
        }

        this.textViewMenuItem.setEnabled(viewAllowed);
        this.xmlViewMenuItem.setEnabled(viewAllowed);
        this.htmlViewMenuItem.setEnabled(viewAllowed);
        this.imageViewMenuItem.setEnabled(viewAllowed);
        this.listViewMenuItem.setEnabled(viewAllowed);
    }

    /**
     * Required by TreeSelectionListener interface.
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        Object userObject = node.getUserObject();
        if (userObject instanceof TreeNodeInfo) {
            this.selectedNodeInfo = (TreeNodeInfo) userObject;
            PropertiesGridModel model = this.propertiesGrid.getPropertiesGridModel();
            if (model != null) {
                model.setProperties(this.selectedNodeInfo.getProperties(), this.selectedNodeInfo);
            }
        }
    }

    /**
     * Recursively traverses the configuration and creates visual tree representation.
     *
     * @param root
     * @param defs
     */
    private void createNodes(DefaultMutableTreeNode root, IElementDef[] defs) {
        if (defs != null) {
            for (IElementDef elementDef : defs) {
                // constant text is not interesting to be in the visual tree
                if (!(elementDef instanceof ConstantDef)) {
                    TreeNodeInfo treeNodeInfo = new TreeNodeInfo(elementDef);
                    this.nodeInfos.put(treeNodeInfo.getElementDef().getLineNumber(), treeNodeInfo);
                    DefaultMutableTreeNode node = treeNodeInfo.getNode();
                    this.treeModel.insertNodeInto(node, root, root.getChildCount());
                    createNodes(node, elementDef.getOperationDefs());
                }
            }
        }
    }

    /**
     * Loads configuration from the specified source.
     *
     * @param source CAn be instance of File, URL or String
     */
    public void loadConfig(Object source) {
        try {
            if (source instanceof URL) {
                this.configDocument.load((URL) source);
            } else if (source instanceof File) {
                this.configDocument.load((File) source);
            } else {
                this.configDocument.load(source == null ? "" : source.toString());
            }

            refreshTree();
        } catch (IOException e) {
            GuiUtils.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Refreshes tree view.
     *
     * @return
     */
    public boolean refreshTree() {
        xmlPane.clearMarkerLine();
        xmlPane.clearErrorLine();
        xmlPane.clearStopDebugLine();
        updateControls();

        try {
            // FIXME rbala although temporary solution it is duplicated (CommandLine)
            loadHarvester(configDocument.getConfigSource());
            ide.setTabIcon(this, null);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            ide.setTabIcon(this, ResourceManager.SMALL_ERROR_ICON);
            GuiUtils.showErrorMessage(e.getMessage());
            return false;
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
            ide.setTabIcon(this, ResourceManager.SMALL_ERROR_ICON);
            GuiUtils.showErrorMessage(e.getMessage());
            return false;
        }

        return true;
    }

    private void loadHarvester(final ConfigSource config) throws IOException {


        this.harvester = createHarvest().getHarvester(config,
                new HarvestLoadCallback() {

            @Override
            public void onSuccess(final List<IElementDef> elements) {
                IElementDef[] defs = new IElementDef[elements.size()];
                Iterator<IElementDef> it = elements.iterator();
                int index = 0;
                while (it.hasNext()) {
                    defs[index++] = it.next();
                }

                ConfigPanel.this.topNode.removeAllChildren();
                ConfigPanel.this.nodeInfos.clear();
                createNodes(ConfigPanel.this.topNode, defs);
                ConfigPanel.this.treeModel.reload();
                expandTree();

            }

        });
    }

    /**
     * Expands whole tree.
     */
    private void expandTree() {
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
    }

    private void onNewProcessorExecution(Processor processor) {
        final IElementDef elementDef = processor.getElementDef();
        if (elementDef != null) {
            TreeNodeInfo nodeInfo = this.nodeInfos.get(elementDef.getLineNumber());
            if (nodeInfo != null) {
                nodeInfo.increaseExecutionCount();
                //
                //  Fri Feb 22 17:28:26 2013 -- Scott R. Turner
                //
                //  This prevents the node tree from updating if DynamicConfigLocate
                //  is turned off
                //
                if (ide.getSettings().isDynamicConfigLocate()) {
                    setExecutingNode(nodeInfo);
                };
                int lineNumber = locateInSource(nodeInfo.getNode(), true) - 1;
                if (xmlPane.getBreakpoints().isThereBreakpoint(lineNumber)) {

                    harvest.postEvent(new ScraperExecutionPausedEvent(harvester));

                    xmlPane.clearMarkerLine();
                    xmlPane.setStopDebugLine(lineNumber);
                    ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_BREAKPOINT_ICON);
                } else if (ide.getSettings().isDynamicConfigLocate()) {
                    xmlPane.setMarkerLine(lineNumber);
                    xmlPane.repaint();
                }
            }
        }
    }

    private void onExecutionStart() {
        xmlPane.clearStopDebugLine();
        xmlPane.clearErrorLine();
        xmlPane.clearMarkerLine();
        updateControls();
        this.ide.updateGUI();
    }

    private void onExecutionContinued() {
        xmlPane.clearStopDebugLine();
        xmlPane.clearErrorLine();
        xmlPane.clearMarkerLine();
        this.ide.updateGUI();
    }

    private void onExecutionPaused() {
        this.ide.updateGUI();
    }

    /**
     * Helper method invoking when execution has finished.
     */
    private void onExecutionFinished() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_FINISHED_ICON);
                if (ide.getSettings().isShowFinishDialog()) {
                    GuiUtils.showInfoMessage("Configuration \"" + configDocument.getName() + "\" finished execution.");
                }
            }
        });

        onExecutionEnd();
    }

    /**
     * Helper method invoking when execution has been stopped.
     */
    private void onExecutionStopped() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GuiUtils.showWarningMessage("Configuration \"" + configDocument.getName() + "\" aborted by user!");
                ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_FINISHED_ICON);
            }
        });

        onExecutionEnd();
    }

    /**
     * Helper method invoking when execution has finished with error.
     *
     * @param message
     *            cause of execution's exit
     */
    private void onExecutionExit(final String message) {
        if (message != null && !"".equals(message.trim())) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GuiUtils.showWarningMessage("Configuration exited: " + message);
                    ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_FINISHED_ICON);
                }
            });
        }

        onExecutionEnd();
    }

    private void onExecutionEnd() {
        final Settings settings = ide.getSettings();
        if (settings.isDynamicConfigLocate()) {
            this.xmlPane.setEditable(true);
        }

        // refresh last executing node
        TreeNodeInfo previousNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        setExecutingNode(null);
        if (previousNodeInfo != null) {
            this.treeModel.nodeChanged(previousNodeInfo.getNode());
        }

        xmlPane.clearMarkerLine();
        xmlPane.clearStopDebugLine();

        // update GUI controls
        this.ide.updateGUI();

        // releases scraper in order help garbage collector
//        releaseScraper();
    }


    private void onProcessorExecutionFinished(Processor processor, Map properties) {
        final IElementDef elementDef = processor.getElementDef();
        if (elementDef != null) {
            TreeNodeInfo nodeInfo = this.nodeInfos.get(elementDef.getLineNumber());
            if (nodeInfo != null) {
                nodeInfo.setProperties(properties);
                if (nodeInfo == this.selectedNodeInfo) {
                    PropertiesGridModel model = this.propertiesGrid.getPropertiesGridModel();
                    if (model != null) {
                        model.setProperties(nodeInfo.getProperties(), nodeInfo);
                    }
                }
            }
        }
    }

    private void onExecutionError(Exception e) {
        final Settings settings = ide.getSettings();
        if (settings.isDynamicConfigLocate()) {
            this.xmlPane.setEditable(true);
        }

        markException(e);
        String errorMessage = e.getMessage();

        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));

        LOG.error(errorMessage + "\n" + writer.getBuffer().toString());

        if (settings.isShowFinishDialog()) {
            GuiUtils.showErrorMessage(errorMessage);
        }

        this.ide.setTabIcon(this, ResourceManager.SMALL_ERROR_ICON);
        this.ide.updateGUI();

        xmlPane.clearMarkerLine();
        xmlPane.clearStopDebugLine();

        // releases scraper in order to help garbage collector
//        releaseScraper();
    }

    private void setExecutingNode(TreeNodeInfo nodeInfo) {
        TreeNodeInfo previousNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        if (previousNodeInfo != null) {
            this.treeModel.nodeChanged(previousNodeInfo.getNode());
        }
        this.nodeRenderer.setExecutingNodeInfo(nodeInfo);
        if (nodeInfo != null) {
            this.treeModel.nodeChanged(nodeInfo.getNode());
        }
    }

    public void markException(Exception e) {
        this.nodeRenderer.markException(e);
        TreeNodeInfo treeNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        if (treeNodeInfo != null) {
            this.treeModel.nodeChanged(treeNodeInfo.getNode());
            int line = locateInSource(treeNodeInfo.getNode(), true) - 1;
            xmlPane.setErrorLine(line);
        }
    }

    public void runConfiguration() {
        if ((harvester != null) && this.scraperState == ScraperState.PAUSED) {
            harvest.postEvent(new ScraperExecutionContinuedEvent(this.harvester));

            ide.setTabIcon(this, ResourceManager.SMALL_RUN_ICON);
        } else if ((this.harvester == null) || this.scraperState != ScraperState.RUNNING) {
            boolean ok = refreshTree();
            if (ok) {


                final Harvester.ContextInitCallback callback =
                        new Harvester.ContextInitCallback() {

                    @Override
                    public void onSuccess(final DynamicScopeContext context) {
                        context.setLocalVar(initParams);
                        //FIXME mczapiewski This is a dirty way to get reference
                        //to the context. It is required to instantiate
                        //ViewerFrame until it is not well-designed.
                        ConfigPanel.this.scraperContext = context;
                    }
                };

                this.logTextArea.setText(null);

                ide.setTabIcon(this, ResourceManager.SMALL_RUN_ICON);

                // starts scrapping in separate thread
                new ScraperExecutionThread(this.harvester, callback, this.logTextArea).start();
            }
        }
    }

    private ProxySettings loadProxySettings() {
        final Settings settings = ide.getSettings();

        if (!settings.isProxyEnabled()) {
            return ProxySettings.NO_PROXY_SET;
        }

        final String proxyServer = settings.getProxyServer();
        int proxyPort = settings.getProxyPort();

        final ProxySettings.Builder proxySettingsBuilder =
            new ProxySettings.Builder(proxyServer);
        if (proxyPort > 0) {
            proxySettingsBuilder.setProxyPort(proxyPort);
        }

        if (settings.isProxyAuthEnabled()) {
            final String ntlmHost = settings.isNtlmAuthEnabled()
                ? settings.getNtlmHost() : null;
            final String ntlmDomain = settings.isNtlmAuthEnabled()
                ? settings.getNtlmDomain() : null;

            proxySettingsBuilder.setProxyCredentialsUsername(
                    settings.getProxyUserename());
            proxySettingsBuilder.setProxyCredentialsPassword(
                    settings.getProxyPassword());
            proxySettingsBuilder.setProxyCredentialsNTHost(ntlmHost);
            proxySettingsBuilder.setProxyCredentialsNTDomain(ntlmDomain);
        }

        return proxySettingsBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    public DynamicScopeContext getContext() {
        return scraperContext;
    }

    public synchronized ScraperState getScraperStatus() {
        return scraperState;
    }

    public Ide getIde() {
        return ide;
    }

    public synchronized void stopScraperExecution() {
        if (this.harvester != null) {
            this.harvest.postEvent(new ScraperExecutionStoppedEvent(harvester));
        }
    }

    public synchronized void pauseScraperExecution() {
        if (this.harvester != null) {
            harvest.postEvent(new ScraperExecutionPausedEvent(harvester));
            ide.setTabIcon(this, ResourceManager.SMALL_PAUSED_ICON);
        }
    }

    private int locateInSource(DefaultMutableTreeNode treeNode, boolean locateAtLineBeginning) {
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof TreeNodeInfo) {
                TreeNodeInfo treeNodeInfo = (TreeNodeInfo) userObject;
                IElementDef elementDef =  treeNodeInfo.getElementDef();
                int lineNumber = elementDef.getLineNumber();
                int columnNumber = elementDef.getColumnNumber();

                try {
                    String content = this.xmlPane.getDocument().getText(0, this.xmlPane.getDocument().getLength());
                    String[] lines = content.split("\n");
                    int offset = 0;
                    int lineCount = 1;
                    for (String line : lines) {
                        if (lineCount == lineNumber) {
                            offset += locateAtLineBeginning ? 1 : columnNumber;
                            break;
                        }
                        lineCount++;
                        if (lineCount > 2) {
                            offset++;
                        }
                        offset += line.length();
                    }

                    if (offset < content.length()) {
                        content = content.substring(0, offset);
                    }

                    int startIndex = content.lastIndexOf('<');

                    //
                    //  Fri Feb 22 17:29:19 2013 -- Scott R. Turner
                    //
                    //  Setting the caret forces the xmlPane to update, so this needs to
                    //  be prevented when DynamicConfigLocate is off.
                    //
                    if (ide.getSettings().isDynamicConfigLocate()) {
                        this.xmlPane.setCaretPosition(startIndex >= 0 ? startIndex : 0);
                    };
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    //todo: swallow exception?
                }

                return lineNumber;
            }
        }

        return -1;
    }

    public void undo() {
        this.xmlPane.undo();
    }

    public void redo() {
        this.xmlPane.redo();
    }

    public String getXml() {
        return this.xmlPane.getText();
    }

    public XmlTextPane getXmlPane() {
        return xmlPane;
    }

    public XmlEditorScrollPane getXmlEditorScrollPane() {
        return xmlEditorScrollPane;
    }

    public ConfigDocument getConfigDocument() {
        return configDocument;
    }

    public void showHierarchy() {
        boolean isVisible = this.leftView.isVisible();
        if (isVisible) {
            this.leftDividerLocation = this.leftSplitter.getDividerLocation();
        }
        this.leftView.setVisible(!isVisible);
        if (!isVisible) {
            this.leftSplitter.setDividerLocation(this.leftDividerLocation);
        }
    }

    public void showLog() {
        boolean isVisible = this.bottomView.isVisible();
        if (isVisible) {
            this.bottomDividerLocation = this.bottomSplitter.getDividerLocation();
        }
        this.bottomView.setVisible(!isVisible);
        if (!isVisible) {
            this.bottomSplitter.setDividerLocation(this.bottomDividerLocation);
        }
    }

    public boolean isHierarchyVisible() {
        return this.leftView.isVisible();
    }

    public boolean isLogVisible() {
        return this.bottomView.isVisible();
    }

    public Map getInitParams() {
        return initParams;
    }

    public void setInitParams(Map initParams) {
        this.initParams = initParams;
    }

    public void dispose() {
        if (this.configDocument != null) {
            this.configDocument.dispose();
        }
        if (this.harvester != null) {
            this.harvester = null;
        }

        this.xmlPane.removeCaretListener(this);
        this.tree.removeTreeSelectionListener(this);

        this.ide = null;
        this.tree = null;
        this.treeModel = null;
        this.nodeInfos = null;
        this.nodeRenderer = null;
        this.configDocument = null;
        this.topNode = null;
    }

}