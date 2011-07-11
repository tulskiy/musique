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

package com.tulskiy.musique.plugins.discogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.tulskiy.musique.gui.menu.Menu;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.plugins.discogs.dialog.DiscogsDialog;
import com.tulskiy.musique.plugins.discogs.dialog.SettingsDialog;
import com.tulskiy.musique.spi.Plugin;
import com.tulskiy.musique.system.Application;

/**
 * @author mliauchuk
 */
public class DiscogsPlugin extends Plugin {
	
	public static final String API_KEY = "09ff0d5c2b";

	public static final String DEFAULT_CACHE_ROOT_DIR = System.getProperty("java.io.tmpdir", "");
	public static final String CACHE_SUB_DIR = "musique-discogs-cache" + File.separator;

	public static final String CONF_PARAM_CACHE_ENABLED = "discogs.cache.enabled";
	public static final String CONF_PARAM_CACHE_LOC_TYPE = "discogs.cache.location.type";
	public static final String CONF_PARAM_CACHE_LOC_DIR = "discogs.cache.location.dir";
	
    @Override
    public boolean init() {
        createMenu();
        return true;
    }

    private void createMenu() {
    	// TODO think about case when no tracks selected but context menu with Discogs setting is to be appeared
        registerMenu(MenuType.TRACKS, new Menu.MenuCallback() {
            @Override
            public JMenu create(final ArrayList<Track> tracks, final Playlist playlist) {
            	return createMenu(tracks, playlist);
            }
        });
    }

    @Override
    public void shutdown() {

    }

    @Override
    public Description getDescription() {
        return new Description("Last.fm plugin", "1.0");
    }
    
    private JMenu createMenu(final ArrayList<Track> tracks, final Playlist playlist) {
        JMenu menu = new JMenu("Discogs");
        
        if (tracks.size() > 0) {
            JMenuItem retrieve = new JMenuItem("Query");
            retrieve.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					new DiscogsDialog(tracks, playlist).setVisible(true);
				}
            });
            menu.add(retrieve);
        }
        
        JMenuItem settings = new JMenuItem("Settings");
        settings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new SettingsDialog().setVisible(true);
			}
        });
        menu.add(settings);

        return menu;
    }
    
    // Configuration settings
    public static void setCacheEnabled(boolean b) {
    	Application.getInstance().getConfiguration().setBoolean(
    			DiscogsPlugin.CONF_PARAM_CACHE_ENABLED, b);
    }

	public static boolean isCacheEnabled() {
		return Application.getInstance().getConfiguration().getBoolean(
				DiscogsPlugin.CONF_PARAM_CACHE_ENABLED, true);
	}
    
    public static void setCacheDirType(int n) {
    	Application.getInstance().getConfiguration().setInt(
    			DiscogsPlugin.CONF_PARAM_CACHE_LOC_TYPE, n);
    }
	
	public static int getCacheDirType() {
		return Application.getInstance().getConfiguration().getInt(
				DiscogsPlugin.CONF_PARAM_CACHE_LOC_TYPE, 1);
	}
    
    public static void setCacheRootDir(String s) {
    	Application.getInstance().getConfiguration().setString(
    			DiscogsPlugin.CONF_PARAM_CACHE_LOC_DIR, s);
    }
	
	public static String getCacheRootDir() {
		return Application.getInstance().getConfiguration().getString(
				DiscogsPlugin.CONF_PARAM_CACHE_LOC_DIR, DiscogsPlugin.DEFAULT_CACHE_ROOT_DIR);
	}
	
	public static String getCacheDir() {
		String cacheRoot = getCacheRootDir();

	    if (cacheRoot == null) {
	    	cacheRoot = "";
	    }
	    else if (!"".equals(cacheRoot) && !cacheRoot.endsWith(File.separator)) {
	    	cacheRoot += File.separator;
	    }

	    return cacheRoot + CACHE_SUB_DIR;
	}

}
