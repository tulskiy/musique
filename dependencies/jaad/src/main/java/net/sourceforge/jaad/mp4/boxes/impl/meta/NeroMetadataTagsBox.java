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
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

public class NeroMetadataTagsBox extends BoxImpl {

	private final Map<String, String> pairs;

	public NeroMetadataTagsBox() {
		super("Nero Metadata Tags Box");
		pairs = new HashMap<String, String>();
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		in.skipBytes(12); //meta box

		String key, val;
		int len;
		//TODO: what are the other skipped fields for?
		while(getLeft(in)>0&&in.read()==0x80) {
			in.skipBytes(2); //x80 x00 x06/x05
			key = in.readUTFString((int) getLeft(in), MP4InputStream.UTF8);
			in.skipBytes(4); //0x00 0x01 0x00 0x00 0x00
			len = in.read();
			val = in.readString(len);
			pairs.put(key, val);
		}
	}

	public Map<String, String> getPairs() {
		return pairs;
	}
}
