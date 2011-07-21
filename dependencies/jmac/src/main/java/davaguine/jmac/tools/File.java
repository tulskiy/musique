/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package davaguine.jmac.tools;

import java.io.DataInput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public abstract class File implements DataInput {

    public static File createFile(final String name, final String mode) throws IOException {
        URL url = null;
        try {
            url = new URL(name);
            return new InputStreamFile(url);
        } catch (MalformedURLException e) {
            return new RandomAccessFile(new java.io.File(name), mode);
        }
    }

    public abstract void mark(int readlimit) throws IOException;

    public abstract void reset() throws IOException;

    public abstract int read() throws IOException;

    public short readShortBack() throws IOException {
        return (short) (read() | (read() << 8));
    }

    public int readIntBack() throws IOException {
        return read() | (read() << 8) | (read() << 16) | (read() << 24);
    }

    public long readLongBack() throws IOException {
        return read() |
                (read() << 8) |
                (read() << 16) |
                (read() << 24) |
                (read() << 32) |
                (read() << 40) |
                (read() << 48) |
                (read() << 56);
    }

    public abstract int read(byte[] b) throws IOException;

    public abstract int read(byte[] b, int offs, int len) throws IOException;

    public abstract void readFully(byte[] b) throws IOException;

    public abstract void readFully(byte[] b, int offs, int len) throws IOException;

    public abstract void close() throws IOException;

    public abstract boolean readBoolean() throws IOException;

    public abstract byte readByte() throws IOException;

    public abstract char readChar() throws IOException;

    public abstract double readDouble() throws IOException;

    public abstract float readFloat() throws IOException;

    public abstract int readInt() throws IOException;

    public abstract String readLine() throws IOException;

    public abstract long readLong() throws IOException;

    public abstract short readShort() throws IOException;

    public abstract int readUnsignedByte() throws IOException;

    public abstract int readUnsignedShort() throws IOException;

    public abstract String readUTF() throws IOException;

    public abstract int skipBytes(int n) throws IOException;

    public abstract long length() throws IOException;

    public abstract void seek(long pos) throws IOException;

    public abstract long getFilePointer() throws IOException;

    public abstract void setLength(long newLength) throws IOException;

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public abstract void write(byte[] b, int off, int len) throws IOException;

    public abstract boolean isLocal();

    public abstract String getFilename();

    public String getExtension() {
        final String filename = getFilename();
        int index = filename.lastIndexOf('.');
        return index >= 0 ? filename.substring(index) : "";
    }
}
