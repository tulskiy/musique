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

package com.tulskiy.musique.audio.player.io;

import com.tulskiy.musique.playlist.Track;

import javax.sound.sampled.AudioFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Denis Tulskiy
 * Date: 1/15/11
 */
public class Buffer {
    private RingBuffer buffer;
    private BlockingQueue<NextEntry> trackQueue = new LinkedBlockingDeque<NextEntry>();
    private Queue<Integer> when = new LinkedList<Integer>();
    private int bytesLeft = 0;

    public Buffer(int size) {
        buffer = new RingBuffer(size);
    }

    public Buffer() {
        this(65536);
    }

    public void write(byte[] b, int off, int len) {
        buffer.put(b, off, len);
    }

    public void addNextTrack(Track track, AudioFormat format, long startSample) {
        System.out.println("addNextTrack");
        int bytesLeft = available();
        for (Integer left : when) {
            bytesLeft -= left;
        }
        if (trackQueue.isEmpty())
            this.bytesLeft = bytesLeft;
        else
            when.add(bytesLeft);
        trackQueue.add(new NextEntry(track, format, startSample));
    }

    public NextEntry pollNextTrack() {
        System.out.println("pollNextTrack");
        NextEntry nextEntry = null;
        try {
            nextEntry = trackQueue.take();
            buffer.setEOF(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!when.isEmpty()) {
            bytesLeft = when.poll();
        } else {
            bytesLeft = -1;
        }
        return nextEntry;
    }

    public int read(byte[] b, int off, int len) {
        if (bytesLeft > 0) {
            if (bytesLeft < len) {
                len = bytesLeft;
            }
            bytesLeft -= len;
        } else if (bytesLeft == 0) {
            return -1;
        }
        return buffer.get(b, off, len);
    }

    public synchronized int available() {
        return buffer.getAvailable();
    }

    public int size() {
        return buffer.size();
    }

    public void flush() {
        buffer.empty();
    }

    public void removeTail(long bytes) {
        buffer.removeTail(bytes);
    }

    public synchronized void fadeout() {
        int fadeout = buffer.getAvailable() / 2;

        buffer.removeTail(buffer.size() - fadeout);
        byte[] bytes = new byte[fadeout];
        buffer.get(bytes, 0, bytes.length);

        double val = 1;
        double d = 1 / fadeout;
        for (int i = 0; i < bytes.length; i += 2) {
            int v = bytes[i] << 8 | bytes[i + 1];
            v *= val;
            bytes[i] = (byte) (v >> 8 & 0xFF);
            bytes[i+1] = (byte) (v & 0xFF);
            val -= d;
        }
    }

    public class NextEntry {
        public Track track;
        public AudioFormat format;
        public long startSample;

        NextEntry(Track track, AudioFormat format, long startSample) {
            this.track = track;
            this.format = format;
            this.startSample = startSample;
        }
    }
}
