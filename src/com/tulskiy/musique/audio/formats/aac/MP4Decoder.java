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

package com.tulskiy.musique.audio.formats.aac;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.aac.libjfaad.libjfaad;
import com.tulskiy.musique.playlist.Track;

import javax.sound.sampled.AudioFormat;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 5, 2009
 */
public class MP4Decoder implements Decoder {
    private static libjfaad lib = new libjfaad();

    private int handler;
    private AudioFormat audioFormat;

    public boolean open(Track track) {
        handler = lib.open(track.getFile().getAbsolutePath());

        if (handler == -1) {
            return false;
        }

        audioFormat = new AudioFormat(lib.getSampleRate(handler), 16, lib.getChannels(handler), true, false);

        return true;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long sample) {
        lib.seek(handler, sample);
    }

    public int decode(byte[] buf) {
        return lib.decode(handler, buf, buf.length);
    }

    public void close() {
        if (handler != -1)
            lib.close(handler);
    }
}
