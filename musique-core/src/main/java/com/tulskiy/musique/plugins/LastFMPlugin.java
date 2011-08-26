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

package com.tulskiy.musique.plugins;

import com.tulskiy.musique.gui.menu.Menu;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.spi.Plugin;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: 2/27/11
 */
public class LastFMPlugin extends Plugin {
    @Override
    public boolean init() {
        createMenu();
        return true;
    }

    private void createMenu() {
        registerMenu(MenuType.TRACKS, new Menu.MenuCallback() {
            @Override
            public JMenu create(ArrayList<Track> tracks, Playlist playlist) {
                // only allow to love current track
                if (tracks.size() == 1 && player.getTrack() == tracks.get(0)) {
                    JMenu menu = new JMenu("Last.fm");
                    menu.add(new JMenuItem("Love this track"));
                    return menu;
                } else {
                    return null;
                }

            }
        });
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Description getDescription() {
        return new Description("Last.fm", "Denis Tulskiy", "Last.fm scrobbling using 2.0 API");
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }
}
