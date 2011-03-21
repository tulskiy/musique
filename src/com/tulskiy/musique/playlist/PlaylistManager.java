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

/**
 * @Author: Denis Tulskiy
 * @Date: Dec 30, 2009
 */
package com.tulskiy.musique.playlist;

import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.tulskiy.musique.playlist.PlaylistListener.Event;

public class PlaylistManager {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private File PLAYLIST_PATH = new File(app.CONFIG_HOME, "playlists");
    private Logger logger = Logger.getLogger("musique");
    private ArrayList<Playlist> playlists = new ArrayList<Playlist>();
    private Playlist activePlaylist;
    private Playlist visiblePlaylist;
    private PlaybackOrder order = new PlaybackOrder();
    private Library library;
    private List<PlaylistListener> listeners = new ArrayList<PlaylistListener>();

    public ArrayList<Playlist> getPlaylists() {
        return playlists;
    }

    public void setActivePlaylist(Playlist playlist) {
        if (activePlaylist != playlist) {
            activePlaylist = playlist;
            notifyListeners(playlist, Event.ACTIVATED);
        }
        order.setPlaylist(playlist);
    }

    public Playlist getActivePlaylist() {
        return activePlaylist;
    }

    public void setVisiblePlaylist(Playlist playlist) {
        if (visiblePlaylist != playlist)
            notifyListeners(playlist, Event.SELECTED);
        this.visiblePlaylist = playlist;
    }

    public Playlist getVisiblePlaylist() {
        return visiblePlaylist;
    }

    public Library getLibrary() {
        return library;
    }

    public void loadPlaylists() {
        ArrayList<String> list = config.getList("playlists", new ArrayList<String>());

        Playlist libraryPlaylist = new Playlist();
        File file = new File(PLAYLIST_PATH, "library.mus");
        if (file.exists())
            libraryPlaylist.load(file);
        library = new Library(libraryPlaylist);

        for (int i = 0; i < list.size(); i++) {
            String fmt = list.get(i);
            Playlist playlist = new Playlist(fmt);
            playlist.load(new File(PLAYLIST_PATH, i + ".mus"));
            playlists.add(playlist);
        }

        if (playlists.size() == 0) {
            setActivePlaylist(addPlaylist("Default"));
        }

        int index = config.getInt("playlist.activePlaylist", -1);
        if (index < 0 || index >= playlists.size())
            index = 0;
        setActivePlaylist(playlists.get(index));
        app.getPlayer().setPlaybackOrder(order);

        int lastPlayed = config.getInt("player.lastPlayed", 0);
        if (lastPlayed >= 0 && lastPlayed < activePlaylist.size()) {
            order.setLastPlayed(activePlaylist.get(lastPlayed));
        }

        //need to do it here because lastPlayed index gets shifted
        //after regrouping
        for (Playlist playlist : playlists) {
            playlist.firePlaylistChanged();
        }
    }

    public void saveSettings() {
        //noinspection ResultOfMethodCallIgnored
        PLAYLIST_PATH.mkdir();
        File[] files = PLAYLIST_PATH.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".mus")) {
                if (!file.delete()) {
                    logger.severe("Could not delete old playlist. Check file permissions");
                }
            }
        }

        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);
            playlist.save(new File(PLAYLIST_PATH, i + ".mus"));
        }
        library.getData().save(new File(PLAYLIST_PATH, "library.mus"));

        config.setList("playlists", playlists);
        if (activePlaylist != null && playlists.contains(activePlaylist)) {
            config.setInt("playlist.activePlaylist", playlists.indexOf(activePlaylist));

            Track lastPlayed = app.getPlayer().getTrack();
            if (lastPlayed != null) {
                activePlaylist.cleanUp();
                int index = activePlaylist.indexOf(lastPlayed);
                config.setInt("player.lastPlayed", index);
            }
        }
    }

    public int getTotalPlaylists() {
        return playlists.size();
    }

    public Playlist addPlaylist(String name) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlists.add(playlist);
        notifyListeners(playlist, Event.ADDED);
        return playlist;
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        notifyListeners(playlist, Event.ADDED);
    }

    public void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
        if (playlists.size() == 0) {
            addPlaylist("Default");
        }
        notifyListeners(playlist, Event.REMOVED);
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

    public void addPlaylistListener(PlaylistListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(Playlist playlist, Event event) {
        for (PlaylistListener listener : listeners) {
            switch (event) {
                case ADDED:
                    listener.playlistAdded(playlist);
                    break;
                case REMOVED:
                    listener.playlistRemoved(playlist);
                    break;
                case SELECTED:
                    listener.playlistSelected(playlist);
                    break;
                case UPDATED:
                    listener.playlistUpdated(playlist);
                    break;
                case ACTIVATED:
                    listener.playlistActivated(playlist);
                    break;
            }

        }
    }

    public void removePlaylistListener(PlaylistListener playlistListener) {
        listeners.remove(playlistListener);
    }
}
