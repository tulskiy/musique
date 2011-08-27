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
package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * This box provides a compact marking of the random access points within the
 * stream. The table is arranged in strictly increasing order of sample number.
 *
 * If the sync sample box is not present, every sample is a random access point.
 *
 * @author in-somnia
 */
public class SyncSampleBox extends FullBox {

	private long[] sampleNumbers;

	public SyncSampleBox() {
		super("Sync Sample Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int entryCount = (int) in.readBytes(4);
		sampleNumbers = new long[entryCount];
		for(int i = 0; i<entryCount; i++) {
			sampleNumbers[i] = in.readBytes(4);
		}
	}

	/**
	 * Gives the numbers of the samples for each entry that are random access
	 * points in the stream.
	 * 
	 * @return a list of sample numbers
	 */
	public long[] getSampleNumbers() {
		return sampleNumbers;
	}
}
