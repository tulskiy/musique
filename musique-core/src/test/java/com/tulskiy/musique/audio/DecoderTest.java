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

package com.tulskiy.musique.audio;

import com.tulskiy.musique.audio.formats.ape.APEFileReader;
import com.tulskiy.musique.audio.formats.flac.FLACFileReader;
import com.tulskiy.musique.audio.formats.mp3.MP3FileReader;
import com.tulskiy.musique.audio.formats.mp4.MP4FileReader;
import com.tulskiy.musique.audio.formats.ogg.OGGFileReader;
import com.tulskiy.musique.audio.formats.tta.TTAFileReader;
import com.tulskiy.musique.audio.formats.uncompressed.PCMFileReader;
import com.tulskiy.musique.audio.formats.wavpack.WavPackFileReader;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Codecs;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Author: Denis Tulskiy
 * Date: 15.07.2009
 */

public class DecoderTest {
    @Test
    public void testMP3() {
        test(new MP3FileReader(), "testfiles/mp3/sample.mp3");
    }

//    @Test

//    public void testNativeMP3() {
//        MP3FileReader mp3FileReader = new MP3FileReader();
//        mp3FileReader.setUseNativeDecoder(true);
//        test(mp3FileReader, "testfiles/mp3/sample.mp3");
//    }

    @Test
    public void testFLAC() {
//        test(new FLACFileReader(), "testfiles/flac/sample.oga");
        test(new FLACFileReader(), "testfiles/flac/sample.flac");
    }

    @Test
    public void testAPE() {
        test(new APEFileReader(), "testfiles/ape/sample.ape");
    }

//    @Test

//    public void testAPENative() {
//        APEFileReader fileReader = new APEFileReader();
//        fileReader.setUseNativeDecoder(true);
//        test(fileReader, "testfiles/ape/sample.ape");
//    }

    @Test
    public void testWavPack() {
        test(new WavPackFileReader(), "testfiles/wavpack/sample.wv");
    }

    @Test
    public void testOGG() {
        test(new OGGFileReader(), "testfiles/ogg/sample.ogg");
    }

    @Test
    public void testPCM() {
        test(new PCMFileReader(), "testfiles/uncompressed/sample.aiff");
        test(new PCMFileReader(), "testfiles/uncompressed/sample.au");
        test(new PCMFileReader(), "testfiles/uncompressed/sample.wav");
    }

    @Test
    public void testAAC() {
        test(new MP4FileReader(), "testfiles/aac/sample_faac.mp4");
        test(new MP4FileReader(), "testfiles/aac/sample_ffmpeg.mp4");
        test(new MP4FileReader(), "testfiles/aac/sample.mp4");
        test(new MP4FileReader(), "testfiles/aac/sample_nero.mp4");
        test(new MP4FileReader(), "testfiles/aac/sample_itunes.m4a");
        test(new MP4FileReader(), "testfiles/aac/sample_itunes_new.m4a");
    }

    @Test
    public void testALAC() {
        test(new MP4FileReader(), "testfiles/alac/sample_ffmpeg.m4a");
        test(new MP4FileReader(), "testfiles/alac/sample_dbpoweramp.m4a");
    }

    @Test
    public void testTTA() {
        test(new TTAFileReader(), "testfiles/tta/sample.tta");
    }

    private void test(AudioFileReader reader, String fileName) {
        try {
            Track file = reader.read(new File(getClass().getClassLoader().getResource(fileName).toURI()));
            DecoderSeekTester test = new DecoderSeekTester(file, Codecs.getDecoder(file));
            test.start();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
