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

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.player.PlaybackOrder;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.gui.custom.Separator;
import com.tulskiy.musique.gui.custom.GroupTable;
import com.tulskiy.musique.gui.dialogs.ColumnDialog;
import com.tulskiy.musique.gui.dialogs.SongInfoDialog;
import com.tulskiy.musique.gui.playlist.dnd.PlaylistTransferHandler;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.system.TrackIO;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * Author: Denis Tulskiy
 * Date: May 13, 2010
 */
public class PlaylistTable extends GroupTable implements PlaybackOrder {
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

    private Application app = Application.getInstance();
    private Player player = app.getPlayer();
    private Configuration config = app.getConfiguration();

    private Playlist playlist;
    private ArrayList<PlaylistColumn> columns;
    private TableRowSorter<PlaylistModel> sorter;
    private PlaylistModel model;
    private Order order = Order.DEFAULT;
    private LinkedList<Track> queue = new LinkedList<Track>();
    private Track lastPlayed;

    private JScrollPane scrollPane;

    public PlaylistTable(Playlist playlist, ArrayList<PlaylistColumn> columns) {
        this.playlist = playlist;
        this.columns = columns;

        model = new PlaylistModel();
        setModel(model);
        getTableHeader().setPreferredSize(new Dimension(10000, 20));
        scrollPane = new JScrollPane(this);
        buildActions();
        buildMenus();
    }

    public void createSorter() {
        sorter = new TableRowSorter<PlaylistModel>(model);
        setRowSorter(sorter);
    }

