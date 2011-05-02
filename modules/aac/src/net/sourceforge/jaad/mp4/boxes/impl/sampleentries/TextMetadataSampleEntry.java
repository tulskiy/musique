/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl.sampleentries;

import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;

public class TextMetadataSampleEntry extends MetadataSampleEntry {

	private String mimeType;

	public TextMetadataSampleEntry() {
		super("Text Metadata Sample Entry", "mett");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		mimeType = in.readUTFString((int) left, MP4InputStream.UTF8);
		left -= mimeType.length()+1;

		readChildren(in);
	}

	/**
	 * Provides a MIME type which identifies the content format of the timed
	 * metadata. Examples for this field are 'text/html' and 'text/plain'.
	 * 
	 * @return the content's MIME type
	 */
	public String getMimeType() {
		return mimeType;
	}
}
