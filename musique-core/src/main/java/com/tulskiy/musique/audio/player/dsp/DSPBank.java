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

package com.tulskiy.musique.audio.player.dsp;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: 26.07.2009
 */
public class DSPBank {
    private static final int DEFAULT_SAMPLES_SIZE = (int) (44100 * 4 * 0.2);
    private final Logger logger = Logger.getLogger(getClass().getName());

    private ArrayList<Processor> processors = new ArrayList<Processor>();
    private int count;
    private float[] samples = new float[DEFAULT_SAMPLES_SIZE];

    public void addProcessor(Processor processor) {
        processors.add(processor);
    }

    public void removeProcessor(Processor processor) {
        processors.remove(processor);
    }

    public void process(byte[] pcm, int off, int len, int bps, boolean bigEndian) {
        if (processors.size() == 0) {
            return;
        }

        int length = len / (bps >> 3);
        if (samples.length < length) {
            samples = new float[length + 100];
        }

        int j = 0;
        for (int i = off; i < off + len; i += bps / 8) {
            if (!bigEndian) {
                samples[j++] = (float) ((pcm[i] & 0xFF | pcm[i + 1] << 8) / 32767.0);
            } else {
                samples[j++] = (float) ((pcm[i + 1] & 0xFF | pcm[i] << 8) / 32767.0);
            }
        }

        for (int i = 0; i < processors.size(); i++) {
            processors.get(i).process(samples, len);
        }

        j = 0;
        for (int i = off; i < off + len; i += bps / 8) {
            int v = (int) (samples[j] * 32767);
            if (v > 32767) {
                v = 32767;
            }
            if (v < -32768) {
                v = -32768;
            }
            if (!bigEndian) {
                pcm[i] = (byte) (v & 0xff);
                pcm[i + 1] = (byte) (v >> 8 & 0xff);
            } else {
                pcm[i + 1] = (byte) (v & 0xff);
                pcm[i] = (byte) ((v >> 8) & 0xff);
            }

            j++;
        }
    }
}
