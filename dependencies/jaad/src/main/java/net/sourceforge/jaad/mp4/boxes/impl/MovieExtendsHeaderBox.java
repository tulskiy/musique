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
 * The Movie Extends Header is optional, and provides the overall duration,
 * including fragments, of a fragmented movie. If this box is not present, the
 * overall duration must be computed by examining each fragment.
 * 
 * @author in-somnia
 */
public class MovieExtendsHeaderBox extends FullBox {

	private long fragmentDuration;

	public MovieExtendsHeaderBox() {
		super("Movie Extends Header Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int len = (version==1) ? 8 : 4;
		fragmentDuration = in.readBytes(len);
	}

	/**
	 * The fragment duration is an integer that declares length of the
	 * presentation of the whole movie including fragments (in the timescale
	 * indicated in the Movie Header Box). The value of this field corresponds
	 * to the duration of the longest track, including movie fragments. If an
	 * MP4 file is created in real-time, such as used in live streaming, it is
	 * not likely that the fragment duration is known in advance and this box
	 * may be omitted.
	 * 
	 * @return the fragment duration
	 */
	public long getFragmentDuration() {
		return fragmentDuration;
	}
}
