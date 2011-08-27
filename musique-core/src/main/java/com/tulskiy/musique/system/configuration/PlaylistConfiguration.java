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

package com.tulskiy.musique.system.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.tulskiy.musique.gui.playlist.PlaylistColumn;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.system.Application;

/**
 * Author: Maksim Liauchuk
 * Date: Aug 27, 2011
 */
// TODO move all playlist configs to playlists.playlist (will allow to keep different column set for each playlist, etc.)
public class PlaylistConfiguration {
    
    private PlaylistConfiguration() {
        // prevent instantiation
    }

    // TODO refactor column to store separate fields (name/expr/size/align) instead of solid formatted string
    public static String getColumnKey() {
        return "playlist.columns.column";
    }
    
    public static List<PlaylistColumn> getColumns() {
        Configuration config = Application.getInstance().getConfiguration();
        List<String> columnsRaw = (List<String>) config.getList(getColumnKey());
        ArrayList<PlaylistColumn> columns = new ArrayList<PlaylistColumn>();
        if (!CollectionUtils.isEmpty(columnsRaw)) {
            for (String columnRaw : columnsRaw) {
                columns.add(new PlaylistColumn(columnRaw));
            }
        }

        return columns;
    }
    
    public static List<PlaylistColumn> getColumns(PlaylistColumn... def) {
        Configuration config = Application.getInstance().getConfiguration();
        List<String> columnsRaw = (List<String>) config.getList(getColumnKey());
        ArrayList<PlaylistColumn> columns = new ArrayList<PlaylistColumn>();
        if (CollectionUtils.isEmpty(columnsRaw)) {
            columns.addAll(Arrays.asList(def));
        }
        else {
            for (String columnRaw : columnsRaw) {
                columns.add(new PlaylistColumn(columnRaw));
            }
        }

        return columns;
    }

    @Deprecated
    /*
     * Temporary method to convert old configuration values.
     */
    public static void setColumnsRaw(List<String> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getColumnKey(), values);
    }
    
    public static void setColumns(List<PlaylistColumn> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getColumnKey(), values);
    }

    public static String getTabBoundKey() {
        return "playlist.tabs.bounds.bound";
    }
    
    // TODO refactor bounds: String->Int
    public static List<String> getTabBounds() {
        Configuration config = Application.getInstance().getConfiguration();
        return (List<String>) config.getList(getTabBoundKey());
    }
    
    public static List<String> getTabBounds(List<String> def) {
        Configuration config = Application.getInstance().getConfiguration();
        return (List<String>) config.getList(getTabBoundKey(), def);
    }
    
    @Deprecated
    /*
     * Temporary method to convert old configuration values.
     */
    public static void setTabBoundsRaw(List<String> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getTabBoundKey(), values);
    }
    
    public static void setTabBounds(List<Integer> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getTabBoundKey(), values);
    }

    // TODO refactor column to store separate fields (name/groupBy/libraryView) instead of solid formatted string
    public static String getPlaylistKey() {
        return "playlists.playlist.raw";
    }
    
    public static List<Playlist> getPlaylists() {
        Configuration config = Application.getInstance().getConfiguration();
        List<String> columnsRaw = (List<String>) config.getList(getPlaylistKey());
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();
        if (!CollectionUtils.isEmpty(columnsRaw)) {
            for (String columnRaw : columnsRaw) {
                playlists.add(new Playlist(columnRaw));
            }
        }

        return playlists;
    }
    
    public static List<Playlist> getPlaylists(List<Playlist> def) {
        Configuration config = Application.getInstance().getConfiguration();
        List<String> columnsRaw = (List<String>) config.getList(getPlaylistKey());
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();
        if (CollectionUtils.isEmpty(columnsRaw)) {
            playlists.addAll(def);
        }
        else {
            for (String columnRaw : columnsRaw) {
                playlists.add(new Playlist(columnRaw));
            }
        }

        return playlists;
    }

    @Deprecated
    /*
     * Temporary method to convert old configuration values.
     */
    public static void setPlaylistsRaw(List<String> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getPlaylistKey(), values);
    }
    
    public static void setPlaylists(List<Playlist> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getPlaylistKey(), values);
    }

}


