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
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.tulskiy.musique.gui.menu.Menu;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.plugins.discogs.dialog.DiscogsDialog;
import com.tulskiy.musique.spi.Plugin;

/**
 * @author mliauchuk
 */
public class DiscogsPlugin extends Plugin {
	
	public static final String API_KEY = "09ff0d5c2b";
	
    @Override
    public boolean init() {
        createMenu();
        return true;
    }

    private void createMenu() {
        registerMenu(MenuType.TRACKS, new Menu.MenuCallback() {
            @Override
            public JMenu create(final ArrayList<Track> tracks, final Playlist playlist) {
                if (tracks.size() > 0) {
                    JMenu menu = new JMenu("Discogs");
                    
                    JMenuItem retrieve = new JMenuItem("Query");
                    retrieve.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							new DiscogsDialog(tracks, playlist).setVisible(true);
						}
                    });
                    menu.add(retrieve);
                    
                    JMenuItem settings = new JMenuItem("Settings");
                    settings.setVisible(false);
                    settings.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							// TODO Auto-generated method stub
							
						}
                    });
                    menu.add(settings);

                    return menu;
                }

                return null;
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
}
