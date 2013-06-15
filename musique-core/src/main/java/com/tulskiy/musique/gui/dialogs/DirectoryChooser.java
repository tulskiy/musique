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

import javax.swing.Painter;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

@SuppressWarnings({"unchecked"})
public class DirectoryChooser extends JTree {
    private static FileSystemView fsv = FileSystemView.getFileSystemView();
    private boolean enableFiles;

    public DirectoryChooser(boolean enableFiles) {
        this(null, enableFiles);
    }

    public DirectoryChooser(File dir, boolean enableFiles) {
        this.enableFiles = enableFiles;
        setModel(new DefaultTreeModel(new DirectoryNode(fsv.getRoots()[0])));
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setSelectedFile(dir);
        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent ev) {
                File oldDir = null;
                TreePath oldPath = ev.getOldLeadSelectionPath();
                if (oldPath != null) {
                    oldDir = ((FileNode) oldPath.getLastPathComponent()).getFile();
                    if (!fsv.isFileSystem(oldDir)) {
                        oldDir = null;
                    }
                }
                File[] files = getSelectedFiles();
                File newDir = files.length > 0 ? files[0] : null;
                firePropertyChange("selectedDirectory", oldDir, newDir);
            }
        });
        setToggleClickCount(2);
        putClientProperty("JTree.lineStyle", "None");
        buildListeners();
    }

    private void buildListeners() {
        addMouseListener(new MouseAdapter() {
            @SuppressWarnings({"unchecked"})
            @Override
            public void mouseClicked(MouseEvent e) {
                final int row = getClosestRowForLocation(e.getX(), e.getY());

                if (row != -1) {
                    Rectangle bounds = getRowBounds(row);
                    boolean isInBounds = bounds.getX() < e.getX();
                    boolean isExtraSpace = bounds.getX() + bounds.getWidth() < e.getX();
                    if (e.getButton() == MouseEvent.BUTTON1 && isExtraSpace) {
                        if (e.isControlDown()) {
                            if (!isRowSelected(row))
                                addSelectionRow(row);
                            else
                                removeSelectionRow(row);
                        } else if (e.isShiftDown()) {
                            int start = getSelectionModel().getLeadSelectionRow();
                            if (start == -1)
                                start = row;
                            if (start < row)
                                start = getSelectionModel().getMinSelectionRow();
                            setSelectionInterval(
                                    start,
                                    row);
                        } else {
                            setSelectionRow(row);
                        }
                    }

                    if (e.isPopupTrigger() && isInBounds) {
                        if (!isRowSelected(row)) {
                            setSelectionRow(row);
                        }
                    }
                }
            }
        });
    }

    public boolean isEnableFiles() {
        return enableFiles;
    }

    public void setEnableFiles(boolean enableFiles) {
        this.enableFiles = enableFiles;
    }

    public void setSelectedFile(File file) {
        if (file == null) {
            file = fsv.getDefaultDirectory();
        }
        TreePath path;
        try {
            path = makePath(file);
        } catch (Exception e) {
            path = makePath(fsv.getDefaultDirectory());
        }
        setSelectionPath(path);
        expandPath(path);
        int row = Math.max(0, getRowForPath(path) - 4);
        Rectangle rect = getRowBounds(row);
        rect.x = 0;
        rect.width = getWidth();
        scrollRectToVisible(rect);
    }

    public File[] getSelectedFiles() {
        TreePath[] paths = getSelectionPaths();
        ArrayList<File> files = new ArrayList<File>();
        if (paths != null)
            for (TreePath path : paths) {
                FileNode node = (FileNode) path.getLastPathComponent();
                File file = node.getFile();
                if (fsv.isFileSystem(file)) {
                    files.add(file);
                }

            }

        return files.toArray(new File[files.size()]);
    }

    private TreePath makePath(File dir) {
        DirectoryNode root = (DirectoryNode) getModel().getRoot();
        if (root.getFile().equals(dir)) {
            return new TreePath(root);
        }

        TreePath parentPath = makePath(fsv.getParentDirectory(dir));
        DirectoryNode parentNode = (DirectoryNode) parentPath.getLastPathComponent();
        Enumeration enumeration = parentNode.children();
        while (enumeration.hasMoreElements()) {
            FileNode child = (FileNode) enumeration.nextElement();
            if (child.getFile().equals(dir)) {
                return parentPath.pathByAddingChild(child);
            }
        }
        return null;
    }

    public void updateUI() {
        super.updateUI();

        if (!Util.isNimbusLaF()) {
            MetalTreeUI newUI = new MetalTreeUI() {
                @Override
                protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf) {
                    if (tree.isRowSelected(row)) {
                        Color bgColor;
                        bgColor = ((DefaultTreeCellRenderer) currentCellRenderer).getBackgroundSelectionColor();
                        g.setColor(bgColor);
                        g.fillRect(clipBounds.x, bounds.y, clipBounds.width, bounds.height);
                    }
                    super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);

                    if (shouldPaintExpandControl(path, row, isExpanded, hasBeenExpanded, isLeaf)) {
                        paintExpandControl(g, clipBounds, insets, bounds,
                                path, row, isExpanded,
                                hasBeenExpanded, isLeaf);
                    }
                }


            };
            setUI(newUI);
        }

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        if (Util.isGTKLaF()) {
            renderer.setClosedIcon(Images.loadIcon("directory.png"));
            renderer.setOpenIcon(Images.loadIcon("directory.png"));
            renderer.setLeafIcon(Images.loadIcon("file.png"));
        }
        setCellRenderer(renderer);

        setForeground(renderer.getTextNonSelectionColor());

        setBackground(renderer.getBackgroundNonSelectionColor());

        UIDefaults defaults = new UIDefaults();

        setRowHeight(getFont().getSize() + 10);
        renderer.setBorderSelectionColor(renderer.getBackgroundSelectionColor());

        Painter collapsedIconPainter = new Painter() {
            @Override
            public void paint(Graphics2D g, Object object, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getForeground());
                g.fillPolygon(
                        new int[]{0, (int) (height * Math.sqrt(0.75)), 0},
                        new int[]{0, height / 2, height},
                        3
                );
            }
        };
        Painter expandedIconPainter = new Painter() {
            @Override
            public void paint(Graphics2D g, Object object, int width, int height) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getForeground());
                g.fillPolygon(
                        new int[]{0, height, height / 2},
                        new int[]{0, 0, (int) (height * Math.sqrt(0.75))},
                        3
                );
            }
        };

        defaults.put("Tree[Enabled].collapsedIconPainter", collapsedIconPainter);
        defaults.put("Tree[Enabled].expandedIconPainter", expandedIconPainter);
        defaults.put("Tree:TreeCell[Focused+Selected].backgroundPainter", new SelectionBackgroundPainter(renderer.getBackgroundSelectionColor()));

        TreeUI treeUI = getUI();
        if (treeUI instanceof MetalTreeUI) {
            BasicTreeUI basicUI = (BasicTreeUI) treeUI;
            int size = 7;
            BufferedImage expandedIcon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            expandedIconPainter.paint(expandedIcon.createGraphics(), null, size, size);
            BufferedImage collapsedIcon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            collapsedIconPainter.paint(collapsedIcon.createGraphics(), null, size, size);
            basicUI.setCollapsedIcon(new ImageIcon(collapsedIcon));
            basicUI.setExpandedIcon(new ImageIcon(expandedIcon));
        }

        putClientProperty("Nimbus.Overrides", defaults);
        putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    }

    private class SelectionBackgroundPainter implements Painter {
        private final Color selection;

        public SelectionBackgroundPainter(Color selection) {
            this.selection = selection;
        }

        @Override
        public void paint(Graphics2D g, Object object, int width, int height) {
            g.setColor(selection);
            g.fillRect(0, 0, width, height);
        }
    }

    class FileNode extends DefaultMutableTreeNode {
        File file;

        FileNode(File file) {
            super(file);
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }

    class DirectoryNode extends FileNode {
        DirectoryNode(File file) {
            super(file);
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

        public void reload() {
            children = null;
            populateChildren();
        }

        private void populateChildren() {
            if (children == null) {
                children = new Vector();
                File[] files = fsv.getFiles(file, true);
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isDirectory() ^ o2.isDirectory()) {
                            return o1.isDirectory() ? -1 : 1;
                        } else {
                            return o1.compareTo(o2);
                        }
                    }
                });
                for (File f : files) {
                    if (fsv.isTraversable(f)) {
                        insert(new DirectoryNode(f),
                                (children == null) ? 0 : children.size());
                    } else if (f.isFile() && isEnableFiles()) {
                        insert(new FileNode(f),
                                (children == null) ? 0 : children.size());
                    }
                }
            }
        }

        public String toString() {
            return fsv.getSystemDisplayName(file);
        }

        public boolean equals(Object o) {
            return (o instanceof DirectoryNode &&
                    userObject.equals(((DirectoryNode) o).getUserObject()));
        }
    }
}
