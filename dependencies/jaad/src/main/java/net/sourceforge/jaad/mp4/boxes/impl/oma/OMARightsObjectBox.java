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
package net.sourceforge.jaad.mp4.boxes.impl.oma;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The rights object box may be used to insert a Protected Rights Object, 
 * defined in 'OMA DRM v2.1' section 5.3.9, into a DCF or PDCF. A Mutable DRM 
 * Information box may include zero or more Rights Object boxes.
 * 
 * @author in-somnia
 */
public class OMARightsObjectBox extends FullBox {

	private byte[] data;

	public OMARightsObjectBox() {
		super("OMA DRM Rights Object Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		data = new byte[(int) getLeft(in)];
		in.readBytes(data);
	}

	/**
	 * Returns an array containing the rights object.
	 * 
	 * @return a rights object
	 */
	public byte[] getData() {
		return data;
	}
}
