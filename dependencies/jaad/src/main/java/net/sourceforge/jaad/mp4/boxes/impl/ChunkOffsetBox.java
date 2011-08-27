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

import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;

public class ChunkOffsetBox extends FullBox {

	private long[] chunks;

	public ChunkOffsetBox() {
		super("Chunk Offset Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int len = (type==BoxTypes.CHUNK_LARGE_OFFSET_BOX) ? 8 : 4;
		final int entryCount = (int) in.readBytes(4);
		chunks = new long[entryCount];

		for(int i = 0; i<entryCount; i++) {
			chunks[i] = in.readBytes(len);
		}
	}

	public long[] getChunks() {
		return chunks;
	}
}
