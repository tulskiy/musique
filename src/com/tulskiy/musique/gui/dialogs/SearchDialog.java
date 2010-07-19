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

import com.tulskiy.musique.gui.playlist.PlaylistColumn;
import com.tulskiy.musique.gui.playlist.PlaylistTable;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: Jun 9, 2010
 */
public class SearchDialog extends JDialog {
    private final String[] fields = {
            "artist", "title", "album", "albumArtist", "year", "genre", "fileName"
    };

    private JTextField searchField;
    private PlaylistTable table;
    private Playlist view = new Playlist();
    private ArrayList<Integer> viewToModel = new ArrayList<Integer>();
    private Playlist playlist;

    public SearchDialog(final PlaylistTable playlistTable) {
        super(playlistTable.getParentFrame(), "Search", false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 300);
        setLocationRelativeTo(playlistTable.getParentFrame());
        setDefaultLookAndFeelDecorated(false);

        setLayout(new BorderLayout());
        searchField = new JTextField();

        ArrayList<PlaylistColumn> columns = new ArrayList<PlaylistColumn>();
        columns.add(new PlaylistColumn("Name", 400, "[%artist% - ]$if3(%title%,%fileName%)"));
        columns.add(new PlaylistColumn("Length", 60, "%length%"));
        columns.add(new PlaylistColumn("Album", 100, "%album%"));

        playlist = playlistTable.getPlaylist();
        table = new PlaylistTable(view, columns);
        table.setTrackSelection(false);

        add(searchField, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        final ListSelectionListener listener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] rows = table.getSelectedRows();
                int[] toSelect = new int[rows.length];
                for (int i = 0; i < rows.length; i++) {
                    toSelect[i] = viewToModel.get(rows[i]);
                }
                playlistTable.clearSelection();
                if (toSelect.length > 0)
                    playlistTable.scrollToRow(toSelect[0]);
                for (int row : toSelect) {
                    playlistTable.addRowSelectionInterval(row, row);
                }
            }
        };
        table.getSelectionModel().addListSelectionListener(listener);
        table.addKeyboardAction(KeyStroke.getKeyStroke("ESCAPE"), "escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        table.addKeyboardAction(KeyStroke.getKeyStroke("ENTER"), "playSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlistTable.runAction("playSelected");
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                String text = searchField.getText().toLowerCase().trim();
                view.clear();
                viewToModel.clear();
                if (!text.isEmpty()) {
                    for (int i = 0; i < playlist.size(); i++) {
                        Track track = playlist.get(i);

                        for (String field : fields) {
                            String value = track.getMeta(field);
                            if (!Util.isEmpty(value)) {
                                if (value.toLowerCase().contains(text)) {
                                    view.add(track);
                                    viewToModel.add(i);
                                    break;
                                }
                            }
                        }
                    }
                }
                table.update();
                if (!view.isEmpty()) {
                    table.setRowSelectionInterval(0, 0);
                    listener.valueChanged(null);
                }
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    searchField.transferFocus();
                    if (!view.isEmpty())
                        table.setRowSelectionInterval(0, 0);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                }
            }
        });
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlistTable.runAction("playSelected");
            }
        });
    }
}
