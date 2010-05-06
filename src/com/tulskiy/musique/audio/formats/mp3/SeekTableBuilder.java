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

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

class SeekTableBuilder extends Thread {
    private static final int SEEK_TABLE_CACHE_LIMIT = 10;
    private static Hashtable<File, SeekTable> seekTableCache = new Hashtable<File, SeekTable>(SEEK_TABLE_CACHE_LIMIT);

    private File inputStream;
    private long samples;
    private long streamSize;
    private final static int SIZE = 10000;
    private long samplesPerFrame;

    public SeekTableBuilder(File inputStream, long streamSize, long samples, long samplesPerFrame) {
        this.inputStream = inputStream;
        this.samples = samples;
        this.streamSize = streamSize;
        this.samplesPerFrame = samplesPerFrame;
    }

    @Override
    public void run() {
        Bitstream b = null;
        try {
            b = new Bitstream(new FileInputStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SeekTable seekTable = new SeekTable((int) ((float) samples / SIZE) + 1);
        synchronized (seekTable) {
            if (seekTableCache.size() > SEEK_TABLE_CACHE_LIMIT)
                seekTableCache.clear();
            seekTableCache.put(inputStream, seekTable);
            long currentSample = 0;
            for (long i = 0; i < samples; i += SIZE) {
                try {
                    while (currentSample < i) {
                        currentSample += samplesPerFrame;
                        b.readFrame();
                        b.closeFrame();
                    }
                    seekTable.addSeekPoint(currentSample, streamSize - b.getPosition());
                    if (seekTable.getPointsCount() % 100 == 0)
                        seekTable.wait(10);
                } catch (BitstreamException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//                System.out.println("Done scanning: " + seekTable.getPointsCount());
        }
    }

    public static Hashtable<File, SeekTable> getSeekTableCache() {
        return seekTableCache;
    }
}
