/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphael Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.audio.generic;

import org.jaudiotagger.audio.AudioFile;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Contains various frequently used static functions in the different tag
 * formats
 *
 * @author Raphael Slinckx
 */
public class Utils {

    /**
     * Copies the bytes of <code>srd</code> to <code>dst</code> at the
     * specified offset.
     *
     * @param src       The byte to be copied.
     * @param dst       The array to copy to
     * @param dstOffset The start offset for the bytes to be copied.
     */
    public static void copy(byte[] src, byte[] dst, int dstOffset) {
        System.arraycopy(src, 0, dst, dstOffset, src.length);
    }

    /**
     * Returns {@link String#getBytes()}.<br>
     *
     * @param s The String to call, decode bytes using the specfied charset
     * @return The bytes.
     */
    public static byte[] getDefaultBytes(String s, String charSet) {
        try {
            return s.getBytes(charSet);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }

    }

    /*
      * Returns the extension of the given file.
      * The extension is empty if there is no extension
      * The extension is the string after the last "."
      *
      * @param f The file whose extension is requested
      * @return The extension of the given file
      */
    public static String getExtension(File f) {
        String name = f.getName().toLowerCase();
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return "";
        }

        return name.substring(i + 1);
    }

    /*
    * Computes a number whereby the 1st byte is the least signifcant and the last
    * byte is the most significant.
    *
    * @param b The byte array @param start The starting offset in b
    * (b[offset]). The less significant byte @param end The end index
    * (included) in b (b[end]). The most significant byte @return a long number
    * represented by the byte sequence.
    *
    * So if storing a number which only requires one byte it will be stored in the first
    * byte.
    */
    public static long getLongLE(ByteBuffer b, int start, int end) {
        long number = 0;
        for (int i = 0; i < (end - start + 1); i++) {
            number += ((b.get(start + i) & 0xFF) << i * 8);
        }

        return number;
    }

    /*
     * Computes a number whereby the 1st byte is the most significant and the last
     * byte is the least significant.
     *
     * So if storing a number which only requires one byte it will be stored in the last
     * byte.
     */
    public static long getLongBE(ByteBuffer b, int start, int end) {
        int number = 0;
        for (int i = 0; i < (end - start + 1); i++) {
            number += ((b.get(end - i) & 0xFF) << i * 8);
        }

        return number;
    }

    public static int getIntLE(byte[] b) {
        return (int) getLongLE(ByteBuffer.wrap(b), 0, b.length - 1);
    }

    /*
      * same as above, but returns an int instead of a long @param b The byte
      * array @param start The starting offset in b (b[offset]). The less
      * significant byte @param end The end index (included) in b (b[end]). The
      * most significant byte @return a int number represented by the byte
      * sequence.
      */
    public static int getIntLE(byte[] b, int start, int end) {
        return (int) getLongLE(ByteBuffer.wrap(b), start, end);
    }

    public static int getIntBE(byte[] b, int start, int end) {
        return (int) getLongBE(ByteBuffer.wrap(b), start, end);
    }

    public static int getIntBE(ByteBuffer b, int start, int end) {
        return (int) getLongBE(b, start, end);
    }

    public static short getShortBE(ByteBuffer b, int start, int end) {
        return (short) getIntBE(b, start, end);
    }

    /**
     * Convert int to byte representation - Big Endian (as used by mp4)
     *
     * @param size
     * @return byte represenetation
     */
    public static byte[] getSizeBEInt32(int size) {
        byte[] b = new byte[4];
        b[0] = (byte) ((size >> 24) & 0xFF);
        b[1] = (byte) ((size >> 16) & 0xFF);
        b[2] = (byte) ((size >> 8) & 0xFF);
        b[3] = (byte) (size & 0xFF);
        return b;
    }

    /**
     * Convert short to byte representation - Big Endian (as used by mp4)
     *
     * @param size
     * @return byte represenetation
     */
    public static byte[] getSizeBEInt16(short size) {
        byte[] b = new byte[2];
        b[0] = (byte) ((size >> 8) & 0xFF);
        b[1] = (byte) (size & 0xFF);
        return b;
    }

    /**
     * Convert int to byte representation - Little Endian (as used by ogg vorbis)
     *
     * @param size
     * @return byte represenetation
     */
    public static byte[] getSizeLEInt32(int size) {
        byte[] b = new byte[4];
        b[0] = (byte) (size & 0xff);
        b[1] = (byte) ((size >>> 8) & 0xffL);
        b[2] = (byte) ((size >>> 16) & 0xffL);
        b[3] = (byte) ((size >>> 24) & 0xffL);
        return b;
    }

    /**
     * Create String starting from offset upto length using encoding
     *
     * @param b
     * @param offset
     * @param length
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getString(byte[] b, int offset, int length, String encoding) {
        try {
            return new String(b, offset, length, encoding);
        } catch (UnsupportedEncodingException ue) {
            //Shouldnt have to worry about this exception as should only be calling with well defined charsets
            throw new RuntimeException(ue);
        }
    }

    /**
     * Create String offset from position by offset upto length using encoding, and position of buffer
     * is moved to after position + offset + length
     *
     * @param buffer
     * @param offset
     * @param length
     * @param encoding
     * @return
     */
    public static String getString(ByteBuffer buffer, int offset, int length, String encoding) {
        byte[] b = new byte[length];
        buffer.position(buffer.position() + offset);
        buffer.get(b);
        try {
            return new String(b, 0, length, encoding);
        } catch (UnsupportedEncodingException uee) {
            //TODO, will we ever use unsupported encodings
            throw new RuntimeException(uee);
        }
    }

    /*
      * Tries to convert a string into an UTF8 array of bytes If the conversion
      * fails, return the string converted with the default encoding.
      *
      * @param s The string to convert @return The byte array representation of
      * this string in UTF8 encoding
      */
    public static byte[] getUTF8Bytes(String s) throws UnsupportedEncodingException {
        return s.getBytes("UTF-8");
    }

    /**
     * Overflow checking since java can't handle unsigned numbers.
     */
    public static int readUint32AsInt(DataInput di) throws IOException {
        final long l = readUint32(di);
        if (l > Integer.MAX_VALUE) {
            throw new IOException("uint32 value read overflows int");
        }
        return (int) l;
    }

    public static long readUint32(DataInput di) throws IOException {
        final byte[] buf8 = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        di.readFully(buf8, 4, 4);
        final long l = ByteBuffer.wrap(buf8).getLong();
        return l;
    }

    public static int readUint16(DataInput di) throws IOException {
        final byte[] buf = {0x00, 0x00, 0x00, 0x00};
        di.readFully(buf, 2, 2);
        final int i = ByteBuffer.wrap(buf).getInt();
        return i;
    }

    public static String readString(DataInput di, int charsToRead) throws IOException {
        final byte[] buf = new byte[charsToRead];
        di.readFully(buf);
        return new String(buf);
    }

    public static long readUInt64(ByteBuffer b) {
        long result = 0;
        result += (readUBEInt32(b) << 32);
        result += readUBEInt32(b);
        return result;
    }

    public static int readUBEInt32(ByteBuffer b) {
        int result = 0;
        result += readUBEInt16(b) << 16;
        result += readUBEInt16(b);
        return result;
    }

    public static int readUBEInt24(ByteBuffer b) {
        int result = 0;
        result += readUBEInt16(b) << 16;
        result += readUInt8(b);
        return result;
    }

    public static int readUBEInt16(ByteBuffer b) {
        int result = 0;
        result += readUInt8(b) << 8;
        result += readUInt8(b);
        return result;
    }

    public static int readUInt8(ByteBuffer b) {
        return read(b);
    }

    public static int read(ByteBuffer b) {
        int result = (b.get() & 0xFF);
        return result;
    }

    /**
     * @param file
     * @return filename with audioformat seperator stripped of, lengthened to ensure not too small for calid tempfile
     *         creation.
     */
    public static String getMinBaseFilenameAllowedForTempFile(File file) {
        String s = AudioFile.getBaseFilename(file);
        if (s.length() >= 3) {
            return s;
        }
        if (s.length() == 1) {
            return s + "000";
        } else if (s.length() == 1) {
            return s + "00";
        } else if (s.length() == 2) {
            return s + "0";
        }
        return s;
    }
}
