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

import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.db.DBMapper;
import com.tulskiy.musique.gui.custom.Column;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class PlaylistPanel extends JPanel {
    private Application app = Application.getInstance();
    private PlaylistTable table;
    private Playlist playlist;
    private PlaylistManager playlistManager;
    private JComboBox playlistSelection;

    public PlaylistPanel() {
        playlistManager = app.getPlaylistManager();
        playlist = playlistManager.getCurrentPlaylist();
        Parser p = new Parser();
        Playlist[] playlists = new Playlist[playlistManager.getTotalPlaylists()];
        for (int i = 0; i < playlists.length; i++)
            playlists[i] = playlistManager.getPlaylist(i);
        playlistSelection = new JComboBox(playlists);
        playlistSelection.setModel(new DefaultComboBoxModel() {
            @Override
            public int getSize() {
                return playlistManager.getTotalPlaylists();
            }

            @Override
            public Object getElementAt(int index) {
                return playlistManager.getPlaylist(index);
            }
        });

        playlistSelection.setSelectedItem(playlist);
        playlistSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                table.setPlaylist((Playlist) box.getSelectedItem());
                playlistManager.selectPlaylist(box.getSelectedIndex());
            }
        });

        table = new PlaylistTable();
        table.setPlaylist(playlistManager.getCurrentPlaylist());

        table.setSelectionBackground(new Color(Integer.valueOf("f5b796", 16)));

        setLayout(new BorderLayout());
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(new JLabel("Playlist"));

        add(playlistSelection, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(table);

        add(tableScrollPane, BorderLayout.CENTER);
        buildListeners();
    }

    public void updatePanel() {
        table.updateTable();
        playlistSelection.repaint();
    }

    public void buildListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    app.getPlayer().open(table.getSelectedSong());
                    app.getPlayer().play();
                }
            }
        });

        app.getPlayer().addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                table.updateTable();
            }
        });

    }

    public static void main(String[] args) {
        Application app = Application.getInstance();
        app.load();
        app.getPlaylistManager().getPlaylist(1).clear();
        app.getPlaylistManager().savePlaylists();
        JFrame frame = new JFrame("Test");
        frame.setContentPane(new PlaylistPanel());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        frame.setVisible(true);
    }


}
