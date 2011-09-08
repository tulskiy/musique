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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;

import com.tulskiy.musique.gui.playlist.PlaylistColumn;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.system.Application;

/**
 * Author: Maksim Liauchuk
 * Date: Aug 27, 2011
 */
public class PlaylistConfiguration {
    
    private PlaylistConfiguration() {
        // prevent instantiation
    }

    @Deprecated
    public static String getColumnKey() {
        return getPlaylistKey() + ".columns.column";
    }
    
    @Deprecated
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

    @Deprecated
    /*
     * Temporary method to convert old configuration values.
     */
    public static void setColumnsRaw(List<String> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getColumnKey(), values);
    }

    public static String getTabBoundKey() {
        return "playlists.tabs.bounds.bound";
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

    public static String getPlaylistKey() {
        return "playlists.playlist";
    }
    
    public static List<Playlist> getPlaylists() {
        ArrayList<Playlist> playlists = new ArrayList<Playlist>();

        Configuration config = Application.getInstance().getConfiguration();
        
        Iterator pConfs = config.configurationsAt(getPlaylistKey()).iterator();
        while (pConfs.hasNext()) {
            Playlist playlist = new Playlist();
            playlist.setColumns(new LinkedList<PlaylistColumn>());

            HierarchicalConfiguration pConf = (HierarchicalConfiguration) pConfs.next();
            playlist.setName(pConf.getString("name", "Default"));
            playlist.setGroupBy(pConf.getString("groupBy"));
            playlist.setLibraryView(pConf.getBoolean("isLibraryView", false));
            
            Iterator cConfs = pConf.configurationsAt("columns.column").iterator();
            while (cConfs.hasNext()) {
                PlaylistColumn column = new PlaylistColumn();

                HierarchicalConfiguration cConf = (HierarchicalConfiguration) cConfs.next();
                column.setName(cConf.getString("name"));
                column.setExpression(cConf.getString("expression"));
                column.setSize(cConf.getInt("size"));
                column.setAllign(cConf.getInt("alignment"));

                playlist.getColumns().add(column);
            }

            playlists.add(playlist);
        }

        return playlists;
    }
    
    public static List<Playlist> getPlaylists(List<Playlist> def) {
        List<Playlist> playlists = getPlaylists();

        if (CollectionUtils.isEmpty(playlists)) {
            playlists.addAll(def);
        }

        return playlists;
    }

    @Deprecated
    /*
     * Temporary method to convert old configuration values.
     */
    public static void setPlaylistsRaw(List<String> playlistsRaw) {
        List<Playlist> playlists = new LinkedList<Playlist>();

        for (String playlistRaw : playlistsRaw) {
            Playlist playlist = new Playlist(playlistRaw);
            playlist.setColumns(getColumns());
            playlists.add(playlist);
        }
        
        setPlaylists(playlists);
    }
    
    public static void setPlaylists(List<Playlist> playlists) {
        Configuration config = Application.getInstance().getConfiguration();

        config.clearTree(getPlaylistKey());
        for (int i = 0; i < playlists.size(); i++) {
            Playlist playlist = playlists.get(i);

            config.addProperty(getPlaylistKey() + "(-1)" + ".name", playlist.getName());
            config.addProperty(getPlaylistKey() + ".groupBy", playlist.getGroupBy());
            config.addProperty(getPlaylistKey() + ".isLibraryView", playlist.isLibraryView());

            for (int j = 0; j < playlist.getColumns().size(); j++) {
                PlaylistColumn column = playlist.getColumns().get(j);

                if (i == 0 && j == 0) {
                    config.addProperty(getPlaylistKey() + ".columns(-1).column(-1).name", column.getName());
                }
                else {
                    config.addProperty(getPlaylistKey() + ".columns.column(-1).name", column.getName());
                }
                config.addProperty(getPlaylistKey() + ".columns.column.expression", column.getExpression());
                config.addProperty(getPlaylistKey() + ".columns.column.size", column.getSize());
                config.addProperty(getPlaylistKey() + ".columns.column.alignment", column.getAllign());
            }
        }
    }

}


