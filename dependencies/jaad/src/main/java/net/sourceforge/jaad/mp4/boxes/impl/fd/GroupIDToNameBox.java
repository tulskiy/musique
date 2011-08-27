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
package net.sourceforge.jaad.mp4.boxes.impl.fd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

public class GroupIDToNameBox extends FullBox {

	private final Map<Long, String> map;

	public GroupIDToNameBox() {
		super("Group ID To Name Box");
		map = new HashMap<Long, String>();
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int entryCount = (int) in.readBytes(2);
		long id;
		String name;
		for(int i = 0; i<entryCount; i++) {
			id = in.readBytes(4);
			name = in.readUTFString((int) getLeft(in), MP4InputStream.UTF8);
			map.put(id, name);
		}
	}

	/**
	 * Returns the map that contains the ID-name-pairs for all groups.
	 *
	 * @return the ID to name map
	 */
	public Map<Long, String> getMap() {
		return map;
	}
}
