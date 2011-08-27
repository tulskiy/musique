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

public class AppleLosslessBox extends FullBox {

	private long maxSamplePerFrame, maxCodedFrameSize, bitRate, sampleRate;
	private int sampleSize, historyMult, initialHistory, kModifier, channels;

	public AppleLosslessBox() {
		super("Apple Lossless Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		maxSamplePerFrame = in.readBytes(4);
		in.skipBytes(1); //?
		sampleSize = in.read();
		historyMult = in.read();
		initialHistory = in.read();
		kModifier = in.read();
		channels = in.read();
		in.skipBytes(2); //?
		maxCodedFrameSize = in.readBytes(4);
		bitRate = in.readBytes(4);
		sampleRate = in.readBytes(4);
	}

	public long getMaxSamplePerFrame() {
		return maxSamplePerFrame;
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public int getHistoryMult() {
		return historyMult;
	}

	public int getInitialHistory() {
		return initialHistory;
	}

	public int getkModifier() {
		return kModifier;
	}

	public int getChannels() {
		return channels;
	}

	public long getMaxCodedFrameSize() {
		return maxCodedFrameSize;
	}

	public long getBitRate() {
		return bitRate;
	}

	public long getSampleRate() {
		return sampleRate;
	}
}
