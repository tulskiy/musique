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

package com.tulskiy.musique.gui.menu;

import com.tulskiy.musique.gui.library.LibraryTree;
import com.tulskiy.musique.gui.playlist.PlaylistColumn;
import com.tulskiy.musique.gui.playlist.PlaylistTable;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static com.tulskiy.musique.gui.library.LibraryAction.*;

/**
 * Author: Denis Tulskiy
 * Date: 2/5/11
 */
public class LibraryMenu extends Menu {
    private PlaylistTable fakeTable;

    public JPopupMenu create(LibraryTree parent, Playlist playlist, ArrayList<Track> tracks) {
        if (fakeTable == null) {
            fakeTable = new PlaylistTable(playlist, new ArrayList<PlaylistColumn>());
            fakeTable.dispose();
        }
        ActionMap aMap = parent.getActionMap();
        JPopupMenu popup = new JPopupMenu();
        JMenuItem sendToCurrent = new JMenuItem(aMap.get(SEND_TO_CURRENT));
        sendToCurrent.setIcon(Images.getEmptyIcon());
        sendToCurrent.setAccelerator(SEND_TO_CURRENT.getKeyStroke());
        popup.add(sendToCurrent);

        JMenuItem sendToNew = new JMenuItem(aMap.get(SEND_TO_NEW));
        sendToNew.setAccelerator(SEND_TO_NEW.getKeyStroke());
        popup.add(sendToNew);

        JMenuItem addToCurrent = new JMenuItem(aMap.get(ADD_TO_CURRENT));
        addToCurrent.setAccelerator(ADD_TO_CURRENT.getKeyStroke());
        popup.add(addToCurrent);

        popup.addSeparator();

        TracksMenu tracksMenu = new TracksMenu();
        JPopupMenu menu = tracksMenu.create(fakeTable, playlist, tracks);
        for (Component component : menu.getComponents()) {
            popup.add(component);
        }

        return popup;
    }

    public static void addMenu(JMenu menu) {

    }
}
