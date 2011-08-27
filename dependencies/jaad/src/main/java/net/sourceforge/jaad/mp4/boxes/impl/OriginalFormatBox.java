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
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

/**
 * The Original Format Box contains the four-character-code of the original
 * un-transformed sample description.
 *
 * @author in-somnia
 */
public class OriginalFormatBox extends BoxImpl {

	private long originalFormat;

	public OriginalFormatBox() {
		super("Original Format Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		originalFormat = in.readBytes(4);
	}

	/**
	 * The original format is the four-character-code of the original
	 * un-transformed sample entry (e.g. 'mp4v' if the stream contains protected
	 * MPEG-4 visual material).
	 *
	 * @return the stream's original format
	 */
	public long getOriginalFormat() {
		return originalFormat;
	}
}
