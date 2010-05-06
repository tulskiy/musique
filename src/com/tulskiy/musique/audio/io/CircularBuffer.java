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

package com.tulskiy.musique.audio.io;

/**
 * @Author: Denis Tulskiy
 * @Date: 24.06.2009
 */
public class CircularBuffer implements PCMOutputStream {
    private int front = 0, tail = 0;
    private int buflen = 65536;
    /* Rinbuffer */
    private byte buffer[];
    private boolean flush = false;

    public CircularBuffer() {
        this(65536);
    }

    public CircularBuffer(int size) {
        buflen = size;
        buffer = new byte[buflen];
    }

    public int available() {
        if (front >= tail)
            return front - tail;
        else
            return buflen - (tail - front);
    }

    public synchronized void write(byte b[], int offset, int len) {
        if (len >= buflen) {
            return;
        }
        while (available() + len >= buflen) {
            try {
                wait();
            }
            catch (InterruptedException ignored) {
            }
            if (flush) {
                flush = false;
                return;
            }
        }
        flush = false;

        if (buflen - front >= len) {
            System.arraycopy(b, offset, buffer, front, len);
        } else {
            int len2 = buflen - front;
            System.arraycopy(b, offset, buffer, front, len2);
            System.arraycopy(b, offset + len2, buffer, 0, len - len2);
        }
        front += len;
        if (front >= buflen)
            front -= buflen;

        notify();
    }

    public synchronized int read(byte b[], int offset, int len) {
        int len1;
        while ((len1 = available()) == 0) {
            try {
                wait();
            }
            catch (InterruptedException ignored) {
            }
        }
        if (len1 > len)
            len1 = len;
        if (buflen - tail >= len1) {
            System.arraycopy(buffer, tail, b, offset, len1);
        } else {
            int len2 = buflen - tail;
            System.arraycopy(buffer, tail, b, offset, len2);
            System.arraycopy(buffer, 0, b, offset + len2, len1 - len2);
        }
        tail += len1;
        if (tail >= buflen)
            tail -= buflen;
        notifyAll();
        return len1;
    }

    public synchronized void flush() {
        tail = 0;
        front = 0;
        flush = true;
        notifyAll();
    }

    public long getTotalBytes() {
        return size() - available();
    }

    public void reset() {

    }

    public void removeTail(long bytes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int size() {
        return buflen;
    }
}
