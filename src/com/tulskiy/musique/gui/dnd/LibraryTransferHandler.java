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

package com.tulskiy.musique.gui.dnd;

import com.tulskiy.musique.gui.library.LibraryTree;

import javax.swing.*;
import java.awt.datatransfer.Transferable;

/**
 * Author: Denis Tulskiy
 * Date: 1/4/11
 */
public class LibraryTransferHandler extends TransferHandler {
    @Override
    public int getSourceActions(JComponent c) {
        return LINK;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        LibraryTree tree = (LibraryTree) c;
        return new SongsSelection(tree.getSelectedTracks(true));
    }
}
