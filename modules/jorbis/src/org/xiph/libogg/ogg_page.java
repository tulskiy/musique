/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.xiph.libogg;

public class ogg_page {

    public byte[] header;        // unsigned char
    public int header_len;        // long
    public byte[] body;            // unsigned char
    public int body_len;        // long

    // static ogg_uint32_t crc_lookup[256]={
    private static int[] crc_lookup = new int[256];

    static {
        for (int i = 0; i < crc_lookup.length; i++) {
            crc_lookup[i] = crc_entry(i);
        }
    }

    private static int crc_entry(int index) {

        int r = index << 24;
        for (int i = 0; i < 8; i++) {

            if ((r & 0x80000000) != 0) {
                r = (r << 1) ^ 0x04c11db7;
                /* The same as the ethernet generator
                     * polynomial, although we use an
                     * unreflected alg and an init/final
                     * of 0, not 0xffffffff */
            } else {
                r <<= 1;
            }
        }
        return (r & 0xffffffff);
    }

    public ogg_page() {
    }

    public int ogg_page_eos() {

        return ((int) (header[5] & 0x04));
    }

    public void ogg_page_checksum_set() {

        // ogg_uint32_t crc_reg=0;
        int crc_reg = 0;
        int i;

        // safety; needed for API behavior, but not framing code
        header[22] = 0;
        header[23] = 0;
        header[24] = 0;
        header[25] = 0;

        for (i = 0; i < header_len; i++) {
            crc_reg = (crc_reg << 8) ^ crc_lookup[((crc_reg >>> 24) & 0xff) ^ (header[i] & 0xff)];
        }
        for (i = 0; i < body_len; i++) {
            crc_reg = (crc_reg << 8) ^ crc_lookup[((crc_reg >>> 24) & 0xff) ^ (body[i] & 0xff)];
        }

        header[22] = (byte) crc_reg /*&0xff*/;
        header[23] = (byte) (crc_reg >>> 8) /*&0xff*/;
        header[24] = (byte) (crc_reg >>> 16) /*&0xff*/;
        header[25] = (byte) (crc_reg >>> 24) /*&0xff*/;
    }
}