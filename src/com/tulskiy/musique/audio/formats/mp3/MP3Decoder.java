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

import com.tulskiy.musique.audio.io.PCMOutputStream;
import com.tulskiy.musique.playlist.*;
import com.tulskiy.musique.util.AudioMath;
import javazoom.jl.decoder.*;
import javazoom.jl.decoder.Header;

import javax.sound.sampled.AudioFormat;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 12.06.2009
 */
public class MP3Decoder implements com.tulskiy.musique.audio.Decoder {
    private static final int DECODE_AFTER_SEEK = 9;

    private Bitstream bitstream;
    private javazoom.jl.decoder.Decoder decoder;
    private AudioFormat audioFormat;
    private Header readFrame;
    private Song inputFile;

    private PCMOutputStream outputStream;
    private long totalSamples;
    private long streamSize;
    private byte[] buffer = new byte[5000];
    private int samplesPerFrame;
    private int sampleOffset = 0;
    private int encDelay;
    private long currentSample;
    private boolean skipFrame = false;

    private Header skipFrame() throws BitstreamException {
        readFrame = bitstream.readFrame();
        if (readFrame == null) {
            return null;
        }
        bitstream.closeFrame();

        return readFrame;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private boolean createBitstream(long fileOffset, long targetSample) {
        if (bitstream != null)
            bitstream.close();
        bitstream = null;
        try {
            FileInputStream fis = new FileInputStream(inputFile.getFile());
            if (fileOffset > 0) {
                fis.skip(fileOffset);
            }
            bitstream = new Bitstream(fis);
            decoder = new javazoom.jl.decoder.Decoder();
            targetSample += encDelay;
            if (targetSample > totalSamples) {
                targetSample = totalSamples;
            }
            long targetFrame;
            if (samplesPerFrame == 0) {
                targetFrame = 0;
                sampleOffset = 0;
            } else {
                targetFrame = (long) ((double) targetSample / samplesPerFrame);
                sampleOffset = (int) (targetSample - targetFrame * samplesPerFrame) * audioFormat.getFrameSize();
//                System.out.println("Seek to frame: " + targetFrame + " with offset: " + sampleOffset + ". File offset: " + fileOffset);
            }
            readFrame = null;
            for (int i = 0; i < targetFrame - DECODE_AFTER_SEEK; i++) {
                skipFrame();
            }

            int framesToDecode = targetFrame < DECODE_AFTER_SEEK ? (int) targetFrame : DECODE_AFTER_SEEK;
            for (int i = 0; i < framesToDecode; i++) {
                readFrame = bitstream.readFrame();
                if (readFrame != null)
                    decoder.decodeFrame(readFrame, bitstream);
                bitstream.closeFrame();
            }


            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean open(Song song) {
        if (song == null)
            return false;
        this.inputFile = song;
        streamSize = inputFile.getFile().length();
        try {
            FileInputStream fis = new FileInputStream(inputFile.getFile());
            Bitstream bs = new Bitstream(fis);
            Header header = bs.readFrame();
            encDelay = header.getEncDelay();
            int encPadding = header.getEncPadding();
            samplesPerFrame = (int) (header.ms_per_frame() * header.frequency() / 1000);
            totalSamples = samplesPerFrame * (header.max_number_of_frames(streamSize) + header.min_number_of_frames(streamSize)) / 2;
            if (encPadding < totalSamples)
                totalSamples -= encPadding;
            totalSamples -= encDelay;
            bs.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        encDelay = (int) song.getCustomHeaderNumber("enc_delay");
//        System.out.println("EncDelay1: " + encDelay + " EncDelay2: " + t);
        int sampleRate = song.getSamplerate();
        int channels = song.getChannels();
//        samplesPerFrame = (int) song.getCustomHeaderNumber("samples_per_frame");
//        System.out.println("Samples per frame: " + samplesPerFrame);
        audioFormat = new AudioFormat(sampleRate, 16, channels, true, false);
//        long t = song.getCustomHeaderNumber("mp3_total_samples");
//        System.out.println("Total: " + totalSamples + " t: " + t);
        createBitstream(0, 0);

        return true;
    }

    public void setOutputStream(PCMOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long targetSample) {
//        if (currentSample == targetSample)
//            return;
        currentSample = targetSample;
        SeekTable seekTable = SeekTableBuilder.getSeekTableCache().get(inputFile.getFile());
        if (seekTable != null && seekTable.getPointsCount() > 0) {
            synchronized (seekTable) {
                SeekTable.SeekPoint sp = seekTable.getHigher(targetSample - DECODE_AFTER_SEEK * samplesPerFrame);
                createBitstream(sp.byteOffset, targetSample - sp.sampleNumber);
            }
        } else {
            if (seekTable == null) {
                SeekTableBuilder stb = new SeekTableBuilder(inputFile.getFile(), streamSize, totalSamples, samplesPerFrame);
                stb.start();
            }
            createBitstream(0, targetSample);
        }
    }

    public int decode(byte[] buf) {
        try {
            readFrame = bitstream.readFrame();
            if (readFrame == null) {
                return -1;
            }
            if (currentSample >= totalSamples)
                return -1;
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(readFrame, bitstream);
            bitstream.closeFrame();
            int dataLen = output.getBufferLength() * 2;
            int len = dataLen - sampleOffset;
            if (dataLen == 0) {
                return 0;
            }

            toByteArray(output.getBuffer(), 0, output.getBufferLength());
            currentSample += AudioMath.bytesToSamples(len, audioFormat.getFrameSize());

            if (currentSample > totalSamples) {
                len -= AudioMath.samplesToBytes(currentSample - totalSamples, audioFormat.getFrameSize());
            }
//            outputStream.write(buffer, sampleOffset, len);
            System.arraycopy(buffer, sampleOffset, buf, 0, len);
//            len -= sampleOffset;
            sampleOffset = 0;
            readFrame = null;
            return len;
        } catch (BitstreamException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        bitstream.close();
        readFrame = null;
    }

    private void toByteArray(short[] samples, int offs, int len) {
        if (buffer.length < len * 2) {
            buffer = new byte[len * 2 + 1024];
        }
        int idx = 0;
        short s;
        while (len-- > 0) {
            s = samples[offs++];
            buffer[idx++] = (byte) s;
            buffer[idx++] = (byte) (s >>> 8);
        }
    }
}
