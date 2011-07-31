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

package com.tulskiy.musique.util;

import javax.sound.sampled.AudioFormat;

/**
 * @Author: Denis Tulskiy
 * @Date: 13.07.2009
 */
public class AudioMath {
    public static long bytesToSamples(long bytes, int frameSize) {
        return bytes / frameSize;
    }

    public static long samplesToBytes(long samples, int frameSize) {
        return samples * frameSize;
    }

    public static double samplesToMillis(long samples, int sampleRate) {
        return (double) samples / sampleRate * 1000;
    }

    public static double bytesToMillis(long bytes, AudioFormat fmt) {
        long l = AudioMath.bytesToSamples(bytes, fmt.getFrameSize());
        return samplesToMillis(l, (int) fmt.getSampleRate());
    }

    public static int convertBuffer(byte[] input, int[] output, int len, AudioFormat fmt) {
        int bps = fmt.getSampleSizeInBits() / 8;
        int target = 0;
        int i = 0;
        while (target < len) {
            switch (bps) {
                case 1:
                    output[i++] = input[target++];
                    break;
                case 2:
                    output[i++] = (short)((input[target++] & 0xFF) | (input[target++] << 8));
                    break;
                case 3:
                    output[i++] = (input[target++] & 0xFF) | (input[target++] << 8 & 0xFF00) | (input[target++] << 16);
                    break;
            }
        }
        return i;
    }

    public static int millisToSamples(int millis, int sampleRate) {
        return millis / 1000 * sampleRate;
    }
}
