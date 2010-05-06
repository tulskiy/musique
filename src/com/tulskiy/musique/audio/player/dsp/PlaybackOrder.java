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

package com.tulskiy.musique.audio.player.dsp;

import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 23, 2010
 */
public class PlaybackOrder {
    private Application app = Application.getInstance();
    private PlaylistManager playlistManager = app.getPlaylistManager();

    public Song next(Song song) {
        Playlist playlist = playlistManager.getCurrentPlaylist();

        int index = playlist.indexOf(song);
        if (index >= playlist.size() - 1) {
            return null;
        }

        return playlist.get(index + 1);
    }
}
