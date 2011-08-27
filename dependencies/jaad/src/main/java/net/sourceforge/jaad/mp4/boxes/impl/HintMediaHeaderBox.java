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
 * The hint media header contains general information, independent of the
 * protocol, for hint tracks.
 *
 * @author in-somnia
 */
public class HintMediaHeaderBox extends FullBox {

	private long maxPDUsize, avgPDUsize, maxBitrate, avgBitrate;

	public HintMediaHeaderBox() {
		super("Hint Media Header Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		maxPDUsize = in.readBytes(2);
		avgPDUsize = in.readBytes(2);

		maxBitrate = in.readBytes(4);
		avgBitrate = in.readBytes(4);

		in.skipBytes(4); //reserved
	}

	/**
	 * The maximum PDU size gives the size in bytes of the largest PDU (protocol
	 * data unit) in this hint stream.
	 */
	public long getMaxPDUsize() {
		return maxPDUsize;
	}

	/**
	 * The average PDU size gives the average size of a PDU over the entire
	 * presentation.
	 */
	public long getAveragePDUsize() {
		return avgPDUsize;
	}

	/**
	 * The maximum bitrate gives the maximum rate in bits/second over any window
	 * of one second.
	 */
	public long getMaxBitrate() {
		return maxBitrate;
	}

	/**
	 * The average bitrate gives the average rate in bits/second over the entire
	 * presentation.
	 */
	public long getAverageBitrate() {
		return avgBitrate;
	}
}
