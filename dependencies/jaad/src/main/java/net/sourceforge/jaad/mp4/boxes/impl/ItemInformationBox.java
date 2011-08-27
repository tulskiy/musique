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
 * The item information box provides extra information about selected items,
 * including symbolic ('file') names. It may optionally occur, but if it does,
 * it must be interpreted, as item protection or content encoding may have
 * changed the format of the data in the item. If both content encoding and
 * protection are indicated for an item, a reader should first un-protect the
 * item, and then decode the item's content encoding. If more control is needed,
 * an IPMP sequence code may be used.
 *
 * This box contains an array of entries, and each entry is formatted as a box.
 * This array is sorted by increasing item ID in the entry records.
 *
 * Two versions of the item info entry are defined. Version 1 includes
 * additional information to version 0 as specified by an extension type. For
 * instance, it shall be used with extension type 'fdel' for items that are
 * referenced by the file partition box ('fpar'), which is defined for source
 * file partitionings and applies to file delivery transmissions.
 *
 * If no extension is desired, the box may terminate without the extension type
 * field and the extension; if, in addition, content encoding is not desired,
 * that field also may be absent and the box terminate before it.
 *
 * @author in-somnia
 */
public class ItemInformationBox extends FullBox {

	public ItemInformationBox() {
		super("Item Information Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int protectionCount = (int) in.readBytes(2);

		readChildren(in, protectionCount);
	}
}
