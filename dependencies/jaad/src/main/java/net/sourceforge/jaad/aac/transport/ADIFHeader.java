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
package net.sourceforge.jaad.aac.transport;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.PCE;

public final class ADIFHeader {

	private static final long ADIF_ID = 0x41444946; //'ADIF'
	private long id;
	private boolean copyrightIDPresent;
	private byte[] copyrightID;
	private boolean originalCopy, home, bitstreamType;
	private int bitrate;
	private int pceCount;
	private int[] adifBufferFullness;
	private PCE[] pces;

	public static boolean isPresent(BitStream in) throws AACException {
		return in.peekBits(32)==ADIF_ID;
	}

	private ADIFHeader() {
		copyrightID = new byte[9];
	}

	public static ADIFHeader readHeader(BitStream in) throws AACException {
		final ADIFHeader h = new ADIFHeader();
		h.decode(in);
		return h;
	}

	private void decode(BitStream in) throws AACException {
		int i;
		id = in.readBits(32); //'ADIF'
		copyrightIDPresent = in.readBool();
		if(copyrightIDPresent) {
			for(i = 0; i<9; i++) {
				copyrightID[i] = (byte) in.readBits(8);
			}
		}
		originalCopy = in.readBool();
		home = in.readBool();
		bitstreamType = in.readBool();
		bitrate = in.readBits(23);
		pceCount = in.readBits(4)+1;
		pces = new PCE[pceCount];
		adifBufferFullness = new int[pceCount];
		for(i = 0; i<pceCount; i++) {
			if(bitstreamType) adifBufferFullness[i] = -1;
			else adifBufferFullness[i] = in.readBits(20);
			pces[i] = new PCE();
			pces[i].decode(in);
		}
	}

	public PCE getFirstPCE() {
		return pces[0];
	}
}
