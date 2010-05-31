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
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import java.util.ArrayList;

public class PlaylistManager {
    private ArrayList<Playlist> playlists = new ArrayList<Playlist>();
    private Configuration config = Application.getInstance().getConfiguration();
    private Playlist currentPlaylist;
    private DBMapper<Playlist> playlistDBMapper = new DBMapper<Playlist>(Playlist.class);

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public void selectPlaylist(Playlist playlist) {
        currentPlaylist = playlist;
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    public void loadPlaylists() {
        playlistDBMapper.loadAll(playlists);

        for (Playlist playlist : playlists) {
            playlist.load();
        }

        if (playlists.size() == 0) {
            currentPlaylist = addPlaylist("Default");
            savePlaylists();
        }

        int index = config.getInt("playlist.currentPlaylist", -1);
        selectPlaylist(playlists.get(0));
        for (Playlist playlist : playlists) {
            if (playlist.getPlaylistID() == index) {
                selectPlaylist(playlist);
                break;
            }
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
        playlistDBMapper.save(playlist);
        playlists.add(playlist);
        return playlist;
    }

    public void removePlaylist(Playlist pl) {
        pl.clear();
        playlistDBMapper.delete(pl);
        playlists.remove(pl);
    }

    public void savePlaylists() {
        for (Playlist playlist : playlists) {
            playlist.save();
        }
        config.setInt("playlist.currentPlaylist", currentPlaylist.getPlaylistID());
    }
}
