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
package net.sourceforge.jaad.mp4.boxes;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;

public class FullBox extends BoxImpl {

	protected int version, flags;

	public FullBox(String name, String shortName) {
		super(name, shortName);
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		version = in.read();
		flags = (int) in.readBytes(3);
		left -= 4;
	}
}
