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
public class AntiPredictorFast0000To3320 extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {

        //short frame handling
        if (NumberOfElements < 32) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        //the initial
        pOutputArray[0] = pInputArray[0];
        pOutputArray[1] = pInputArray[1] + pOutputArray[0];
        pOutputArray[2] = pInputArray[2] + pOutputArray[1];
        pOutputArray[3] = pInputArray[3] + pOutputArray[2];
        pOutputArray[4] = pInputArray[4] + pOutputArray[3];
        pOutputArray[5] = pInputArray[5] + pOutputArray[4];
        pOutputArray[6] = pInputArray[6] + pOutputArray[5];
        pOutputArray[7] = pInputArray[7] + pOutputArray[6];

        //the rest
        int p, pw;
        int m = 4000;
        int ip, op, op1;

        op1 = 7;
        p = (pOutputArray[op1] * 2) - pOutputArray[6];
        pw = (p * m) >> 12;

        for (op = 8, ip = 8; ip < NumberOfElements; ip++, op++, op1++) {
            pOutputArray[op] = pInputArray[ip] + pw;


            //adjust m
            if (pInputArray[ip] > 0)
                m += (p > 0) ? 4 : -4;
            else if (pInputArray[ip] < 0)
                m += (p > 0) ? -4 : 4;

            p = (pOutputArray[op] * 2) - pOutputArray[op1];
            pw = (p * m) >> 12;
        }
    }
}
