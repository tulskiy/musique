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

import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class ByteArrayReader {

    private byte[] data = null;
    private int index = 0;

    public ByteArrayReader() {
    }

    public ByteArrayReader(final File io, int size) throws IOException {
        this.data = new byte[size];
        io.readFully(data);
    }

    public ByteArrayReader(final byte[] data) {
        this.data = data;
    }

    public ByteArrayReader(int size) {
        this.data = new byte[size];
    }

    public byte[] getData() {
        return data;
    }

    public void reset(byte[] data, int index) {
        this.data = data;
        this.index = index;
    }

    public void reset(final File io, int size) throws IOException {
        index = 0;
        try {
            io.readFully(data, 0, size);
        } catch (EOFException e) {
        }
    }

    public void skipBytes(long n) {
        index += n;
    }

    public short readUnsignedByte() {
        return (short) (data[index++] & 0xff);
    }

    public int readUnsignedShort() {
        byte a1[];
        return ((a1 = data)[index++] & 0xff) | ((a1[index++] & 0xff) << 8);
    }

    public long readUnsignedInt() {
        byte a1[];
        return ((long) ((a1 = data)[index++] & 0xff)) | (((long) (a1[index++] & 0xff)) << 8) | (((long) (a1[index++] & 0xff)) << 16) | (((long) (a1[index++] & 0xff)) << 24);
    }

    public byte readByte() {
        return data[index++];
    }

    public short readShort() {
        byte a1[];
        return (short) (((a1 = data)[index++] & 0xff) | ((a1[index++] & 0xff) << 8));
    }

    public int readInt() {
        byte a1[];
        return (int) (((long) ((a1 = data)[index++] & 0xff)) | (((long) (a1[index++] & 0xff)) << 8) | (((long) (a1[index++] & 0xff)) << 16) | (((long) (a1[index++] & 0xff)) << 24));
    }

    public long readLong() {
        byte a1[];
        return ((long) ((a1 = data)[index++] & 0xff)) | (((long) (a1[index++] & 0xff)) << 8) | (((long) (a1[index++] & 0xff)) << 16) | (((long) (a1[index++] & 0xff)) << 24) | (((long) (a1[index++] & 0xff)) << 32) | (((long) (a1[index++] & 0xff)) << 40) | (((long) (a1[index++] & 0xff)) << 48) | (((long) (a1[index++] & 0xff)) << 56);
    }

    public void readFully(byte[] buf) {
        System.arraycopy(data, index, buf, 0, buf.length);
        index += buf.length;
    }

    public String readString(int size, String encoding) {
        Charset c;
        if (encoding == null)
            c = Charset.defaultCharset();
        else
            c = Charset.forName(encoding);
        String res = new String(data, index, size, c);
        index += size;
        return res;
    }

    public String readString(String encoding) {
        int size = 0;
        int i = index;
        while (data[i] != 0) {
            size++;
            i++;
        }
        String res;
        try {
            res = new String(data, index, size, encoding);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new JMACException("Unsupported encoding", e);
        }
        index += (size + 1);
        return res;
    }

}
