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

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Vladimir Nikic
 * Date: Apr 19, 2007
 */
public class ResourceManager {

    private static final Class clazz = ResourceManager.class;

    public static final Icon WELCOME_LOGO_ICON = getIcon("resources/welcomelogo.jpg");
    public static final Icon NONE_ICON = getIcon("resources/icons/none.gif");
    public static final Icon WEB_HARVEST_ICON = getIcon("resources/icons/webharvest.gif");
    public static final Icon NEW_ICON = getIcon("resources/icons/new.gif");
    public static final Icon OPEN_ICON = getIcon("resources/icons/open.gif");
    public static final Icon CLOSE_ICON = getIcon("resources/icons/close.gif");
    public static final Icon SAVE_ICON = getIcon("resources/icons/save.gif");
    public static final Icon REFRESH_ICON = getIcon("resources/icons/refresh.gif");
    public static final Icon RUN_ICON = getIcon("resources/icons/run.gif");
    public static final Icon PAUSE_ICON = getIcon("resources/icons/pause.gif");
    public static final Icon STOP_ICON = getIcon("resources/icons/stop.gif");
    public static final Icon VIEWVALUES_ICON = getIcon("resources/icons/viewvalues.gif");
    public static final Icon COPY_ICON = getIcon("resources/icons/copy.gif");
    public static final Icon CUT_ICON = getIcon("resources/icons/cut.gif");
    public static final Icon PASTE_ICON = getIcon("resources/icons/paste.gif");
    public static final Icon UNDO_ICON = getIcon("resources/icons/undo.gif");
    public static final Icon REDO_ICON = getIcon("resources/icons/redo.gif");
    public static final Icon FIND_ICON = getIcon("resources/icons/find.gif");
    public static final Icon SETTINGS_ICON = getIcon("resources/icons/settings.gif");
    public static final Icon RUN_PARAMS_ICON = getIcon("resources/icons/runparams.gif");
    public static final Icon HELP_ICON = getIcon("resources/icons/help.gif");
    public static final Icon HELP32_ICON = getIcon("resources/icons/help32.gif");
    public static final Icon HOMEPAGE_ICON = getIcon("resources/icons/homepage.gif");
    public static final Icon DOWNLOAD_ICON = getIcon("resources/icons/download.gif");
    public static final Icon VIEW_ICON = getIcon("resources/icons/view.gif");
    public static final Icon VALIDATE_ICON = getIcon("resources/icons/validate.gif");
    public static final Icon ZOOMIN_ICON = getIcon("resources/icons/zoomin.gif");
    public static final Icon ZOOMOUT_ICON = getIcon("resources/icons/zoomout.gif");
    public static final Icon PRETTY_PRINT_ICON = getIcon("resources/icons/prettyprint.gif");
    public static final Icon SMALL_RUN_ICON = getIcon("resources/icons/small_run.gif");
    public static final Icon SMALL_ERROR_ICON = getIcon("resources/icons/small_error.gif");
    public static final Icon SMALL_PAUSED_ICON = getIcon("resources/icons/small_paused.gif");
    public static final Icon SMALL_FINISHED_ICON = getIcon("resources/icons/small_finished.gif");
    public static final Icon SMALL_BREAKPOINT_ICON = getIcon("resources/icons/small_breakpoint.gif");
    public static final Icon SMALL_VIEW_ICON = getIcon("resources/icons/small_view.gif");
    public static final Icon SMALL_TRASHCAN_ICON = getIcon("resources/icons/trashcan.gif");
    public static final Icon TEXTTYPE_ICON = getIcon("resources/icons/text_type.gif");
    public static final Icon XMLTYPE_ICON = getIcon("resources/icons/xml_type.gif");
    public static final Icon HTMLTYPE_ICON = getIcon("resources/icons/html_type.gif");
    public static final Icon IMAGETYPE_ICON = getIcon("resources/icons/image_type.gif");
    public static final Icon LISTTYPE_ICON = getIcon("resources/icons/list_type.gif");
    public static final Icon HELPDIR_ICON = getIcon("resources/icons/helpdir.gif");
    public static final Icon HELPTOPIC_ICON = getIcon("resources/icons/helptopic.gif");
    public static final Icon VALID_ICON = getIcon("resources/icons/valid.gif");
    public static final Icon INVALID_ICON = getIcon("resources/icons/invalid.gif");

    // UI icons and images
    public static Icon RADIO_BUTTON_ICON = getIcon("resources/icons/ui/radio_button_unselected.gif");
    public static Icon RADIO_BUTTON_SELECTED_ICON = getIcon("resources/icons/ui/radio_button_selected.gif");
    public static Icon RADIO_BUTTON_EMPTY_ICON = getIcon("resources/icons/ui/radio_button_empty.gif");
    public static Icon CHECKBOX_ICON = getIcon("resources/icons/ui/checkbox_unselected.gif");
    public static Icon CHECKBOX_SELECTED_ICON = getIcon("resources/icons/ui/checkbox_selected.gif");
    public static Icon EXPAND_ICON = getIcon("resources/icons/ui/expand.gif");
    public static Icon COLLAPSE_ICON = getIcon("resources/icons/ui/collapse.gif");
    public static Icon ERROR_ICON = getIcon("resources/icons/ui/error.gif");
    public static Icon WARNING_ICON = getIcon("resources/icons/ui/warning.gif");
    public static Icon INFO_ICON = getIcon("resources/icons/ui/information.gif");
    public static Icon QUESTION_ICON = getIcon("resources/icons/ui/question.gif");
    public static Icon TRASHCAN_ICON = getIcon("resources/icons/ui/trashcan.gif");


    public static Image BREAKPOINT_IMAGE = getImage("resources/icons/breakpoint.gif");

    public static InputStream getResourceAsStream(String path) {
        return clazz.getResourceAsStream(path);
    }

    public static Icon getIcon(String path) {
        return new ImageIcon(clazz.getResource(path));
    }

    public static URL getWelcomeUrl() {
        return clazz.getResource("resources/welcome.html");
    }

    public static URL getAboutUrl() {
        return clazz.getResource("resources/about.html");
    }

    public static URL getHelpContentUrl() {
        return clazz.getResource("resources/help.xml");
    }

    public static URL getHelpFileUrl(String helpId) {
        return clazz.getResource("resources/help/" + helpId + ".html");
    }

    public static Properties getAttrValuesProperties() {
        Properties properties = new Properties();
        try {
            properties.load(ResourceManager.class.getResourceAsStream("resources/attrvalues.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Image getImage(String path) {
        return Toolkit.getDefaultToolkit().getImage(clazz.getResource(path));
    }
}