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

package com.tulskiy.musique.audio;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.PluginLoader;
import com.tulskiy.musique.util.Util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 9, 2009
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class TagTest {
    @Test
    public void testMP3() {
        testRead("testfiles/mp3/sample.mp3");
        testWrite("testfiles/mp3/sample_notag.mp3");
    }

    @Test
    public void testFLAC() {
        testRead("testfiles/flac/sample.flac");
        testWrite("testfiles/flac/sample_notag.flac");
    }

    @Test
    public void testAPE() {
        testRead("testfiles/ape/sample.ape");
        testWrite("testfiles/ape/sample_notag.ape");
    }

    @Test
    public void testWavPack() {
        testRead("testfiles/wavpack/sample.wv");
        testWrite("testfiles/wavpack/sample_notag.wv");
    }

    @Test
    public void testOGG() {
        testRead("testfiles/ogg/sample.ogg");
        testWrite("testfiles/ogg/sample_notag.ogg");
    }

    @Test
    public void testMP4() {
        testRead("testfiles/aac/sample.mp4");
        testWrite("testfiles/aac/sample_notag.mp4");
    }


    private void testRead(String file) {
        Track track = PluginLoader.getAudioFileReader(file).readSingle(new File(file));

        assertEquals("artist", track.getArtist());
        assertEquals("title", track.getTitle());
        assertEquals("album", track.getAlbum());
        assertEquals("date", track.getYear());
        assertEquals("genre", track.getGenre());
        assertEquals("album artist", track.getAlbumArtist());
        assertEquals("1", track.getTrackNumber());
        assertEquals("2", track.getTotalTracks());
        assertEquals("3", track.getDiscNumber());
        assertEquals("4", track.getTotalDiscs());
        assertEquals("comment", track.getComment());
    }

    private void testWrite(String name) {
        Track track = new Track();

        track.setAlbum("album");
        track.setAlbumArtist("album artist");
        track.setArtist("artist");
        track.setComment("comment");
        track.setDiscNumber("3/4");
        track.setTrackNumber("1/2");
        track.setGenre("genre");
        track.setTitle("title");
        track.setYear("date");

        File file = new File(name);
        File fo = new File("testfiles/temp." + Util.getFileExt(name));
        copy(file, fo);
        track.setFile(fo);
        PluginLoader.getAudioFileWriter(fo.getName()).write(track);

        testRead(fo.getPath());
        fo.delete();
    }

    private void copy(File f1, File f2) {
        f2.delete();
        try {
            f2.createNewFile();
            FileInputStream fis = new FileInputStream(f1);
            FileOutputStream fos = new FileOutputStream(f2);
            byte[] b = new byte[1024];
            while (true) {
                int len = fis.read(b);
                if (len <= 0)
                    break;
                fos.write(b, 0, len);
            }
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
