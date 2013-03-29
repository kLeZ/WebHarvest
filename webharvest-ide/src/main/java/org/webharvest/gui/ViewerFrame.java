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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.saxon.trans.XPathException;

import org.webharvest.WHConstants;
import org.webharvest.exception.ScriptException;
import org.webharvest.gui.component.CommonButton;
import org.webharvest.gui.component.DropDownButton;
import org.webharvest.gui.component.DropDownButtonListener;
import org.webharvest.gui.component.EditableComboBox;
import org.webharvest.gui.component.MenuElements;
import org.webharvest.gui.component.ProportionalSplitPane;
import org.webharvest.gui.component.SmallButton;
import org.webharvest.gui.component.WHCheckBox;
import org.webharvest.gui.component.WHScrollPane;
import org.webharvest.runtime.ContextHolder;
import org.webharvest.runtime.DynamicScopeContext;
import org.webharvest.runtime.RuntimeConfig;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.WebScraper;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.ListVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.KeyValuePair;
import org.webharvest.utils.XmlUtil;
import org.webharvest.utils.XmlValidator;
import org.xml.sax.InputSource;

/**
 * @author Vladimir Nikic
 *         Date: May 8, 2007
 */
public class ViewerFrame extends JFrame implements DropDownButtonListener, ActionListener {

    private static final int FIND_OPEN = 0;
    private static final int FIND_NEXT = 1;
    private static final int FIND_PREVIOUS = 2;

    public static final int TEXT_VIEW = 0;
    public static final int XML_VIEW = 1;
    public static final int HTML_VIEW = 2;
    public static final int IMAGE_VIEW = 3;
    public static final int LIST_VIEW = 4;

    public static final String MSG_NO_RESULT = "No results";
    public static final String HTML_HEADER = "<html><head></head><body style='font-family:Tahoma,Verdana;font-size:11pt;'>";
    public static final String HTML_FOOTER = "</body></html>";

    private abstract class MyAction extends AbstractAction {

