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


/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class AntiPredictorHigh3600To3700 extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {
        //variable declares
        int q;

        //short frame handling
        if (NumberOfElements < 16) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        //make the first five samples identical in both arrays
        System.arraycopy(pInputArray, 0, pOutputArray, 0, 13);

        //initialize values
        int bm1 = 0;
        int bm2 = 0;
        int bm3 = 0;
        int bm4 = 0;
        int bm5 = 0;
        int bm6 = 0;
        int bm7 = 0;
        int bm8 = 0;
        int bm9 = 0;
        int bm10 = 0;
        int bm11 = 0;
        int bm12 = 0;
        int bm13 = 0;


        int m2 = 64;

        int m3 = 28;
        int m4 = 16;
        int OP0;
        int p4 = pInputArray[12];
        int p3 = (pInputArray[12] - pInputArray[11]) << 1;
        int p2 = pInputArray[12] + ((pInputArray[10] - pInputArray[11]) << 3);
        int bp1 = pOutputArray[12];
        int bp2 = pOutputArray[11];
        int bp3 = pOutputArray[10];
        int bp4 = pOutputArray[9];
        int bp5 = pOutputArray[8];
        int bp6 = pOutputArray[7];
        int bp7 = pOutputArray[6];
        int bp8 = pOutputArray[5];
        int bp9 = pOutputArray[4];
        int bp10 = pOutputArray[3];
        int bp11 = pOutputArray[2];
        int bp12 = pOutputArray[1];
        int bp13 = pOutputArray[0];

        for (q = 13; q < NumberOfElements; q++) {
            pInputArray[q] = pInputArray[q] - 1;
            OP0 = (pInputArray[q] - ((bp1 * bm1) >> 8) + ((bp2 * bm2) >> 8) - ((bp3 * bm3) >> 8) - ((bp4 * bm4) >> 8) - ((bp5 * bm5) >> 8) - ((bp6 * bm6) >> 8) - ((bp7 * bm7) >> 8) - ((bp8 * bm8) >> 8) - ((bp9 * bm9) >> 8) + ((bp10 * bm10) >> 8) + ((bp11 * bm11) >> 8) + ((bp12 * bm12) >> 8) + ((bp13 * bm13) >> 8));

            if (pInputArray[q] > 0) {
                bm1 -= bp1 > 0 ? 1 : -1;
                bm2 += bp2 >= 0 ? 1 : -1;
                bm3 -= bp3 > 0 ? 1 : -1;
                bm4 -= bp4 >= 0 ? 1 : -1;
                bm5 -= bp5 > 0 ? 1 : -1;
                bm6 -= bp6 >= 0 ? 1 : -1;
                bm7 -= bp7 > 0 ? 1 : -1;
                bm8 -= bp8 >= 0 ? 1 : -1;
                bm9 -= bp9 > 0 ? 1 : -1;
                bm10 += bp10 >= 0 ? 1 : -1;
                bm11 += bp11 > 0 ? 1 : -1;
                bm12 += bp12 >= 0 ? 1 : -1;
                bm13 += bp13 > 0 ? 1 : -1;

            } else if (pInputArray[q] < 0) {
                bm1 -= bp1 <= 0 ? 1 : -1;
                bm2 += bp2 < 0 ? 1 : -1;
                bm3 -= bp3 <= 0 ? 1 : -1;
                bm4 -= bp4 < 0 ? 1 : -1;
                bm5 -= bp5 <= 0 ? 1 : -1;
                bm6 -= bp6 < 0 ? 1 : -1;
                bm7 -= bp7 <= 0 ? 1 : -1;
                bm8 -= bp8 < 0 ? 1 : -1;
                bm9 -= bp9 <= 0 ? 1 : -1;
                bm10 += bp10 < 0 ? 1 : -1;
                bm11 += bp11 <= 0 ? 1 : -1;
                bm12 += bp12 < 0 ? 1 : -1;
                bm13 += bp13 <= 0 ? 1 : -1;

            }

            bp13 = bp12;
            bp12 = bp11;
            bp11 = bp10;
            bp10 = bp9;
            bp9 = bp8;
            bp8 = bp7;
            bp7 = bp6;
            bp6 = bp5;
            bp5 = bp4;
            bp4 = bp3;
            bp3 = bp2;
            bp2 = bp1;
            bp1 = OP0;

            pInputArray[q] = OP0 + ((p2 * m2) >> 11) + ((p3 * m3) >> 9) + ((p4 * m4) >> 9);

            if (OP0 > 0) {
                m2 -= p2 > 0 ? -1 : 1;
                m3 -= p3 > 0 ? -1 : 1;
                m4 -= p4 > 0 ? -1 : 1;
            } else if (OP0 < 0) {
                m2 -= p2 > 0 ? 1 : -1;
                m3 -= p3 > 0 ? 1 : -1;
                m4 -= p4 > 0 ? 1 : -1;
            }

            p2 = pInputArray[q] + ((pInputArray[q - 2] - pInputArray[q - 1]) << 3);
            p3 = (pInputArray[q] - pInputArray[q - 1]) << 1;
            p4 = pInputArray[q];

            pOutputArray[q] = pInputArray[q];
        }

        m4 = 370;

        pOutputArray[1] = pInputArray[1] + pOutputArray[0];
        pOutputArray[2] = pInputArray[2] + pOutputArray[1];
        pOutputArray[3] = pInputArray[3] + pOutputArray[2];
        pOutputArray[4] = pInputArray[4] + pOutputArray[3];
        pOutputArray[5] = pInputArray[5] + pOutputArray[4];
        pOutputArray[6] = pInputArray[6] + pOutputArray[5];
        pOutputArray[7] = pInputArray[7] + pOutputArray[6];
        pOutputArray[8] = pInputArray[8] + pOutputArray[7];
        pOutputArray[9] = pInputArray[9] + pOutputArray[8];
        pOutputArray[10] = pInputArray[10] + pOutputArray[9];
        pOutputArray[11] = pInputArray[11] + pOutputArray[10];
        pOutputArray[12] = pInputArray[12] + pOutputArray[11];

        p4 = (2 * pInputArray[12]) - pInputArray[11];
        int p6 = 0;
        int p5 = pOutputArray[12];
        int IP0, IP1;
        int m6 = 0;

        IP1 = pInputArray[12];
        for (q = 13; q < NumberOfElements; q++) {
            IP0 = pOutputArray[q] + ((p4 * m4) >> 9) - ((p6 * m6) >> 10);
            if ((pOutputArray[q] ^ p4) >= 0)
                m4++;
            else
                m4--;
            if ((pOutputArray[q] ^ p6) >= 0)
                m6--;
            else
                m6++;
            p4 = (2 * IP0) - IP1;
            p6 = IP0;

            pOutputArray[q] = IP0 + ((p5 * 31) >> 5);
            p5 = pOutputArray[q];

            IP1 = IP0;
        }
    }
}
