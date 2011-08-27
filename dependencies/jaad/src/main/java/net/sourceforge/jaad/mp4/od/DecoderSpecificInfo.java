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
package net.sourceforge.jaad.mp4.od;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;

/**
 * The <code>DecoderSpecificInfo</code> constitutes an opaque container with
 * information for a specific media decoder. Depending on the required amout of
 * data, two classes with a maximum of 255 and 2<sup>32</sup>-1 bytes of data
 * are provided. The existence and semantics of the
 * <code>DecoderSpecificInfo</code> depends on the stream type and object
 * profile of the parent <code>DecoderConfigDescriptor</code>.
 *
 * @author in-somnia
 */
public class DecoderSpecificInfo extends Descriptor {

	private byte[] data;

	@Override
	void decode(MP4InputStream in) throws IOException {
		data = new byte[size];
		in.readBytes(data);
	}

	/**
	 * A byte array containing the decoder specific information.
	 *
	 * @return the decoder specific information
	 */
	public byte[] getData() {
		return data;
	}
}
