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
import com.tulskiy.musique.gui.custom.SeparatorTable;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Song;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Author: Denis Tulskiy
 * Date: May 13, 2010
 */
public class PlaylistTable extends SeparatorTable implements PlaybackOrder {
    enum Order {
        DEFAULT("Default"),
        REPEAT("Repeat"),
        REPEAT_TRACK("Repeat Track"),
        SHUFFLE("Random");

        private String text;

        Order(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private Playlist playlist;
    private ArrayList<PlaylistColumn> columns;
    private TableRowSorter<PlaylistModel> sorter;
    private PlaylistModel model;
    private Order order = Order.DEFAULT;
    private LinkedList<Song> queue = new LinkedList<Song>();
    private Song lastPlayed;

    public PlaylistTable(Playlist playlist, ArrayList<PlaylistColumn> columns) {
        this.playlist = playlist;
        this.columns = columns;

        model = new PlaylistModel();
        setModel(model);
        sorter = new TableRowSorter<PlaylistModel>(model);
        setRowSorter(sorter);
        getTableHeader().setPreferredSize(new Dimension(10000, 25));
    }

    @Override
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                PlaylistColumn pc = columns.get(i);
                getColumnModel().getColumn(i).setPreferredWidth(pc.getSize());
            }
        }
    }

    public void filter(String text) {
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlaylist(Playlist playlist) {
        sorter.setRowFilter(null);
        this.playlist = playlist;
        model.fireTableDataChanged();
        update();
    }

    public void dataChanged() {
        model.fireTableDataChanged();
    }

    public void update() {
//        model.fireTableDataChanged();
        getTableHeader().revalidate();
        getTableHeader().repaint();
        revalidate();
        repaint();
    }

    public int indexOf(Song song) {
        int index = playlist.indexOf(song);
        if (index != -1)
            index = convertRowIndexToView(index);

        return index;
    }

    public void scrollToSong(Song song) {
        int index = indexOf(song);
        if (index != -1)
            scrollToRow(index);
    }

    public Song getSelectedSong() {
        int index = getSelectedRow();
        if (index >= 0)
            return playlist.get(convertRowIndexToModel(index));

        return null;
    }

    public ArrayList<Song> selectSongsAt(Point p) {
        int index = rowAtPoint(p);
        if (index == -1)
            return null;

        ArrayList<Song> res = new ArrayList<Song>();

        if (!isRowSelected(index)) {
            setRowSelectionInterval(index, index);
        }

        for (int i : getSelectedRows()) {
            res.add(playlist.get(convertRowIndexToModel(i)));
        }

        return res;
    }

    public void removeSelected() {
        ArrayList<Song> toRemove = new ArrayList<Song>();

        for (int i : getSelectedRows()) {
            toRemove.add(playlist.get(convertRowIndexToModel(i)));
        }

        playlist.removeAll(toRemove);
        sorter.rowsDeleted(
                getSelectionModel().getMinSelectionIndex(),
                getSelectionModel().getMaxSelectionIndex());

        clearSelection();

        model.fireTableDataChanged();
        update();
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setLastPlayed(Song lastPlayed) {
        this.lastPlayed = lastPlayed;
        int index = indexOf(lastPlayed);
        if (index != -1) {
            scrollToRow(index);
            setRowSelectionInterval(index, index);
        }
    }

    public void enqueue(Song song) {
        queue.add(song);
        updateQueuePositions();
    }

    public void clearQueue() {
        for (Song song : queue) {
            song.setQueuePosition(-1);
        }
        queue.clear();
        updateQueuePositions();
    }

    private void updateQueuePositions() {
        for (int i = 0; i < queue.size(); i++) {
            queue.get(i).setQueuePosition(i + 1);
        }
        update();
    }

    @Override
    public Song next(Song file) {
        int index;

        if (!queue.isEmpty()) {
            Song song = queue.poll();
            song.setQueuePosition(-1);
            updateQueuePositions();
            return song;
        }

        if (file == null) {
            index = indexOf(lastPlayed);
            if (index == -1)
                index = 0;
        } else {
            index = indexOf(file);
            if (index == -1)
                return null;


            int size = sorter.getViewRowCount();

            switch (order) {
                case DEFAULT:
                    index = index < size - 1 ? index + 1 : -1;
                    break;
                case REPEAT:
                    index = (index + 1) % size;
                    break;
                case REPEAT_TRACK:
                    break;
                case SHUFFLE:
                    index = (int) (Math.random() * size);
                    break;
            }
        }

        return index != -1 ? playlist.get(convertRowIndexToModel(index)) : null;
    }

    @Override
    public Song prev(Song file) {
        int index = indexOf(file);
        if (index == -1)
            return null;

        int size = sorter.getViewRowCount();

        switch (order) {
            case DEFAULT:
                index--;
                break;
            case REPEAT:
                index--;
                if (index < 0)
                    index += size;
                break;
            case REPEAT_TRACK:
                break;
            case SHUFFLE:
                index = (int) (Math.random() * size);
                break;
        }

        return index > -1 ? playlist.get(convertRowIndexToModel(index)) : null;
    }

    class PlaylistModel extends AbstractTableModel {
        public int getRowCount() {
            return playlist == null ? 0 : playlist.size();
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
    }

    public void saveColumns() {
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            TableColumn tc = getColumnModel().getColumn(i);
            PlaylistColumn pc = columns.get(tc.getModelIndex());
            pc.setPosition(i);
            pc.setSize(tc.getWidth());
        }
    }
}
