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

import com.tulskiy.musique.audio.io.Buffer;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.PluginLoader;
import com.tulskiy.musique.util.AudioMath;
import javazoom.jl.converter.WaveFile;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * @Author: Denis Tulskiy
 * @Date: 15.07.2009
 */

public class DecoderSeekTest {
    private Decoder decoder;
    private long totalSamples;
    private ByteBuffer refB;
    private static final int CASES_TO_TEST = 10;
    private int frameSize;

    public DecoderSeekTest(Song song, Decoder decoder) {
        this.decoder = decoder;
        assertNotNull("Decoder is null", decoder);
        assertTrue("Decoder returned an error", this.decoder.open(song));
        totalSamples = song.getTotalSamples();
        System.out.println("File: " + song.getFile().getName() + ", Total samples: " + totalSamples);
    }

    public void start() {
        try {
            frameSize = decoder.getAudioFormat().getFrameSize();
            SeekTestBuffer ref = new SeekTestBuffer();
            refB = ByteBuffer.allocate((int) (totalSamples * frameSize) + 10000);
            refB.position(0);
            ref.saveReference = true;
            ref.oFile = "testfiles/" + decoder.getClass().getCanonicalName() + ".dat";
            checkSeek(ref, 0);
            ref.waveFile.Close();
            System.out.println("Done first decoder pass. Samples decoded: " + ref.currentSample);
//            assertTrue(totalSamples >= ref.currentSample);
//            assertEquals(totalSamples, ref.currentSample);
            if (totalSamples != ref.currentSample) {
                System.out.println("Warning: decoded less samples than declared");
                totalSamples = ref.currentSample;
            }
            SeekTestBuffer test = new SeekTestBuffer();
            long[] testcase = new long[CASES_TO_TEST];
            testcase[0] = 0;
            testcase[1] = 1;
            testcase[2] = totalSamples;
            testcase[3] = totalSamples - 1;
            for (int i = 4; i < CASES_TO_TEST; i++)
                testcase[i] = (int) (Math.random() * totalSamples);
            for (int i = 0; i < 10; i++) {
                long sample = testcase[i];
//                int sample = (1152 * 5) + 576;
//                int sample = 12345;
                System.out.println("Seek to: " + sample);
                test.currentSample = 0;
                refB.rewind();
                refB.position((int) (sample * frameSize));
                try {
//                    test.saveReference = true;
//                    test.oFile = "testfiles/2.wav";
                    checkSeek(test, sample);
//                    test.waveFile.Close();
                    assertEquals(totalSamples - sample, test.currentSample);
                    System.err.println("Samples decoded: " + test.currentSample);
//                    break;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    System.out.println("Test failed: " + e.getMessage());
                    throw e;
                }
            }
        } finally {
            refB = null;
            decoder.close();
            System.gc();
        }
    }

    private void checkSeek(SeekTestBuffer b, long sample) {
//        decoder.setOutputStream(b);
        decoder.seekSample(sample);
        byte[] buffer = new byte[65536];
        while (true) {
            int ret = decoder.decode(buffer);
            if (ret == -1) {
                break;
            } else {
                b.write(buffer, 0, ret);
            }
        }
    }

    private class SeekTestBuffer extends Buffer {
        int currentSample = 0;
        boolean saveReference = false;
        String oFile;
        WaveFile waveFile = null;

        @Override
        public void write(byte[] b, int off, int len) {
            if (saveReference) {
                if (waveFile == null) {
                    waveFile = new WaveFile();
                    File f = new File(oFile);
                    if (f.exists()) {
                        f.delete();
                    }
                    waveFile.OpenForWrite(oFile, 44100, (short) 16, (short) 2);
                }
                waveFile.Write(b, off, len);
                try {
                    refB.put(b, off, len);
                } catch (BufferOverflowException e) {
//                    e.printStackTrace();
                    return;
                }
            } else {
                refB.mark();
                for (int i = off; i < off + len; i++) {
                    if (refB.position() >= refB.limit()) {
                        throw new RuntimeException("Too much samples decoded");
                    }
                    byte bb = refB.get();
                    if (bb != b[i]) {
                        try {
                            FileOutputStream f1 = new FileOutputStream("testfiles/frame1.dmp");
                            FileOutputStream f2 = new FileOutputStream("testfiles/frame2.dmp");
                            refB.reset();
                            byte[] frame = new byte[len];
                            refB.get(frame);
                            f1.write(frame);
                            f2.write(b, off, len);
                            f1.close();
                            f2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        fail("Not equals " + refB.position());
                    }
//                    assertEquals("Sample " + refB.position(), refB.get(), b[i]);
                }
            }
            currentSample += AudioMath.bytesToSamples(len, frameSize);
        }
    }
}