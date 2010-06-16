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

import com.tulskiy.musique.gui.custom.SeparatorTable;
import com.tulskiy.musique.playlist.Track;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Author: Denis Tulskiy
 * Date: May 22, 2010
 */
public class SongInfoDialog extends JDialog {
    private boolean accept = false;
    private boolean changed = false;
    private String[] tagKeys = new String[]{
            "Artist", "Title", "Album", "Year",
            "Album Artist", "Genre", "Track Number",
            "Total Tracks", "Disc Number", "Total Discs"
    };

    private String[] tagValues = new String[tagKeys.length];

    private String[] propKeys = new String[]{
            "File Path", "Codec", "Length", "Total Samples",
            "Sample Rate", "Bitrate", "Sunsong Index"
    };

    private String[] propValues = new String[propKeys.length];

    private JButton write;
    private JLabel status;

    public SongInfoDialog(JFrame owner, final Track track) {
        super(owner, "Song Properties", true);
        setLayout(new BorderLayout());

        JComponent tagsTable = createTable(new MyModel(new String[][]{tagKeys, tagValues}));
        JComponent propsTable = createTable(new MyModel(new String[][]{propKeys, propValues}) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });

        JTabbedPane tp = new JTabbedPane();
        tp.setFocusable(false);
        tp.addTab("Metadata", tagsTable);
        tp.addTab("Properties", propsTable);

        add(tp, BorderLayout.CENTER);

        Box b1 = new Box(BoxLayout.X_AXIS);
        b1.add(Box.createHorizontalGlue());
        write = new JButton("Write");
        b1.add(write);
        write.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (changed) {
                    saveSong(track);
                    accept = true;
                } else {
                    accept = false;
                }
                setVisible(false);
            }
        });
        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accept = false;
                setVisible(false);
            }
        });

        b1.add(Box.createHorizontalStrut(5));
        b1.add(cancel);
        b1.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        add(b1, BorderLayout.SOUTH);

        status = new JLabel();
        status.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        add(status, BorderLayout.NORTH);

        setSize(400, 380);
        setLocationRelativeTo(owner);

        loadSong(track);
    }

    private void loadSong(Track track) {
        changed = false;
//        tagValues[0] = track.getArtist();
//        tagValues[1] = track.getTitle();
//        tagValues[2] = track.getAlbum();
//        tagValues[3] = track.getYear();
//        tagValues[4] = track.getAlbumArtist();
//        tagValues[5] = track.getGenre();
//        tagValues[6] = track.getTrackNumber();
//        tagValues[7] = track.getTotalTracks();
//        tagValues[8] = track.getDiscNumber();
//        tagValues[9] = track.getTotalDiscs();

//        propValues[0] = track.getFilePath();
//        propValues[1] = track.getCodec();
//        propValues[2] = track.getLength();
        propValues[3] = String.valueOf(track.getTotalSamples());
        propValues[4] = String.valueOf(track.getSampleRate()) + " Hz";
//        propValues[5] = String.valueOf(track.getBitrate()) + " kbps";
        propValues[6] = String.valueOf(track.getSubsongIndex());

//        if (track.getCueID() != -1) {
//            write.setEnabled(false);
//            status.setText("WARNING: Editing tags for CUE files is not implemented");
//        } else {
//            write.setEnabled(true);
//            status.setText("");
//        }
    }

    private void saveSong(Track track) {
        track.setArtist(tagValues[0]);
        track.setTitle(tagValues[1]);
        track.setAlbum(tagValues[2]);
        track.setYear(tagValues[3]);
        track.setAlbumArtist(tagValues[4]);
        track.setGenre(tagValues[5]);
        track.setTrackNumber(tagValues[6]);
        track.setTotalTracks(tagValues[7]);
        track.setDiscNumber(tagValues[8]);
        track.setTotalDiscs(tagValues[9]);
    }

    public boolean showDialog() {
        setVisible(true);
        return accept;
    }

    private JComponent createTable(TableModel model) {
        final SeparatorTable table = new SeparatorTable();
        table.setModel(model);

        table.getColumn("Key").setPreferredWidth(100);
        table.getColumn("Value").setPreferredWidth(270);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setGridColor(Color.lightGray);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addKeyboardAction(KeyStroke.getKeyStroke("ENTER"), "startEditing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.editCellAt(table.getSelectedRow(), 1);
            }
        });

        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (table.isEditing() && (
                        e.getKeyCode() == KeyEvent.VK_DOWN ||
                        e.getKeyCode() == KeyEvent.VK_UP)) {
                    table.getCellEditor().stopCellEditing();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return scrollPane;
    }

    private class MyModel extends AbstractTableModel {
        private String[][] items;

        private MyModel(String[][] items) {
            this.items = items;
        }

        @Override
        public int getRowCount() {
            return items[0].length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return items[columnIndex][rowIndex];
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? "Key" : "Value";
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            tagValues[rowIndex] = String.valueOf(aValue);
            changed = true;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 && write.isEnabled();
        }
    }
}
