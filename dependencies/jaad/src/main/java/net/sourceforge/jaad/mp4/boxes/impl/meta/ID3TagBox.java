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
package net.sourceforge.jaad.mp4.boxes.impl.meta;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.boxes.Utils;

//TODO: use nio ByteBuffer instead of array
public class ID3TagBox extends FullBox {

	private String language;
	private byte[] id3Data;

	public ID3TagBox() {
		super("ID3 Tag Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		language = Utils.getLanguageCode(in.readBytes(2));

		id3Data = new byte[(int) getLeft(in)];
		in.readBytes(id3Data);
	}

	public byte[] getID3Data() {
		return id3Data;
	}

	/**
	 * The language code for the following text. See ISO 639-2/T for the set of
	 * three character codes.
	 */
	public String getLanguage() {
		return language;
	}
}
