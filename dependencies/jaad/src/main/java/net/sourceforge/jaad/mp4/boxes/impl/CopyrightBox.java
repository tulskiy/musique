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
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.boxes.Utils;

/**
 * The Copyright box contains a copyright declaration which applies to the
 * entire presentation, when contained within the Movie Box, or, when contained
 * in a track, to that entire track. There may be multiple copyright boxes using
 * different language codes.
 */
public class CopyrightBox extends FullBox {

	private String languageCode, notice;

	public CopyrightBox() {
		super("Copyright Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		if(parent.getType()==BoxTypes.USER_DATA_BOX) {
			super.decode(in);
			//1 bit padding, 5*3 bits language code (ISO-639-2/T)
			languageCode = Utils.getLanguageCode(in.readBytes(2));

			notice = in.readUTFString((int) getLeft(in));
		}
		else if(parent.getType()==BoxTypes.ITUNES_META_LIST_BOX) readChildren(in);
	}

	/**
	 * The language code for the following text. See ISO 639-2/T for the set of
	 * three character codes.
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * The copyright notice.
	 */
	public String getNotice() {
		return notice;
	}
}
