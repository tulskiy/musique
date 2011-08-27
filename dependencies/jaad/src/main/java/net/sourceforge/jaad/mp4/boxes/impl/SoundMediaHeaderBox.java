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
 * The sound media header contains general presentation information, independent
 * of the coding, for audio media. This header is used for all tracks containing
 * audio.
 *
 * @author in-somnia
 */
public class SoundMediaHeaderBox extends FullBox {

	private double balance;

	public SoundMediaHeaderBox() {
		super("Sound Media Header Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		balance = in.readFixedPoint(8, 8);
		in.skipBytes(2); //reserved
	}

	/**
	 * The balance is a floating-point number that places mono audio tracks in a
	 * stereo space: 0 is centre (the normal value), full left is -1.0 and full
	 * right is 1.0.
	 *
	 * @return the stereo balance for a mono track
	 */
	public double getBalance() {
		return balance;
	}
}
