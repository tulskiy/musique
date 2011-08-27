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
package net.sourceforge.jaad.aac.syntax;

import net.sourceforge.jaad.aac.AACException;

class DSE extends Element {

	private byte[] dataStreamBytes;

	DSE() {
		super();
	}

	void decode(BitStream in) throws AACException {
		final boolean byteAlign = in.readBool();
		int count = in.readBits(8);
		if(count==255) count += in.readBits(8);

		if(byteAlign) in.byteAlign();

		dataStreamBytes = new byte[count];
		for(int i = 0; i<count; i++) {
			dataStreamBytes[i] = (byte) in.readBits(8);
		}
	}
}
