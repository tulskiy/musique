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

package com.tulskiy.musique.util;

import java.awt.*;
import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * @Author: Denis Tulskiy
 * @Date: 24.09.2008
 */
public class SettingsBuilder {
    public static void main(String[] args) throws Exception {
        buildSettings();
    }

    public static void buildSettings() {
        try {
            HashMap<String, Object> settings = new HashMap<String, Object>();
            settings.put("textColor", Color.white);
            settings.put("bgColor1", new Color(43, 73, 135));
            settings.put("bgColor2", new Color(30, 61, 126));
            settings.put("selectionBg", new Color(84, 110, 171));
            settings.put("playingBg", new Color(103, 136, 201));
            settings.put("tableHeaderBg", Color.white);
            settings.put("tableHeaderTextColor", Color.black);
            settings.put("controlPanelBg", new Color(238, 238, 238));
            settings.put("columns", new String[]{"#", "Name", "Time", "Album", "Year"});
            settings.put("playlistFont", new Font("SansSerif", Font.PLAIN, 16));
            settings.put("headerFont", new Font("SansSerif", Font.BOLD, 12));
            settings.put("columnWidth", new int[]{75, 300, 75, 100, 50});
            settings.put("currentPlayingFile", 0);
            settings.put("volume", 1.0f);
            settings.put("playbackModes", new String[][]{
                    {"Default", "playbackDefault"},
                    {"Repeat (Playlist)", "playbackRepeatPlaylist"},
                    {"Repeat (Track)", "playbackRepeatTrack"},
                    {"Shuffle", "playbackShuffle"}});
            settings.put("playbackMode", "playbackRepeatPlaylist");
            settings.put("mainWindowPosition", new Rectangle(0, 0, 800, 500));
            settings.put("mainWindowState", 0);
            settings.put("albumartPath", "resources/images/albumart");
            settings.put("albumartNoImagePath", "resources/images/noImage.jpg");
            XMLEncoder enc = new XMLEncoder(new FileOutputStream("resources/settings.xml"));
            enc.writeObject(settings);
            enc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.out);
        }
    }
}
