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

package com.tulskiy.musique.audio.formats.wavpack;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.ape.APETagProcessor;
import com.tulskiy.musique.playlist.Song;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.audio.wavpack.WavPackReader;

/**
 * @Author: Denis Tulskiy
 * @Date: 01.07.2009
 */
public class WavPackFileReader extends AudioFileReader {
    private static Decoder decoder = new WavPackDecoder();
    private static APETagProcessor apeTagProcessor = new APETagProcessor();

    public Song readSingle(Song song) {
        WavPackReader reader = new WavPackReader();
        try {
            apeTagProcessor.readAPEv2Tag(song);
            org.jaudiotagger.audio.AudioFile audioFile = reader.read(song.getFile());
            copyHeaderFields((GenericAudioHeader) audioFile.getAudioHeader(), song);
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + song.getFilePath());
        }
        return song;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("wv");
    }

    @Override
    public Decoder getDecoder() {
        return decoder;
    }
}
