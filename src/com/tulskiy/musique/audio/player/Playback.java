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

package com.tulskiy.musique.audio.player;

import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;

/**
 * @Author: Denis Tulskiy
 * @Date: 01.07.2009
 */
public class Playback {
    private static final int DEFAULT = 0;

    protected PlaylistManager playlistManager = Application.getInstance().getPlaylistManager();
    private PlaybackMode currentMode;

    public Playback() {
        setPlaybackMode(DEFAULT);
    }

    public void setPlaybackMode(int mode) {
        switch (mode) {
            case DEFAULT:
                currentMode = new Default();
                break;
        }
    }

    public Song next(Song file) {
        return currentMode.next(file);
    }

    public Song prev(Song file) {
        return currentMode.prev(file);
    }

    private class Default implements PlaybackMode {
        public Song next(Song file) {
            Playlist playlist = playlistManager.getCurrentPlaylist();
            int index = file == null ? 0 : playlist.indexOf(file);
            return index >= playlist.size() - 1 ? null : playlist.get(index + 1);
        }

        public Song prev(Song file) {
            return null;
        }
    }

    private interface PlaybackMode {
        public Song next(Song file);

        public Song prev(Song file);
    }
}
