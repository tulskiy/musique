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
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.boxes.Utils;

public class RatingBox extends FullBox {

	private String languageCode, rating;

	public RatingBox() {
		super("Rating Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		//3gpp or iTunes
		if(parent.getType()==BoxTypes.USER_DATA_BOX) {
			super.decode(in);

			//TODO: what to do with both?
			final long entity = in.readBytes(4);
			final long criteria = in.readBytes(4);
			languageCode = Utils.getLanguageCode(in.readBytes(2));
			final byte[] b = in.readTerminated((int) getLeft(in), 0);
			rating = new String(b, MP4InputStream.UTF8);
		}
		else readChildren(in);
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public String getRating() {
		return rating;
	}
}
