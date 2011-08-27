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

public class ThreeGPPAlbumBox extends ThreeGPPMetadataBox {

	private int trackNumber;

	public ThreeGPPAlbumBox() {
		super("3GPP Album Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		trackNumber = (getLeft(in)>0) ? in.read() : -1;
	}

	/**
	 * The track number (order number) of the media on this album. This is an 
	 * optional field. If the field is not present, -1 is returned.
	 * 
	 * @return the track number
	 */
	public int getTrackNumber() {
		return trackNumber;
	}
}
