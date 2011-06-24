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

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class ByteBuffer {

    private byte[] data;
    private int index;

    public ByteBuffer() {
    }

    public ByteBuffer(final byte[] output, final int index) {
        this.data = output;
        this.index = index;
    }

    public void reset(byte[] data) {
        this.data = data;
        this.index = 0;
    }

    public void reset(byte[] data, int index) {
        this.data = data;
        this.index = index;
    }

    public void append(final byte value) {
        data[index++] = value;
    }

    public void append(final byte value1, final byte value2) {
        byte abyte0[];
        (abyte0 = data)[index++] = value1;
        abyte0[index++] = value2;
    }

    public void append(final short value) {
        byte abyte0[];
        (abyte0 = data)[index++] = (byte) (value & 0xff);
        abyte0[index++] = (byte) (value >> 8);
    }

    public void append(final short value1, final short value2) {
        byte abyte0[];
        (abyte0 = data)[index++] = (byte) (value1 & 0xff);
        abyte0[index++] = (byte) (value1 >> 8);
        abyte0[index++] = (byte) (value2 & 0xff);
        abyte0[index++] = (byte) (value2 >> 8);
    }

    public void append24(final int value) {
        byte abyte0[];
        (abyte0 = data)[index++] = (byte) (value & 0xff);
        abyte0[index++] = (byte) (value >> 8 & 0xff);
        abyte0[index++] = (byte) (value >> 16 & 0xff);
    }

    public void append24(final int value1, final int value2) {
        byte abyte0[];
        (abyte0 = data)[index++] = (byte) (value1 & 0xff);
        abyte0[index++] = (byte) (value1 >> 8 & 0xff);
        abyte0[index++] = (byte) (value1 >> 16 & 0xff);
        abyte0[index++] = (byte) (value2 & 0xff);
        abyte0[index++] = (byte) (value2 >> 8 & 0xff);
        abyte0[index++] = (byte) (value2 >> 16 & 0xff);
    }

    public void append(final int value) {
        byte abyte0[];
        (abyte0 = data)[index++] = (byte) (value & 0xff);
        abyte0[index++] = (byte) (value >> 8 & 0xff);
        abyte0[index++] = (byte) (value >> 16 & 0xff);
        abyte0[index++] = (byte) (value >> 24 & 0xff);
    }

    public void append(byte[] bytes) {
        append(bytes, 0, bytes.length);
    }

    public void append(byte[] bytes, int off, int len) {
        if (0 < len) {
            byte abyte1[];
            (abyte1 = data)[index++] = bytes[off];
            for (int i = off + 1; i < len; i++)
                abyte1[index++] = bytes[i];
        }
    }

    public byte[] getBytes() {
        return data;
    }

    public int getIndex() {
        return index;
    }

}
