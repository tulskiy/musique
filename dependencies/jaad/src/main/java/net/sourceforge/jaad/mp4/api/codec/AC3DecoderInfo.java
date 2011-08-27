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
package net.sourceforge.jaad.mp4.api.codec;

import net.sourceforge.jaad.mp4.api.DecoderInfo;
import net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec.AC3SpecificBox;
import net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec.CodecSpecificBox;

public class AC3DecoderInfo extends DecoderInfo {

	private AC3SpecificBox box;

	public AC3DecoderInfo(CodecSpecificBox box) {
		this.box = (AC3SpecificBox) box;
	}

	public boolean isLfeon() {
		return box.isLfeon();
	}

	public int getFscod() {
		return box.getFscod();
	}

	public int getBsmod() {
		return box.getBsmod();
	}

	public int getBsid() {
		return box.getBsid();
	}

	public int getBitRateCode() {
		return box.getBitRateCode();
	}

	public int getAcmod() {
		return box.getAcmod();
	}
}
