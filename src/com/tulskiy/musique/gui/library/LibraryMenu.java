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

package com.tulskiy.musique.gui.library;

import com.tulskiy.musique.gui.ContextMenu;
import com.tulskiy.musique.images.Images;

import javax.swing.*;

/**
 * Author: Denis Tulskiy
 * Date: 2/5/11
 */
public class LibraryMenu implements ContextMenu<LibraryTree> {
    public JPopupMenu create(LibraryTree tree) {
        ActionMap aMap = tree.getActionMap();
        JPopupMenu popup = new JPopupMenu();
        JMenuItem sendToCurrent = new JMenuItem(aMap.get("sendToCurrent"));
        sendToCurrent.setIcon(Images.getEmptyIcon());
        sendToCurrent.setAccelerator(LibraryTree.SEND_TO_CURRENT_KEY_STROKE);
        popup.add(sendToCurrent);

        JMenuItem sendToNew = new JMenuItem(aMap.get("sendToNew"));
        sendToNew.setAccelerator(LibraryTree.SEND_TO_NEW_KEY_STROKE);
        popup.add(sendToNew);

        JMenuItem addToCurrent = new JMenuItem(aMap.get("addToCurrent"));
        addToCurrent.setAccelerator(LibraryTree.ADD_TO_CURRENT_KEY_STROKE);
        popup.add(addToCurrent);

        return popup;
    }
}
