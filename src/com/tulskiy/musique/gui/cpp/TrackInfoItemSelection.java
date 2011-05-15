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

package com.tulskiy.musique.gui.cpp;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tulskiy.musique.gui.model.TrackInfoItem;
import com.tulskiy.musique.util.FieldKeyMetaHelper;
import com.tulskiy.musique.util.Util;

public class TrackInfoItemSelection implements Transferable, ClipboardOwner {
	
	public static final DataFlavor objectFlavor = new DataFlavor(List.class, "Musique.TrackInfoItem object");
	public static final DataFlavor textFlavor = DataFlavor.stringFlavor;

	private final DataFlavor[] supportedFlavors = {objectFlavor, textFlavor};	

	private List<TrackInfoItem> items = null;

	public TrackInfoItemSelection(List<TrackInfoItem> data) {
		if (data != null) {
			items = new ArrayList<TrackInfoItem>(data.size());
			for (TrackInfoItem item : data) {
				items.add(new TrackInfoItem(item.getKey(), item.getTracks()));
			}
		}
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (objectFlavor.equals(flavor)) {
			return items;
		}
		else if (textFlavor.equals(flavor)) {
			return toString();
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return supportedFlavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor df : supportedFlavors) {
			if (df.equals(flavor)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		items = null;
	}

	public String toString() {
		if (items == null || items.isEmpty()) {
			return null;
		}

		StringBuilder result = new StringBuilder();
		
		for (TrackInfoItem item : items) {
			result.append(FieldKeyMetaHelper.getDisplayName(item.getKey()))
				.append(" : ")
				.append(Util.formatFieldValues(item.getState().getValues(), "; "));
		}
		
		return result.toString();
	}

}
