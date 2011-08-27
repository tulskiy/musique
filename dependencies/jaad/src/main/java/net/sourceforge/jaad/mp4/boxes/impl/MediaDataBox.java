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

import net.sourceforge.jaad.mp4.boxes.BoxImpl;
import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;

/**
 * The Media Data Box contains the media data. In video tracks, this box would
 * contain video frames. A presentation may contain zero or more Media Data
 * Boxes. The actual media data follows the type field; its structure is
 * described by the metadata in the movie box.
 * There may be any number of these boxes in the file (including zero, if all
 * the media data is in other files). The metadata refers to media data by its
 * absolute offset within the file.
 * @author in-somnia
 */
public class MediaDataBox extends BoxImpl {

	public MediaDataBox() {
		super("Media Data Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		//if random access: skip, else: do nothing
	}
}
