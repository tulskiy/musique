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

public class BitStream {

	private static final int WORD_BITS = 32;
	private static final int WORD_BYTES = 4;
	private static final int BYTE_MASK = 0xff;
	private byte[] buffer;
	private int pos; //offset in the buffer array
	private int cache; //current 4 bytes, that are read from the buffer
	protected int bitsCached; //remaining bits in current cache
	protected int position; //number of total bits read

	public BitStream() {
	}

	public BitStream(byte[] data) {
		setData(data);
	}

	public void destroy() {
		reset();
		buffer = null;
	}

	public final void setData(byte[] data) {
		//make the buffer size an integer number of words
		final int size = WORD_BYTES*((data.length+WORD_BYTES-1)/WORD_BYTES);
		//only reallocate if needed
		if(buffer==null||buffer.length!=size) buffer = new byte[size];
		System.arraycopy(data, 0, buffer, 0, data.length);
		reset();
	}

	public void byteAlign() throws AACException {
		final int toFlush = bitsCached&7;
		if(toFlush>0) skipBits(toFlush);
	}

	public final void reset() {
		pos = 0;
		bitsCached = 0;
		cache = 0;
		position = 0;
	}

	public int getPosition() {
		return position;
	}

	public int getBitsLeft() {
		return 8*(buffer.length-pos)+bitsCached;
	}

	/**
	 * Reads the next four bytes.
	 * @param peek if true, the stream pointer will not be increased
	 */
	protected int readCache(boolean peek) throws AACException {
		int i;
		if(pos>buffer.length-WORD_BYTES) throw new AACException("end of stream", true);
		else i = ((buffer[pos]&BYTE_MASK)<<24)
					|((buffer[pos+1]&BYTE_MASK)<<16)
					|((buffer[pos+2]&BYTE_MASK)<<8)
					|(buffer[pos+3]&BYTE_MASK);
		if(!peek) pos += WORD_BYTES;
		return i;
	}

	public int readBits(int n) throws AACException {
		int result;
		if(bitsCached>=n) {
			bitsCached -= n;
			result = (cache>>bitsCached)&maskBits(n);
			position += n;
		}
		else {
			position += n;
			final int c = cache&maskBits(bitsCached);
			final int left = n-bitsCached;
			cache = readCache(false);
			bitsCached = WORD_BITS-left;
			result = ((cache>>bitsCached)&maskBits(left))|(c<<left);
		}
		return result;
	}

	public int readBit() throws AACException {
		int i;
		if(bitsCached>0) {
			bitsCached--;
			i = (cache>>(bitsCached))&1;
			position++;
		}
		else {
			cache = readCache(false);
			bitsCached = WORD_BITS-1;
			position++;
			i = (cache>>bitsCached)&1;
		}
		return i;
	}

	public boolean readBool() throws AACException {
		return (readBit()&0x1)!=0;
	}

	public int peekBits(int n) throws AACException {
		int ret;
		if(bitsCached>=n) {
			ret = (cache>>(bitsCached-n))&maskBits(n);
		}
		else {
			//old cache
			final int c = cache&maskBits(bitsCached);
			n -= bitsCached;
			//read next & combine
			ret = ((readCache(true)>>WORD_BITS-n)&maskBits(n))|(c<<n);
		}
		return ret;
	}

	public int peekBit() throws AACException {
		int ret;
		if(bitsCached>0) {
			ret = (cache>>(bitsCached-1))&1;
		}
		else {
			final int word = readCache(true);
			ret = (word>>WORD_BITS-1)&1;
		}
		return ret;
	}

	public void skipBits(int n) throws AACException {
		position += n;
		if(n<=bitsCached) {
			bitsCached -= n;
		}
		else {
			n -= bitsCached;
			while(n>=WORD_BITS) {
				n -= WORD_BITS;
				readCache(false);
			}
			if(n>0) {
				cache = readCache(false);
				bitsCached = WORD_BITS-n;
			}
			else {
				cache = 0;
				bitsCached = 0;
			}
		}
	}

	public void skipBit() throws AACException {
		position++;
		if(bitsCached>0) {
			bitsCached--;
		}
		else {
			cache = readCache(false);
			bitsCached = WORD_BITS-1;
		}
	}

	public int maskBits(int n) {
		int i;
		if(n==32) i = -1;
		else i = (1<<n)-1;
		return i;
	}
}
