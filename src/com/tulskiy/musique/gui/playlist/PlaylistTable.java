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

import com.tulskiy.musique.db.DBMapper;
import com.tulskiy.musique.gui.custom.SeparatorTable;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Song;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.util.ArrayList;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 8, 2010
 */
public class PlaylistTable extends SeparatorTable {
    private DBMapper<PlaylistColumn> columnDBMapper = DBMapper.create(PlaylistColumn.class);

    private ArrayList<PlaylistColumn> columns = new ArrayList<PlaylistColumn>();
    private Playlist playlist;

    public PlaylistTable() {
        columnDBMapper.loadAll("select * from playlist_columns order by position", columns);
        setModel(new PlaylistModel());
        setColumnModel(new PlaylistColumnModel());
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        updateTable();
    }

    public void updateTable() {
        revalidate();
        repaint();
    }

    public Song getSelectedSong() {
        int index = getSelectedRow();
        return playlist.get(index);
    }

    class PlaylistModel extends AbstractTableModel {
        public int getRowCount() {
            return playlist.size();
        }

        public int getColumnCount() {
            return 0;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return columns.get(columnIndex).getValue(playlist.get(rowIndex));
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return ImageIcon.class;
            else return super.getColumnClass(columnIndex);
        }
    }

    class PlaylistColumnModel extends DefaultTableColumnModel {
        PlaylistColumnModel() {
            for (int i = 0; i < columns.size(); i++) {
                PlaylistColumn pc = columns.get(i);
                TableColumn tc = new TableColumn(i, pc.getSize());
                tc.setHeaderValue(pc.getName());
                addColumn(tc);
            }
        }

        @Override
        protected void fireColumnMarginChanged() {
            super.fireColumnMarginChanged();

            for (int i = 0; i < getColumnCount(); i++) {
                TableColumn tableColumn = tableColumns.get(i);
                PlaylistColumn pc = columns.get(tableColumn.getModelIndex());
                pc.setSize(tableColumn.getWidth());
                columnDBMapper.save(pc);
            }
        }
    }
}
