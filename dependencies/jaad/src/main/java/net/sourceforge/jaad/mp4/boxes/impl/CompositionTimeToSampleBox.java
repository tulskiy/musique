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

import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;

/**
 * This box provides the offset between decoding time and composition time.
 * Since decoding time must be less than the composition time, the offsets are
 * expressed as unsigned numbers such that
 * CT(n) = DT(n) + CTTS(n)
 * where CTTS(n) is the (uncompressed) table entry for sample n.
 *
 * The composition time to sample table is optional and must only be present if
 * DT and CT differ for any samples.
 *
 * Hint tracks do not use this box.
 * 
 * @author in-somnia
 */
public class CompositionTimeToSampleBox extends FullBox {

	private long[] sampleCounts, sampleOffsets;

	public CompositionTimeToSampleBox() {
		super("Time To Sample Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		
		final int entryCount = (int) in.readBytes(4);
		sampleCounts = new long[entryCount];
		sampleOffsets = new long[entryCount];

		for(int i = 0; i<entryCount; i++) {
			sampleCounts[i] = in.readBytes(4);
			sampleOffsets[i] = in.readBytes(4);
		}
	}

	public long[] getSampleCounts() {
		return sampleCounts;
	}

	public long[] getSampleOffsets() {
		return sampleOffsets;
	}
}
