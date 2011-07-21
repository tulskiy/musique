/*
 * Based on TTA1-C++ library functions
 * Copyright (c) 2011 Aleksander Djuric. All rights reserved.
 * Distributed under the GNU Lesser General Public License (LGPL).
 * The complete text of the license can be found in the COPYING
 * file included in the distribution.
 */

package com.tulskiy.tta;

/**
 * Author: Denis Tulskiy
 * Date: 5/31/11
 */
public class Macros {
    static int MUL_FRAME_TIME(int x) {
        return (256 * (x) / 245);
    } // = x * FRAME_TIME

    static int DIV_FRAME_TIME(int x) {
        return (int) (245 * (x) / 256);
    } // = x / FRAME_TIME

    static void WRITE_BUFFER(int x, byte[] buf, int pos, int depth) {
        if (depth == 2) {
            buf[pos] = (byte) (x & 0xFF);
            buf[pos + 1] = (byte) ((x >> 8) & 0xFF);
        } else if (depth == 1) {
            buf[pos] = (byte) (x & 0xFF);
        } else {
            buf[pos] = (byte) (x & 0xFF);
            buf[pos + 1] = (byte) ((x >> 8) & 0xFF);
            buf[pos + 2] = (byte) ((x >> 16) & 0xFF);
        }
    }
}
