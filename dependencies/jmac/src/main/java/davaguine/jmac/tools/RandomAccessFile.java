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

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 12.03.2004
 * Time: 13:35:13
 */
public class RandomAccessFile extends File {
    private java.io.RandomAccessFile file = null;
    private java.io.File f = null;
    private long markPosition = -1;

    public RandomAccessFile(final java.io.File file, final String mode) throws FileNotFoundException {
        this.f = file;
        this.file = new java.io.RandomAccessFile(file, mode);
    }

    public void mark(int readlimit) throws IOException {
        markPosition = file.getFilePointer();
    }

    public void reset() throws IOException {
        if (markPosition >= 0)
            file.seek(markPosition);
    }

    public int read() throws IOException {
        return file.read();
    }

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

    public int read(byte[] b) throws IOException {
        return file.read(b);
    }

    public int read(byte[] b, int offs, int len) throws IOException {
        return file.read(b, offs, len);
    }

    public void readFully(byte[] b) throws IOException {
        file.readFully(b);
    }

    public void readFully(byte[] b, int offs, int len) throws IOException {
        file.readFully(b, offs, len);
    }

    public void close() throws IOException {
        file.close();
    }

    public boolean readBoolean() throws IOException {
        return file.readBoolean();
    }

    public byte readByte() throws IOException {
        return file.readByte();
    }

    public char readChar() throws IOException {
        return file.readChar();
    }

    public double readDouble() throws IOException {
        return file.readDouble();
    }

    public float readFloat() throws IOException {
        return file.readFloat();
    }

    public int readInt() throws IOException {
        return file.readInt();
    }

    public String readLine() throws IOException {
        return file.readLine();
    }

    public long readLong() throws IOException {
        return file.readLong();
    }

    public short readShort() throws IOException {
        return file.readShort();
    }

    public int readUnsignedByte() throws IOException {
        return file.readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException {
        return file.readUnsignedShort();
    }

    public String readUTF() throws IOException {
        return file.readUTF();
    }

    public int skipBytes(int n) throws IOException {
        return file.skipBytes(n);
    }

    public long length() throws IOException {
        return file.length();
    }

    public void seek(long pos) throws IOException {
        file.seek(pos);
    }

    public long getFilePointer() throws IOException {
        return file.getFilePointer();
    }

    public void setLength(long newLength) throws IOException {
        file.setLength(newLength);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len);
    }

    public boolean isLocal() {
        return true;
    }

    public String getFilename() {
        return f.getName();
    }
}