    public void buildActions() {
        ActionMap aMap = getActionMap();
        InputMap iMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        aMap.put("next", new AbstractAction("Next") {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.next();
            }
        });
        aMap.put("stop", new AbstractAction("Stop") {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.stop();
            }
        });
        aMap.put("play", new AbstractAction("Play") {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.play();
            }
        });
        aMap.put("pause", new AbstractAction("Pause") {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.pause();
            }
        });
        aMap.put("prev", new AbstractAction("Previous") {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.prev();
            }
        });
        aMap.put("playSelected", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Track> tracks = getSelectedSongs();
                if (!tracks.isEmpty()) {
                    player.open(tracks.get(0));
                    player.play();
                    app.getPlaylistManager().selectPlaylist(playlist);
                }
            }
        });
        aMap.put("showProperties", new AbstractAction("Properties") {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInfo(getSelectedSongs());
            }
        });
        aMap.put("showNowPlaying", new AbstractAction("Scroll to Now Playing") {
            @Override
            public void actionPerformed(ActionEvent e) {
                scrollToSong(player.getSong());
            }
        });
        aMap.put("removeSelected", new AbstractAction("Remove") {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlist.removeAll(getSelectedSongs());
//                sorter.rowsDeleted(
//                        getSelectionModel().getMinSelectionIndex(),
//                        getSelectionModel().getMaxSelectionIndex());

                clearSelection();

                model.fireTableDataChanged();
                update();
            }
        });
        aMap.put("enqueue", new AbstractAction("Add to Queue") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Track track : getSelectedSongs()) {
                    enqueue(track);
                }
            }
        });
        aMap.put("clearQueue", new AbstractAction("Clear Playback Queue") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Track track : queue) {
                    track.setQueuePosition(-1);
                }
                queue.clear();
                updateQueuePositions();
            }
        });


        iMap.put(KeyStroke.getKeyStroke("B"), "next");
        iMap.put(KeyStroke.getKeyStroke("V"), "stop");
        iMap.put(KeyStroke.getKeyStroke("C"), "pause");
        iMap.put(KeyStroke.getKeyStroke("X"), "play");
        iMap.put(KeyStroke.getKeyStroke("Z"), "prev");

        iMap.put(KeyStroke.getKeyStroke("ENTER"), "playSelected");
        iMap.put(KeyStroke.getKeyStroke("alt ENTER"), "showProperties");
        iMap.put(KeyStroke.getKeyStroke("SPACE"), "showNowPlaying");
        iMap.put(KeyStroke.getKeyStroke("DELETE"), "removeSelected");
        iMap.put(KeyStroke.getKeyStroke("Q"), "enqueue");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    runAction("playSelected");
                }
            }
        });
        player.addListener(new PlayerListener() {
            public void onEvent(PlayerEvent e) {
                update();
                switch (e.getEventCode()) {
                    case FILE_OPENED:
                        runAction("showNowPlaying");
                }
            }
        });

        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TableColumnModel model = getTableHeader().getColumnModel();
                int index = model.getColumnIndexAtX(e.getX());
                final int col = model.getColumn(index).getModelIndex();
                final PlaylistColumn pc = columns.get(col);
                Collections.sort(playlist, new Comparator<Track>() {
                    @Override
                    public int compare(Track o1, Track o2) {
                        Object v1 = pc.getValue(o1);
                        Object v2 = pc.getValue(o2);
                        if (v1 != null && v2 != null) {
                            return v1.toString().compareTo(v2.toString());
                        }
                        return 0;
                    }
                });
            }
        });
    }


    public JScrollPane getScrollPane() {
        return scrollPane;
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

    public void setUpDndCCP() {
        setDragEnabled(true);
        setDropMode(DropMode.INSERT_ROWS);
        setTransferHandler(new PlaylistTransferHandler(this));
        ActionMap map = getActionMap();

        Action cutAction = TransferHandler.getCutAction();
        map.put(cutAction.getValue(Action.NAME), cutAction);
        Action copyAction = TransferHandler.getCopyAction();
        map.put(copyAction.getValue(Action.NAME), copyAction);
        Action pasteAction = TransferHandler.getPasteAction();
        map.put(pasteAction.getValue(Action.NAME), pasteAction);
    }


    public void filter(String text) {
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        model.fireTableDataChanged();
        update();
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void dataChanged() {
        model.fireTableDataChanged();
    }

    public void update() {
        getTableHeader().revalidate();
        getTableHeader().repaint();
        revalidate();
        repaint();
    }

    public int indexOf(Track track) {
        int index = playlist.indexOf(track);
        if (index != -1)
            index = convertRowIndexToView(index);

        return index;
    }

    public void scrollToSong(Track track) {
        int index = indexOf(track);
        if (index != -1)
            scrollToRow(index);
    }

    public ArrayList<Track> getSelectedSongs() {
        int[] rows = getSelectedRows();
        ArrayList<Track> tracks = new ArrayList<Track>();
        for (int row : rows) {
            Track track = playlist.get(convertRowIndexToModel(row));
            if (!(track instanceof Separator)) {
                tracks.add(track);
            }
        }
        return tracks;
    }

    private void selectSongsAt(Point p) {
        int index = rowAtPoint(p);
        if (index != -1 && !isRowSelected(index)) {
            setRowSelectionInterval(index, index);
        }
    }

    //stuff for popup menu

    private TableColumn selectedColumn;
    private JFrame parentFrame;

    public JFrame getParentFrame() {
        if (parentFrame == null) {
            try {
                parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            } catch (Exception ignored) {
            }
        }
        return parentFrame;
    }

    public void showInfo(ArrayList<Track> tracks) {
        if (tracks.isEmpty())
            return;
        Track s = tracks.get(0);
        SongInfoDialog dialog = new SongInfoDialog(getParentFrame(), s);
        if (dialog.showDialog()) {
            try {
                TrackIO.getAudioFileWriter(s.getFile().getAbsolutePath()).write(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void buildMenus() {
        ActionMap aMap = getActionMap();
        final JPopupMenu headerMenu = new JPopupMenu();
        final JTableHeader header = getTableHeader();

        headerMenu.add(new JMenuItem("Add Column")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaylistColumn column = new PlaylistColumn();
                ColumnDialog dialog = new ColumnDialog(getParentFrame(), "Add Column", column);
                if (dialog.showDialog()) {
                    saveColumns();
                    columns.add(column);
                    createDefaultColumnsFromModel();
                }
            }
        });
        headerMenu.add(new JMenuItem("Edit Column")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedColumn == null) return;
                PlaylistColumn column = columns.get(selectedColumn.getModelIndex());
                ColumnDialog dialog = new ColumnDialog(getParentFrame(), "Edit Column", column);
                if (dialog.showDialog()) {
                    selectedColumn.setHeaderValue(column.getName());
                    update();
                }
            }
        });
        final JComponent comp = this;
        headerMenu.add(new JMenuItem("Remove Column")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedColumn == null) return;
                saveColumns();
                columns.remove(selectedColumn.getModelIndex());
                // trying to fix ArrayIndexOutOfBoundException
                RepaintManager.currentManager(comp).markCompletelyClean(comp);
                createDefaultColumnsFromModel();
            }
        });
        JCheckBoxMenuItem hideScrollbar = new JCheckBoxMenuItem("Hide Scrollbar");
        headerMenu.add(hideScrollbar).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
                if (item.isSelected()) {
                    config.setBoolean("playlist.hideScrollBar", true);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                } else {
                    config.setBoolean("playlist.hideScrollBar", false);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                }
            }
        });
        hideScrollbar.setSelected(!config.getBoolean("playlist.hideScrollBar", false));
        hideScrollbar.doClick();

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
                        selectedColumn = header.getColumnModel().getColumn(index);
                    }
                    headerMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        final JPopupMenu tableMenu = new JPopupMenu();

        tableMenu.add(aMap.get("enqueue")).setAccelerator(KeyStroke.getKeyStroke("Q"));
        tableMenu.add(new JMenuItem("Reload Tags")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Track track : getSelectedSongs()) {
                    if (track.getSubsongIndex() == 0) {
                        AudioFileReader reader = TrackIO.getAudioFileReader(track.getFile().getName());
                        reader.readSingle(track);
                        update();
                    }
                }
            }
        });
        tableMenu.add(aMap.get("removeSelected")).setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        tableMenu.add(aMap.get("showProperties")).setAccelerator(KeyStroke.getKeyStroke("alt ENTER"));

        addMouseListener(new MouseAdapter() {
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
                    selectSongsAt(e.getPoint());
                    tableMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    // playback order

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setLastPlayed(Track lastPlayed) {
        this.lastPlayed = lastPlayed;
        int index = indexOf(lastPlayed);
        if (index != -1) {
//            scrollToRow(index);
            setRowSelectionInterval(index, index);
        }
    }

    public void enqueue(Track track) {
        queue.add(track);
        updateQueuePositions();
    }

    private void updateQueuePositions() {
        for (int i = 0; i < queue.size(); i++) {
            queue.get(i).setQueuePosition(i + 1);
        }
        update();
    }

    @Override
    public Track next(Track file) {
        int index;

        if (!queue.isEmpty()) {
            Track track = queue.poll();
            track.setQueuePosition(-1);
            updateQueuePositions();
            return track;
        }

        if (file == null) {
            index = indexOf(lastPlayed);
            if (index == -1)
                index = 0;
            //todo check me!
//                index = indexOf(new Track(0));
        } else {
            index = indexOf(file);
            if (index == -1)
                return null;

            int size;
            if (sorter != null)
                size = sorter.getViewRowCount();
            else
                size = playlist.size();

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
    public Track prev(Track file) {
        int index = indexOf(file);
        if (index == -1)
            return null;

        int size;
        if (sorter != null)
            size = sorter.getViewRowCount();
        else
            size = playlist.size();

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
            Track track = playlist.get(rowIndex);
            if (track instanceof Separator)
                return track;
            else
                return columns.get(columnIndex).getValue(track);
        }
    }

    public void saveColumns() {
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            TableColumn tc = getColumnModel().getColumn(i);
            PlaylistColumn pc = columns.get(tc.getModelIndex());
            pc.setPosition(i);
            pc.setSize(tc.getWidth());
        }

        Collections.sort(columns);
    }
}
