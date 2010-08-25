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
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerEvent;
import com.tulskiy.musique.audio.player.PlayerListener;
import com.tulskiy.musique.gui.dialogs.*;
import com.tulskiy.musique.gui.grouptable.GroupTable;
import com.tulskiy.musique.gui.grouptable.Separator;
import com.tulskiy.musique.gui.playlist.dnd.PlaylistTransferHandler;
import com.tulskiy.musique.playlist.PlaybackOrder;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Author: Denis Tulskiy
 * Date: May 13, 2010
 */
public class PlaylistTable extends GroupTable {
    private static Application app = Application.getInstance();
    private static Player player = app.getPlayer();
    private static Configuration config = app.getConfiguration();

    private Playlist playlist;
    private ArrayList<PlaylistColumn> columns;
    private PlaylistModel model;

    private JScrollPane scrollPane;
    private final ImageIcon emptyIcon = new ImageIcon(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));

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
                    PlaybackOrder order = player.getPlaybackOrder();
                    order.setLastPlayed(null);
                    app.getPlaylistManager().selectPlaylist(playlist);
                }
            }
        });
        final PlaylistTable comp = this;
        aMap.put("showProperties", new AbstractAction("Properties") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Track> tracks = getSelectedSongs();
                if (tracks.isEmpty())
                    return;
                TracksInfoDialog dialog = new TracksInfoDialog(comp, tracks);
                dialog.setVisible(true);
            }
        });
        aMap.put("showNowPlaying", new AbstractAction("Scroll to Now Playing") {
            @Override
            public void actionPerformed(ActionEvent e) {
                scrollToSong(player.getTrack());
            }
        });
        aMap.put("removeSelected", new AbstractAction("Remove") {
            @Override
            public void actionPerformed(ActionEvent e) {
                playlist.removeAll(getSelectedSongs());

                clearSelection();
                playlist.firePlaylistChanged();

                model.fireTableDataChanged();
                update();
            }
        });
        aMap.put("enqueue", new AbstractAction("Add to Queue  ") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Track track : getSelectedSongs()) {
                    PlaybackOrder order = player.getPlaybackOrder();
                    order.enqueue(track, playlist);
                    update();
                }
            }
        });
        aMap.put("clearQueue", new AbstractAction("Clear Playback Queue") {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlaybackOrder order = player.getPlaybackOrder();
                order.flushQueue();
                update();
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
                        if (config.getBoolean("playlist.cursorFollowsPlayback", true)) {
                            runAction("showNowPlaying");
                        }

                        if (config.getBoolean("playlist.playbackFollowsCursor", false)) {
                            PlaybackOrder order = player.getPlaybackOrder();
                            order.setLastPlayed(null);
                        }
                        break;
                    case STOPPED:
                        int index = indexOf(player.getTrack());
                        if (index != -1)
                            setRowSelectionInterval(index, index);
                        break;
                }
            }
        });

        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    TableColumnModel model = getTableHeader().getColumnModel();
                    int index = model.getColumnIndexAtX(e.getX());
                    if (index != -1) {
                        final int col = model.getColumn(index).getModelIndex();
                        final PlaylistColumn pc = columns.get(col);
                        playlist.sort(pc.getExpression(), true);
                    }
                }
            }
        });

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ArrayList<Track> tracks = getSelectedSongs();
                if (tracks.isEmpty())
                    return;
                Track track = tracks.get(0);
                config.put("playlist.selectedTrack", track);
                if (config.getBoolean("playlist.playbackFollowsCursor", false)) {
                    PlaybackOrder order = player.getPlaybackOrder();
                    order.setLastPlayed(track);
                }
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

    @Override
    public void updateUI() {
        super.updateUI();

        Color text = config.getColor("gui.color.text", null);
        if (text != null) {
            setForeground(text);
        }

        Color background = config.getColor("gui.color.background", null);
        if (background != null) {
            setBackground(background);
        }

        Color selection = config.getColor("gui.color.selection", null);
        if (selection != null) {
            setSelectionBackground(selection);
        }

        Color highlight = config.getColor("gui.color.highlight", null);
        if (highlight != null) {
            setSeparatorColor(highlight);
        }

        Font defaultFont = config.getFont("gui.font.default", null);
        if (defaultFont != null) {
            setFont(defaultFont);
        }
    }

    public int indexOf(Track track) {
        return playlist.indexOf(track);
    }

    public void scrollToSong(Track track) {
        int index = indexOf(track);
        if (index != -1) {
            scrollToRow(index);
            setRowSelectionInterval(index, index);
        }
    }

    public ArrayList<Track> getSelectedSongs() {
        int[] rows = getSelectedRows();
        ArrayList<Track> tracks = new ArrayList<Track>();
        for (int row : rows) {
            Track track = playlist.get(row);
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

    public void buildMenus() {
        final JTableHeader header = getTableHeader();
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
                    JPopupMenu headerMenu = buildHeaderMenu();
                    headerMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

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
                    JPopupMenu popup = buildTableMenu();
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private JPopupMenu buildHeaderMenu() {
        final JPopupMenu headerMenu = new JPopupMenu();

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
        headerMenu.add(new JMenuItem("Remove Column")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedColumn == null) return;
                saveColumns();
                getTableHeader().setDraggedColumn(null);
                columns.remove(selectedColumn.getModelIndex());
                // trying to fix ArrayIndexOutOfBoundException
                RepaintManager.currentManager(getTableHeader()).markCompletelyClean(getTableHeader());
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

        final String[] groupItems = {"None", "Artist", "Album Artist", "Artist/Album",
                "Artist/Album/Date", null, "Custom"};
        final String[] groupValues = {null, "%artist%", "%albumArtist%", "%albumArtist%[ - %album%",
                "%albumArtist%[ - %album%][ '['%year%']']"
        };
        JMenu groups = new JMenu("Groups");
        ActionListener groupListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = rowAtPoint(getVisibleRect().getLocation());
                Track firstVisibleTrack;
                do {
                    firstVisibleTrack = playlist.get(row++);
                } while (firstVisibleTrack instanceof Separator);

                JMenuItem src = (JMenuItem) e.getSource();
                Integer index = (Integer) src.getClientProperty("index");
                if (index < groupItems.length - 1) {
                    playlist.groupBy(groupValues[index]);
                } else {
                    Object ret = JOptionPane.showInputDialog(getParentFrame(),
                            "Select formatting",
                            config.getString("playlist.groupBy", playlist.getGroupBy()));
                    if (ret != null) {
                        playlist.groupBy(ret.toString());
                        config.setString("playlist.groupBy", ret.toString());
                    }
                }

                int firstVisibleIndex = playlist.indexOf(firstVisibleTrack);
                if (firstVisibleIndex != -1) {
                    Rectangle cellRect = getCellRect(firstVisibleIndex, 0, true);
                    Rectangle visibleRect = getVisibleRect();
                    cellRect.setSize(visibleRect.width, visibleRect.height);
                    scrollRectToVisible(cellRect);
                }

                update();
            }
        };

        for (int i = 0; i < groupItems.length; i++) {
            String groupValue = groupItems[i];
            if (groupValue == null) {
                groups.addSeparator();
                continue;
            }

            AbstractButton item = groups.add(groupValue);
            item.setIcon(emptyIcon);
            item.addActionListener(groupListener);
            item.putClientProperty("index", i);
        }

        headerMenu.add(groups);

        JMenu sort = new JMenu("Sort");
        String[] sortItems = {
                "Sort by...", "Randomize", "Reverse",
                "Sort by Artist", "Sort by Album",
                "Sort by File Path", "Sort by Title",
                "Sort by Track Number", "Sort by Album Artist/Year/Album/Disc/Track/File Name"
        };

        final String[] sortValues = {
                null, null, null, "%artist%", "%album%",
                "%file%", "%title%", "%trackNumber%",
                "%albumArtist% - %year% - %album% - %discNumber% - %trackNumber% - %fileName%"
        };

        ActionListener sortListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem src = (JMenuItem) e.getSource();
                Integer index = (Integer) src.getClientProperty("index");
                switch (index) {
                    case 0:
                        Object ret = JOptionPane.showInputDialog(getParentFrame(),
                                "Sort By...",
                                config.getString("playlist.sortString", ""));
                        if (ret != null) {
                            playlist.sort(ret.toString(), false);
                            config.setString("playlist.sortString", ret.toString());
                        }

                        break;
                    case 1:
                        Collections.shuffle(playlist);
                        playlist.firePlaylistChanged();
                        break;
                    case 2:
                        Collections.reverse(playlist);
                        playlist.firePlaylistChanged();
                        break;
                    default:
                        playlist.sort(sortValues[index], false);
                }

                update();
            }
        };

        for (int i = 0; i < sortItems.length; i++) {
            String sortValue = sortItems[i];
            if (sortValue == null) {
                sort.addSeparator();
                continue;
            }

            AbstractButton item = sort.add(sortValue);
            item.setIcon(emptyIcon);
            item.addActionListener(sortListener);
            item.putClientProperty("index", i);
        }

        headerMenu.add(sort);
        Util.fixIconTextGap(headerMenu);
        return headerMenu;
    }

    private JPopupMenu buildTableMenu() {
        final ActionMap aMap = getActionMap();
        final JPopupMenu tableMenu = new JPopupMenu();
        final JTable owner = this;
        JMenuItem item;

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                Action action = aMap.get(cmd);
                if (action != null)
                    action.actionPerformed(new ActionEvent(owner,
                            ActionEvent.ACTION_PERFORMED,
                            null));
            }
        };

        item = tableMenu.add("Cut");
        item.addActionListener(listener);
        item.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));

        item = tableMenu.add("Copy");
        item.addActionListener(listener);
        item.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));

        item = tableMenu.add("Paste");
        item.addActionListener(listener);
        item.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));

        tableMenu.addSeparator();
        item = tableMenu.add(aMap.get("enqueue"));
        item.setIcon(emptyIcon);
        item.setAccelerator(KeyStroke.getKeyStroke("Q"));
        tableMenu.add(new JMenuItem("Reload Tags")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgressDialog dialog = new ProgressDialog(getParentFrame(), "Reloading Tags");
                dialog.show(new Task() {
                    String currentTrack;
                    float progress = 0;
                    boolean abort = false;

                    @Override
                    public String getStatus() {
                        return "Reading tags: " + currentTrack;
                    }

                    @Override
                    public void abort() {
                        abort = true;
                    }

                    @Override
                    public void start() {
                        ArrayList<Track> selectedSongs = getSelectedSongs();
                        for (int i = 0; i < selectedSongs.size(); i++) {
                            Track track = selectedSongs.get(i);
                            if (abort)
                                break;
                            if (track.isFile() && track.getSubsongIndex() == 0) {
                                currentTrack = track.getFile().getName();
                                progress = (float) i / selectedSongs.size();
                                AudioFileReader reader = TrackIO.getAudioFileReader(track.getFile().getName());
                                track.clearTags();
                                reader.readSingle(track);
                            }
                        }
                        update();
                    }

                    @Override
                    public boolean isIndeterminate() {
                        return false;
                    }

                    @Override
                    public float getProgress() {
                        return progress;
                    }
                });
            }
        });
        tableMenu.add("Convert").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ConverterDialog(owner, getSelectedSongs()).setVisible(true);
            }
        });
        tableMenu.add(aMap.get("removeSelected")).setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        tableMenu.add(aMap.get("showProperties")).setAccelerator(KeyStroke.getKeyStroke("alt ENTER"));
        Util.fixIconTextGap(tableMenu);
        return tableMenu;
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
