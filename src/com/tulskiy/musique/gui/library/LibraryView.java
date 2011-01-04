/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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

package com.tulskiy.musique.gui.library;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.gui.SearchWorker;
import com.tulskiy.musique.gui.components.SearchField;
import com.tulskiy.musique.gui.dialogs.ProgressDialog;
import com.tulskiy.musique.gui.dialogs.SettingsDialog;
import com.tulskiy.musique.gui.dialogs.Task;
import com.tulskiy.musique.gui.dnd.LibraryTransferHandler;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.Library;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * Author: Denis Tulskiy
 * Date: 10/31/10
 */
public class LibraryView extends JPanel {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private LibraryTree tree;
    private Library library;
    private Playlist libraryPlaylist;
    private PlaylistManager playlistManager;
    private SearchWorker searchWorker;
    private Library libraryView;

    public LibraryView() {
        playlistManager = app.getPlaylistManager();
        library = playlistManager.getLibrary();
        for (Playlist playlist : playlistManager.getPlaylists()) {
            if (playlist.isLibraryView()) {
                libraryPlaylist = playlist;
                break;
            }
        }

        initComponents();

        initListeners();

        initDND();
    }

    private void initDND() {
        tree.setDragEnabled(true);
        tree.setTransferHandler(new LibraryTransferHandler());
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        tree = new LibraryTree();
        tree.setRootNode(library.getRootNode());
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        final SearchField searchField = new SearchField();
        searchField.setColumns(30);
        Box viewBox = Box.createHorizontalBox();
        JComboBox viewCombo = new JComboBox(new Object[]{"By Album Artist"});
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(viewCombo);
        p1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "View",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.TOP, new Font("Sans Serif", 0, 11)));
        viewBox.add(p1);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.add(searchField);
        p2.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Filter",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.TOP, new Font("Sans Serif", 0, 11)));
        viewBox.add(p2);
        add(viewBox, BorderLayout.PAGE_END);
        searchField.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (searchWorker != null && !searchWorker.isDone()) {
                    searchWorker.cancel(true);
                }

                searchWorker = new SearchWorker(library.getData(), searchField.getText(), true) {
                    @Override
                    protected void done() {
                        try {
                            Playlist result = get();
                            if (result != null) {
                                libraryView = new Library(result);
                                tree.setRootNode(libraryView.getRootNode());
                            } else {
                                libraryView = null;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                };
                searchWorker.execute();
            }
        });
    }

    private void initListeners() {
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @SuppressWarnings({"unchecked"})
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (config.getBoolean("library.libraryView", false))
                    fillLibraryView();
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    Player player = app.getPlayer();
                    playlistManager.setActivePlaylist(libraryPlaylist);
                    player.getPlaybackOrder().flushQueue();
                    Track nextTrack = null;
                    do {
                        nextTrack = player.getPlaybackOrder().next(nextTrack);
                    } while (nextTrack != null && nextTrack.getLocation() == null);

                    player.open(nextTrack);
                    player.play();
                }
            }
        });

        config.addPropertyChangeListener("library.libraryView", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (config.getBoolean(evt.getPropertyName(), false)) {
                    createLibraryView(false);
                } else {
                    for (Playlist playlist : playlistManager.getPlaylists()) {
                        if (playlist.isLibraryView()) {
                            playlistManager.removePlaylist(playlist);
                            break;
                        }
                    }
                }

            }
        });
    }

    private void fillLibraryView() {
        createLibraryView(true);
        libraryPlaylist.clear();
        libraryPlaylist.addAll(tree.getSelectedTracks(false));
        libraryPlaylist.firePlaylistChanged();
    }

    private void createLibraryView(boolean setVisible) {
        if (libraryPlaylist == null) {
            libraryPlaylist = playlistManager.addPlaylist("Library");
            libraryPlaylist.setLibraryView(true);
        }

        if (!playlistManager.getPlaylists().contains(libraryPlaylist)) {
            playlistManager.addPlaylist(libraryPlaylist);
        }
        if (setVisible)
            playlistManager.setVisiblePlaylist(libraryPlaylist);
    }

    public void addMenu(JMenuBar menuBar) {
        JMenu menu = new JMenu("Library");
        menuBar.add(menu);
        JMenuItem configure = menu.add("Configure     ");
        configure.setIcon(Images.getEmptyIcon());
        configure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SettingsDialog(getRootPane(), "Library").setVisible(true);
            }
        });
        menu.add("Rescan").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgressDialog progressDialog = new ProgressDialog(getRootPane(), "Library Rescan");
                progressDialog.show(new Task() {
                    HashMap<String, Object> map = new HashMap<String, Object>();

                    @Override
                    public String getStatus() {
                        return "Scanning library\n" + String.valueOf(map.get("processing.file"));
                    }

                    @Override
                    public void abort() {
                        map.put("processing.stop", true);
                    }

                    @Override
                    public void start() {
                        int[] selectionRows = tree.getSelectionRows();
                        library.rescan(map);
                        ((DefaultTreeModel) tree.getModel()).reload();
                        tree.revalidate();
                        tree.repaint();
                        if (selectionRows != null)
                            tree.setSelectionRows(selectionRows);
                    }
                });
            }
        });

        Util.fixIconTextGap(menu);
    }

}
