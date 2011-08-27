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
import net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec.AVCSpecificBox;
import net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec.CodecSpecificBox;

public class AVCDecoderInfo extends DecoderInfo {

	private AVCSpecificBox box;

	public AVCDecoderInfo(CodecSpecificBox box) {
		this.box = (AVCSpecificBox) box;
	}

	public int getConfigurationVersion() {
		return box.getConfigurationVersion();
	}

	public int getProfile() {
		return box.getProfile();
	}

	public byte getProfileCompatibility() {
		return box.getProfileCompatibility();
	}

	public int getLevel() {
		return box.getLevel();
	}

	public int getLengthSize() {
		return box.getLengthSize();
	}

	public byte[][] getSequenceParameterSetNALUnits() {
		return box.getSequenceParameterSetNALUnits();
	}

	public byte[][] getPictureParameterSetNALUnits() {
		return box.getPictureParameterSetNALUnits();
	}
}
