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

package org.xiph.libvorbis;

public class vorbis_info {

    int version;
    public int channels;
    int rate;                // long rate
    int bitrate_upper;        // long bitrate_upper
    int bitrate_nominal;    // long bitrate_nominal
    int bitrate_lower;        // long bitrate_lower
    int bitrate_window;        // long bitrate_window

    codec_setup_info codec_setup;    // void * codec_setup


    public vorbis_info() {

        codec_setup = new codec_setup_info();
    }

    public void vorbis_info_clear() {

        version = 0;
        channels = 0;
        rate = 0;
        bitrate_upper = 0;
        bitrate_nominal = 0;
        bitrate_lower = 0;
        bitrate_window = 0;

        // free codec_setup memory
        codec_setup = null;
    }
}