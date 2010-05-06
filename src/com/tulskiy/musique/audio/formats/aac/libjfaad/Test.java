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

/**
 * Created by IntelliJ IDEA.
 * User: tulskiy
 * Date: Oct 3, 2009
 * Time: 4:55:45 PM
 */
public class Test {
    public static void main(String[] args) {
        libjfaad lib = new libjfaad();

        int handler = lib.open("testfiles/aac/sample_nero.mp4");

        if (handler != -1) {
            System.out.println("Channels: " + lib.getChannels(handler));
            System.out.println("Samplerate: " + lib.getSampleRate(handler));

            byte[] buffer = new byte[5000];

            int len;
//            while ((len = lib.decode(handler, buffer, buffer.length)) != -1) {
            //            System.out.println(len);
            //            System.out.println(buffer[23]);
//            }
        }

        lib.close(handler);
    }
}
