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

package com.tulskiy.musique.audio.formats.flac.oggflac;

import org.junit.Test;
import org.kc7bfi.jflac.metadata.Padding;
import org.kc7bfi.jflac.metadata.VorbisComment;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author: Denis Tulskiy
 * Date: 22.07.2009
 */
public class OggFlacDecoderTest {
    OggFlacDecoder oggFlacDecoder;
    @Test
    public void dummy(){}

    //    @Before
    public void setUp() {
        oggFlacDecoder = new OggFlacDecoder();
        try {
            assertEquals(0, oggFlacDecoder.open(new RandomAccessFile("testfiles/flac/Running Up That Hill (Kate Bush).oga", "r")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //    @Test
    public void testMetadata() {
        assertEquals(1, oggFlacDecoder.majorVersion);
        assertEquals(0, oggFlacDecoder.minorVersion);
        assertEquals(3, oggFlacDecoder.headerPackets);
        assertEquals(13113839, oggFlacDecoder.getStreamInfo().getTotalSamples());
        assertTrue(oggFlacDecoder.getMetadata()[0] instanceof VorbisComment);
        assertTrue(oggFlacDecoder.getMetadata()[2] instanceof Padding);
        assertEquals("Placebo", ((VorbisComment) oggFlacDecoder.getMetadata()[0]).getCommentByName("ARTIST")[0]);
    }
}
