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

import com.tulskiy.musique.audio.io.PCMOutputStream;
import com.tulskiy.musique.playlist.Song;

import javax.sound.sampled.AudioFormat;

/**
 * @Author: Denis Tulskiy
 * @Date: 12.06.2009
 */
public interface Decoder {
    /**
     * Open the file and prepare for decoding.
     * This method sets the decoder to play the file from startIndex
     *
     * @param inputFile
     */
    public boolean open(Song inputFile);

    /**
     * Get format of the PCM data. Usually it is 44100 kHz, 16 bit, signed,
     * little or big endian
     *
     * @return audio format of PCM data
     */
    public AudioFormat getAudioFormat();

    public void seekSample(long sample);

    /**
     * Decode chunk of PCM data and write to OutputStream
     *
     * @param buf
     * @return true if success
     */
    public int decode(byte[] buf);

    public void close();

}
