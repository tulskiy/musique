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
package net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

/**
 * The <code>CodecSpecificBox</code> can be used instead of an <code>ESDBox</code>
 * in a sample entry. It contains <code>DecoderSpecificInfo</code>s.
 *
 * @author in-somnia
 */
public abstract class CodecSpecificBox extends BoxImpl {

	private long vendor;
	private int decoderVersion;

	public CodecSpecificBox(String name) {
		super(name);
	}

	protected void decodeCommon(MP4InputStream in) throws IOException {
		vendor = in.readBytes(4);
		decoderVersion = in.read();
	}

	public long getVendor() {
		return vendor;
	}

	public int getDecoderVersion() {
		return decoderVersion;
	}
}
