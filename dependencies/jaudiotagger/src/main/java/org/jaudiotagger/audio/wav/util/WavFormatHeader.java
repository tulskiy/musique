/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Rapha�l Slinckx <raphael@slinckx.net>
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
package org.jaudiotagger.audio.wav.util;

public class WavFormatHeader {

    private boolean isValid = false;

    private int channels, sampleRate, bytesPerSecond, bitrate;

    public WavFormatHeader(byte[] b) {
        String fmt = new String(b, 0, 3);
        //System.err.println(fmt);
        if (fmt.equals("fmt") && b[8] == 1) {
            channels = b[10];
            //System.err.println(channels);
            sampleRate = u(b[15]) * 16777216 + u(b[14]) * 65536 + u(b[13]) * 256 + u(b[12]);
            //System.err.println(sampleRate);
            bytesPerSecond = u(b[19]) * 16777216 + u(b[18]) * 65536 + u(b[17]) * 256 + u(b[16]);
            //System.err.println(bytesPerSecond);
            bitrate = u(b[22]);

            isValid = true;
        }

    }

    public boolean isValid() {
        return isValid;
    }

    public int getChannelNumber() {
        return channels;
    }

    public int getSamplingRate() {
        return sampleRate;
    }

    public int getBytesPerSecond() {
        return bytesPerSecond;
    }

    public int getBitrate() {
        return bitrate;
    }

    private int u(int n) {
        return n & 0xff;
    }

    public String toString() {
        String out = "RIFF-WAVE Header:\n";
        out += "Is valid?: " + isValid;
        return out;
    }
}