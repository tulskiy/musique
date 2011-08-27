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
import net.sourceforge.jaad.aac.SampleFrequency;
import net.sourceforge.jaad.aac.sbr2.SBR;

public abstract class Element implements Constants {

	private int elementInstanceTag;
	private SBR sbr;

	protected void readElementInstanceTag(BitStream in) throws AACException {
		elementInstanceTag = in.readBits(4);
	}

	public int getElementInstanceTag() {
		return elementInstanceTag;
	}

	void decodeSBR(BitStream in, SampleFrequency sf, int count, boolean stereo, boolean crc, boolean downSampled) throws AACException {
		if(sbr==null) sbr = new SBR(sf, downSampled);
		sbr.decode(in, count, stereo, crc);
	}

	boolean isSBRPresent() {
		return sbr!=null;
	}

	SBR getSBR() {
		return sbr;
	}
}
