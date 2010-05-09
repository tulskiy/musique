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
 * @Date: Oct 30, 2009
 */
package com.tulskiy.musique.system;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.audio.player.PlayerImpl;
import com.tulskiy.musique.db.DBManager;
import com.tulskiy.musique.gui.MainWindow;
import com.tulskiy.musique.playlist.PlaylistManager;

import javax.swing.*;
import java.io.File;

public class Application {
    private static Application ourInstance = new Application();

    private Player player;
    private Configuration configuration;
    private PlaylistManager playlistManager;
    private DBManager dbManager;
    private MainWindow mainWindow;

    public static Application getInstance() {
        return ourInstance;
    }

    private Application() {

    }

    public void load() {
        boolean firstRun = !new File("resources/db/library.script").exists();

        dbManager = new DBManager();
        dbManager.connect();

        if (firstRun) {
            dbManager.runScript("install.sql");
            dbManager.runScript("defaultSettings.sql");
        }

        configuration = new Configuration();
        configuration.load();

        playlistManager = new PlaylistManager();
        playlistManager.loadPlaylists();

        player = new PlayerImpl();

        try {
            UIManager.setLookAndFeel(configuration.getProperty("gui.LAF"));
        } catch (Exception e) {
            System.err.println("Could not load LaF");
        }
    }

    public void start() {
        try {
            mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        player.pause();
        if (mainWindow != null)
            mainWindow.shutdown();
        playlistManager.savePlaylists();
        configuration.save();
        dbManager.closeConnection();
        System.exit(0);
    }

    public Player getPlayer() {
        return player;
    }

    public void installDB() {
        if (dbManager != null) {
            dbManager.runScript("install.sql");
        }
    }

    public void defaultSettings() {
        if (dbManager != null) {
            dbManager.runScript("defaultSettings.sql");
            configuration = new Configuration();
            configuration.load();
        }
    }


    public Configuration getConfiguration() {
        return configuration;
    }

    public PlaylistManager getPlaylistManager() {
        return playlistManager;
    }

    public DBManager getDbManager() {
        return dbManager;
    }

    public void error(String s, Object... params) {
        System.err.printf("Error: " + s, params);
    }
}
