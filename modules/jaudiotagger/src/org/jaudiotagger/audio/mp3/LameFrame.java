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
package org.jaudiotagger.audio.mp3;

import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The first frame can sometimes contain a LAME frame at the end of the Xing frame
 * <p/>
 * <p>This useful to the library because it allows the encoder to be identified, full specification
 * can be found at http://gabriel.mp3-tech.org/mp3infotag.html
 * <p/>
 * Summarized here:
 * 4 bytes:LAME
 * 5 bytes:LAME Encoder Version
 * 1 bytes:VNR Method
 * 1 bytes:Lowpass filter value
 * 8 bytes:Replay Gain
 * 1 byte:Encoding Flags
 * 1 byte:minimal byte rate
 * 3 bytes:extra samples
 * 1 byte:Stereo Mode
 * 1 byte:MP3 Gain
 * 2 bytes:Surround Dound
 * 4 bytes:MusicLength
 * 2 bytes:Music CRC
 * 2 bytes:CRC Tag
 */
public class LameFrame {
    public static final int LAME_HEADER_BUFFER_SIZE = 36;
    public static final int ENCODER_SIZE = 9;   //Includes LAME ID
    public static final int LAME_ID_SIZE = 4;
    public static final byte[] LAME_ID = "LAME".getBytes();
    private String encoder;
    private byte vbrMethod;
    private int lowpass;
    private float replayGainPeak;
    private short replayGainRadio;
    private short replayGainAudiophile;
    private byte encFlags;
    private byte bitrate;
    private int encDelay;
    private int encPadding;

    /**
     * Initilise a Lame Mpeg Frame
     *
     * @param lameHeader
     */
    private LameFrame(ByteBuffer lameHeader) {
        encoder = Utils.getString(lameHeader, 0, ENCODER_SIZE, TextEncoding.CHARSET_ISO_8859_1);
        vbrMethod = (byte) (lameHeader.get() & 0x0F);
        lowpass = lameHeader.get() * 100;
        replayGainPeak = lameHeader.getFloat();
        replayGainRadio = lameHeader.getShort();
        replayGainAudiophile = lameHeader.getShort();
        encFlags = lameHeader.get();
        bitrate = lameHeader.get();
        byte[] delay = new byte[3];
        lameHeader.get(delay);
        encDelay = delay[0] << 4 | (delay[1] >> 4 & 0x0F);
        encPadding = (delay[1] & 0x0F) << 8 | (delay[2] & 0xFF);
        //TODO do the rest
    }

    /**
     * Parse frame
     *
     * @param bb
     * @return frame or null if not exists
     */
    public static LameFrame parseLameFrame(ByteBuffer bb) {
        ByteBuffer lameHeader = bb.slice();
        byte[] id = new byte[LAME_ID_SIZE];
        lameHeader.get(id);
        if (Arrays.equals(id, LAME_ID)) {
            lameHeader.position(lameHeader.position() - 4);
            return new LameFrame(lameHeader);
        }
        lameHeader.position(lameHeader.position() - 3);
        return null;
    }

    /**
     * @return encoder
     */
    public String getEncoder() {
        return encoder;
    }

    public byte getVbrMethod() {
        return vbrMethod;
    }

    public int getLowpass() {
        return lowpass;
    }

    public float getReplayGainPeak() {
        return replayGainPeak;
    }

    public short getReplayGainRadio() {
        return replayGainRadio;
    }

    public short getReplayGainAudiophile() {
        return replayGainAudiophile;
    }

    public byte getEncFlags() {
        return encFlags;
    }

    public byte getBitrate() {
        return bitrate;
    }

    public int getEncDelay() {
        return encDelay;
    }

    public int getEncPadding() {
        return encPadding;
    }
}

