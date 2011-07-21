/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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

package com.tulskiy.musique.audio.player;

import com.tulskiy.musique.audio.player.io.Buffer;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.AudioMath;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

/**
 * Author: Denis Tulskiy
 * Date: 1/15/11
 */
public class BufferTest {
    @Test
    public void testRW() {
        Buffer buffer = new Buffer();
        byte[] buf = new byte[100];
        Track t = new Track();
        Arrays.fill(buf, (byte) 1);
        t.getTrackData().setBitrate(5);
        buffer.addNextTrack(t, null, 0, false);
        buffer.write(buf, 0, buf.length);
        buffer.write(buf, 0, buf.length);

        t = new Track();
        t.getTrackData().setBitrate(10);
        buffer.addNextTrack(t, null, 0, false);
        t = new Track();
        t.getTrackData().setBitrate(20);

        buffer.addNextTrack(t, null, 0, false);
        buffer.write(buf, 0, buf.length);
        t = new Track();
        t.getTrackData().setBitrate(30);
        buffer.addNextTrack(t, null, 0, false);

        assertEquals(-1, buffer.read(buf, 0, 100));
        Buffer.NextEntry nextTrack = buffer.pollNextTrack();
        assertEquals(5, nextTrack.track.getTrackData().getBitrate());

        assertEquals(55, buffer.read(buf, 0, 55));
        assertEquals(100, buffer.read(buf, 0, 100));
        assertEquals(45, buffer.read(buf, 0, 100));

        assertEquals(-1, buffer.read(buf, 0, 100));
        nextTrack = buffer.pollNextTrack();
        assertEquals(10, nextTrack.track.getTrackData().getBitrate());

        assertEquals(-1, buffer.read(buf, 0, 100));
        nextTrack = buffer.pollNextTrack();

        assertEquals(20, nextTrack.track.getTrackData().getBitrate());
        assertEquals(100, buffer.read(buf, 0, 100));
        assertEquals(-1, buffer.read(buf, 0, 100));
    }

    @Test
    public void testConvert() {
        byte[] input = new byte[]{
                (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0xAB,
                0x12, 0x34, 0x56, 0x78, 0x12, 0x34, 0x56, 0x78
        };

        int[] output;
        AudioFormat fmt;

        output = new int[12];
        fmt = new AudioFormat(44100, 8, 1, true, false);
        assertEquals(AudioMath.convertBuffer(input, output, input.length, fmt), output.length);

        assertArrayEquals(new int[]{
                (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0xAB,
                0x12, 0x34, 0x56, 0x78,
                0x12, 0x34, 0x56, 0x78
        }, output);

        output = new int[12];
        fmt = new AudioFormat(44100, 8, 2, true, false);
        assertEquals(AudioMath.convertBuffer(input, output, input.length, fmt), output.length);

        assertArrayEquals(new int[]{
                (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, (byte) 0xAB,
                0x12, 0x34, 0x56, 0x78,
                0x12, 0x34, 0x56, 0x78
        }, output);

        output = new int[6];
        fmt = new AudioFormat(44100, 16, 1, true, false);
        assertEquals(AudioMath.convertBuffer(input, output, input.length, fmt), output.length);

        assertArrayEquals(new int[]{
                (short)0xCDAB, (short)0xABEF,
                (short)0x3412, (short)0x7856,
                (short)0x3412, (short)0x7856,
        }, output);

        output = new int[6];
        fmt = new AudioFormat(44100, 16, 2, true, false);
        assertEquals(AudioMath.convertBuffer(input, output, input.length, fmt), output.length);

        assertArrayEquals(new int[]{
                (short)0xCDAB, (short)0xABEF,
                (short)0x3412, (short)0x7856,
                (short)0x3412, (short)0x7856,
        }, output);

        output = new int[4];
        fmt = new AudioFormat(44100, 24, 1, true, false);
        assertEquals(AudioMath.convertBuffer(input, output, input.length, fmt), output.length);

        assertArrayEquals(new int[]{
                0xFFEFCDAB, 0x3412AB, 0x127856, 0x785634,
        }, output);
    }
}
