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

import com.tulskiy.musique.audio.IcyInputStream;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.util.AudioMath;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Author: Denis Tulskiy
 * Date: 8/31/11
 */
public class AACDecoder implements com.tulskiy.musique.audio.Decoder {
    private static final int BUFFER_SIZE = 30000;

    private AudioFormat audioFormat;
    private Decoder decoder;
    private ADTSDemultiplexer adts;
    private SampleBuffer buffer;
    private InputStream in;
    private Track track;
    private int errorCount;

    @Override
    public boolean open(Track track) {
        this.track = track;
        try {
            TrackData trackData = track.getTrackData();
            if (trackData.isStream()) {
                in = new BufferedInputStream(IcyInputStream.create(track), BUFFER_SIZE);
                trackData.setCodec("AAC Stream");
            } else
                in = new BufferedInputStream(new FileInputStream(trackData.getFile()), BUFFER_SIZE);

            adts = new ADTSDemultiplexer(in);
            audioFormat = new AudioFormat(adts.getSampleFrequency(), 16, adts.getChannelCount(), true, true);
            decoder = new Decoder(adts.getDecoderSpecificInfo());
            buffer = new SampleBuffer();
            trackData.setChannels(adts.getChannelCount());
            trackData.setSampleRate(adts.getSampleFrequency());
            trackData.setBps(16);
            errorCount = 0;
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not open AAC stream", e);
        }
        return false;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    @Override
    public void seekSample(long sample) {
    }

    @Override
    public int decode(byte[] buf) {
        try {
            byte[] data = adts.readNextFrame();
            decoder.decodeFrame(data, buffer);
            int length = buffer.getData().length;
            System.arraycopy(buffer.getData(), 0, buf, 0, length);
            track.getTrackData().setBitrate((int) (data.length * 8 / AudioMath.bytesToMillis(length, audioFormat)));
            return length;
        } catch (IOException e) {
            if (track.getTrackData().isStream()) {
                logger.log(Level.WARNING, "Error decoding AAC stream", e);
                close();
                //save it here because we set errorCount to 0 in open
                int oldErr = errorCount++;
                if (!open(track) || errorCount > 5) {
                    return -1;
                }
                errorCount = oldErr;
                return 0;
            } else {
                // due to the way AAC api works, this probably means EOF
                // do nothing
            }
        }
        return -1;
    }

    @Override
    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not close AAC stream", e);
            }
        }

        decoder = null;
        adts = null;
        buffer = null;
        track = null;
    }
}
