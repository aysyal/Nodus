/**
 * Copyright (c) 1991-2019 Université catholique de Louvain
 *
 * <p>Center for Operations Research and Econometrics (CORE)
 *
 * <p>http://www.uclouvain.be
 *
 * <p>This file is part of Nodus.
 *
 * <p>Nodus is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package edu.uclouvain.core.nodus.gui;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.util.I18n;

import edu.uclouvain.core.nodus.NodusMapPanel;
import edu.uclouvain.core.nodus.swing.EscapeDialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

/**
 * Displays the "About" dialog.
 *
 * @author Bart Jourquin
 */
public class AboutDlg extends EscapeDialog {

  private static I18n i18n = Environment.getI18n();

  private static final long serialVersionUID = -6000434231982983552L;

  private JPanel mainPanel = new JPanel();

  private GridBagLayout mainPanelGridBagLayout = new GridBagLayout();

  private JEditorPane nodusInfoHTMLPane = new JEditorPane();

  private JScrollPane nodusInfoScrollPane = new JScrollPane();

  private JButton okButton = new JButton();

  private JTextArea openMapTextCopyright = new JTextArea();

  private JButton systemInfoButton = null;

  /**
   * Creates a new "About" box.
   *
   * @param nodusMapPanel The NodusMapPanel.
   */
  public AboutDlg(NodusMapPanel nodusMapPanel) {
    super(nodusMapPanel.getMainFrame(), i18n.get(AboutDlg.class, "About", "About"), true);

    initialize();
    getRootPane().setDefaultButton(okButton);
    requestFocus();
  }

  /**
   * This method initializes systemInfoButton.
   *
   * @return javax.swing.JButton
   */
  private JButton getSystemInfoButton() {
    if (systemInfoButton == null) {
      systemInfoButton = new JButton();
      systemInfoButton.setText(i18n.get(AboutDlg.class, "System_info", "System info"));

      final JDialog _this = this;
      systemInfoButton.addActionListener(
          new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
              ArchInfoDlg dlg = new ArchInfoDlg(_this);
              dlg.setVisible(true);
            }
          });
    }
    return systemInfoButton;
  }

  /**
   * Called by the constructor. Set up a dialog box and displays the "About" text contained in
   * resource bundle.
   */
  private void initialize() {
    GridBagConstraints okButtonConstraints =
        new GridBagConstraints(
            0,
            2,
            1,
            1,
            0.0,
            0.0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0, 0, 10, 0),
            0,
            0);
    okButtonConstraints.gridy = 3;

    GridBagConstraints systemInfoButtonConstraints = new GridBagConstraints();
    systemInfoButtonConstraints.gridx = 0;
    systemInfoButtonConstraints.insets = new Insets(0, 0, 5, 0);
    systemInfoButtonConstraints.gridy = 2;

    mainPanel.setLayout(mainPanelGridBagLayout);

    nodusInfoHTMLPane.setEditable(false);

    try {
      nodusInfoHTMLPane.setPage(getClass().getResource("about.htm"));
    } catch (MalformedURLException ex) {
      System.out.println("invalid url");
    } catch (IOException ex) {
      System.out.println("file not found");
    }

    okButton.setText(i18n.get(AboutDlg.class, "Ok", "Ok"));
    okButton.addActionListener(
        new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            okButton_actionPerformed(e);
          }
        });

    nodusInfoScrollPane.setPreferredSize(new Dimension(430, 250));

    nodusInfoScrollPane.setViewportView(nodusInfoHTMLPane);
    openMapTextCopyright.setBackground(UIManager.getColor("Button.background"));
    openMapTextCopyright.setFont(new java.awt.Font("Dialog", 0, 12));
    openMapTextCopyright.setEditable(false);
    openMapTextCopyright.setText(MapBean.getCopyrightMessage());
    openMapTextCopyright.setRows(0);
    setResizable(false);
    mainPanel.add(
        nodusInfoScrollPane,
        new GridBagConstraints(
            0,
            0,
            1,
            1,
            0.05,
            0.05,
            GridBagConstraints.NORTH,
            GridBagConstraints.HORIZONTAL,
            new Insets(10, 10, 10, 10),
            0,
            0));
    mainPanel.add(okButton, okButtonConstraints);
    mainPanel.add(
        openMapTextCopyright,
        new GridBagConstraints(
            0,
            1,
            1,
            1,
            0.0,
            0.0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0, 10, 10, 10),
            0,
            0));
    mainPanel.add(getSystemInfoButton(), systemInfoButtonConstraints);
    nodusInfoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    nodusInfoScrollPane.setViewportView(nodusInfoHTMLPane);

    setContentPane(mainPanel);
    pack();
  }

  /**
   * Close "About" dialog.
   *
   * @param e ActionEvent
   */
  private void okButton_actionPerformed(ActionEvent e) {
    setVisible(false);
  }
}
