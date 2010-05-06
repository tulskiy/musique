/*
 * Copyright (c) 2008, 2009 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.audio.io;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * @Author: Denis Tulskiy
 * @Date: 25.07.2009
 */
public class BufferTest {
    private PCMOutputStream buffer;
    private boolean doneWriting = false;

    @Before
    public void setUp() {
        buffer = new Buffer(128);
        doneWriting = false;
        Thread t = new Thread() {
            @Override
            public void run() {
                byte[] b = new byte[30];
                Arrays.fill(b, (byte) 1);
                while (buffer.size() - buffer.available() >= b.length) {
                    buffer.write(b, 0, b.length);
                }
                doneWriting = true;
            }
        };

        t.start();

        while (!doneWriting) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testFlush1() {
        new Thread() {
            @Override
            public void run() {
                buffer.write(new byte[30], 0, 30);
            }
        }.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        buffer.flush();

        assertEquals(0, buffer.available());

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(0, buffer.available());
    }

    @Test
    public void testFlush2() {
        buffer.flush();

        buffer.write(new byte[10], 0, 10);

        assertEquals(10, buffer.available());
    }
}
