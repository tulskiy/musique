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
 * In some streams the media samples do not occupy all bits of the bytes given
 * by the sample size, and are padded at the end to a byte boundary. In some
 * cases, it is necessary to record externally the number of padding bits used.
 * This table supplies that information.
 * 
 * @author in-somnia
 */
public class PaddingBitBox extends FullBox {

	private int[] pad1, pad2;

	public PaddingBitBox() {
		super("Padding Bit Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int sampleCount = (int) (in.readBytes(4)+1)/2;
		pad1 = new int[sampleCount];
		pad2 = new int[sampleCount];

		byte b;
		for(int i = 0; i<sampleCount; i++) {
			b = (byte) in.read();
			//1 bit reserved
			//3 bits pad1
			pad1[i] = (b>>4)&7;
			//1 bit reserved
			//3 bits pad2
			pad2[i] = b&7;
		}
	}

	/**
	 * Integer values from 0 to 7, indicating the number of bits at the end of
	 * sample (i*2)+1.
	 */
	public int[] getPad1() {
		return pad1;
	}

	/**
	 * Integer values from 0 to 7, indicating the number of bits at the end of
	 * sample (i*2)+2.
	 */
	public int[] getPad2() {
		return pad2;
	}
}
