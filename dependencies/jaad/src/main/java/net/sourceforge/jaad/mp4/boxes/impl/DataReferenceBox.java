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
 * The data reference object contains a table of data references (normally URLs)
 * that declare the location(s) of the media data used within the presentation.
 * The data reference index in the sample description ties entries in this table
 * to the samples in the track. A track may be split over several sources in
 * this way.
 * The data entry is either a DataEntryUrnBox or a DataEntryUrlBox.
 * 
 * @author in-somnia
 */
public class DataReferenceBox extends FullBox {

	public DataReferenceBox() {
		super("Data Reference Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int entryCount = (int) in.readBytes(4);

		readChildren(in, entryCount); //DataEntryUrlBox, DataEntryUrnBox
	}
}
