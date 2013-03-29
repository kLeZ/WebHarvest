package org.webharvest.gui;

import org.webharvest.ApplicationInfo;
import org.webharvest.utils.CommonUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

public class AboutWindow extends JWindow implements HyperlinkListener {

    private static final Dimension WINDOW_DIMENSION = new Dimension(350, 320);

    // Ide instance where this dialog belongs.
    private Ide ide;

    public AboutWindow(Ide ide) throws HeadlessException {
        super(ide);
        this.ide = ide;

        createGUI();
    }

    private void createGUI() {
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        JEditorPane htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setContentType("text/html");
        htmlPane.setEditorKit(new HTMLEditorKit());
        htmlPane.setBorder(new LineBorder(Color.black));
        htmlPane.addHyperlinkListener(this);
        htmlPane.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                AboutWindow.this.setVisible(false);
            }
        });
        htmlPane.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                setVisible(false);
            }
        });

        try {
            URL aboutUrl = ResourceManager.getAboutUrl();
            String content = CommonUtil.readStringFromUrl(aboutUrl);
            content = content.replace("#program.version#", ApplicationInfo.WEB_HARVEST_VERSION);
            content = content.replace("#program.date#", ApplicationInfo.WEB_HARVEST_DATE);
            content = content.replace("#java.version#", System.getProperty("java.version"));
            content = content.replace("#java.vendor#", System.getProperty("java.vendor"));
            content = content.replace("#year#", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            ((HTMLDocument) htmlPane.getDocument()).setBase(ResourceManager.getAboutUrl());
            htmlPane.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        contentPane.add(htmlPane, BorderLayout.CENTER);

        this.pack();
    }

    public Dimension getPreferredSize() {
        return WINDOW_DIMENSION;
    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String url = e.getDescription();
            ide.openURLInBrowser(url);
        }
    }

    public void open() {
        setLocationRelativeTo(this.ide);
        setVisible(true);
    }

}
