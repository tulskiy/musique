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
import org.junit.Test;
import static org.junit.Assert.*;

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
        buffer.addNextTrack(t, null, 0);
        buffer.write(buf, 0, buf.length);
        buffer.write(buf, 0, buf.length);

        t = new Track();
        t.getTrackData().setBitrate(10);
        buffer.addNextTrack(t, null, 0);
        t = new Track();
        t.getTrackData().setBitrate(20);

        buffer.addNextTrack(t, null, 0);
        buffer.write(buf, 0, buf.length);
        t = new Track();
        t.getTrackData().setBitrate(30);
        buffer.addNextTrack(t, null, 0);

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
}
