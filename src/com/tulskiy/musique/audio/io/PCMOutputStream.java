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

package com.tulskiy.musique.audio.io;

import javax.sound.sampled.AudioFormat;

/**
 * @Author: Denis Tulskiy
 * @Date: 24.06.2009
 */
public interface PCMOutputStream {
    public void write(byte[] b, int off, int len);

    public int read(byte[] b, int off, int len);

    public int available();

    public int size();

    public void flush();

    public long getTotalBytes();

    public void reset();

    public void removeTail(long bytes);
}
