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
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.PluginLoader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class PlaylistPanel extends JPanel {
    private DBMapper<PlaylistColumn> columnDBMapper = DBMapper.create(PlaylistColumn.class);
    private DBMapper<Song> songDBMapper = DBMapper.create(Song.class);

    private Application app = Application.getInstance();
    private PlaylistTable table;
    private PlaylistManager playlistManager;
    private JComboBox playlistSelection;
    private ArrayList<PlaylistColumn> columns = new ArrayList<PlaylistColumn>();
    private Playlist playlist;
    private JTextField searchField;

    //stuff for popup menu
    private TableColumn tc;
    private Song song;
    private JFrame parentFrame;

    public PlaylistPanel() {

        playlistManager = app.getPlaylistManager();
        playlist = playlistManager.getCurrentPlaylist();
        playlistSelection = new JComboBox(new Vector<Playlist>(playlistManager.getPlaylists()));
        playlistSelection.setSelectedItem(playlist);

        playlistSelection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                playlist = (Playlist) box.getSelectedItem();
                searchField.setText("");
                table.setPlaylist(playlist);
                playlistManager.selectPlaylist(playlist);
            }
        });

        columnDBMapper.loadAll("select * from playlist_columns order by position", columns);
        table = new PlaylistTable(playlist, columns);

        setLayout(new BorderLayout());
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(5));
        box.add(new JLabel("Playlist "));
        box.add(playlistSelection);
        box.add(Box.createHorizontalStrut(10));
        box.add(new JLabel("Search: "));
        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(300, 40));
        searchField.setPreferredSize(new Dimension(300, 0));
        box.add(searchField);
        box.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        add(box, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(table);
        add(tableScrollPane, BorderLayout.CENTER);

        buildListeners();
        createPopupMenu();
    }

    private void createPopupMenu() {
        final JPopupMenu headerMenu = new JPopupMenu();
        final JTableHeader header = table.getTableHeader();

        headerMenu.add(new JMenuItem("Add Column")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaylistColumn column = new PlaylistColumn();
                ColumnDialog dialog = new ColumnDialog(getParentFrame(), "Add Column", column);
                if (dialog.showDialog()) {
                    table.saveColumns();
                    columns.add(column);
                    table.createDefaultColumnsFromModel();
                    columnDBMapper.save(column);
                }
            }
        });
        headerMenu.add(new JMenuItem("Edit Column")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tc == null) return;
                PlaylistColumn column = columns.get(tc.getModelIndex());
                ColumnDialog dialog = new ColumnDialog(getParentFrame(), "Edit Column", column);
                if (dialog.showDialog()) {
                    tc.setHeaderValue(column.getName());
                    table.update();
                }
            }
        });
        headerMenu.add(new JMenuItem("Remove Column")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tc == null) return;
                PlaylistColumn pc = columns.remove(tc.getModelIndex());
                table.createDefaultColumnsFromModel();
                columnDBMapper.delete(pc);
            }
        });

        header.addMouseListener(new MouseAdapter() {
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
                    int index = header.getColumnModel().getColumnIndexAtX(e.getX());
                    if (index != -1) {
                        tc = header.getColumnModel().getColumn(index);
                    }
                    headerMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        final JPopupMenu tableMenu = new JPopupMenu();

        tableMenu.add(new JMenuItem("Add to Queue")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        tableMenu.add(new JMenuItem("Remove")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        tableMenu.add(new JMenuItem("Properties")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInfo(song);
            }
        });

        table.addMouseListener(new MouseAdapter() {
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
                    song = table.selectSongAt(e.getPoint());
                    tableMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

    }

    public JFrame getParentFrame() {
        if (parentFrame == null) {
            parentFrame = (JFrame) getRootPane().getParent();
        }
        return parentFrame;
    }

    public void showInfo(Song s) {
        SongInfoDialog dialog = new SongInfoDialog(getParentFrame(), s);
        if (dialog.showDialog()) {
            try {
                songDBMapper.save(song);
                PluginLoader.getAudioFileWriter(song.getFilePath()).write(song);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        table.update();
    }

    public void buildListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
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

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                table.filter(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                table.filter(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                table.filter(searchField.getText());
            }
        });
    }

    public void shutdown() {
        table.saveColumns();

        for (PlaylistColumn c : columns) {
            columnDBMapper.save(c);
        }
    }

    public void removeSelected() {
        ArrayList<Song> toRemove = new ArrayList<Song>();
        Playlist pl = playlistManager.getCurrentPlaylist();

        for (int i : table.getSelectedRows()) {
            toRemove.add(pl.get(table.convertColumnIndexToModel(i)));
        }

        pl.removeAll(toRemove);
        table.getRowSorter().rowsDeleted(
                table.getSelectionModel().getMinSelectionIndex(),
                table.getSelectionModel().getMaxSelectionIndex());
        table.clearSelection();

        update();
    }

    public void addPlaylist(String name) {
        playlist = playlistManager.addPlaylist(name);
        playlistSelection.addItem(playlist);
        playlistSelection.setSelectedItem(playlist);
    }

    public void removePlaylist() {
        playlistManager.removePlaylist(playlist);
        playlistSelection.removeItem(playlist);
        if (playlistManager.getTotalPlaylists() == 0) {
            addPlaylist("Default");
        }
        playlistSelection.setSelectedIndex(0);
    }

    public void clearPlaylist() {
        Playlist pl = playlistManager.getCurrentPlaylist();
//        int size = pl.size();
//        table.getRowSorter().allRowsChanged();
        pl.clear();
        table.update();
    }
}
