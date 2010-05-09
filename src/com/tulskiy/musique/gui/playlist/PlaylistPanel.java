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
import com.tulskiy.musique.gui.custom.SeparatorTable;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class PlaylistPanel extends JPanel {
    private DBMapper<PlaylistColumn> columnDBMapper = DBMapper.create(PlaylistColumn.class);

    private Application app = Application.getInstance();
    private PlaylistTable table;
    private PlaylistManager playlistManager;
    private JComboBox playlistSelection;
    private ArrayList<PlaylistColumn> columns = new ArrayList<PlaylistColumn>();

    public PlaylistPanel() {
        playlistManager = app.getPlaylistManager();
        Playlist playlist = playlistManager.getCurrentPlaylist();
        final ArrayList<Playlist> playlists = playlistManager.getPlaylists();
        playlistSelection = new JComboBox();
        playlistSelection.setModel(new DefaultComboBoxModel() {
            @Override
            public int getSize() {
                return playlists.size();
            }

            @Override
            public Object getElementAt(int index) {
                return playlists.get(index);
            }

            @Override
            public int getIndexOf(Object anObject) {
                return playlists.indexOf(anObject);
            }
        });

        playlistSelection.setSelectedIndex(0);
        playlistSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                System.out.println(box.getSelectedItem());
                table.setPlaylist((Playlist) box.getSelectedItem());
                playlistManager.selectPlaylist(box.getSelectedIndex());
                System.out.println(box.getSelectedIndex());
            }
        });

        columnDBMapper.loadAll("select * from playlist_columns order by position", columns);
        table = new PlaylistTable(playlist);

        table.setSelectionBackground(new Color(Integer.valueOf("f5b796", 16)));

        setLayout(new BorderLayout());
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JLabel("Playlist "));
        box.add(playlistSelection);

        add(box, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(table);

        add(tableScrollPane, BorderLayout.CENTER);

//        JTabbedPane tabbedPane = new JTabbedPane();
//        add(tabbedPane, BorderLayout.CENTER);
//        tabbedPane.addTab("Default", tableScrollPane);
//        JLabel tabName = new JLabel("Default");
//        tabName.setPreferredSize(new Dimension(50, 10));
//        tabbedPane.setTabComponentAt(0, tabName);
//        tabbedPane.setFocusable(false);

        buildListeners();
    }

    class SelectionModel extends DefaultComboBoxModel {

    }

    public void update() {
        table.update();
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
                table.update();
            }
        });
    }

    public void shutdown() {
        table.saveColumns();

        for (PlaylistColumn c : columns) {
            columnDBMapper.save(c);
        }
    }

    public void removeItems() {
        ArrayList<Song> toRemove = new ArrayList<Song>();
        Playlist pl = playlistManager.getCurrentPlaylist();

        for (int i : table.getSelectedRows()) {
            toRemove.add(pl.get(i));
        }

        pl.removeAll(toRemove);
        table.clearSelection();

        update();
    }

    class PlaylistTable extends SeparatorTable {

        private Playlist playlist;

        public PlaylistTable(Playlist playlist) {
            this.playlist = playlist;
            setModel(new PlaylistModel());

            for (int i = 0; i < columns.size(); i++) {
                PlaylistColumn pc = columns.get(i);
                getColumnModel().getColumn(i).setPreferredWidth(pc.getSize());
            }
        }

        public void setPlaylist(Playlist playlist) {
            this.playlist = playlist;
            update();
        }

        public void update() {
            revalidate();
            repaint();
        }

        public Song getSelectedSong() {
            int index = getSelectedRow();
            if (index > 0)
                return playlist.get(index);

            return null;
        }

        class PlaylistModel extends AbstractTableModel {
            public int getRowCount() {
                return playlist.size();
            }

            public int getColumnCount() {
                return columns.size();
            }

            @Override
            public String getColumnName(int column) {
                return columns.get(column).getName();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return columns.get(columnIndex).getValue(playlist.get(rowIndex));
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columns.get(columnIndex).getType();
            }
        }

        private void saveColumns() {
            for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                TableColumn tc = getColumnModel().getColumn(i);
                PlaylistColumn pc = columns.get(tc.getModelIndex());
                pc.setPosition(i);
                pc.setSize(tc.getWidth());
            }
        }
    }
}
