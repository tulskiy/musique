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
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The Progressive download information box aids the progressive download of an
 * ISO file. The box contains pairs of numbers (to the end of the box)
 * specifying combinations of effective file download bitrate in units of
 * bytes/sec and a suggested initial playback delay in units of milliseconds.
 *
 * The download rate can be estimated from the download rate and obtain an upper
 * estimate for a suitable initial delay by linear interpolation between pairs,
 * or by extrapolation from the first or last entry.
 * @author in-somnia
 */
public class ProgressiveDownloadInformationBox extends FullBox {

	private Map<Long, Long> pairs;

	public ProgressiveDownloadInformationBox() {
		super("Progressive Download Information Box");
		pairs = new HashMap<Long, Long>();
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		long rate, initialDelay;
		while(getLeft(in)>0) {
			rate = in.readBytes(4);
			initialDelay = in.readBytes(4);
			pairs.put(rate, initialDelay);
		}
	}

	/**
	 * The map contains pairs of bitrates and playback delay.
	 * @return the information pairs
	 */
	public Map<Long, Long> getInformationPairs() {
		return pairs;
	}
}
