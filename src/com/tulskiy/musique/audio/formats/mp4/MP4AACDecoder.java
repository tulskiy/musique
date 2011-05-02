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

package com.tulskiy.musique.audio.formats.mp4;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;
import net.sourceforge.jaad.DecoderConfig;
import net.sourceforge.jaad.SampleBuffer;
import net.sourceforge.jaad.mp4.AudioFrame;
import net.sourceforge.jaad.mp4.MP4Reader;

import javax.sound.sampled.AudioFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Author: Denis Tulskiy
 * Date: 4/5/11
 */
public class MP4AACDecoder implements Decoder {
    private FileInputStream is;
    private net.sourceforge.jaad.Decoder decoder;
    private MP4Reader mp4;
    private SampleBuffer sampleBuffer;
    private AudioFormat audioFormat;
    private int currentSample;
    private int totalSamples;
    private int gaplessPadding;
    private int offset;
    private int bps = 2;
    private AudioFrame frame;

    @Override
    public boolean open(Track track) {
        try {
            is = new FileInputStream(track.getTrackData().getFile());
            sampleBuffer = new SampleBuffer();
            initDecoder(0);
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error opening file " + track.getTrackData().getFile().getAbsolutePath(), e);
        }
        return false;
    }

    private void initDecoder(long sample) throws IOException {
        is.getChannel().position(0);
        mp4 = new MP4Reader(is);
        final DecoderConfig conf = DecoderConfig.parseMP4DecoderSpecificInfo(mp4.getDecoderSpecificInfo());
        decoder = new net.sourceforge.jaad.Decoder(conf);

        sample += mp4.getGaplessDelay();
        gaplessPadding = mp4.getGaplessPadding();
        totalSamples = (int) mp4.getNumSamples();
        bps = mp4.getBitsPerSample() / 8;
        int target = 0;
        currentSample = -1;
        for (int i = 0; i < totalSamples; i++) {
            target += mp4.getSampleDuration(i);
            if (sample < target) {
                currentSample = i;
                break;
            }
        }

        if (currentSample == -1) currentSample = totalSamples;

        int preheat = 2;
        int s = currentSample - preheat;
        if (s < 0) s = 0;

        for (int i = s; i < currentSample; i++) {
            frame = mp4.readSample(i);
            decoder.decodeFrame(frame.getData(), sampleBuffer);
        }

        offset = (int) (sample - (target - mp4.getSampleDuration(currentSample)));
        if (audioFormat == null)
            audioFormat = new AudioFormat((float) mp4.getSampleRate(), bps * 8, (int) mp4.getChannelCount(), true, false);
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    @Override
    public void seekSample(long sample) {
        try {
            initDecoder(sample);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while trying to see to sample " + sample, e);
        }
    }

    @Override
    public int decode(byte[] buf) {
        try {
            frame = mp4.readSample(currentSample++);
            if (frame == null) {
                return -1;
            }
            decoder.decodeFrame(frame.getData(), sampleBuffer);

            int len;
            if (currentSample == totalSamples && gaplessPadding != 0) {
                len = gaplessPadding * sampleBuffer.getChannels();
            } else {
                len = sampleBuffer.getSamples();
            }
            len *= bps;
            int off = offset * bps * sampleBuffer.getChannels();
            System.arraycopy(sampleBuffer.getData(), off, buf, 0, len - off);
            offset = 0;
            return len - off;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error decoding mp4 file", e);
        }
        return -1;
    }

    @Override
    public void close() {
        try {
            mp4.close();
            mp4 = null;
            decoder = null;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing file input stream", e);
        }
    }
}
