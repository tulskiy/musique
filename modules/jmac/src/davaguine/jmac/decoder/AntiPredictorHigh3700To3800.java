/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package davaguine.jmac.decoder;

import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class AntiPredictorHigh3700To3800 extends AntiPredictor {
    private final static int FIRST_ELEMENT = 16;
    private int[] bm = new int[FIRST_ELEMENT];

    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {
        //the frame to start prediction on

        //short frame handling
        if (NumberOfElements < 20) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        //make the first five samples identical in both arrays
        System.arraycopy(pInputArray, 0, pOutputArray, 0, FIRST_ELEMENT);

        //variable declares and initializations
        Arrays.fill(bm, 0);
        int m2 = 64, m3 = 115, m4 = 64, m5 = 740, m6 = 0;
        int p4 = pInputArray[FIRST_ELEMENT - 1];
        int p3 = (pInputArray[FIRST_ELEMENT - 1] - pInputArray[FIRST_ELEMENT - 2]) << 1;
        int p2 = pInputArray[FIRST_ELEMENT - 1] + ((pInputArray[FIRST_ELEMENT - 3] - pInputArray[FIRST_ELEMENT - 2]) << 3);
        int op = FIRST_ELEMENT;
        int ip = FIRST_ELEMENT;
        int IPP2 = pInputArray[ip - 2];
        int IPP1 = pInputArray[ip - 1];
        int p7 = 2 * pInputArray[ip - 1] - pInputArray[ip - 2];
        int opp = pOutputArray[op - 1];
        int Original;

        //undo the initial prediction stuff
        for (int q = 1; q < FIRST_ELEMENT; q++) {
            pOutputArray[q] += pOutputArray[q - 1];
        }

        //pump the primary loop
        for (; op < NumberOfElements; op++, ip++) {

            Original = pInputArray[ip] - 1;
            pInputArray[ip] = Original - (((pInputArray[ip - 1] * bm[0]) + (pInputArray[ip - 2] * bm[1]) + (pInputArray[ip - 3] * bm[2]) + (pInputArray[ip - 4] * bm[3]) + (pInputArray[ip - 5] * bm[4]) + (pInputArray[ip - 6] * bm[5]) + (pInputArray[ip - 7] * bm[6]) + (pInputArray[ip - 8] * bm[7]) + (pInputArray[ip - 9] * bm[8]) + (pInputArray[ip - 10] * bm[9]) + (pInputArray[ip - 11] * bm[10]) + (pInputArray[ip - 12] * bm[11]) + (pInputArray[ip - 13] * bm[12]) + (pInputArray[ip - 14] * bm[13]) + (pInputArray[ip - 15] * bm[14]) + (pInputArray[ip - 16] * bm[15])) >> 8);

            if (Original > 0) {
                bm[0] -= pInputArray[ip - 1] > 0 ? 1 : -1;

                bm[1] += (((long) (pInputArray[ip - 2]) >> 30) & 2) - 1;

                bm[2] -= pInputArray[ip - 3] > 0 ? 1 : -1;
                bm[3] += (((long) (pInputArray[ip - 4]) >> 30) & 2) - 1;

                bm[4] -= pInputArray[ip - 5] > 0 ? 1 : -1;
                bm[5] += (((long) (pInputArray[ip - 6]) >> 30) & 2) - 1;
                bm[6] -= pInputArray[ip - 7] > 0 ? 1 : -1;
                bm[7] += (((long) (pInputArray[ip - 8]) >> 30) & 2) - 1;
                bm[8] -= pInputArray[ip - 9] > 0 ? 1 : -1;
                bm[9] += (((long) (pInputArray[ip - 10]) >> 30) & 2) - 1;
                bm[10] -= pInputArray[ip - 11] > 0 ? 1 : -1;
                bm[11] += (((long) (pInputArray[ip - 12]) >> 30) & 2) - 1;
                bm[12] -= pInputArray[ip - 13] > 0 ? 1 : -1;
                bm[13] += (((long) (pInputArray[ip - 14]) >> 30) & 2) - 1;
                bm[14] -= pInputArray[ip - 15] > 0 ? 1 : -1;
                bm[15] += (((long) (pInputArray[ip - 16]) >> 30) & 2) - 1;
            } else if (Original < 0) {
                bm[0] -= pInputArray[ip - 1] <= 0 ? 1 : -1;

                bm[1] -= (((long) (pInputArray[ip - 2]) >> 30) & 2) - 1;

                bm[2] -= pInputArray[ip - 3] <= 0 ? 1 : -1;
                bm[3] -= (((long) (pInputArray[ip - 4]) >> 30) & 2) - 1;
                bm[4] -= pInputArray[ip - 5] <= 0 ? 1 : -1;
                bm[5] -= (((long) (pInputArray[ip - 6]) >> 30) & 2) - 1;
                bm[6] -= pInputArray[ip - 7] <= 0 ? 1 : -1;
                bm[7] -= (((long) (pInputArray[ip - 8]) >> 30) & 2) - 1;
                bm[8] -= pInputArray[ip - 9] <= 0 ? 1 : -1;
                bm[9] -= (((long) (pInputArray[ip - 10]) >> 30) & 2) - 1;
                bm[10] -= pInputArray[ip - 11] <= 0 ? 1 : -1;
                bm[11] -= (((long) (pInputArray[ip - 12]) >> 30) & 2) - 1;
                bm[12] -= pInputArray[ip - 13] <= 0 ? 1 : -1;
                bm[13] -= (((long) (pInputArray[ip - 14]) >> 30) & 2) - 1;
                bm[14] -= pInputArray[ip - 15] <= 0 ? 1 : -1;
                bm[15] -= (((long) (pInputArray[ip - 16]) >> 30) & 2) - 1;
            }

            /////////////////////////////////////////////
            pOutputArray[op] = pInputArray[ip] + (((p2 * m2) + (p3 * m3) + (p4 * m4)) >> 11);

            if (pInputArray[ip] > 0) {
                m2 -= p2 > 0 ? -1 : 1;
                m3 -= p3 > 0 ? -4 : 4;
                m4 -= p4 > 0 ? -4 : 4;
            } else if (pInputArray[ip] < 0) {
                m2 -= p2 > 0 ? 1 : -1;
                m3 -= p3 > 0 ? 4 : -4;
                m4 -= p4 > 0 ? 4 : -4;
            }

            p4 = pOutputArray[op];
            p2 = p4 + ((IPP2 - IPP1) << 3);
            p3 = (p4 - IPP1) << 1;

            IPP2 = IPP1;
            IPP1 = p4;

            /////////////////////////////////////////////
            pOutputArray[op] += (((p7 * m5) - (opp * m6)) >> 10);

            if ((IPP1 ^ p7) >= 0)
                m5 += 2;
            else
                m5 -= 2;
            if ((IPP1 ^ opp) >= 0)
                m6--;
            else
                m6++;

            p7 = 2 * pOutputArray[op] - opp;
            opp = pOutputArray[op];

            /////////////////////////////////////////////
            pOutputArray[op] += ((pOutputArray[op - 1] * 31) >> 5);
        }
    }
}
