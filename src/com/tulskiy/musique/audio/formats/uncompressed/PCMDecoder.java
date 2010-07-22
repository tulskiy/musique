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

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 30.06.2009
 */
public class PCMDecoder implements Decoder {
    private AudioInputStream audioInputStream;
    private Track inputFile;

    public boolean open(Track track) {
        try {
            logger.fine("Opening file: " + track.getFile());
            this.inputFile = track;
            audioInputStream = AudioSystem.getAudioInputStream(track.getFile());
            audioInputStream = AudioSystem.getAudioInputStream(new AudioFormat(audioInputStream.getFormat().getSampleRate(), audioInputStream.getFormat().getSampleSizeInBits(), audioInputStream.getFormat().getChannels(), true, false), audioInputStream);
            return true;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public AudioFormat getAudioFormat() {
        return audioInputStream.getFormat();
    }

    public void seekSample(long sample) {
        open(inputFile);
        try {
            long toSkip = sample * audioInputStream.getFormat().getFrameSize();
            long skipped = 0;
            while (skipped < toSkip) {
                long b = audioInputStream.skip(toSkip - skipped);
                if (b == 0) break;
                skipped += b;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int decode(byte[] buf) {
        try {
            return audioInputStream.read(buf, 0, buf.length - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        try {
            if (audioInputStream != null)
                audioInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
