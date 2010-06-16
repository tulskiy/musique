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

package com.tulskiy.musique.audio.formats.ogg;

import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 17.07.2009
 */
public class VorbisDecoder implements Decoder {
    private VorbisFile vorbisFile;
    private AudioFormat audioFormat;

    public boolean open(Track track) {
        try {
            vorbisFile = new VorbisFile(track.getFile().getAbsolutePath());
            Info info = vorbisFile.getInfo()[0];
            audioFormat = new AudioFormat(info.rate, 16, info.channels, true, false);
        } catch (JOrbisException e) {
            e.printStackTrace();
        }
        return true;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long sample) {
        vorbisFile.pcm_seek(sample);
    }

    public int decode(byte[] buf) {
        int ret = vorbisFile.read(buf, buf.length);
        if (ret <= 0)
            return -1;
        return ret;
    }

    public void close() {
        try {
            if (vorbisFile != null)
                vorbisFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
