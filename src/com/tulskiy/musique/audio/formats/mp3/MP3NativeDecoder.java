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

package com.tulskiy.musique.audio.formats.mp3;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.mp3.mpg123.Mpg123;
import com.tulskiy.musique.audio.io.PCMOutputStream;
import com.tulskiy.musique.playlist.Song;

import javax.sound.sampled.AudioFormat;
import java.io.File;

/**
 * @Author: Denis Tulskiy
 * @Date: 16.06.2009
 */
public class MP3NativeDecoder implements Decoder {
    private Mpg123 lib = Mpg123.getInstance();
    private AudioFormat audioFormat;
    private PCMOutputStream outputStream;
    //    private byte[] pcmData;
    private File inputFile;

    public boolean open(Song song) {
        System.out.println("in native");
        if (this.inputFile == song.getFile())
            return true;
        this.inputFile = song.getFile();
        if (!lib.init()) {
            return false;
        }

        if (!lib.open(this.inputFile)) {
            System.err.println("Could not open file: " + lib.getError());
            return false;
        }

//        pcmData = new byte[Mpg123.READ_SIZE.intValue()];
        int sampleRate = lib.getRate();
        int channels = lib.getChannels();
        audioFormat = new AudioFormat(sampleRate, 16, channels, true, false);
        return true;
    }

    public void setOutputStream(PCMOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long sample) {
        lib.seekSample(sample);
    }

    public int decode(byte[] buf) {
        return lib.decode(buf);
    }

    public void close() {
        lib.close();
    }

    public boolean isFormatSupported(String ext) {
        return ext.equalsIgnoreCase("mp3");
    }
}
