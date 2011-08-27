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
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The chapter box allows to specify individual chapters along the main timeline
 * of a movie. The chapter box occurs within a movie box.
 * Defined in "Adobe Video File Format Specification v10".
 *
 * @author in-somnia
 */
public class ChapterBox extends FullBox {

	private final Map<Long, String> chapters;

	public ChapterBox() {
		super("Chapter Box");
		chapters = new HashMap<Long, String>();
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		in.skipBytes(4); //??

		final int count = in.read();

		long timestamp;
		int len;
		String name;
		for(int i = 0; i<count; i++) {
			timestamp = in.readBytes(8);
			len = in.read();
			name = in.readString(len);
			chapters.put(timestamp, name);
		}
	}

	/**
	 * Returns a map that maps the timestamp of each chapter to its name.
	 *
	 * @return the chapters
	 */
	public Map<Long, String> getChapters() {
		return chapters;
	}
}
