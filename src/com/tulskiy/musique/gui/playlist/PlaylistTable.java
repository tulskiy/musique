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

import com.tulskiy.musique.gui.custom.SeparatorTable;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Song;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: May 13, 2010
 */
public class PlaylistTable extends SeparatorTable {

    private Playlist playlist;
    private ArrayList<PlaylistColumn> columns;
    private TableRowSorter<PlaylistModel> sorter;
    private PlaylistModel model;

    public PlaylistTable(Playlist playlist, ArrayList<PlaylistColumn> columns) {
        this.playlist = playlist;
        this.columns = columns;

        model = new PlaylistModel();
        setModel(model);
        sorter = new TableRowSorter<PlaylistModel>(model);
        setRowSorter(sorter);
        getTableHeader().setPreferredSize(new Dimension(100, 25));
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
        update();
    }

    public void update() {
        model.fireTableDataChanged();
        getTableHeader().revalidate();
        getTableHeader().repaint();
        revalidate();
        repaint();
    }

    public Song getSelectedSong() {
        int index = getSelectedRow();
        if (index >= 0)
            return playlist.get(convertRowIndexToModel(index));

        return null;
    }

    public Song selectSongAt(Point p) {
        int index = rowAtPoint(p);
        if (index >= 0) {
            setRowSelectionInterval(index, index);
            return playlist.get(convertRowIndexToModel(index));
        }

        return null;
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

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columns.get(columnIndex).getType();
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
