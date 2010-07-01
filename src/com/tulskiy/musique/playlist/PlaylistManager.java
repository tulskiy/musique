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

/**
 * @Author: Denis Tulskiy
 * @Date: Dec 30, 2009
 */
package com.tulskiy.musique.playlist;

import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PlaylistManager {
    public static final String PLAYLISTS_PATH = "playlists/";

    private Logger logger = Logger.getLogger(getClass().getName());
    private ArrayList<Playlist> playlists = new ArrayList<Playlist>();
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private Playlist currentPlaylist;
    private PlaylistOrder order = new PlaylistOrder();

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public void selectPlaylist(Playlist playlist) {
        currentPlaylist = playlist;
        order.setPlaylist(playlist);
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    public void loadPlaylists() {
        ArrayList<String> list = config.getList("playlists", new ArrayList<String>());
        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i);
            Playlist playlist = new Playlist();
            playlist.setName(name);
            playlist.load(new File(PLAYLISTS_PATH + i + ".dat"));
            playlists.add(playlist);
        }

        if (playlists.size() == 0) {
            selectPlaylist(addPlaylist("Default"));
        }

        int index = config.getInt("playlist.currentPlaylist", -1);
        if (index < 0 || index >= playlists.size())
            index = 0;
        selectPlaylist(playlists.get(index));
        app.getPlayer().setPlaybackOrder(order);

        int lastPlayed = config.getInt("player.lastPlayed", 0);
        if (lastPlayed >= 0 && lastPlayed < currentPlaylist.size()) {
            order.setLastPlayed(currentPlaylist.get(lastPlayed));
        }
    }

    public void saveSettings() {
        File dir = new File(PLAYLISTS_PATH);
        //noinspection ResultOfMethodCallIgnored
        dir.mkdir();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".dat")) {
                if (!file.delete()) {
                    logger.severe("Could not delete old playlist. Check file permissions");
                }
            }
        }

        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);
            playlist.save(new File(PLAYLISTS_PATH + i + ".dat"));
        }

        config.setList("playlists", playlists);
        config.setInt("playlist.currentPlaylist", playlists.indexOf(currentPlaylist));

        Track lastPlayed = app.getPlayer().getSong();
        if (lastPlayed != null) {
            int index = currentPlaylist.indexOf(lastPlayed);
            config.setInt("player.lastPlayed", index);
        }
    }

    public int getTotalPlaylists() {
        return playlists.size();
    }

    public Playlist getPlaylist(int index) {
        return playlists.get(index);
    }

    public Playlist addPlaylist(String name) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlists.add(playlist);
        return playlist;
    }

    public void removePlaylist(Playlist pl) {
        playlists.remove(pl);
    }

    public void movePlaylist(int from, int to) {
        Playlist p = playlists.get(from);
        if (from > to)
            from++;
        else
            to++;
        playlists.add(to, p);
        playlists.remove(from);
    }
}