        public MyAction(String text, Icon icon, String desc, KeyStroke keyStroke) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(ACCELERATOR_KEY, keyStroke);
        }
    }

    // find dialog
    private FindReplaceDialog findDialog;

    // array of flags indicating if specific view is refreshed with new value
    private boolean refreshed[] = new boolean[5];

    // name of the property being viewed
    private String propertyName;

    // value that should be displayed in this viewer
    private Object value;

    // node info for which this view is connected
    private TreeNodeInfo nodeInfo;

    // index of currently open view
    private int currentView = TEXT_VIEW;

    // display components
    private JPanel cardPanel;
    private JTextArea textArea;
    private JEditorPane htmlPane;
    private XmlTextPane xmlPane;
    private JPanel imagePanel;
    private JLabel imageLabel;
    private int zoomFactor = 100;
    private JLabel zoomFactorLabel;
    private JTable listTable;
    private JCheckBox wrapTextCheckBox;
    private JButton findButton;
    private JButton zoomInButton;
    private JButton zoomOutButton;
    private JButton xmlValidateButton;
    private JButton xmlPrettyPrintButton;

    // runtime configuration is required for XPath evaluation
    private RuntimeConfig runtimeConfig = new RuntimeConfig();
    private JEditorPane xpathResultPane;

    /**
     * Constructor.
     *
     * @param contextHolder
     * @param propertyName
     * @param value
     * @param nodeInfo
     */
    public ViewerFrame(final ContextHolder contextHolder, final String propertyName, final Object value, final TreeNodeInfo nodeInfo, final int viewIndex) {
        if (propertyName == null) {
            setTitle("Runtime Value Viewer");
        } else {
            String elementName = nodeInfo.getElementDef().getShortElementName();
            setTitle(elementName + "->" + propertyName);
        }

        this.setIconImage(((ImageIcon) ResourceManager.VIEW_ICON).getImage());

        this.propertyName = propertyName;
        this.value = value;
        this.nodeInfo = nodeInfo;

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                ViewerFrame.this.nodeInfo = null;
                ViewerFrame.this.value = null;
                dispose();
            }
        });

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 0));
        toolBar.setFloatable(false);

        if (contextHolder != null && contextHolder.getContext() != null) {
            final DynamicScopeContext context = contextHolder.getContext();

            if (propertyName == null) {
                final EditableComboBox variablesComboBox = new EditableComboBox(20) {
                    protected void execute(final Object value) {
                        if (value != null) {
                            try {
                                ViewerFrame.this.value = BaseTemplater.evaluateToString("${" + value + "}", null, context);
                            } catch (ScriptException e) {
                                ViewerFrame.this.value = "Error evaluating \"" + value + "\"!";
                            }
                            for (int i = 0; i < refreshed.length; i++) {
                                refreshed[i] = false;
                            }
                            refresh(currentView);
                        }
                    }
                };
                for (KeyValuePair<Variable> pair : context) {
                    variablesComboBox.addItem(pair.getKey());
                }
                SmallButton goButton = new SmallButton("Eval");
                goButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        variablesComboBox.onValue();
                    }
                });
                variablesComboBox.getEditor().setItem("");
                toolBar.add(new JLabel("Expression:"));
                toolBar.add(variablesComboBox);
                toolBar.add(goButton);
                toolBar.addSeparator(new Dimension(10, 0));
            }

        }

        DropDownButton viewTypeButton = new DropDownButton();
        viewTypeButton.addMenuItem(new MenuElements.MenuItem("Text    ", ResourceManager.TEXTTYPE_ICON));
        viewTypeButton.addMenuItem(new MenuElements.MenuItem("XML    ", ResourceManager.XMLTYPE_ICON));
        viewTypeButton.addMenuItem(new MenuElements.MenuItem("HTML    ", ResourceManager.HTMLTYPE_ICON));
        viewTypeButton.addMenuItem(new MenuElements.MenuItem("Image    ", ResourceManager.IMAGETYPE_ICON));
        viewTypeButton.addMenuItem(new MenuElements.MenuItem("List    ", ResourceManager.LISTTYPE_ICON));
        viewTypeButton.changeSelectedTo(viewIndex);
        viewTypeButton.addListener(this);
        toolBar.add(new JLabel(" View as: "));
        toolBar.add(viewTypeButton);

        final MyAction findTextAction = new MyAction("Find", ResourceManager.FIND_ICON, "Find text", KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK)) {
            public void actionPerformed(ActionEvent e) {
                findText(FIND_OPEN);
            }
        };

        final MyAction findNextAction = new MyAction("Find Next", null, "Find next occurence", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)) {
            public void actionPerformed(ActionEvent e) {
                findText(FIND_NEXT);
            }
        };

        final MyAction findPrevAction = new MyAction("Find Previous", null, "Find previous occurence", KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK)) {
            public void actionPerformed(ActionEvent e) {
                findText(FIND_PREVIOUS);
            }
        };

        final MyAction zoomInAction = new MyAction("Zoom In", ResourceManager.ZOOMIN_ICON, "Zoom Image In", KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0)) {
            public void actionPerformed(ActionEvent e) {
                zoom(true);
            }
        };

        final MyAction zoomOutAction = new MyAction("Zoom Out", ResourceManager.ZOOMOUT_ICON, "Zoom Image Out", KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0)) {
            public void actionPerformed(ActionEvent e) {
                zoom(false);
            }
        };

        this.findButton = new CommonButton(findTextAction);
        this.findButton.registerKeyboardAction(findTextAction, KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.findButton.registerKeyboardAction(findNextAction, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        this.findButton.registerKeyboardAction(findPrevAction, KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.SHIFT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        this.wrapTextCheckBox = new WHCheckBox("Wrap Text");
        this.wrapTextCheckBox.addActionListener(this);
        toolBar.add(this.wrapTextCheckBox);

        this.zoomInButton = new CommonButton(zoomInAction);
        this.zoomInButton.registerKeyboardAction(zoomInAction, KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        this.zoomOutButton = new CommonButton(zoomOutAction);
        this.zoomOutButton.registerKeyboardAction(zoomOutAction, KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        this.zoomFactorLabel = new JLabel();

        this.xmlValidateButton = new CommonButton("Validate", ResourceManager.VALIDATE_ICON);
        this.xmlValidateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validateXml(true);
            }
        });

        this.xmlPrettyPrintButton = new CommonButton("Pretty-print", ResourceManager.PRETTY_PRINT_ICON);
        this.xmlPrettyPrintButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                prettyPrintXml();
            }
        });

        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(this.findButton);
        toolBar.add(this.xmlValidateButton);
        toolBar.add(this.xmlPrettyPrintButton);
        toolBar.add(this.zoomInButton);
        toolBar.add(this.zoomOutButton);
        toolBar.add(this.zoomFactorLabel);

        contentPane.add(toolBar, BorderLayout.NORTH);

        this.cardPanel = new JPanel(new CardLayout());

        // text view
        this.textArea = new JTextArea();
        this.textArea.setEditable(false);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        this.cardPanel.add(new WHScrollPane(this.textArea), String.valueOf(TEXT_VIEW));

        // XML view
        this.xmlPane = new XmlTextPane();
        this.xmlPane.setEditable(false);

        JPanel xpathPanel = new JPanel(new BorderLayout());
        JToolBar xpathToolbar = new JToolBar();
        final JComboBox xpathComboBox = new EditableComboBox(50) {
            protected void execute(Object value) {
                evaluateXPath((String) getEditor().getItem());
            }
        };

        final JButton xpathEvalButton = new SmallButton("Evaluate");

        xpathEvalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String expression = (String) xpathComboBox.getSelectedItem();
                if (!CommonUtil.isEmptyString(expression)) {
                    xpathComboBox.removeItem(expression);
                    xpathComboBox.insertItemAt(expression, 0);
                    xpathComboBox.setSelectedItem(expression);
                    evaluateXPath(expression);
                }
            }
        });
        xpathToolbar.setFloatable(false);
        xpathToolbar.add(new JLabel(" XPath expression: "));
        xpathToolbar.add(xpathComboBox);
        xpathToolbar.add(xpathEvalButton);

        this.xpathResultPane = new JEditorPane();
        this.xpathResultPane.setEditable(false);
        this.xpathResultPane.setContentType("text/html");
        this.xpathResultPane.setEditorKit(new HTMLEditorKit());

        xpathPanel.add(xpathToolbar, BorderLayout.NORTH);
        xpathPanel.add(new WHScrollPane(this.xpathResultPane), BorderLayout.CENTER);

        JSplitPane splitPane = new ProportionalSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(1.0d);
        splitPane.setBorder(null);
        splitPane.setTopComponent(new XmlEditorScrollPane(xmlPane, false));
        splitPane.setBottomComponent(xpathPanel);
        splitPane.setDividerLocation(0.75d);
        splitPane.setDividerSize(WHConstants.SPLITTER_WIDTH);

        this.cardPanel.add(splitPane, String.valueOf(XML_VIEW));

        // HTML view
        this.htmlPane = new JEditorPane();
        this.htmlPane.setEditable(false);
        this.htmlPane.setContentType("text/html");
        this.htmlPane.setEditorKit(new HTMLEditorKit());
        this.cardPanel.add(new WHScrollPane(this.htmlPane), String.valueOf(HTML_VIEW));

        // image view
        this.imagePanel = new JPanel(new BorderLayout());
        this.imagePanel.setBackground(Color.white);
        this.imageLabel = new JLabel("", JLabel.CENTER);
        this.imagePanel.add(imageLabel, BorderLayout.CENTER);
        this.imageLabel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom(e.getWheelRotation() > 0);
            }
        });
        this.cardPanel.add(new WHScrollPane(this.imagePanel), String.valueOf(IMAGE_VIEW));

        // List view
        this.listTable = new JTable(new DefaultTableModel(0, 2) {
            public boolean isCellEditable(int row, int column) {
                return column == 1;
            }
        });
        this.listTable.setTableHeader(null);
        this.listTable.getColumnModel().getColumn(0).setMaxWidth(30);
        this.listTable.setColumnSelectionAllowed(true);
        this.listTable.setRowSelectionAllowed(true);
        this.listTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane tableScrollPane = new WHScrollPane(this.listTable);
        tableScrollPane.getViewport().setBackground(Color.white);
        this.cardPanel.add(tableScrollPane, String.valueOf(LIST_VIEW));

        openView(viewIndex);

        contentPane.add(cardPanel, BorderLayout.CENTER);

        this.pack();
    }

    private void findText(int type) {
        if (this.currentView == TEXT_VIEW || this.currentView == XML_VIEW) {
            if (this.findDialog == null) {
                this.findDialog = new FindReplaceDialog(this);
            }

            JTextComponent textComponent;
            if (this.currentView == TEXT_VIEW) {
                textComponent = textArea;
            } else {
                textComponent = xmlPane;
            }

            if (type == FIND_NEXT) {
                this.findDialog.findNext(textComponent);
            } else if (type == FIND_PREVIOUS) {
                this.findDialog.findPrev(textComponent);
            } else {
                this.findDialog.open(textComponent, false);
            }
        }
    }

    /**
     * Evaluates XPath expression against XML panel's text
     *
     * @param text XML text to be searched
     */
    private void evaluateXPath(String text) {
        try {
            ListVariable result = XmlUtil.evaluateXPath(text, xmlPane.getText(), runtimeConfig);
            java.util.List resultList = result.toList();
            if (resultList.size() == 0) {
                this.xpathResultPane.setText(
                        new StringBuilder(HTML_HEADER.length() + MSG_NO_RESULT.length() + HTML_FOOTER.length()).
                                append(HTML_HEADER).
                                append(MSG_NO_RESULT).
                                append(HTML_FOOTER).
                                toString());
            } else {
                StringBuilder tableHtml = new StringBuilder(HTML_HEADER);
                tableHtml.append("<table width='100%' cellspacing='0' cellpadding='2'>");
                Iterator iterator = resultList.iterator();
                int index = 0;
                while (iterator.hasNext()) {
                    index++;
                    Object curr = iterator.next();
                    tableHtml.append("<tr style='background:");
                    tableHtml.append(index % 2 == 0 ? "#E1E1E1" : "#EEEEEE");
                    tableHtml.append("'><td align='left' width='30'>");
                    tableHtml.append(index);
                    tableHtml.append(".&nbsp;</td><td align='left' nowrap>");
                    tableHtml.append(CommonUtil.escapeXml(curr.toString()));
                    tableHtml.append("</td></tr>");
                }
                tableHtml.append("</table>");
                tableHtml.append(HTML_FOOTER);
                this.xpathResultPane.setText(tableHtml.toString());
                this.xpathResultPane.setCaretPosition(0);
            }
        } catch (XPathException e) {
            String html = new StringBuilder().
                    append(HTML_HEADER).
                    append("<div style='color:#800000'>").
                    append(e.getMessage()).
                    append("</div>").
                    append(HTML_FOOTER).
                    toString();
            this.xpathResultPane.setText(html);
        }
    }

    private void prettyPrintXml() {
        boolean valid = validateXml(false);
        if (valid) {
            String xmlText = this.xmlPane.getText();
            try {
                String prettyXml = XmlUtil.prettyPrintXml(xmlText);
                this.xmlPane.setText(prettyXml);
                this.xmlPane.setCaretPosition(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "parsing error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateXml(boolean showOkMessage) {
        XmlValidator validator = new XmlValidator();
        String s = value == null ? "" : value.toString();
        boolean valid = validator.parse(new InputSource(new StringReader(s)));
        if (valid) {
            if (showOkMessage) {
                GuiUtils.showInfoMessage("XML is well-formed.");
            }
        } else {
            String msg = "XML is not well-formed: " + validator.getException().getMessage() +
                    " [line: " + validator.getLineNumber() + ", col: " + validator.getColumnNumber() + "].";
            GuiUtils.showErrorMessage(msg);
        }

        return valid;
    }

    private void refresh(int viewIndex) {
        switch (viewIndex) {
            case TEXT_VIEW:
                if (!this.refreshed[TEXT_VIEW]) {
                    this.textArea.setText(value == null ? null : value.toString());
                    this.textArea.setCaretPosition(0);
                }
                break;
            case XML_VIEW:
                if (!this.refreshed[XML_VIEW]) {
                    this.xmlPane.setText(value == null ? null : value.toString());

                }
                this.xmlPane.setCaretPosition(0);
                break;
            case HTML_VIEW:
                if (!this.refreshed[HTML_VIEW]) {
                    if (value != null) {
                        // Workaround for BUG 4695909: parse the HTML String and remove everything in the HEAD section before calling setText(String).
                        Pattern pattern = Pattern.compile("<head(.)*</head>", Pattern.DOTALL | Pattern.UNICODE_CASE);
                        Matcher matcher = pattern.matcher(value.toString());
                        String htmlText = matcher.replaceFirst("");

                        this.htmlPane.setText(htmlText);
                    } else {
                        this.htmlPane.setText(null);
                    }
                    htmlPane.setCaretPosition(0);
                }
                break;
            case IMAGE_VIEW:
                if (!this.refreshed[IMAGE_VIEW]) {
                    if (value instanceof Variable) {
                        this.imageLabel.setIcon(createImgIcon());
                    } else {
                        this.imageLabel.setIcon(null);
                    }
                    this.zoomFactorLabel.setText("  Size: " + zoomFactor + "%");
                    this.imageLabel.repaint();
                    this.imagePanel.getParent().validate();
                }
                break;
            case LIST_VIEW:
                if (!this.refreshed[LIST_VIEW]) {
                    DefaultTableModel model = (DefaultTableModel) listTable.getModel();

                    int rowCount = model.getRowCount();
                    for (int i = rowCount - 1; i >= 0; i--) {
                        model.removeRow(i);
                    }

                    if (value instanceof Variable) {
                        Variable var = (Variable) value;
                        java.util.List list = var.toList();
                        for (int i = 0; i < list.size(); i++) {
                            Object curr = list.get(i);
                            String stringValue = curr == null ? "" : curr.toString();
                            model.addRow(new String[]{String.valueOf(i + 1), stringValue});
                        }
                    } else {
                        model.addRow(new String[]{"1", value == null ? "" : value.toString()});
                    }
                }
                break;
        }

        if (viewIndex >= 0 && viewIndex < this.refreshed.length) {
            this.refreshed[viewIndex] = true;
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(640, 480);
    }

    private void updateControls() {
        this.findButton.setVisible(this.currentView == TEXT_VIEW || this.currentView == XML_VIEW);
        this.xmlValidateButton.setVisible(this.currentView == XML_VIEW);
        this.xmlPrettyPrintButton.setVisible(this.currentView == XML_VIEW);
        this.zoomInButton.setVisible(this.currentView == IMAGE_VIEW);
        this.zoomOutButton.setVisible(this.currentView == IMAGE_VIEW);
        this.zoomFactorLabel.setVisible(this.currentView == IMAGE_VIEW);
        this.wrapTextCheckBox.setVisible(this.currentView == TEXT_VIEW);
    }

    private void openView(int viewIndex) {
        CardLayout cardLayout = (CardLayout) (this.cardPanel.getLayout());
        this.currentView = viewIndex;
        refresh(viewIndex);
        cardLayout.show(this.cardPanel, String.valueOf(viewIndex));
        updateControls();
    }

    /**
     * When button changes
     *
     * @param dropDownButton
     */
    public void onChange(DropDownButton dropDownButton) {
        openView(dropDownButton.getSelectedItem());
    }

    public void setValue(Map properties) {
        if (properties != null && propertyName != null) {
            this.value = properties.get(propertyName);

            // invalidate views
            for (int i = 0; i < refreshed.length; i++) {
                refreshed[i] = false;

            }

            refresh(this.currentView);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == this.wrapTextCheckBox) {
            this.textArea.setLineWrap(this.wrapTextCheckBox.isSelected());
        }
    }

    private void zoom(boolean zoomIn) {
        if (zoomIn && zoomFactor < 500) {
            this.zoomFactor += 20;
        } else if (!zoomIn && zoomFactor > 20) {
            this.zoomFactor -= 20;
        }
        refreshed[currentView] = false;
        refresh(currentView);
    }

    private ImageIcon createImgIcon() {
        Variable var = (Variable) value;
        ImageIcon imageIcon = new ImageIcon(var.toBinary());
        Image img = imageIcon.getImage();

        int newWidth = (int) (img.getWidth(this) * zoomFactor / 100.0);
        int newHeight = (int) (img.getHeight(this) * zoomFactor / 100.0);

        if (newWidth > 0 && newHeight > 0) {
            Image resizedImg = zoomFactor != 100 ? img.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST) : img;
            imageIcon.setImage(resizedImg);
            return imageIcon;
        }

        return null;
    }

    public void setVisible(boolean b) {
        if (b) {
            Frame activeFrame = GuiUtils.getActiveFrame();
            if (activeFrame != null) {
                GuiUtils.centerRelativeTo(this, activeFrame);
            }
        }
        super.setVisible(b);
    }

}