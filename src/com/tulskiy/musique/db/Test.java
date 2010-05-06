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

package com.tulskiy.musique.db;

import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.system.Application;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 5, 2010
 */
public class Test {
    public static void main(String[] args) {
        Application app = Application.getInstance();
        app.load();
//        app.getDbManager().runScript("install.sql");

        PlaylistManager playlistManager = app.getPlaylistManager();
        Playlist playlist = playlistManager.getPlaylist(0);
        playlist.addFiles(new File[]{new File("c:/Users/tulskiy/Music")});
        app.exit();
    }
}
