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

public class SampleToChunkBox extends FullBox {

	private long[] firstChunks, samplesPerChunk, sampleDescriptionIndex;

	public SampleToChunkBox() {
		super("Sample To Chunk Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int entryCount = (int) in.readBytes(4);
		firstChunks = new long[entryCount];
		samplesPerChunk = new long[entryCount];
		sampleDescriptionIndex = new long[entryCount];

		for(int i = 0; i<entryCount; i++) {
			firstChunks[i] = in.readBytes(4);
			samplesPerChunk[i] = in.readBytes(4);
			sampleDescriptionIndex[i] = in.readBytes(4);
		}
	}

	public long[] getFirstChunks() {
		return firstChunks;
	}

	public long[] getSamplesPerChunk() {
		return samplesPerChunk;
	}

	public long[] getSampleDescriptionIndex() {
		return sampleDescriptionIndex;
	}
}
