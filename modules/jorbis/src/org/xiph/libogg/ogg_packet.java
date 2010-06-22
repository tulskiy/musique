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

public class ogg_packet {

    public byte[] packet;    // unsigned char *packet;
    public int bytes;    // long
    public int b_o_s;    // long
    public int e_o_s;    // long

    public int granulepos;    // ogg_int64_t

    public int packetno;    // ogg_int64_t

    // sequence number for decode; the framing knows where there's a hole in the data, but we need
    // coupling so that the codec (which is in a seperate abstraction layer) also knows about the gap


    public ogg_packet() {
    }

}