/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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

package com.tulskiy.musique.gui.library;

import com.sun.java.swing.Painter;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.library.MappedTreeNode;
import com.tulskiy.musique.library.TrackNode;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.configuration.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static com.tulskiy.musique.gui.library.LibraryAction.*;


/**
 * Author: Denis Tulskiy
 * Date: 1/4/11
 */
public class LibraryTree extends JTree {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private PlaylistManager playlistManager = app.getPlaylistManager();
    private Player player = app.getPlayer();

    public LibraryTree() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setToggleClickCount(0);
        setShowsRootHandles(false);
        buildListeners();
        putClientProperty("JTree.lineStyle", "None");
        updateUI();
    }

    private void buildListeners() {
        addMouseListener(new ExpandListener());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    LibraryAction action = config.getEnum("library.doubleClickAction", SEND_TO_CURRENT);
                    getActionMap().get(action).actionPerformed(null);
                }

                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (selectRowAt(e.getPoint())) {
                        LibraryAction action = config.getEnum("library.middleClickAction", SEND_TO_CURRENT);
                        getActionMap().get(action).actionPerformed(null);
                    }
                }
            }
        });

        final ActionMap aMap = getActionMap();
        aMap.put(SEND_TO_CURRENT, new AbstractAction(SEND_TO_CURRENT.getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendToPlaylist(playlistManager.getVisiblePlaylist());
            }
        });

        aMap.put(SEND_TO_NEW, new AbstractAction(SEND_TO_NEW.getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Playlist playlist = playlistManager.addPlaylist(getSelectionPath().getLastPathComponent().toString());
                sendToPlaylist(playlist);
            }
        });
        aMap.put(ADD_TO_CURRENT, new AbstractAction(ADD_TO_CURRENT.getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Playlist playlist = playlistManager.getVisiblePlaylist();
                playlist.addAll(getSelectedTracks(true));
                playlist.firePlaylistChanged();
            }
        });

        aMap.put(EXPAND_COLLAPSE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = getSelectionPath();
                if (isExpanded(path))
                    collapsePath(path);
                else
                    expandPath(path);
            }
        });

        InputMap iMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        iMap.put(SEND_TO_CURRENT.getKeyStroke(), SEND_TO_CURRENT);
        iMap.put(SEND_TO_NEW.getKeyStroke(), SEND_TO_NEW);
        iMap.put(ADD_TO_CURRENT.getKeyStroke(), ADD_TO_CURRENT);
    }

    public boolean selectRowAt(Point p) {
        int row = getClosestRowForLocation(p.x, p.y);
        if (row != -1) {
            setSelectionRow(row);
            return true;
        }
        return false;
    }

    private void sendToPlaylist(Playlist playlist) {
        ArrayList<Track> tracks = getSelectedTracks(true);
        if (tracks.size() > 0) {
            playlist.clear();
            playlist.addAll(tracks);
            player.open(playlist.get(0));
            playlistManager.setActivePlaylist(playlist);
            playlist.firePlaylistChanged();
        }
    }


    @SuppressWarnings({"unchecked"})
    @Override
    public void updateUI() {
        super.updateUI();
        if (config == null)
            return;

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
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        renderer.setLeafIcon(null);
        setCellRenderer(renderer);

        UIDefaults defaults = new UIDefaults();
        Color text = config.getColor("gui.color.text", null);
        if (text != null) {
            renderer.setTextNonSelectionColor(text);
        }
        setForeground(renderer.getTextNonSelectionColor());

        Color background = config.getColor("gui.color.background", null);
        if (background != null) {
            renderer.setBackgroundNonSelectionColor(background);
        }
        setBackground(renderer.getBackgroundNonSelectionColor());

        final Color selection = config.getColor("gui.color.selection", null);
        if (selection != null) {
            renderer.setBackgroundSelectionColor(selection);
            renderer.setTextSelectionColor(Util.getContrastColor(selection));
            defaults.put("Tree.selectionBackground", selection);
            defaults.put("Tree:TreeCell[Enabled+Selected].backgroundPainter", new SelectionBackgroundPainter(selection));
        }

        Font font = config.getFont("gui.font.default", null);
        if (font != null) {
            setFont(font);
        }
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

    public void setRootNode(TreeNode rootNode) {
        setModel(new DefaultTreeModel(rootNode));
        revalidate();
        repaint();
    }

    @SuppressWarnings({"unchecked"})
    public ArrayList<Track> getSelectedTracks(boolean createNew) {
        ArrayList<Track> tracks = new ArrayList<Track>();
        TreePath[] selectionPaths = getSelectionPaths();
        if (selectionPaths != null)
            for (TreePath path : selectionPaths) {
                MappedTreeNode node = (MappedTreeNode) path.getLastPathComponent();
                List<MappedTreeNode> nodes = node.iterate();
                for (MappedTreeNode treeNode : nodes) {
                    if (treeNode instanceof TrackNode) {
                        Track track = ((TrackNode) treeNode).getTrack();

                        if (createNew) {
                            track = new Track(track);
                        }

                        tracks.add(track);
                    }
                }
            }
        return tracks;
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

    private class ExpandListener extends MouseAdapter {
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
    }
}
