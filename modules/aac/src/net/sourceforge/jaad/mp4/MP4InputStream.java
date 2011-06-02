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
package net.sourceforge.jaad.mp4;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MP4InputStream {

	public static final int MASK8 = 0xFF;
	public static final int MASK16 = 0xFFFF;
	public static final String UTF8 = "UTF-8";
	public static final String UTF16 = "UTF-16";
	private static final int BYTE_ORDER_MASK = 0xFEFF;
	private final InputStream in;
	private long offset = 0;

	MP4InputStream(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		offset++;
		return in.read();
	}

	public long readBytes(int n) throws IOException {
		int i = -1;
		long result = 0;
		while(n>0&&(i = in.read())!=-1) {
			result = (result<<8)|(i&0xFF);
			offset++;
			n--;
		}
		if(i==-1) throw new EOFException();
		else return result;
	}

	public boolean readBytes(final byte[] b) throws IOException {
		int read = 0;
		int i;
		while(read<b.length) {
			i = in.read(b, read, b.length-read);
			if(i==-1) break;
			else {
				read += i;
				offset += i;
			}
		}
		return read==b.length;
	}

	public String readString(final int n) throws IOException {
		int i = -1;
		int pos = 0;
		char[] c = new char[n];
		while(pos<n&&(i = in.read())!=-1) {
			c[pos] = (char) i;
			offset++;
			pos++;
		}
		if(i==-1) throw new EOFException();
		return new String(c, 0, pos);
	}

	public String readUTFString(int max, String encoding) throws IOException {
		long x = offset;
		byte[] b = new byte[max];
		int pos = 0;
		int i;
		while((i = in.read())!=0) {
			if(i==-1) break;
			b[pos] = (byte) i;
			offset++;
			pos++;
		}
		offset++;

		return new String(b, 0, pos, encoding);
	}

	public String readUTFString(int max) throws IOException {
		int i = (int) readBytes(2);
		String encoding = (i==BYTE_ORDER_MASK) ? UTF16 : UTF8;
		return readUTFString(max, encoding);
	}

	public double readFixedPoint(int len, int mask) throws IOException {
		long l = readBytes(len);
		long mantissa = (l&mask)<<52;
		long exponent = l&mask;
		return Double.longBitsToDouble(mantissa|exponent);
	}

	public void skipBytes(final long n) throws IOException {
		offset += n;
		in.skip(n);
	}

	public long getOffset() {
		return offset;
	}

	void close() throws IOException {
		in.close();
	}

    public void position(long position) {
        if (in instanceof FileInputStream) {
            try {
                ((FileInputStream) in).getChannel().position(position);
                offset = position;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int available() throws IOException {
        return in.available();
    }
}
