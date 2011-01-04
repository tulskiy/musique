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

import com.tulskiy.musique.playlist.Track;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: Jun 11, 2010
 */
public class SongsSelection implements Transferable {
    private static DataFlavor[] flavor;
    private static String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.ArrayList";

    private ArrayList<Track> tracks;

    public static DataFlavor getFlavor() {
        if (flavor == null) {
            try {
                flavor = new DataFlavor[]{new DataFlavor(mimeType)};
            } catch (ClassNotFoundException ignored) {
            }
        }
        return flavor[0];
    }

    public SongsSelection(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavor;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.match(getFlavor());
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return tracks;
        else
            return null;
    }
}
