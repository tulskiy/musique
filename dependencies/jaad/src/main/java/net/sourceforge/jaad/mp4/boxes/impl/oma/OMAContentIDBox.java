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
 * The ContentID box contains the unique identifier for the Content Object the 
 * metadata are associated with. The value of the content-ID must be the value 
 * of the content-ID stored in the Common Headers for this Content Object. There
 * must be exactly one ContentID sub-box per User-Data box, as the first sub-box
 * in the container.
 * 
 * @author in-somnia
 */
public class OMAContentIDBox extends FullBox {

	private String contentID;

	public OMAContentIDBox() {
		super("OMA DRM Content ID Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int len = (int) in.readBytes(2);
		contentID = in.readString(len);
	}

	/**
	 * Returns the content-ID string.
	 * 
	 * @return the content-ID
	 */
	public String getContentID() {
		return contentID;
	}
}
