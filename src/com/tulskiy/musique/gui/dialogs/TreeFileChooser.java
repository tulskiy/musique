/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.gui.dialogs;

import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

/**
 * Author: Denis Tulskiy
 * Date: 12/2/10
 */
public class TreeFileChooser extends JDialog {
    private Application app = Application.getInstance();
    private boolean allowFiles;
    private File[] selectedFiles;
    private DirectoryChooser directoryChooser;
    private JTextField pathField;

    public TreeFileChooser(JComponent owner, String title, boolean allowFiles) {
        super(SwingUtilities.windowForComponent(owner), title, ModalityType.APPLICATION_MODAL);
        this.allowFiles = allowFiles;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);

        initComponents();
        initListeners();
    }

    public void setSelectionMode(int mode) {
        directoryChooser.getSelectionModel().setSelectionMode(mode);
    }

    private void initListeners() {
        pathField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = new File(pathField.getText());
                if (dir.exists() && dir.isDirectory())
                    directoryChooser.setSelectedFile(dir);
                directoryChooser.requestFocus();
            }
        });
        directoryChooser.addPropertyChangeListener("selectedDirectory", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                File[] files = directoryChooser.getSelectedFiles();
                if (files.length > 0)
                    pathField.setText(files[0].getAbsolutePath());
            }
        });
        addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                directoryChooser.requestFocusInWindow();
            }
        });
    }

    private void initComponents() {
        directoryChooser = new DirectoryChooser(allowFiles);
        JPanel panel = new JPanel(new BorderLayout());
        setContentPane(panel);

        final JButton okButton = new JButton("    OK    ");
        final JButton cancelButton = new JButton(" Cancel ");
        getRootPane().setDefaultButton(okButton);

        JPanel top = new JPanel(new BorderLayout());
        JLabel label = new JLabel(getTitle());
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        label.setVerticalAlignment(JLabel.TOP);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));
        label.setPreferredSize(new Dimension(10, 45));
        top.add(label, BorderLayout.CENTER);
        top.add(initToolbar(), BorderLayout.PAGE_END);
        panel.add(top, BorderLayout.PAGE_START);

        panel.add(new JScrollPane(directoryChooser), BorderLayout.CENTER);

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalStrut(5));
        buttonBox.add(cancelButton);
        panel.add(buttonBox, BorderLayout.SOUTH);

        final ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object c = e.getSource();
                if (c == okButton || c == directoryChooser) {
                    selectedFiles = directoryChooser.getSelectedFiles();
                }
                setVisible(false);
            }
        };

        okButton.addActionListener(actionListener);
        cancelButton.addActionListener(actionListener);
        directoryChooser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = directoryChooser.getPathForLocation(e.getX(), e.getY());
                    if (path != null && path.equals(directoryChooser.getSelectionPath()) &&
                        directoryChooser.getSelectedFiles().length > 0) {
                        TreeNode o = (TreeNode) path.getLastPathComponent();
                        if (o.isLeaf() || !o.children().hasMoreElements())
                            actionListener.actionPerformed(new ActionEvent(directoryChooser, 0, null));
                    }
                }
            }
        });
        directoryChooser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ev.getPropertyName().equals("selectedDirectory")) {
                    okButton.setEnabled(directoryChooser.getSelectedFiles() != null);
                }
            }
        });
        directoryChooser.setFocusCycleRoot(true);
        directoryChooser.requestFocusInWindow();
    }

    private Container initToolbar() {
        Box toolBar = Box.createHorizontalBox();
        toolBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        JButton home = new JButton(Images.loadIcon("home.png"));
        home.setFocusable(false);
        home.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                directoryChooser.setSelectedFile(null);
            }
        });

        JButton refresh = new JButton(Images.loadIcon("refresh.png"));
        refresh.setFocusable(false);
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath selectionPath = directoryChooser.getSelectionPath();
                Object o = selectionPath.getLastPathComponent();
                if (o instanceof DirectoryChooser.DirectoryNode) {
                    DirectoryChooser.DirectoryNode node = (DirectoryChooser.DirectoryNode) o;
                    node.reload();
                }
                ((DefaultTreeModel) directoryChooser.getModel()).reload();
                directoryChooser.setSelectionPath(selectionPath);
                directoryChooser.expandPath(selectionPath);
            }
        });
        Dimension size = new Dimension(30, 24);
        refresh.setPreferredSize(size);
        home.setPreferredSize(size);
        if (Util.isGTKLaF()) {
            home.setBorderPainted(false);
            refresh.setBorderPainted(false);
        }

        pathField = new JTextField();
        pathField.setFocusCycleRoot(false);
        pathField.setText(directoryChooser.getSelectedFiles()[0].getAbsolutePath());

        toolBar.add(home);
        toolBar.add(refresh);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(pathField);
        return toolBar;
    }

    public File[] showOpenDialog() {
        Configuration config = app.getConfiguration();
        String path = config.getString("playlist.lastDir", null);
        if (path != null) {
            File file = new File(path);
            if (file.exists())
                directoryChooser.setSelectedFile(file);
        }
        setVisible(true);
        dispose();
        File[] files = directoryChooser.getSelectedFiles();
        if (files.length > 0) {
            File dir = files[0];
            while (dir != null && !dir.isDirectory()) {
                dir = dir.getParentFile();
            }
            if (dir != null) {
                config.setString("playlist.lastDir", dir.getAbsolutePath());
            }
        }
        return selectedFiles;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException ignored) {
        }
        Application app = Application.getInstance();
        app.load();
        TreeFileChooser fileChooser = new TreeFileChooser(new JLabel(), "Select files", true);
        File[] files = fileChooser.showOpenDialog();
        if (files != null) {
            System.out.println(Arrays.toString(files));
        }
        app.exit();
    }
}
