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

import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;

public class DirectoryChooser extends JTree
        implements TreeSelectionListener, MouseListener {

    private static FileSystemView fsv = FileSystemView.getFileSystemView();

    /*--- Begin Public API -----*/

    public DirectoryChooser() {
        this(null);
    }

    public DirectoryChooser(File dir) {
        super(new DirNode(fsv.getRoots()[0]));
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setSelectedDirectory(dir);
        addTreeSelectionListener(this);
        addMouseListener(this);
    }

    public void setSelectedDirectory(File dir) {
        if (dir == null) {
            dir = fsv.getDefaultDirectory();
        }
        TreePath path = mkPath(dir);
        setSelectionPath(path);
        scrollPathToVisible(path);
    }

    public File getSelectedDirectory() {
        DirNode node = (DirNode) getLastSelectedPathComponent();
        if (node != null) {
            File dir = node.getDir();
            if (fsv.isFileSystem(dir)) {
                return dir;
            }
        }
        return null;
    }

    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }

    public ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
    }

    /*--- End Public API -----*/


    /*--- TreeSelectionListener Interface -----*/

    public void valueChanged(TreeSelectionEvent ev) {
        File oldDir = null;
        TreePath oldPath = ev.getOldLeadSelectionPath();
        if (oldPath != null) {
            oldDir = ((DirNode) oldPath.getLastPathComponent()).getDir();
            if (!fsv.isFileSystem(oldDir)) {
                oldDir = null;
            }
        }
        File newDir = getSelectedDirectory();
        firePropertyChange("selectedDirectory", oldDir, newDir);
    }

    /*--- MouseListener Interface -----*/

    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 2) {
            TreePath path = getPathForLocation(e.getX(), e.getY());
            if (path != null && path.equals(getSelectionPath()) &&
                getSelectedDirectory() != null) {

                fireActionPerformed("dirSelected", e);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }


    /*--- Private Section ------*/

    private TreePath mkPath(File dir) {
        DirNode root = (DirNode) getModel().getRoot();
        if (root.getDir().equals(dir)) {
            return new TreePath(root);
        }

        TreePath parentPath = mkPath(fsv.getParentDirectory(dir));
        DirNode parentNode = (DirNode) parentPath.getLastPathComponent();
        Enumeration enumeration = parentNode.children();
        while (enumeration.hasMoreElements()) {
            DirNode child = (DirNode) enumeration.nextElement();
            if (child.getDir().equals(dir)) {
                return parentPath.pathByAddingChild(child);
            }
        }
        return null;
    }


    private void fireActionPerformed(String command, InputEvent evt) {
        ActionEvent e =
                new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                        command, evt.getWhen(), evt.getModifiers());
        ActionListener[] listeners = getActionListeners();
        for (int i = listeners.length - 1; i >= 0; i--) {
            listeners[i].actionPerformed(e);
        }
    }


    private static class DirNode extends DefaultMutableTreeNode {
        DirNode(File dir) {
            super(dir);
        }

        public File getDir() {
            return (File) userObject;
        }

        public int getChildCount() {
            populateChildren();
            return super.getChildCount();
        }

        public Enumeration children() {
            populateChildren();
            return super.children();
        }

        public boolean isLeaf() {
            return false;
        }

        private void populateChildren() {
            if (children == null) {
                File[] files = fsv.getFiles(getDir(), true);
                Arrays.sort(files);
                for (File f : files) {
                    if (fsv.isTraversable(f)) {
                        insert(new DirNode(f),
                                (children == null) ? 0 : children.size());
                    }
                }
            }
        }

        public String toString() {
            return fsv.getSystemDisplayName(getDir());
        }

        public boolean equals(Object o) {
            return (o instanceof DirNode &&
                    userObject.equals(((DirNode) o).userObject));
        }
    }


    /*--- Main for testing  ---*/

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (Exception ignored) {
        }

        final JDialog dialog = new JDialog((JFrame) null, true);
        final DirectoryChooser dc = new DirectoryChooser();
        final JButton okButton = new JButton("   OK   ");
        final JButton cancelButton = new JButton(" Cancel ");
        dialog.getRootPane().setDefaultButton(okButton);

        JPanel panel = new JPanel(new BorderLayout());
        final JTextField path = new JTextField();
        path.setText(String.valueOf(dc.getSelectedDirectory()));
        panel.add(path, BorderLayout.NORTH);

        dc.addPropertyChangeListener("selectedDirectory", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                path.setText(String.valueOf(evt.getNewValue()));
            }
        });

        path.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = new File(path.getText());
                if (dir.exists() && dir.isDirectory())
                    dc.setSelectedDirectory(dir);
                dc.requestFocus();
            }
        });

        panel.add(new JScrollPane(dc), BorderLayout.CENTER);

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(okButton);
        buttonBox.add(Box.createHorizontalStrut(5));
        buttonBox.add(cancelButton);
        panel.add(buttonBox, BorderLayout.SOUTH);

        dialog.add(panel);

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object c = e.getSource();
                if (c == okButton || c == dc) {
                    System.out.println("You selected: " + dc.getSelectedDirectory());
                }
                dialog.setVisible(false);
            }
        };

        dc.addActionListener(actionListener);
        okButton.addActionListener(actionListener);
        cancelButton.addActionListener(actionListener);

        dc.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (ev.getPropertyName().equals("selectedDirectory")) {
                    okButton.setEnabled(dc.getSelectedDirectory() != null);
                }
            }
        });

        dialog.setBounds(200, 200, 300, 350);
        dc.setFocusCycleRoot(true);
        dc.requestFocusInWindow();
        dc.scrollRowToVisible(Math.max(0, dc.getMinSelectionRow() - 4));
        dialog.setVisible(true);
        System.exit(0);
    }
}
