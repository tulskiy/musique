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

import com.tulskiy.musique.audio.player.PlaybackOrder;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.gui.dialogs.ProgressDialog;
import com.tulskiy.musique.gui.dialogs.SearchDialog;
import com.tulskiy.musique.gui.dialogs.Task;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.PlaylistOrder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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
    private ArrayList<PlaylistColumn> columns;
    private PlaylistTabs tabs;
    private TableColumnModel columnModel;

    public PlaylistPanel() {
        columns = loadColumns();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        tabs = new PlaylistTabs(columns);
        add(tabs, BorderLayout.CENTER);

        PlaylistManager playlistManager = app.getPlaylistManager();
        ArrayList<Playlist> playlists = playlistManager.getPlaylists();
        ArrayList<String> bounds = config.getList("playlist.tabs.bounds", null);

        for (int i = 0; i < playlists.size(); i++) {
            Playlist pl = playlists.get(i);
            PlaylistTable newTable = new PlaylistTable(pl, columns);
            newTable.setUpDndCCP();
            if (columnModel == null) {
                columnModel = newTable.getColumnModel();
            } else {
                newTable.setColumnModel(columnModel);
            }

            //try to set last position
            try {
                String s = bounds.get(i);
                Integer y = Integer.valueOf(s);
                newTable.scrollRectToVisible(new Rectangle(0, y, 0, 0));
            } catch (Exception ignored) {
            }

            tabs.add(pl.getName(), newTable.getScrollPane());
        }

        final Playlist playlist = playlistManager.getCurrentPlaylist();

        tabs.setSelectedIndex(-1);
        tabs.setSelectedIndex(playlists.indexOf(playlist));

        PlaylistOrder order = (PlaylistOrder) app.getPlayer().getPlaybackOrder();
        Track lastPlayed = order.getLastPlayed();

        if (lastPlayed != null) {
            PlaylistTable table = tabs.getSelectedTable();
            if (table != null) {
                int index = table.getPlaylist().indexOf(lastPlayed);
                table.setRowSelectionInterval(index, index);
            }
        }

        final Player player = app.getPlayer();

        final Timer update = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaylistTable table = tabs.getSelectedTable();
                if (table != null)
                    table.update();
            }
        });

        player.addListener(new PlayerListener() {
            @Override
            public void onEvent(PlayerEvent e) {
                Track track = player.getTrack();
                if (track != null && track.isStream()) {
                    update.start();
                } else {
                    update.stop();
                }
            }
        });
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

    public void saveSettings() {
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tc = columnModel.getColumn(i);
            PlaylistColumn pc = columns.get(tc.getModelIndex());
            pc.setPosition(i);
            pc.setSize(tc.getWidth());
        }

        Collections.sort(columns);
        config.setObject("playlist.selectedTrack", null);
        config.setList("playlist.columns", columns);

        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            PlaylistTable t = tabs.getTableAt(i);
            list.add(t.getVisibleRect().y);
        }
        config.setList("playlist.tabs.bounds", list);
    }

    private JMenuItem newItem(String name, String hotkey, ActionListener al) {
        JMenuItem item = new JMenuItem(name);
        item.setAccelerator(KeyStroke.getKeyStroke(hotkey));
        item.addActionListener(al);

        return item;
    }

    private Action tableAction(final String actionName, String name) {
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaylistTable table = tabs.getSelectedTable();
                if (table != null)
                    table.runAction(actionName);
            }
        };
    }

    public void addMenu(JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        final JMenu playbackMenu = new JMenu("Playback");
        menuBar.add(playbackMenu);

        ActionMap tMap = tabs.getActionMap();
        fileMenu.add(tMap.get("newPlaylist")).setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        fileMenu.add(tMap.get("removePlaylist"));
        fileMenu.add(tMap.get("loadPlaylist"));
        fileMenu.add(tMap.get("savePlaylist")).setAccelerator(KeyStroke.getKeyStroke("ctrl S"));

        fileMenu.addSeparator();
        fileMenu.add("Add Files").addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();
                String path = config.getString("playlist.lastDir", "");
                if (!path.isEmpty()) fc.setCurrentDirectory(new File(path));
                fc.setMultiSelectionEnabled(true);
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int retVal = fc.showOpenDialog(null);
                final PlaylistTable table = tabs.getSelectedTable();
                if (table == null)
                    return;

                if (retVal == JFileChooser.APPROVE_OPTION) {
                    ProgressDialog dialog = new ProgressDialog(table.getParentFrame(), "Adding Files");
                    dialog.show(new Task.FileAddingTask(table, fc.getSelectedFiles(), -1));
                }

                config.setString("playlist.lastDir", fc.getCurrentDirectory().getAbsolutePath());
                table.dataChanged();
                table.update();
            }
        });
        fileMenu.add("Add Location").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ret = JOptionPane.showInputDialog("Add Location");
                if (!Util.isEmpty(ret)) {
                    PlaylistTable table = tabs.getSelectedTable();
                    if (table == null)
                        return;
                    table.getPlaylist().insertItem(ret, -1, false, null);
                    table.update();
                }
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
                PlaylistTable table = tabs.getSelectedTable();
                if (table == null)
                    return;
                table.getPlaylist().clear();
                table.update();
            }
        });
        editMenu.add(tableAction("removeSelected", "Remove"));
        editMenu.addSeparator();
        editMenu.add(tableAction("clearQueue", "Clear Playback Queue"));
        editMenu.add(newItem("Search", "ctrl F", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaylistTable table = tabs.getSelectedTable();
                if (table == null)
                    return;
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
                    saveSettings();
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
                PlaybackOrder.Order o = PlaybackOrder.Order.valueOf(item.getName());
                app.getPlayer().getPlaybackOrder().setOrder(o);
                config.setInt("player.playbackOrder", o.ordinal());
            }
        };

        int index = config.getInt("player.playbackOrder", 0);
        PlaybackOrder.Order[] orders = PlaybackOrder.Order.values();
        ButtonGroup gr = new ButtonGroup();
        for (PlaybackOrder.Order o : orders) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(o.getText());
            if (o.ordinal() == index) {
                item.setSelected(true);
                app.getPlayer().getPlaybackOrder().setOrder(o);
            }
            item.addActionListener(orderListener);
            item.setName(o.toString());
            gr.add(item);
            orderMenu.add(item);
        }

        playbackMenu.addSeparator();

        playbackMenu.add(tableAction("showNowPlaying", "Scroll to Now Playing"));
        boolean selected = config.getBoolean("playlist.cursorFollowsPlayback", true);
        playbackMenu.add(new JCheckBoxMenuItem("Cursor Follows Playback", selected)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                config.setBoolean("playlist.cursorFollowsPlayback", item.isSelected());
            }
        });

        selected = config.getBoolean("playlist.playbackFollowsCursor", false);
        playbackMenu.add(new JCheckBoxMenuItem("Playback Follows Cursor", selected)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                config.setBoolean("playlist.playbackFollowsCursor", item.isSelected());
            }
        });

        selected = config.getBoolean("player.stopAfterCurrent", false);
        playbackMenu.add(new JCheckBoxMenuItem("Stop After Current", selected)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                config.setBoolean("player.stopAfterCurrent", item.isSelected());
            }
        });
    }
}
