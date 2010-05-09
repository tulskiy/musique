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

import com.tulskiy.musique.db.DBMapper;

import java.util.ArrayList;

public class PlaylistManager {
    private ArrayList<Playlist> playlists = new ArrayList<Playlist>();
    private int currentPlaylist;
    private DBMapper<Playlist> playlistDBMapper = new DBMapper<Playlist>(Playlist.class);


    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public void selectPlaylist(int index) {
        currentPlaylist = index;
    }

    public Playlist getCurrentPlaylist() {
        return playlists.get(currentPlaylist);
    }

    public void loadPlaylists() {
        playlistDBMapper.loadAll(playlists);

        for (Playlist playlist : playlists) {
            playlist.load();
        }

        if (playlists.size() == 0) {
            newPlaylist("Default");
            savePlaylists();
        }
    }

    public int getTotalPlaylists() {
        return playlists.size();
    }

    public Playlist getPlaylist(int index) {
        return playlists.get(index);
    }

    public void newPlaylist(String name) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlistDBMapper.save(playlist);
        playlists.add(playlist);
    }

    public void savePlaylists() {
        for (Playlist playlist : playlists) {
            playlist.save();
        }
    }
}
