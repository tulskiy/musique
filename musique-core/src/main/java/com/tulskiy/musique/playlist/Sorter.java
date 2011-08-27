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

package com.tulskiy.musique.playlist;

import com.tulskiy.musique.gui.playlist.PlaylistTable;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.configuration.Configuration;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: 6/3/11
 */
public class Sorter {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();

    public JMenu createMenu(Playlist playlist, final List<Track> tracks) {
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
                        String ret = JOptionPane.showInputDialog(null,
                                "Sort By...",
                                config.getString("playlist.sortString", ""));
                        if (ret != null) {
                            Collections.sort(tracks, new TrackComparator(Parser.parse(ret)));
                            config.setString("playlist.sortString", ret);
                        }

                        break;
                    case 1:
                        Collections.shuffle(tracks);
                        break;
                    case 2:
                        Collections.reverse(tracks);
                        break;
                    default:
                        Collections.sort(tracks, new TrackComparator(Parser.parse(sortValues[index])));
                }
            }
        };

        for (int i = 0; i < sortItems.length; i++) {
            String sortValue = sortItems[i];
            if (sortValue == null) {
                sort.addSeparator();
                continue;
            }

            AbstractButton item = sort.add(sortValue);
            item.setIcon(Images.getEmptyIcon());
            item.addActionListener(sortListener);
            item.putClientProperty("index", i);
        }
        return sort;
    }
}
