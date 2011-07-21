/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl;

import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntryDescriptor {

	public final static int ES_DESCRIPTOR = 3;
	public final static int DECODER_CONFIG_DESCRIPTOR = 4;
	public final static int DECODER_SPECIFIC_INFO_DESCRIPTOR = 5;
	//general
	protected int type;
	protected int size;
	protected int bytesRead;
	protected List<EntryDescriptor> children;
	//decoder specific info
	protected int decSpecificDataSize;
	protected long decSpecificDataOffset;
	private byte[] dsid;

	public static EntryDescriptor createDescriptor(MP4InputStream stream) throws IOException {
		final int tag = stream.read();
		int readed = 1;
		int size = 0;
		int b = 0;
		do {
			b = stream.read();
			size <<= 7;
			size |= b&0x7f;
			readed++;
		}
		while((b&0x80)==0x80);
		final EntryDescriptor desc = new EntryDescriptor(tag, size);
		switch(tag) {
			case ES_DESCRIPTOR:
				desc.createESDescriptor(stream);
				break;
			case DECODER_CONFIG_DESCRIPTOR:
				desc.createDecoderConfigDescriptor(stream);
				break;
			case DECODER_SPECIFIC_INFO_DESCRIPTOR:
				desc.createDecoderSpecificInfoDescriptor(stream);
				break;
			default:
				break;
		}
		stream.skipBytes(desc.size-desc.bytesRead);
		desc.bytesRead = readed+desc.size;
		return desc;
	}

	public EntryDescriptor(int type, int size) {
		this.bytesRead = 0;
		this.type = type;
		this.size = size;
		children = new ArrayList<EntryDescriptor>();
	}

	public void createESDescriptor(MP4InputStream in) throws IOException {
		in.skipBytes(2);
		final int flags = in.read();
		final boolean streamDependenceFlag = (flags&(1<<7))!=0;
		final boolean urlFlag = (flags&(1<<6))!=0;
		final boolean ocrFlag = (flags&(1<<5))!=0;
		bytesRead += 3;
		if(streamDependenceFlag) {
			in.skipBytes(2);
			bytesRead += 2;
		}
		if(urlFlag) {
			final int len = in.read();
			in.skipBytes(len);
			bytesRead += len+1;
		}
		if(ocrFlag) {
			in.skipBytes(2);
			bytesRead += 2;
		}
		EntryDescriptor desc;
		while(bytesRead<size) {
			desc = createDescriptor(in);
			children.add(desc);
			bytesRead += desc.getBytesRead();
		}
	}

	public void createDecoderConfigDescriptor(MP4InputStream in) throws IOException {
		in.skipBytes(13);
		bytesRead += 13;
		if(bytesRead<size) {
			final EntryDescriptor desc = createDescriptor(in);
			children.add(desc);
			bytesRead += desc.getBytesRead();
		}
	}

	public void createDecoderSpecificInfoDescriptor(MP4InputStream in) throws IOException {
		decSpecificDataOffset = in.getOffset();
		dsid = new byte[size];
		in.readBytes(dsid);
		bytesRead += size;
		decSpecificDataSize = size-bytesRead;
	}

	public long getDecoderSpecificDataOffset() {
		return decSpecificDataOffset;
	}

	public int getDecoderSpecificDataSize() {
		return decSpecificDataSize;
	}

	//children
	public List<EntryDescriptor> getChildren() {
		return Collections.unmodifiableList(children);
	}

	//getter
	public int getType() {
		return type;
	}

	public int getBytesRead() {
		return bytesRead;
	}

	public byte[] getDSID() {
		return dsid;
	}
}
