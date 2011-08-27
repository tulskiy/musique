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
package net.sourceforge.jaad.spi.javasound;

import net.sourceforge.jaad.spi.javasound.CircularBuffer.Trigger;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

abstract class AsynchronousAudioInputStream extends AudioInputStream implements Trigger {

	private byte[] singleByte;
	protected final CircularBuffer buffer;

	AsynchronousAudioInputStream(InputStream in, AudioFormat format, long length) throws IOException {
		super(in, format, length);
		buffer = new CircularBuffer(this);
	}

	@Override
	public int read() throws IOException {
		int i = -1;
		if(singleByte==null) singleByte = new byte[1];
		if(buffer.read(singleByte, 0, 1)==-1) i = -1;
		else i = singleByte[0]&0xFF;
		return i;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return buffer.read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return buffer.read(b, off, len);
	}

	@Override
	public long skip(long len) throws IOException {
		int l = (int) len;
		final byte[] b = new byte[l];
		while(l>0) {
			l -= buffer.read(b, 0, l);
		}
		return len;
	}

	@Override
	public int available() throws IOException {
		return buffer.availableRead();
	}

	@Override
	public void close() throws IOException {
		buffer.close();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void mark(int limit) {
	}

	@Override
	public void reset() throws IOException {
		throw new IOException("mark not supported");
	}
}
