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
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The FD item information box is optional, although it is mandatory for files
 * using FD hint tracks. It provides information on the partitioning of source
 * files and how FD hint tracks are combined into FD sessions. Each partition
 * entry provides details on a particular file partitioning, FEC encoding and
 * associated FEC reservoirs. It is possible to provide multiple entries for one
 * source file (identified by its item ID) if alternative FEC encoding schemes
 * or partitionings are used in the file. All partition entries are implicitly
 * numbered and the first entry has number 1.
 *
 * @author in-somnia
 */
public class FDItemInformationBox extends FullBox {

	public FDItemInformationBox() {
		super("FD Item Information Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int entryCount = (int) in.readBytes(2);
		readChildren(in, entryCount); //partition entries

		readChildren(in); //FDSessionGroupBox and GroupIDToNameBox
	}
}
