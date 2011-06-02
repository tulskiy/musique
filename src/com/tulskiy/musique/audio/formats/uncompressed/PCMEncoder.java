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

import com.tulskiy.musique.audio.Encoder;
import com.tulskiy.musique.system.Configuration;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author: Denis Tulskiy
 * Date: Jul 25, 2010
 */
public class PCMEncoder implements Encoder {
    private RandomAccessFile output;
    private int length;
    private AudioFormat fmt;

    @Override
    public boolean open(File outputFile, AudioFormat fmt, Configuration options) {
        this.fmt = fmt;
        try {
            output = new RandomAccessFile(outputFile, "rw");
            output.setLength(0);
            output.write(new byte[44]);
            length = 0;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void encode(byte[] buf, int len) {
        try {
            output.write(buf, 0, len);
            length += len;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (output != null) {
                short channels = (short) fmt.getChannels();
                int sampleRate = (int) fmt.getSampleRate();
                short bps = (short) fmt.getSampleSizeInBits();
                ByteBuffer header = ByteBuffer.allocate(44);
                header.order(ByteOrder.LITTLE_ENDIAN);
                header.put("RIFF".getBytes());
                header.putInt(36 + length);
                header.put("WAVE".getBytes());
                header.put("fmt ".getBytes());
                header.putInt(16);
                header.putShort((short) 1);
                header.putShort(channels);
                header.putInt(sampleRate);
                header.putInt(sampleRate * channels * bps / 8);
                header.putShort((short) (channels * bps / 8));
                header.putShort(bps);
                header.put("data".getBytes());
                header.putInt(length);
                output.seek(0);
                output.write(header.array());
                output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
