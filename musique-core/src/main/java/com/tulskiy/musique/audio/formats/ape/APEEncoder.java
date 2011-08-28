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

package com.tulskiy.musique.audio.formats.ape;

import com.tulskiy.musique.audio.Encoder;
import com.tulskiy.musique.system.configuration.Configuration;

import davaguine.jmac.encoder.IAPECompress;
import davaguine.jmac.info.CompressionLevel;
import davaguine.jmac.info.WaveFormat;
import davaguine.jmac.tools.RandomAccessFile;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Author: Denis Tulskiy
 * Date: Jul 26, 2010
 */
public class APEEncoder implements Encoder {
    private IAPECompress encoder;
    private ByteBuffer buffer;
    private int available;
    private byte[] tempBuf;

    @Override
    public boolean open(File outputFile, AudioFormat fmt, Configuration options) {
        try {
            outputFile.delete();
            buffer = ByteBuffer.allocate((int) Math.pow(2, 18));
            tempBuf = new byte[(int) Math.pow(2, 15)];
            encoder = IAPECompress.CreateIAPECompress();
            WaveFormat format = new WaveFormat();
            fillWaveFormat(format, fmt);
            int level = CompressionLevel.COMPRESSION_LEVEL_NORMAL;
            if (options != null) {
                level = options.getInt("encoder.ape.level", level);
            }
            encoder.StartEx(new RandomAccessFile(outputFile, "rw"),
                    format, IAPECompress.MAX_AUDIO_BYTES_UNKNOWN,
                    level);
            available = 0;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void fillWaveFormat(WaveFormat waveFormat, AudioFormat fmt) {
        waveFormat.cbSize = 0;
        waveFormat.nChannels = (short) fmt.getChannels();
        waveFormat.wFormatTag = 1;
        waveFormat.nSamplesPerSec = (int) fmt.getSampleRate();
        waveFormat.wBitsPerSample = (short) fmt.getSampleSizeInBits();
        waveFormat.nBlockAlign = (short) (fmt.getChannels() * fmt.getSampleSizeInBits() / 8);
        waveFormat.nAvgBytesPerSec = waveFormat.nBlockAlign * waveFormat.nSamplesPerSec;
    }

    @Override
    public void encode(byte[] buf, int len) {
        try {
            buffer.put(buf, 0, len);
            available += len;
            buffer.rewind();

            int toRead = tempBuf.length;
            while (available >= toRead) {
                available -= toRead;
                buffer.get(tempBuf, 0, toRead);
                encoder.AddData(tempBuf, toRead);
            }
            buffer.compact();
            buffer.position(available);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (available > 0) {
                encoder.AddData(buffer.array(), available);
            }
            encoder.Finish(null, 0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
