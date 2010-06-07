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

package com.tulskiy.musique.system;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.aac.MP4FileReader;
import com.tulskiy.musique.audio.formats.aac.MP4TagWriter;
import com.tulskiy.musique.audio.formats.ape.APEFileReader;
import com.tulskiy.musique.audio.formats.ape.APETagWriter;
import com.tulskiy.musique.audio.formats.cue.CUEFileReader;
import com.tulskiy.musique.audio.formats.flac.FLACFileReader;
import com.tulskiy.musique.audio.formats.ogg.VorbisTagWriter;
import com.tulskiy.musique.audio.formats.mp3.MP3FileReader;
import com.tulskiy.musique.audio.formats.mp3.MP3TagWriter;
import com.tulskiy.musique.audio.formats.ogg.OGGFileReader;
import com.tulskiy.musique.audio.formats.uncompressed.PCMFileReader;
import com.tulskiy.musique.audio.formats.wavpack.WavPackFileReader;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.util.Util;

import java.util.ArrayList;

/**
 * @Author: Denis Tulskiy
 * @Date: 24.06.2009
 */
public class PluginLoader {
    private static ArrayList<AudioFileReader> readers;
    private static ArrayList<AudioTagWriter> writers;

    static {
        readers = new ArrayList<AudioFileReader>();
        readers.add(new MP3FileReader());
        readers.add(new MP4FileReader());
        readers.add(new APEFileReader());
        readers.add(new CUEFileReader());
        readers.add(new FLACFileReader());
        readers.add(new OGGFileReader());
        readers.add(new PCMFileReader());
        readers.add(new WavPackFileReader());

        writers = new ArrayList<AudioTagWriter>();
        writers.add(new MP3TagWriter());
        writers.add(new APETagWriter());
        writers.add(new VorbisTagWriter());
        writers.add(new MP4TagWriter());
    }

    public static AudioFileReader getAudioFileReader(String fileName) {
        String ext = Util.getFileExt(fileName);
        for (AudioFileReader reader : readers) {
            if (reader.isFileSupported(ext))
                return reader;
        }

        return null;
    }

    public static AudioTagWriter getAudioFileWriter(String fileName) {
        String ext = Util.getFileExt(fileName);
        for (AudioTagWriter writer : writers) {
            if (writer.isFileSupported(ext))
                return writer;
        }

        return null;
    }

    public static Decoder getDecoder(Song audioFile) {
        AudioFileReader reader = getAudioFileReader(audioFile.getFile().getName());
        if (reader != null)
            return reader.getDecoder();
        else
            return null;
    }
}
