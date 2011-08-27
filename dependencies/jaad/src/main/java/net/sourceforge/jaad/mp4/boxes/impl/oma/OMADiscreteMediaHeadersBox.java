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
 * The Discrete Media headers box includes fields specific to the DCF format and
 * the Common Headers box, followed by an optional user-data box. There must be 
 * exactly one OMADiscreteHeaders box in a single OMA DRM Container box, as the 
 * first box in the container.
 * 
 * @author in-somnia
 */
public class OMADiscreteMediaHeadersBox extends FullBox {

	private String contentType;

	public OMADiscreteMediaHeadersBox() {
		super("OMA DRM Discrete Media Headers Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int len = in.read();
		contentType = in.readString(len);
		
		readChildren(in);
	}

	/**
	 * The content type indicates the original MIME media type of the Content 
	 * Object i.e. what content type the result of a successful extraction of 
	 * the OMAContentBox represents.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		return contentType;
	}
}
