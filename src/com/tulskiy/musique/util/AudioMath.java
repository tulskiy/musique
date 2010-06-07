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

package com.tulskiy.musique.util;

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
}
