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
public class AntiPredictorNormal3320To3800 extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {
        //variable declares
        int q;

        //short frame handling
        if (NumberOfElements < 8) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        //make the first five samples identical in both arrays
        System.arraycopy(pInputArray, 0, pOutputArray, 0, 5);

        //initialize values
        int m1 = 0;
        int m2 = 64;
        int m3 = 28;
        int OP0;

        int p3 = (3 * (pOutputArray[4] - pOutputArray[3])) + pOutputArray[2];
        int p2 = pInputArray[4] + ((pInputArray[2] - pInputArray[3]) << 3) - pInputArray[1] + pInputArray[0];
        int p1 = pOutputArray[4];

        for (q = 5; q < NumberOfElements; q++) {
            OP0 = pInputArray[q] + ((p1 * m1) >> 8);
            if ((pInputArray[q] ^ p1) > 0)
                m1++;
            else
                m1--;
            p1 = OP0;

            pInputArray[q] = OP0 + ((p2 * m2) >> 11);
            if ((OP0 ^ p2) > 0)
                m2++;
            else
                m2--;
            p2 = pInputArray[q] + ((pInputArray[q - 2] - pInputArray[q - 1]) << 3) - pInputArray[q - 3] + pInputArray[q - 4];

            pOutputArray[q] = pInputArray[q] + ((p3 * m3) >> 9);
            if ((pInputArray[q] ^ p3) > 0)
                m3++;
            else
                m3--;
            p3 = (3 * (pOutputArray[q] - pOutputArray[q - 1])) + pOutputArray[q - 2];
        }

        int m4 = 370;
        int m5 = 3900;

        //pOutputArray[0] = pInputArray[0];
        pOutputArray[1] = pInputArray[1] + pOutputArray[0];
        pOutputArray[2] = pInputArray[2] + pOutputArray[1];
        pOutputArray[3] = pInputArray[3] + pOutputArray[2];
        pOutputArray[4] = pInputArray[4] + pOutputArray[3];

        int p4 = (2 * pInputArray[4]) - pInputArray[3];
        int p5 = pOutputArray[4];
        int IP0, IP1;

        IP1 = pInputArray[4];
        for (q = 5; q < NumberOfElements; q++) {
            IP0 = pOutputArray[q] + ((p4 * m4) >> 9);
            if ((pOutputArray[q] ^ p4) > 0)
                m4++;
            else
                m4--;
            p4 = (2 * IP0) - IP1;

            pOutputArray[q] = IP0 + ((p5 * m5) >> 12);
            if ((IP0 ^ p5) > 0)
                m5++;
            else
                m5--;
            p5 = pOutputArray[q];

            IP1 = IP0;
        }
    }
}
