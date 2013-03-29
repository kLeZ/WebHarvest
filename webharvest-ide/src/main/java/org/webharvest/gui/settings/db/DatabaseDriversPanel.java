/*
 Copyright (c) 2006-2012 the original author or authors.

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
*/

package org.webharvest.gui.settings.db;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.webharvest.gui.component.FixedSizeButton;
import org.webharvest.gui.component.WHList;
import org.webharvest.gui.component.WHScrollPane;

//TODO: prevent against adding the same driver twice!!!
public final class DatabaseDriversPanel extends JPanel
        implements DatabaseDriversView {

    /**
     * Serialized class version identifier.
     */
    private static final long serialVersionUID = -2653116252002534016L;

    private DatabaseDriversView.Presenter presenter;

    private final ControlButtonsPanel controlButtonsPanel;
    private final DriverListPanel driversList;

    public DatabaseDriversPanel() {
        super();

        setUpLayout();

        this.controlButtonsPanel = new ControlButtonsPanel();
        this.driversList = new DriverListPanel();

        add(controlButtonsPanel, BorderLayout.EAST);
        add(driversList, BorderLayout.CENTER);
    }

    private void setUpLayout() {
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(3, 0, 3, 3));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToList(final DatabaseDriverDTO driver) {
        this.driversList.addDriver(driver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFromList(final DatabaseDriverDTO driver) {
        this.driversList.removeDriver(driver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Panel displaying list of registered drivers.
     */
    private class DriverListPanel extends JPanel {

        private static final long serialVersionUID = 2190095393528579764L;

        private final JList driversList;
        private final DefaultListModel driversListModel;

        public DriverListPanel() {
            super(new BorderLayout(5, 5));
            setBorder(new EmptyBorder(3, 3, 3, 3));

            this.driversListModel = new DefaultListModel();
            this.driversList = new WHList(driversListModel);
            add(new WHScrollPane(driversList), BorderLayout.CENTER);
        }

        public void addDriver(final DatabaseDriverDTO dto) {
            driversListModel.addElement(dto);
            driversList.setSelectedIndex(driversListModel.size() - 1);
        }

        public void removeDriver(final DatabaseDriverDTO driver) {
            driversListModel.removeElement(driver);
        }

        public DatabaseDriverDTO getSelected() {
            return (DatabaseDriverDTO) driversList.getSelectedValue();
        }
    }


    /**
     * Panel displaying control buttons - that is, buttons allowing to
     * add or remove database drivers.
     */
    private class ControlButtonsPanel extends JPanel {

        private static final long serialVersionUID = 6011610476005809288L;

        public ControlButtonsPanel() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            initControlButtons();
        }

        private void initControlButtons() {
            final JButton addDriverButton =
                new FixedSizeButton("Add driver", 110, 22);
            final JButton removeDriverButton =
                new FixedSizeButton("Remove driver", 110, 22);

            addDriverButton.addActionListener(new AddButtonListener());
            removeDriverButton.addActionListener(new RemoveButtonListner());

            add(addDriverButton);
            add(Box.createRigidArea(new Dimension(0, 5)));
            add(removeDriverButton);
        }

        private class AddButtonListener implements ActionListener {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter(
                        "JAR files", "jar"));
                int result = chooser.showOpenDialog(
                        DatabaseDriversPanel.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    final File file = chooser.getSelectedFile();
                    presenter.registerDriver(new DatabaseDriverDTO(
                            file.getAbsolutePath()));
                }
            }
        }

        private class RemoveButtonListner implements ActionListener {
            @Override
            public void actionPerformed(final ActionEvent e) {
                presenter.unregisterDriver(driversList.getSelected());
            }
        }
    }
}
