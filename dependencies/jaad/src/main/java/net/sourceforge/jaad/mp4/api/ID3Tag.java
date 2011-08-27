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
package net.sourceforge.jaad.mp4.api;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ID3Tag {

	private static final int ID3_TAG = 4801587; //'ID3'
	private static final int SUPPORTED_VERSION = 4; //id3v2.4
	private final List<ID3Frame> frames;
	private final int tag, flags, len;

	ID3Tag(DataInputStream in) throws IOException {
		frames = new ArrayList<ID3Frame>();

		//id3v2 header
		tag = (in.read()<<16)|(in.read()<<8)|in.read(); //'ID3'
		final int majorVersion = in.read();
		in.read(); //revision
		flags = in.read();
		len = readSynch(in);

		if(tag==ID3_TAG&&majorVersion<=SUPPORTED_VERSION) {
			if((flags&0x40)==0x40) {
				//extended header; TODO: parse
				final int extSize = readSynch(in);
				in.skipBytes(extSize-6);
			}

			//read all id3 frames
			int left = len;
			ID3Frame frame;
			while(left>0) {
				frame = new ID3Frame(in);
				frames.add(frame);
				left -= frame.getSize();
			}
		}
	}

	public List<ID3Frame> getFrames() {
		return Collections.unmodifiableList(frames);
	}

	static int readSynch(DataInputStream in) throws IOException {
		int x = 0;
		for(int i = 0; i<4; i++) {
			x |= (in.read()&0x7F);
		}
		return x;
	}
}
