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
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

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

        Playlist playlist = playlistManager.getCurrentPlaylist();

        tabs.setSelectedIndex(-1);
        tabs.setSelectedIndex(playlists.indexOf(playlist));

        int lastPlayed = config.getInt("player.lastPlayed", -1);
        if (lastPlayed >= 0 && lastPlayed < playlist.size()) {
            tabs.getSelectedTable().setLastPlayed(playlist.get(lastPlayed));
        }
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
        config.setList("playlist.columns", columns);

        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            PlaylistTable t = tabs.getTableAt(i);
            list.add(t.getVisibleRect().y);
        }
        config.setList("playlist.tabs.bounds", list);

        Track track = app.getPlayer().getSong();
        if (track != null) {
            int index = app.getPlaylistManager().getCurrentPlaylist().indexOf(track);
            config.setInt("player.lastPlayed", index);
        }
    }

    private JMenuItem newItem(String name, String hotkey, ActionListener al) {
        JMenuItem item = new JMenuItem(name);
        item.setAccelerator(KeyStroke.getKeyStroke(hotkey));
        item.addActionListener(al);

        return item;
    }

    private Action tableAction(final String name) {
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaylistTable table = tabs.getSelectedTable();
                if (table != null)
                    table.runAction(name);
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
        JMenu playbackMenu = new JMenu("Playback");
        menuBar.add(playbackMenu);

        ActionMap tMap = tabs.getActionMap();
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
                PlaylistTable table = tabs.getSelectedTable();
                if (table == null)
                    return;

                if (retVal == JFileChooser.APPROVE_OPTION) {
                    ProgressDialog dialog = new ProgressDialog(table.getParentFrame(), "Adding files");
                    dialog.addFiles(table.getPlaylist(), Arrays.asList(fc.getSelectedFiles()));
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
                PlaylistTable table = tabs.getSelectedTable();
                table.getPlaylist().clear();
                table.update();
            }
        });
        editMenu.add(tableAction("removeSelected"));
        editMenu.addSeparator();
        editMenu.add(tableAction("clearQueue"));
        editMenu.add(newItem("Search", "ctrl F", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SearchDialog(tabs.getSelectedTable()).setVisible(true);
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
                PlaylistTable.Order o = PlaylistTable.Order.valueOf(item.getName());
                tabs.getSelectedTable().setOrder(o);
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
//                table.setOrder(o);
            }
            item.addActionListener(orderListener);
            item.setName(o.toString());
            gr.add(item);
            orderMenu.add(item);
        }

        JMenu controlMenu = new JMenu("Control");
        playbackMenu.add(controlMenu);

        controlMenu.add(tableAction("next"));
        controlMenu.add(tableAction("play"));
        controlMenu.add(tableAction("pause"));
        controlMenu.add(tableAction("stop"));
        controlMenu.add(tableAction("prev"));

        playbackMenu.addSeparator();

        playbackMenu.add(tableAction("showNowPlaying"));
    }
}
