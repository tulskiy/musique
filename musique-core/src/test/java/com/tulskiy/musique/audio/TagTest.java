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

import static com.tulskiy.musique.system.TrackIO.getAudioFileReader;
import static com.tulskiy.musique.system.TrackIO.getAudioFileWriter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import junit.framework.Assert;

import org.jaudiotagger.tag.FieldKey;
import org.junit.Test;

import com.tulskiy.musique.gui.model.FieldValues;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.util.Util;

/**
 * Author: Denis Tulskiy
 * Date: Oct 9, 2009
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class TagTest {

    private final String ARTIST = "artist";
    private final String[] ARTISTS = {"artist1", "artist2"};
    private final String ALBUM_ARTIST = "album artist";
    private final String TITLE = "title";
    private final String ALBUM = "album";
    private final String YEAR = "2000";
    private final String GENRE = "genre";
    private final String[] GENRES = {"Rock", "Blues", "Jazz"};
    private final String TRACK = "1";
    private final String TRACK_TOTAL = "2";
    private final String DISC_NO = "3";
    private final String DISC_TOTAL = "4";
    private final String RECORD_LABEL = "record label";
    private final String[] RECORD_LABELS = {"record label 1", "record label 2"};
    private final String CATALOG_NO = "catalog no";
    private final String[] CATALOG_NOS = {"catalog no 1", "catalog no 2"};
    // TODO add multiline check
    private final String COMMENT = "comment";
    private final String RATING = "6";

    @Test
    public void testMP3() {
        testRead("testfiles/mp3/sample.mp3");
        testWrite("testfiles/mp3/sample_notag.mp3");
        testEmptyWrite("testfiles/mp3/sample_notag.mp3");
    }

    @Test
    public void testFLAC() {
        testRead("testfiles/flac/sample.flac");
        testWrite("testfiles/flac/sample_notag.flac");
        testEmptyWrite("testfiles/flac/sample_notag.flac");
    }

    @Test
    public void testAPE() {
        testReadApe("testfiles/ape/sample.ape");
        testWriteApe("testfiles/ape/sample_notag.ape");
        testEmptyWrite("testfiles/ape/sample_notag.ape");
    }

    @Test
    public void testWavPack() {
        testReadApe("testfiles/wavpack/sample.wv");
        testWriteApe("testfiles/wavpack/sample_notag.wv");
        testEmptyWrite("testfiles/wavpack/sample_notag.wv");
    }

    @Test
    public void testOGG() {
        testRead("testfiles/ogg/sample.ogg");
        testWrite("testfiles/ogg/sample_notag.ogg");
        testEmptyWrite("testfiles/ogg/sample_notag.ogg");
    }

    @Test
    public void testMP4() {
        testRead("testfiles/aac/sample.mp4");
        testWrite("testfiles/aac/sample_notag.mp4");
        testEmptyWrite("testfiles/aac/sample_notag.mp4");
    }

    private void testEmptyWrite(String name) {
        try {
            Track track = new Track();
            File file = getFileFromResource(name);
            File fo = createTempFile(name, file.getParentFile().getParentFile().getParentFile());
            copy(file, fo);
            track.getTrackData().setLocation(fo.toURI().toString());
            getAudioFileWriter(fo.getName()).write(track);

            fo.delete();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to write empty tag");
        }
    }

    private void testRead(String file) {
        Track track = getAudioFileReader(file).read(getFileFromResource(file));
        TrackData trackData = track.getTrackData();

        // test metadata fields
        testMulti(ARTISTS, trackData.getTagFieldValues(FieldKey.ARTIST));
        Assert.assertTrue(trackData.getTagFieldValues(FieldKey.ARTIST).contains(trackData.getArtist()));
        testSingle(ALBUM_ARTIST, trackData.getAlbumArtist());
        testSingle(TITLE, trackData.getTitle());
        testSingle(ALBUM, trackData.getAlbum());
        testSingle(YEAR, trackData.getYear());
        testMulti(GENRES, trackData.getTagFieldValues(FieldKey.GENRE));
        testMulti(GENRES, trackData.getGenres());
        Assert.assertTrue(trackData.getGenres().contains(trackData.getGenre()));
        testSingle(TRACK, trackData.getTrack());
        testSingle(TRACK_TOTAL, trackData.getTrackTotal());
        testSingle(DISC_NO, trackData.getDisc());
        testSingle(DISC_TOTAL, trackData.getDiscTotal());
        testMulti(RECORD_LABELS, trackData.getTagFieldValues(FieldKey.RECORD_LABEL));
        testMulti(RECORD_LABELS, trackData.getRecordLabels());
        Assert.assertTrue(trackData.getRecordLabels().contains(trackData.getRecordLabel()));
        testMulti(CATALOG_NOS, trackData.getTagFieldValues(FieldKey.CATALOG_NO));
        testMulti(CATALOG_NOS, trackData.getCatalogNos());
        Assert.assertTrue(trackData.getCatalogNos().contains(trackData.getCatalogNo()));
        testSingle(COMMENT, trackData.getComment());
//        testSingle(RATING, trackData.getRating());

        // test technical fields
//        assertEquals(29400, track.getTotalSamples());
        assertEquals(2, trackData.getChannels());
        assertEquals(44100, trackData.getSampleRate());
    }

    private File getFileFromResource(String file) {
        try {
            return new File(getClass().getClassLoader().getResource(file).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            fail();
        }
        return null;
    }

    private void testReadApe(String file) {
        Track track = getAudioFileReader(file).read(getFileFromResource(file));
        TrackData trackData = track.getTrackData();

        // test metadata fields
        testSingle(ARTIST, trackData.getArtist());
        testSingle(ALBUM_ARTIST, trackData.getAlbumArtist());
        testSingle(TITLE, trackData.getTitle());
        testSingle(ALBUM, trackData.getAlbum());
        testSingle(YEAR, trackData.getYear());
        testSingle(GENRE, trackData.getGenre());
        testSingle(TRACK, trackData.getTrack());
        testSingle(TRACK_TOTAL, trackData.getTrackTotal());
        testSingle(DISC_NO, trackData.getDisc());
        testSingle(DISC_TOTAL, trackData.getDiscTotal());
        testSingle(RECORD_LABEL, trackData.getRecordLabel());
        testSingle(CATALOG_NO, trackData.getCatalogNo());
        testSingle(COMMENT, trackData.getComment());
//        testSingle(RATING, trackData.getRating());

        // test technical fields
//        assertEquals(29400, track.getTotalSamples());
        assertEquals(2, trackData.getChannels());
        assertEquals(44100, trackData.getSampleRate());
    }

    private void testWrite(String name) {
        Track track = new Track();
        TrackData trackData = track.getTrackData();

        addMulti(trackData, FieldKey.ARTIST, ARTISTS);
        trackData.addAlbumArtist(ALBUM_ARTIST);
        trackData.addTitle(TITLE);
        trackData.addAlbum(ALBUM);
        trackData.addYear(YEAR);
        addMulti(trackData, FieldKey.GENRE, GENRES);
        trackData.addTrack(TRACK);
        trackData.addTrackTotal(TRACK_TOTAL);
        trackData.addDisc(DISC_NO);
        trackData.addDiscTotal(DISC_TOTAL);
        addMulti(trackData, FieldKey.RECORD_LABEL, RECORD_LABELS);
        addMulti(trackData, FieldKey.CATALOG_NO, CATALOG_NOS);
        trackData.addComment(COMMENT);
        trackData.addRating(RATING);

        try {
            File file = getFileFromResource(name);
            File fo = createTempFile(name, file.getParentFile().getParentFile().getParentFile());
            copy(file, fo);
            trackData.setLocation(fo.toURI().toString());
            getAudioFileWriter(fo.getName()).write(track);
            testRead(fo.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void testWriteApe(String name) {
        Track track = new Track();
        TrackData trackData = track.getTrackData();

        trackData.addArtist(ARTIST);
        trackData.addAlbumArtist(ALBUM_ARTIST);
        trackData.addTitle(TITLE);
        trackData.addAlbum(ALBUM);
        trackData.addYear(YEAR);
        trackData.addGenre(GENRE);
        trackData.addTrack(TRACK);
        trackData.addTrackTotal(TRACK_TOTAL);
        trackData.addDisc(DISC_NO);
        trackData.addDiscTotal(DISC_TOTAL);
        trackData.addRecordLabel(RECORD_LABEL);
        trackData.addCatalogNo(CATALOG_NO);
        trackData.addComment(COMMENT);
        trackData.addRating(RATING);

        File file = getFileFromResource(name);
        File fo = createTempFile(name, file.getParentFile().getParentFile().getParentFile());
        copy(file, fo);
        trackData.setLocation(fo.toURI().toString());
        try {
            getAudioFileWriter(fo.getName()).write(track);
        } catch (TagWriteException e) {
            e.printStackTrace();
        }

        testReadApe(fo.getName());
    }

    private File createTempFile(String name, File directory) {
        try {
            File fo = File.createTempFile("tagtest", "." + Util.getFileExt(name), directory);
            fo.deleteOnExit();
            return fo;
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        return null;
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

    private void testSingle(String expected, String actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected, actual);
    }

    private void testMulti(String[] expected, FieldValues actual) {
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.length, actual.size());
        for (String value : expected) {
            Assert.assertTrue(actual.contains(value));
        }
    }

    private void addMulti(TrackData trackData, FieldKey key, String[] values) {
        trackData.addTagFieldValues(key, new FieldValues(Arrays.asList(values)));
    }

}
