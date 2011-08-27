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
package net.sourceforge.jaad.mp4.boxes.impl.oma;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

public class OMAAccessUnitFormatBox extends FullBox {

	private boolean selectiveEncrypted;
	private int keyIndicatorLength, initialVectorLength;

	public OMAAccessUnitFormatBox() {
		super("OMA DRM Access Unit Format Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		//1 bit selective encryption, 7 bits reserved
		selectiveEncrypted = ((in.read()>>7)&1)==1;
		keyIndicatorLength = in.read(); //always zero?
		initialVectorLength = in.read();
	}

	public boolean isSelectiveEncrypted() {
		return selectiveEncrypted;
	}

	public int getKeyIndicatorLength() {
		return keyIndicatorLength;
	}

	public int getInitialVectorLength() {
		return initialVectorLength;
	}
}
