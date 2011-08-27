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

public class TrackFragmentRandomAccessBox extends FullBox {

	private long trackID;
	private int entryCount;
	private long[] times, moofOffsets, trafNumbers, trunNumbers, sampleNumbers;

	public TrackFragmentRandomAccessBox() {
		super("Track Fragment Random Access Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		trackID = in.readBytes(4);
		//26 bits reserved, 2 bits trafSizeLen, 2 bits trunSizeLen, 2 bits sampleSizeLen
		final long l = in.readBytes(4);
		final int trafNumberLen = (int) ((l>>4)&0x3)+1;
		final int trunNumberLen = (int) ((l>>2)&0x3)+1;
		final int sampleNumberLen = (int) (l&0x3)+1;
		entryCount = (int) in.readBytes(4);

		final int len = (version==1) ? 8 : 4;

		for(int i = 0; i<entryCount; i++) {
			times[i] = in.readBytes(len);
			moofOffsets[i] = in.readBytes(len);
			trafNumbers[i] = in.readBytes(trafNumberLen);
			trunNumbers[i] = in.readBytes(trunNumberLen);
			sampleNumbers[i] = in.readBytes(sampleNumberLen);
		}
	}

	/**
	 * The track ID is an integer identifying the associated track.
	 *
	 * @return the track ID
	 */
	public long getTrackID() {
		return trackID;
	}

	public int getEntryCount() {
		return entryCount;
	}

	/**
	 * The time is an integer that indicates the presentation time of the random
	 * access sample in units defined in the 'mdhd' of the associated track.
	 *
	 * @return the times of all entries
	 */
	public long[] getTimes() {
		return times;
	}

	/**
	 * The moof-Offset is an integer that gives the offset of the 'moof' used in
	 * the an entry. Offset is the byte-offset between the beginning of the file
	 * and the beginning of the 'moof'.
	 *
	 * @return the offsets for all entries
	 */
	public long[] getMoofOffsets() {
		return moofOffsets;
	}

	/**
	 * The 'traf' number that contains the random accessible sample. The number
	 * ranges from 1 (the first 'traf' is numbered 1) in each 'moof'.
	 *
	 * @return the 'traf' numbers for all entries
	 */
	public long[] getTrafNumbers() {
		return trafNumbers;
	}

	/**
	 * The 'trun' number that contains the random accessible sample. The number
	 * ranges from 1 in each 'traf'.
	 *
	 * @return the 'trun' numbers for all entries
	 */
	public long[] getTrunNumbers() {
		return trunNumbers;
	}

	/**
	 * The sample number that contains the random accessible sample. The number
	 * ranges from 1 in each 'trun'.
	 *
	 * @return the sample numbers for all entries
	 */
	public long[] getSampleNumbers() {
		return sampleNumbers;
	}
}
