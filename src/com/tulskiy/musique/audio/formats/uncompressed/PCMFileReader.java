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

package com.tulskiy.musique.audio.formats.uncompressed;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.util.Util;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 30.06.2009
 */
public class PCMFileReader extends AudioFileReader {
    private static Decoder decoder = new PCMDecoder();

    public Song readSingle(File file) {
        Song song = new Song();
        song.setFile(file);

        String title = Util.removeExt(file.getName());
        song.setTitle(title);
        try {
            AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
            AudioFormat aFormat = format.getFormat();
            song.setStartPosition(0);
            song.setSamplerate((int) format.getFormat().getSampleRate());
            song.setTotalSamples(format.getFrameLength());
            song.setChannels(aFormat.getChannels());
            song.setCodec("PCM");
            if (format.getFrameLength() > 0)
                song.setBitrate((int) (file.length() / format.getFrameLength() / format.getFormat().getSampleRate()));
        } catch (UnsupportedAudioFileException e) {
            System.out.println("Unsopported: " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return song;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("wav") || ext.equalsIgnoreCase("au")
                || ext.equalsIgnoreCase("aiff");
    }

    @Override
    public Decoder getDecoder() {
        return decoder;
    }
}
