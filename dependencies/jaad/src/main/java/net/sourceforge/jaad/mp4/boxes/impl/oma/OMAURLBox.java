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
 * This box is used for several sub-boxes of the user-data box in an OMA DRM 
 * file. These boxes have in common, that they only contain one String.
 * 
 * @author in-somnia
 */
public class OMAURLBox extends FullBox {

	private String content;

	public OMAURLBox(String name) {
		super(name);
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final byte[] b = new byte[(int) getLeft(in)];
		in.readBytes(b);
		content = new String(b, MP4InputStream.UTF8);
	}

	/**
	 * Returns the String that this box contains. Its meaning depends on the 
	 * type of this box.
	 * 
	 * @return the content of this box
	 */
	public String getContent() {
		return content;
	}
}
