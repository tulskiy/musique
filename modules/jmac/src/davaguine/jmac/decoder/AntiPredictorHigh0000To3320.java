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
public class AntiPredictorHigh0000To3320 extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {
        //variable declares
        int p, pw;
        int q;
        int m;

        //short frame handling
        if (NumberOfElements < 32) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        ////////////////////////////////////////
        //order 5
        ////////////////////////////////////////
        System.arraycopy(pInputArray, 0, pOutputArray, 0, 8);

        //initialize values
        m = 0;

        for (q = 8; q < NumberOfElements; q++) {
            p = (5 * pOutputArray[q - 1]) - (10 * pOutputArray[q - 2]) + (12 * pOutputArray[q - 3]) - (7 * pOutputArray[q - 4]) + pOutputArray[q - 5];

            pw = (p * m) >> 12;

            pOutputArray[q] = pInputArray[q] + pw;

            //adjust m
            if (pInputArray[q] > 0) {
                if (p > 0)
                    m += 1;
                else
                    m -= 1;
            } else if (pInputArray[q] < 0) {
                if (p > 0)
                    m -= 1;
                else
                    m += 1;
            }

        }

        ///////////////////////////////////////
        //order 4
        ///////////////////////////////////////
        System.arraycopy(pOutputArray, 0, pInputArray, 0, 8);
        m = 0;

        for (q = 8; q < NumberOfElements; q++) {
            p = (4 * pInputArray[q - 1]) - (6 * pInputArray[q - 2]) + (4 * pInputArray[q - 3]) - pInputArray[q - 4];
            pw = (p * m) >> 12;

            pInputArray[q] = pOutputArray[q] + pw;

            //adjust m
            if (pOutputArray[q] > 0) {
                if (p > 0)
                    m += 2;
                else
                    m -= 2;
            } else if (pOutputArray[q] < 0) {
                if (p > 0)
                    m -= 2;
                else
                    m += 2;
            }

        }

        AntiPredictor.AntiPredict(pInputArray, pOutputArray, NumberOfElements);
    }

    private AntiPredictorNormal0000To3320 AntiPredictor = new AntiPredictorNormal0000To3320();
}
