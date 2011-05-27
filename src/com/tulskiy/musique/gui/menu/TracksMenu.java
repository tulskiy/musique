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

package com.tulskiy.musique.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.gui.dialogs.ConverterDialog;
import com.tulskiy.musique.gui.dialogs.FileOperations;
import com.tulskiy.musique.gui.dialogs.ProgressDialog;
import com.tulskiy.musique.gui.dialogs.Task;
import com.tulskiy.musique.gui.playlist.PlaylistTable;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.Util;

/**
 * Author: Denis Tulskiy
 * Date: 2/26/11
 */
public class TracksMenu extends Menu {
    private static Application app = Application.getInstance();
    private static Player player = app.getPlayer();

    public JPopupMenu create(final PlaylistTable parent, final Playlist playlist, final ArrayList<Track> tracks) {
        final ActionMap aMap = parent.getActionMap();
        final JPopupMenu tableMenu = new JPopupMenu();
        JMenuItem item;

        item = tableMenu.add(aMap.get("enqueue"));
        item.setIcon(Images.getEmptyIcon());
        item.setAccelerator(KeyStroke.getKeyStroke("Q"));
        tableMenu.add(new JMenuItem("Reload Tags")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProgressDialog dialog = new ProgressDialog(parent, "Reloading Tags");
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
                        for (int i = 0; i < tracks.size(); i++) {
                            Track track = tracks.get(i);
                            TrackData trackData = track.getTrackData();
                            if (abort)
                                break;
                            if (trackData.isFile() && trackData.getSubsongIndex() == 0) {
                                currentTrack = trackData.getFile().getName();
                                progress = (float) i / tracks.size();
                                AudioFileReader reader = TrackIO.getAudioFileReader(trackData.getFile().getName());
                                trackData.clearTags();
                                reader.reload(track);
                            }
                        }
                        playlist.firePlaylistChanged();
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

        ActionListener fileOpsListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                FileOperations.Operation op = FileOperations.Operation.valueOf(cmd);
                new FileOperations(parent, op, tracks).setVisible(true);
            }
        };
        JMenu fileOps = new JMenu("File Operations");
        fileOps.add("Open containing folder").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Track track : tracks) {
                    if (track.getTrackData().isFile()) {
                        File file = track.getTrackData().getFile().getParentFile();
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });
        fileOps.addSeparator();
        for (FileOperations.Operation op : FileOperations.Operation.values()) {
            fileOps.add(op.name()).addActionListener(fileOpsListener);
        }
        fileOps.addSeparator();
        JMenuItem deleteItem = fileOps.add("Delete       ");
        deleteItem.setIcon(Images.getEmptyIcon());
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int ret = JOptionPane.showConfirmDialog(null, "This will delete file(s) permanently. Are you sure?", "Delete File(s)?", JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    for (Track track : tracks) {
                        if (track.getTrackData().isFile() && !track.getTrackData().isCue()) {
                            if (player.getTrack() == track) {
                                player.stop();
                            }

                            if (track.getTrackData().getFile().delete()) {
                            	File current = track.getTrackData().getFile().getParentFile();
                            	File parent;
                            	File[] files = current.listFiles();
                            	if (files != null && files.length == 0) {
                            		ret = JOptionPane.showConfirmDialog(null, "Do you want delete empty folder(s) as well?", "Delete File(s)?", JOptionPane.YES_NO_OPTION);
                            		if (ret == JOptionPane.YES_OPTION) {
                            			while (current != null) {
                            				parent = current.getParentFile();
                            				current.delete();
                                        	files = parent.listFiles();
                                        	if (files != null && files.length == 0) {
                                        		current = parent;
                                        	}
                                        	else {
                                        		current = null;
                                        	}
                            			}
                            		}
                            	}
                                playlist.remove(track);
                            }
                        }

                    }
                    playlist.firePlaylistChanged();
                    parent.adjustLastSongAfterDelete(tracks);
                }
            }
        });

        tableMenu.add("Convert").addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ConverterDialog(parent, tracks).setVisible(true);
            }
        });
        tableMenu.add(fileOps);

        for (MenuCallback callback : menus) {
            JMenu menu = callback.create(tracks, playlist);
            if (menu != null)
                tableMenu.add(menu);
        }

        JMenuItem properties = new JMenuItem("Properties");
        properties.setAccelerator(KeyStroke.getKeyStroke("alt ENTER"));
        properties.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.showProperties(tracks);
            }
        });
        tableMenu.add(properties);
        Util.fixIconTextGap(tableMenu);
        return tableMenu;
    }
}
