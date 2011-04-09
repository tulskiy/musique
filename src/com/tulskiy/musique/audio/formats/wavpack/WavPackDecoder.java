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

package com.tulskiy.musique.audio.formats.wavpack;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;
import com.wavpack.decoder.Defines;
import com.wavpack.decoder.WavPackUtils;
import com.wavpack.decoder.WavpackContext;

import javax.sound.sampled.AudioFormat;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @Author: Denis Tulskiy
 * @Date: 01.07.2009
 */
public class WavPackDecoder implements Decoder {
    private static final int BUFFER_SIZE = Defines.SAMPLE_BUFFER_SIZE;

    private AudioFormat audioFormat;
    private WavpackContext wpc;
    private int[] buffer = new int[BUFFER_SIZE];
    private int channels;
    private int bps;
    private RandomAccessFile ras;

    public boolean open(Track track) {
        try {
            logger.fine("Opening file: " + track.getFile());
            ras = new RandomAccessFile(track.getFile(), "r");
            wpc = WavPackUtils.WavpackOpenFileInput(ras);
            if (wpc.isError()) {
                logger.warning("WavPack error: " + wpc.getErrorMessage());
                close();
                return false;
            }

            channels = WavPackUtils.WavpackGetReducedChannels(wpc);
            bps = WavPackUtils.WavpackGetBitsPerSample(wpc);
            long samplerate = WavPackUtils.WavpackGetSampleRate(wpc);
            audioFormat = new AudioFormat(samplerate, bps, channels, true, false);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long sample) {
        WavPackUtils.setSample(wpc, sample);
    }

    public int decode(byte[] buf) {
        int samplesUnpacked = (int) WavPackUtils.WavpackUnpackSamples(wpc, buffer, BUFFER_SIZE / channels);
        if (samplesUnpacked <= 0) return -1;
        samplesUnpacked *= channels;
        format_samples(samplesUnpacked, buf);

        return samplesUnpacked * bps / 8;
    }

    public void close() {
        try {
            if (ras != null)
                ras.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void format_samples(long samcnt, byte[] buf) {
        int temp;
        int counter = 0;
        int counter2 = 0;
        int bytesPerSample = bps / 8;

        switch (bytesPerSample) {
            case 1:
                while (samcnt > 0) {
                    buf[counter] = (byte) (0x00FF & (buffer[counter] + 128));
                    counter++;
                    samcnt--;
                }
                break;

            case 2:
                while (samcnt > 0) {
                    temp = buffer[counter2];
                    buf[counter] = (byte) temp;
                    counter++;
                    buf[counter] = (byte) (temp >>> 8);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;

            case 3:
                while (samcnt > 0) {
                    temp = buffer[counter2];
                    buf[counter] = (byte) temp;
                    counter++;
                    buf[counter] = (byte) (temp >>> 8);
                    counter++;
                    buf[counter] = (byte) (temp >>> 16);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;

            case 4:
                while (samcnt > 0) {
                    temp = buffer[counter2];
                    buf[counter] = (byte) temp;
                    counter++;
                    buf[counter] = (byte) (temp >>> 8);
                    counter++;
                    buf[counter] = (byte) (temp >>> 16);
                    counter++;
                    buf[counter] = (byte) (temp >>> 24);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;
        }
    }
}
