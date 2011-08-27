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
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The Movie Fragment Random Access Offset Box provides a copy of the length
 * field from the enclosing Movie Fragment Random Access Box. It is placed last
 * within that box, so that the size field is also last in the enclosing Movie
 * Fragment Random Access Box. When the Movie Fragment Random Access Box is also
 * last in the file this permits its easy location. The size field here must be
 * correct. However, neither the presence of the Movie Fragment Random Access
 * Box, nor its placement last in the file, are assured.
 *
 * @author in-somnia
 */
public class MovieFragmentRandomAccessOffsetBox extends FullBox {

	private long byteSize;

	public MovieFragmentRandomAccessOffsetBox() {
		super("Movie Fragment Random Access Offset Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		byteSize = in.readBytes(4);
	}

	public long getByteSize() {
		return byteSize;
	}
}
