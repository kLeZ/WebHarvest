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

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.webharvest.ApplicationInfo;
import org.webharvest.gui.component.WHScrollPane;
import org.webharvest.utils.CommonUtil;
import org.webharvest.utils.XmlUtil;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

/**
 * @author Vladimir Nikic
 * Date: Apr 25, 2007
 */
public class WelcomePanel extends JPanel implements HyperlinkListener {

    // parent IDE
    private Ide ide;
    private JEditorPane htmlPane;

    /**
     * Constructor.
     *
     * @param ide
     */
    public WelcomePanel(final Ide ide) {
        this.ide = ide;

        setLayout(new BorderLayout(0, 0));
        htmlPane = new JEditorPane() {
            public void paint(Graphics g) {
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paint(g);
            }
        };
        htmlPane.setEditable(false);
        htmlPane.setContentType("text/html");
        htmlPane.setEditorKit(new HTMLEditorKit());
        htmlPane.setBorder(null);
        htmlPane.addHyperlinkListener(this);

        try {
            URL welcomeUrl = ResourceManager.getWelcomeUrl();
            String content = CommonUtil.readStringFromUrl(welcomeUrl);
            content = content.replaceAll("#program.version#", ApplicationInfo.WEB_HARVEST_VERSION);
            ((HTMLDocument) htmlPane.getDocument()).setBase(ResourceManager.getWelcomeUrl());
            htmlPane.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new WHScrollPane(htmlPane);
        this.add(scrollPane, BorderLayout.CENTER);

        new Thread() {
            public void run() {
                downloadAddition();
            }
        }.start();
    }

    private synchronized void downloadAddition() {
        try {

            final Document welcomeDocument = XmlUtil.parse(
                    new InputSource(new StringReader(CommonUtil.readStringFromUrl(new URL(ApplicationInfo.WELCOME_ADDITION_URL)))));

            final String versionNumber = StringUtils.trimToNull(XmlUtil.<String>evaluateXPath("/welcome/version/@number", welcomeDocument));
            final String versionMessage = StringUtils.trimToNull(XmlUtil.<String>evaluateXPath("/welcome/version/text()", welcomeDocument));
            final String start = StringUtils.trimToNull(XmlUtil.<String>evaluateXPath("/welcome/start/text()", welcomeDocument));
            final String end = StringUtils.trimToNull(XmlUtil.<String>evaluateXPath("/welcome/end/text()", welcomeDocument));

            boolean isThereNewVersion = false;
            if (versionNumber != null && versionMessage != null) {
                try {
                    double currVersion = Double.parseDouble(ApplicationInfo.WEB_HARVEST_VERSION);
                    double serverVersion = Double.parseDouble(versionNumber);
                    isThereNewVersion = serverVersion > currVersion;
                } catch (NumberFormatException e) {
                    isThereNewVersion = false;
                }
            }

            if (start != null || end != null || isThereNewVersion) {
                URL welcomeUrl = ResourceManager.getWelcomeUrl();
                String htmlPaneContent = CommonUtil.readStringFromUrl(welcomeUrl);
                htmlPaneContent = htmlPaneContent.replaceAll("#program.version#", ApplicationInfo.WEB_HARVEST_VERSION);
                if (isThereNewVersion) {
                    htmlPaneContent = htmlPaneContent.replaceAll("<!--version-->", versionMessage);
                }
                if (start != null) {
                    htmlPaneContent = htmlPaneContent.replaceAll("<!--start-->", start);
                }
                if (end != null) {
                    htmlPaneContent = htmlPaneContent.replaceAll("<!--end-->", end);
                }
                ((HTMLDocument) htmlPane.getDocument()).setBase(ResourceManager.getWelcomeUrl());
                htmlPane.setText(htmlPaneContent);
            }
        } catch (IOException e) {
            // do nothing - there is probably no file
            //System.out.println("Error reading welcome addon from the site: " + e.getMessage());
        }

    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String url = e.getDescription();

            if ("#new".equalsIgnoreCase(url)) {
                ide.addTab();
            } else if ("#open".equalsIgnoreCase(url)) {
                ide.openConfigFromFile();
            } else if ("#settings".equalsIgnoreCase(url)) {
                ide.defineSettings();
            } else if (url.toLowerCase().startsWith("download:")) {
                String exampleUrl = url.substring(9);
                ide.openConfigFromUrl(exampleUrl);
            } else {
                ide.openURLInBrowser(url);
            }
        }
    }

}