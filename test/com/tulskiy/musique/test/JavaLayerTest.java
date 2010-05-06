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

package com.tulskiy.musique.test;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileInputStream;

/**
 * @Author: Denis Tulskiy
 * @Date: 21.07.2009
 */
public class JavaLayerTest {
    Bitstream bitstream1;
    long file1size = 5856297;
    long file2size = 11701;
    Bitstream bitstream2;

    //    @Before
    public void setUp() {
        try {
            bitstream1 = new Bitstream(new FileInputStream("testfiles/mp3/01.mp3"));
            bitstream2 = new Bitstream(new FileInputStream("testfiles/mp3/sample.mp3"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void testSkipID3() {
        assertEquals(file1size - 0x1000, bitstream1.getPosition());
        assertEquals(file2size - 0, bitstream2.getPosition());
    }

    //    @Test
    public void testSyncHeader() {
        try {
            int ret = bitstream1.syncHeader((byte) 0);
            assertEquals(ret, 0xFFFBB000);
            assertEquals(file1size - 0x1004, bitstream1.getPosition());
            ret = bitstream2.syncHeader((byte) 0);
            assertEquals(ret, 0xFFFB9064);
            assertEquals(file2size - 4, bitstream2.getPosition());
        } catch (BitstreamException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void testSkipXingHeader() {
        try {
            Header h1 = bitstream1.readFrame();
            assertTrue(h1.vbr());
            assertEquals(file1size - 0x14E4, bitstream1.getPosition());
            Header h2 = bitstream2.readFrame();
            assertTrue(h2.vbr());
            assertEquals(file2size - 0x342, bitstream2.getPosition());
        } catch (BitstreamException e) {
            e.printStackTrace();
        }
    }

    //    @After
    public void finish() {
        bitstream1.close();
        bitstream2.close();
    }
}
