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
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * This box is used in custom metadata tags (within the box-type '----'). It
 * contains the name of the custom tag, whose data is stored in the 'data'-box.
 *
 * @author in-somnia
 */
public class ITunesMetadataNameBox extends FullBox {

	private String metaName;

	public ITunesMetadataNameBox() {
		super("iTunes Metadata Name Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		metaName = in.readString((int) getLeft(in));
	}

	public String getMetaName() {
		return metaName;
	}
}
