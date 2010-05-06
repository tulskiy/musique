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

package com.tulskiy.musique.audio.formats.aac.libjfaad;

import com.sun.jna.Platform;

/**
 * Created by IntelliJ IDEA.
 * User: tulskiy
 * Date: Oct 3, 2009
 * Time: 4:35:45 PM
 */
public class libjfaad {
    static {
        if (Platform.isWindows()) {
            System.loadLibrary("libfaad2");
            System.loadLibrary("libjfaad");
        } else {
            System.loadLibrary("faad");
            System.loadLibrary("jfaad");
        }
    }

    public native int open(String file);

    public native int decode(int handle, byte[] buffer, int max);

    public native void seek(int handle, long sample);

    public native void close(int handle);

    public native int getChannels(int handle);

    public native int getSampleRate(int handle);

}
