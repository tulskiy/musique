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
import java.util.Arrays;
import net.sourceforge.jaad.mp4.boxes.BoxTypes;

public class SampleSizeBox extends FullBox {

	private long sampleCount;
	private long[] sampleSizes;

	public SampleSizeBox() {
		super("Sample Size Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final boolean compact = type==BoxTypes.COMPACT_SAMPLE_SIZE_BOX;

		final int sampleSize;
		if(compact) {
			in.skipBytes(3);
			sampleSize = in.read();
		}
		else sampleSize = (int) in.readBytes(4);

		sampleCount = in.readBytes(4);
		sampleSizes = new long[(int) sampleCount];

		if(compact) {
			//compact: sampleSize can be 4, 8 or 16 bits
			if(sampleSize==4) {
				int x;
				for(int i = 0; i<sampleCount; i += 2) {
					x = in.read();
					sampleSizes[i] = (x>>4)&0xF;
					sampleSizes[i+1] = x&0xF;
				}
			}
			else readSizes(in, sampleSize/8);
		}
		else if(sampleSize==0) readSizes(in, 4);
		else Arrays.fill(sampleSizes, sampleSize);
	}

	private void readSizes(MP4InputStream in, int len) throws IOException {
		for(int i = 0; i<sampleCount; i++) {
			sampleSizes[i] = in.readBytes(len);
		}
	}

	public int getSampleCount() {
		return (int) sampleCount;
	}

	public long[] getSampleSizes() {
		return sampleSizes;
	}
}
