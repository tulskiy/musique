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

public class ThreeGPPMetadataBox extends FullBox {

	private String languageCode, data;

	public ThreeGPPMetadataBox(String name) {
		super(name);
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		decodeCommon(in);

		data = in.readUTFString((int) getLeft(in));
	}

	//called directly by subboxes that don't contain the 'data' string
	protected void decodeCommon(MP4InputStream in) throws IOException {
		super.decode(in);
		languageCode = Utils.getLanguageCode(in.readBytes(2));
	}

	/**
	 * The language code for the following text. See ISO 639-2/T for the set of
	 * three character codes.
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	public String getData() {
		return data;
	}
}
