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

package com.tulskiy.musique.gui.playlist;

import com.tulskiy.musique.gui.dialogs.ProgressDialog;
import com.tulskiy.musique.gui.dialogs.SearchDialog;
import com.tulskiy.musique.gui.playlist.dnd.PlaylistTransferHandler;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class PlaylistPanel extends JPanel {
    private PlaylistColumn[] defaultColumns = {
            new PlaylistColumn("Playing", 55, "$isPlaying()"),
            new PlaylistColumn("Name", 325, "[%artist% - ]%title%"),
            new PlaylistColumn("Length", 70, "%length%"),
            new PlaylistColumn("Album", 225, "%album%"),
            new PlaylistColumn("Date", 55, "%year%")
    };

    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private PlaylistTable table;
    private PlaylistManager playlistManager;
    private ArrayList<PlaylistColumn> columns;
    private Playlist playlist;
    private JTabbedPane tabbedPane;

    public PlaylistPanel() {
        playlistManager = app.getPlaylistManager();
        playlist = playlistManager.getCurrentPlaylist();

        columns = loadColumns();

        table = new PlaylistTable(playlist, columns);
        app.getPlayer().setPlaybackOrder(table);
        setUpDndCCP();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        tabbedPane = new TabbedPane();
        add(tabbedPane, BorderLayout.NORTH);
        add(table.getScrollPane(), BorderLayout.CENTER);

//        int lastPlayed = config.getInt("player.lastPlayed", -1);
        //todo fix me here
//        table.setLastPlayed(new Track(lastPlayed));
    }

    private ArrayList<PlaylistColumn> loadColumns() {
        ArrayList<String> list = config.getList("playlist.columns", null);
        ArrayList<PlaylistColumn> res = new ArrayList<PlaylistColumn>();
        if (list == null) {
            res.addAll(Arrays.asList(defaultColumns));
        } else {
            for (String s : list) {
                res.add(new PlaylistColumn(s));
            }
        }
        return res;
    }

    private void setUpDndCCP() {
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new PlaylistTransferHandler(table));
        ActionMap map = table.getActionMap();

        Action cutAction = TransferHandler.getCutAction();
        map.put(cutAction.getValue(Action.NAME), cutAction);
        Action copyAction = TransferHandler.getCopyAction();
        map.put(copyAction.getValue(Action.NAME), copyAction);
        Action pasteAction = TransferHandler.getPasteAction();
        map.put(pasteAction.getValue(Action.NAME), pasteAction);

        InputMap iMap = table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        iMap.put(KeyStroke.getKeyStroke("ctrl X"), cutAction.getValue(Action.NAME));
        iMap.put(KeyStroke.getKeyStroke("ctrl C"), copyAction.getValue(Action.NAME));
        iMap.put(KeyStroke.getKeyStroke("ctrl V"), pasteAction.getValue(Action.NAME));
    }

    public void shutdown() {
        table.saveColumns();

        config.setList("playlist.columns", columns);
    }

    private JMenuItem newItem(String name, String hotkey, ActionListener al) {
        JMenuItem item = new JMenuItem(name);
        item.setAccelerator(KeyStroke.getKeyStroke(hotkey));
        item.addActionListener(al);

        return item;
    }

    public void addMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        JMenu playbackMenu = new JMenu("Playback");
        menuBar.add(playbackMenu);
        final ActionMap aMap = table.getActionMap();

        ActionMap tMap = tabbedPane.getActionMap();
        fileMenu.add(tMap.get("newPlaylist"));
        fileMenu.add(tMap.get("removePlaylist"));
        fileMenu.addSeparator();
        fileMenu.add("Add Files").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                String path = config.getString("playlist.lastDir", "");
                if (!path.isEmpty()) fc.setCurrentDirectory(new File(path));
                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int retVal = fc.showOpenDialog(null);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    ProgressDialog dialog = new ProgressDialog(table.getParentFrame(), "Adding files");
                    dialog.addFiles(playlist, Arrays.asList(fc.getSelectedFiles()));
                    table.update();
                }

                config.setString("playlist.lastDir", fc.getCurrentDirectory().getAbsolutePath());
                table.dataChanged();
                table.update();
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(newItem("Quit", "ctrl Q", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.exit();
            }
        }));

        editMenu.add("Clear").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Playlist pl = playlistManager.getCurrentPlaylist();
                pl.clear();
                table.update();
            }
        });
        editMenu.add(aMap.get("removeSelected"));
        editMenu.addSeparator();
        editMenu.add(aMap.get("clearQueue"));
        editMenu.add(newItem("Search", "ctrl F", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SearchDialog(table).setVisible(true);
            }
        }));

        JMenu laf = new JMenu("Look and Feel");
        ActionListener lafListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String cmd = e.getActionCommand();
                    if (cmd.equals("Metal")) {
                        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    } else if (cmd.equals("Nimbus")) {
                        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    } else {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                    shutdown();
                    app.start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        };

        laf.add("Metal").addActionListener(lafListener);
        laf.add("Nimbus").addActionListener(lafListener);
        laf.add("Native").addActionListener(lafListener);
        viewMenu.add(laf);

        JMenu orderMenu = new JMenu("Order");
        playbackMenu.add(orderMenu);
        ActionListener orderListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                PlaylistTable.Order o = PlaylistTable.Order.valueOf(item.getName());
                table.setOrder(o);
                config.setInt("player.playbackOrder", o.ordinal());
            }
        };

        int index = config.getInt("player.playbackOrder", 0);
        PlaylistTable.Order[] orders = PlaylistTable.Order.values();
        ButtonGroup gr = new ButtonGroup();
        for (PlaylistTable.Order o : orders) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(o.getText());
            if (o.ordinal() == index) {
                item.setSelected(true);
                table.setOrder(o);
            }
            item.addActionListener(orderListener);
            item.setName(o.toString());
            gr.add(item);
            orderMenu.add(item);
        }

        JMenu controlMenu = new JMenu("Control");
        playbackMenu.add(controlMenu);

        controlMenu.add(aMap.get("next"));
        controlMenu.add(aMap.get("play"));
        controlMenu.add(aMap.get("pause"));
        controlMenu.add(aMap.get("stop"));
        controlMenu.add(aMap.get("prev"));

        playbackMenu.addSeparator();

        playbackMenu.add(aMap.get("showNowPlaying"));
    }

    class TabbedPane extends JTabbedPane {
        private ArrayList<Playlist> playlists;
        private int dragTo = -1;
        private int dragFrom;

        TabbedPane() {
            playlists = playlistManager.getPlaylists();
            for (Playlist pl : playlists) {
                add(pl.getName(), null);
            }

            if (UIManager.getLookAndFeel().getName().contains("GTK")) {
                setPreferredSize(new Dimension(10000, 30));
            } else {
                setPreferredSize(new Dimension(10000, 25));
            }
            setFocusable(false);
            setSelectedIndex(playlists.indexOf(playlist));

            buildListeners();
            createPopupMenu();
        }

        private void buildListeners() {
            addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int index = tabbedPane.getSelectedIndex();
                    if (index == -1) return;
                    playlist = playlistManager.getPlaylist(index);
                    table.setPlaylist(playlist);
                    playlistManager.selectPlaylist(playlist);
                }
            });

            addMouseMotionListener(new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    dragTo = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    tabbedPane.repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                        playlistManager.movePlaylist(dragFrom, dragTo);

                        for (int i = 0; i < playlists.size(); i++) {
                            Playlist pl = playlists.get(i);
                            tabbedPane.setTitleAt(i, pl.getName());
                        }

                        tabbedPane.setSelectedIndex(dragTo);
                    }
                    dragTo = -1;
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    dragFrom = tabbedPane.indexAtLocation(e.getX(), e.getY());
                }
            });
        }

        private void createPopupMenu() {
            final JPopupMenu tabMenu = new JPopupMenu();
            ActionMap aMap = getActionMap();
            aMap.put("newPlaylist", new AbstractAction("New Playlist") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String name = JOptionPane.showInputDialog("Enter Playlist Name", "New Playlist");
                    addPlaylist(name);
                }
            });
            aMap.put("renamePlaylist", new AbstractAction("Rename") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = getSelectedIndex();
                    if (index != -1) {
                        Playlist pl = playlistManager.getPlaylist(index);
                        String name = JOptionPane.showInputDialog("Rename", pl.getName());
                        if (name != null && !name.isEmpty()) {
                            pl.setName(name);
                            setTitleAt(index, name);
                        }
                    }
                }
            });
            aMap.put("removePlaylist", new AbstractAction("Remove Playlist") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = getSelectedIndex();
                    if (index != -1) {
                        Playlist pl = playlistManager.getPlaylist(index);
                        tabbedPane.remove(index);
                        playlistManager.removePlaylist(pl);
                        if (playlistManager.getTotalPlaylists() == 0) {
                            addPlaylist("Default");
                        }
                    }
                }
            });

            tabMenu.add(aMap.get("newPlaylist"));
            tabMenu.add(aMap.get("renamePlaylist"));
            tabMenu.add(aMap.get("removePlaylist"));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    show(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    show(e);
                }

                public void show(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
                        if (index != -1)
                            setSelectedIndex(index);
                        tabMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }

        private void addPlaylist(String name) {
            if (name == null || name.isEmpty()) return;
            playlist = playlistManager.addPlaylist(name);

            tabbedPane.add(name, null);
            tabbedPane.setSelectedIndex(playlistManager.getTotalPlaylists() - 1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (dragTo != -1 && dragFrom != -1 && dragTo != dragFrom) {
                Rectangle b = tabbedPane.getBoundsAt(dragTo);
                g.setColor(Color.GRAY);
                int x = (int) (b.getX());
                if (dragTo > dragFrom)
                    x += b.getWidth();
                int y = (int) b.getY();
                g.fillRect(x - 1, y + 2, 3, (int) b.getHeight() - 2);

                int[] xP = {x - 1, x - 5, x + 5, x + 1};
                int[] yP = {y + 2, y - 5, y - 5, y + 2};
                g.fillPolygon(xP, yP, xP.length);
            }
        }
    }

}
