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

package com.tulskiy.musique.gui;

import com.sun.java.swing.Painter;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.gui.dialogs.ProgressDialog;
import com.tulskiy.musique.gui.dialogs.SettingsDialog;
import com.tulskiy.musique.gui.dialogs.Task;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.Library;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Author: Denis Tulskiy
 * Date: 10/31/10
 */
public class LibraryView extends JPanel {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private JTree tree;
    private Library library;
    private Playlist libraryPlaylist;
    private PlaylistManager playlistManager;
    private SearchWorker searchWorker;
    private Library libraryView;

    public LibraryView() {
        playlistManager = app.getPlaylistManager();
        library = playlistManager.getLibrary();
        for (Playlist playlist : playlistManager.getPlaylists()) {
            if (playlist.isLibraryView()) {
                libraryPlaylist = playlist;
                break;
            }
        }

        initComponents();

        initListeners();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        tree = new LibraryTree();
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        final JTextField searchField = new JTextField(30);
        Box viewBox = Box.createHorizontalBox();
        JComboBox viewCombo = new JComboBox(new Object[]{"By Album Artist"});
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(viewCombo);
        p1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "View",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.TOP, new Font("Sans Serif", 0, 11)));
        viewBox.add(p1);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(searchField);
        p2.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Filter",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.TOP, new Font("Sans Serif", 0, 11)));
        viewBox.add(p2);
        add(viewBox, BorderLayout.PAGE_END);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }

            private void search() {
                if (searchWorker != null && !searchWorker.isDone()) {
                    searchWorker.cancel(true);
                }

                searchWorker = new SearchWorker(library.getData(), searchField.getText(), true) {
                    @Override
                    protected void done() {
                        try {
                            Playlist result = get();
                            if (result != null) {
                                libraryView = new Library(result);
                                tree.setModel(new DefaultTreeModel(libraryView.getRootNode()));
                                tree.revalidate();
                                tree.repaint();
                            } else {
                                libraryView = null;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                };
                searchWorker.execute();
            }
        });
    }

    private void initListeners() {
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @SuppressWarnings({"unchecked"})
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                ensureLibraryPlaylistVisible();
                libraryPlaylist.clear();
                TreePath[] selectionPaths = tree.getSelectionPaths();
                if (selectionPaths != null)
                    for (TreePath path : selectionPaths) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Enumeration<DefaultMutableTreeNode> en = node.depthFirstEnumeration();
                        while (en.hasMoreElements()) {
                            DefaultMutableTreeNode o = en.nextElement();
                            if (o.isLeaf() && o.getUserObject() instanceof Track)
                                libraryPlaylist.add((Track) o.getUserObject());
                        }
                    }
                libraryPlaylist.firePlaylistChanged();
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Player player = app.getPlayer();
                    player.getPlaybackOrder().flushQueue();

                    Track nextTrack = null;
                    do {
                        nextTrack = player.getPlaybackOrder().next(nextTrack);
                    } while (nextTrack != null && nextTrack.getLocation() == null);

                    player.open(nextTrack);
                    player.play();
                }
            }
        });
    }

    private void ensureLibraryPlaylistVisible() {
        if (libraryPlaylist == null) {
            libraryPlaylist = playlistManager.addPlaylist("Library");
            libraryPlaylist.setLibraryView(true);
        }

        if (!playlistManager.getPlaylists().contains(libraryPlaylist)) {
            playlistManager.addPlaylist(libraryPlaylist);
        }

        playlistManager.selectPlaylist(null);
        playlistManager.selectPlaylist(libraryPlaylist);
    }

    public void addMenu(JMenuBar menuBar) {
        JMenu menu = new JMenu("Library");
        menuBar.add(menu);
        JMenuItem configure = menu.add("Configure     ");
        configure.setIcon(Images.getEmptyIcon());
        configure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SettingsDialog(getRootPane(), "Library").setVisible(true);
            }
        });
        menu.add("Rescan").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgressDialog progressDialog = new ProgressDialog(getRootPane(), "Library Rescan");
                progressDialog.show(new Task() {
                    HashMap<String, Object> map = new HashMap<String, Object>();

                    @Override
                    public String getStatus() {
                        return "Scanning your library for changes\n" + String.valueOf(map.get("processing.file"));
                    }

                    @Override
                    public void abort() {
                        map.put("processing.stop", true);
                    }

                    @Override
                    public void start() {
                        int[] selectionRows = tree.getSelectionRows();
                        library.rescan(map);
                        ((DefaultTreeModel) tree.getModel()).reload();
                        tree.revalidate();
                        tree.repaint();
                        if (selectionRows != null)
                            tree.setSelectionRows(selectionRows);
                    }
                });
            }
        });

        Util.fixIconTextGap(menu);
    }


    private class LibraryTree extends JTree {
        public LibraryTree() {
            super(library.getRootNode());
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            setToggleClickCount(0);
            setShowsRootHandles(false);
            buildListeners();
            putClientProperty("JTree.lineStyle", "None");
            updateUI();
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
//                    if (e.getButton() == MouseEvent.BUTTON1 && row != -1) {
//                        if (e.getClickCount() == 2) {
//
//                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) getSelectionPath().getLastPathComponent();
//                            Enumeration<DefaultMutableTreeNode> en = node.breadthFirstEnumeration();
//                            while (en.hasMoreElements()) {
//                                DefaultMutableTreeNode o = en.nextElement();
//                                if (o.isLeaf())
//                                    System.out.println(o.getUserObject());
//                            }
//                            expandRow(row);
//                        }
//                    }
                }
            });
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public void updateUI() {
            super.updateUI();

            String laf = UIManager.getLookAndFeel().getName();
            if (laf.contains("GTK") || laf.contains("Metal") || laf.contains("Windows")) {
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

            UIDefaults defaults = new UIDefaults();

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
    }
}
