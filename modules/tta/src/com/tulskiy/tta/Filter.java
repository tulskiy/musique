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
public class Filter {
    static int hybrid_filter_dec(TTA_fltst fs, int in) {
        int[] pA = fs.dl;
        int[] pB = fs.qm;
        int[] pM = fs.dx;
        int sum = fs.round;

        if (fs.error < 0) {
            pB[0] -= pM[0];
            pB[1] -= pM[1];
            pB[2] -= pM[2];
            pB[3] -= pM[3];
            pB[4] -= pM[4];
            pB[5] -= pM[5];
            pB[6] -= pM[6];
            pB[7] -= pM[7];
        } else if (fs.error > 0) {
            pB[0] += pM[0];
            pB[1] += pM[1];
            pB[2] += pM[2];
            pB[3] += pM[3];
            pB[4] += pM[4];
            pB[5] += pM[5];
            pB[6] += pM[6];
            pB[7] += pM[7];
        }

        sum += pA[0] * pB[0] + pA[1] * pB[1] + pA[2] * pB[2] + pA[3] * pB[3] +
                pA[4] * pB[4] + pA[5] * pB[5] + pA[6] * pB[6] + pA[7] * pB[7];

        pM[0] = pM[1];
        pM[1] = pM[2];
        pM[2] = pM[3];
        pM[3] = pM[4];
        pA[0] = pA[1];
        pA[1] = pA[2];
        pA[2] = pA[3];
        pA[3] = pA[4];

        pM[4] = ((pA[4] >> 30) | 1);
        pM[5] = ((pA[5] >> 30) | 2) & ~1;
        pM[6] = ((pA[6] >> 30) | 2) & ~1;
        pM[7] = ((pA[7] >> 30) | 4) & ~3;

        fs.error = in;
        in += (sum >> fs.shift);

        pA[4] = -pA[5];
        pA[5] = -pA[6];
        pA[6] = in - pA[7];
        pA[7] = in;
        pA[5] += pA[6];
        pA[4] += pA[5];
        return in;
    } // hybrid_filter_dec
}
