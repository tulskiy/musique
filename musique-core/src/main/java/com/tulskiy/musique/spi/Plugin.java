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

package com.tulskiy.musique.spi;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.gui.menu.LibraryMenu;
import com.tulskiy.musique.gui.menu.Menu;
import com.tulskiy.musique.gui.menu.TracksMenu;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import java.awt.*;

/**
 * Author: Denis Tulskiy
 * Date: 2/27/11
 */
public abstract class Plugin {
    protected static final Application application = Application.getInstance();
    protected static final Player player = application.getPlayer();
    protected static final Configuration config = application.getConfiguration();
    protected static final PlaylistManager playlistManager = application.getPlaylistManager();

    public abstract boolean init();

    public abstract void shutdown();

    public abstract Description getDescription();

    public void registerMenu(MenuType type, Menu.MenuCallback menu) {
        if (menu != null)
            switch (type) {
                case TRACKS:
                    TracksMenu.addMenu(menu);
                    break;
                case LIBRARY:
                    LibraryMenu.addMenu(menu);
            }
    }

    public enum MenuType {
        TRACKS, LIBRARY, MAIN
    }


    public class Description {
        public String name;
        public String author;

        public Description(String name, String author) {
            this.name = name;
            this.author = author;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(", Author: ").append(author);
            return sb.toString();
        }
    }

    public abstract boolean isConfigurable();
    public void configure() {}

    @Override
    public String toString() {
        Description description = getDescription();
        if (description != null) {
            return description.toString();
        }

        return null;
    }
}
