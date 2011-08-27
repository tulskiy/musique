/*
 *  Copyright (C) 2011 in-somnia
 * 
 *  This file is part of JAAD.
 * 
 *  JAAD is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation; either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  JAAD is distributed in the hope that it will be useful, but WITHOUT 
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl.fd;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The FEC reservoir box associates the source file identified in the file
 * partition box with FEC reservoirs stored as additional items. It contains a
 * list that starts with the first FEC reservoir associated with the first
 * source block of the source file and continues sequentially through the source
 * blocks of the source file.
 *
 * @author in-somnia
 */
public class FECReservoirBox extends FullBox {

	private int[] itemIDs;
	private long[] symbolCounts;

	public FECReservoirBox() {
		super("FEC Reservoir Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int entryCount = (int) in.readBytes(2);
		itemIDs = new int[entryCount];
		symbolCounts = new long[entryCount];
		for(int i = 0; i<entryCount; i++) {
			itemIDs[i] = (int) in.readBytes(2);
			symbolCounts[i] = in.readBytes(4);
		}
	}

	/**
	 * The item ID indicates the location of the FEC reservoir associated with a
	 * source block.
	 *
	 * @return all item IDs
	 */
	public int[] getItemIDs() {
		return itemIDs;
	}

	/**
	 * The symbol count indicates the number of repair symbols contained in the
	 * FEC reservoir.
	 *
	 * @return all symbol counts
	 */
	public long[] getSymbolCounts() {
		return symbolCounts;
	}
}
