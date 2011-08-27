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
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

public class BitRateBox extends BoxImpl {

	private long decodingBufferSize, maxBitrate, avgBitrate;

	public BitRateBox() {
		super("Bitrate Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		decodingBufferSize = in.readBytes(4);
		maxBitrate = in.readBytes(4);
		avgBitrate = in.readBytes(4);
	}

	/**
	 * Gives the size of the decoding buffer for the elementary stream in bytes.
	 * @return the decoding buffer size
	 */
	public long getDecodingBufferSize() {
		return decodingBufferSize;
	}

	/**
	 * Gives the maximum rate in bits/second over any window of one second.
	 * @return the maximum bitrate
	 */
	public long getMaximumBitrate() {
		return maxBitrate;
	}

	/**
	 * Gives the average rate in bits/second over the entire presentation.
	 * @return the average bitrate
	 */
	public long getAverageBitrate() {
		return avgBitrate;
	}
}
