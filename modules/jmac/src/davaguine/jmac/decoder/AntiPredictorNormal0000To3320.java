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
public class AntiPredictorNormal0000To3320 extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {
        //variable declares
        int ip, op, op1, op2;
        int p, pw;
        int m;

        //short frame handling
        if (NumberOfElements < 32) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        ////////////////////////////////////////
        //order 3
        ////////////////////////////////////////
        System.arraycopy(pInputArray, 0, pOutputArray, 0, 8);

        //initialize values
        m = 300;
        op = 8;
        op1 = 7;
        op2 = 6;

        //make the first prediction
        p = (pOutputArray[7] * 3) - (pOutputArray[6] * 3) + pOutputArray[5];
        pw = (p * m) >> 12;

        //loop through the array
        for (ip = 8; ip < NumberOfElements; ip++, op++, op1++, op2++) {

            //figure the output value
            pOutputArray[op] = pInputArray[ip] + pw;

            //adjust m
            if (pInputArray[ip] > 0)
                m += (p > 0) ? 4 : -4;
            else if (pInputArray[ip] < 0)
                m += (p > 0) ? -4 : 4;

            //make the next prediction
            p = (pOutputArray[op] * 3) - (pOutputArray[op1] * 3) + pOutputArray[op2];
            pw = (p * m) >> 12;
        }


        ///////////////////////////////////////
        //order 2
        ///////////////////////////////////////
        System.arraycopy(pInputArray, 0, pOutputArray, 0, 8);
        m = 3000;

        op1 = 7;
        p = (pInputArray[op1] * 2) - pInputArray[6];
        pw = (p * m) >> 12;

        for (op = 8, ip = 8; ip < NumberOfElements; ip++, op++, op1++) {
            pInputArray[op] = pOutputArray[ip] + pw;

            //adjust m
            if (pOutputArray[ip] > 0)
                m += (p > 0) ? 12 : -12;
            else if (pOutputArray[ip] < 0)
                m += (p > 0) ? -12 : 12;

            p = (pInputArray[op] * 2) - pInputArray[op1];
            pw = (p * m) >> 12;

        }

        ///////////////////////////////////////
        //order 1
        ///////////////////////////////////////
        pOutputArray[0] = pInputArray[0];
        pOutputArray[1] = pInputArray[1] + pOutputArray[0];
        pOutputArray[2] = pInputArray[2] + pOutputArray[1];
        pOutputArray[3] = pInputArray[3] + pOutputArray[2];
        pOutputArray[4] = pInputArray[4] + pOutputArray[3];
        pOutputArray[5] = pInputArray[5] + pOutputArray[4];
        pOutputArray[6] = pInputArray[6] + pOutputArray[5];
        pOutputArray[7] = pInputArray[7] + pOutputArray[6];

        m = 3900;

        p = pOutputArray[7];
        pw = (p * m) >> 12;

        for (op = 8, ip = 8; ip < NumberOfElements; ip++, op++) {
            pOutputArray[op] = pInputArray[ip] + pw;

            //adjust m
            if (pInputArray[ip] > 0)
                m += (p > 0) ? 1 : -1;
            else if (pInputArray[ip] < 0)
                m += (p > 0) ? -1 : 1;

            p = pOutputArray[op];
            pw = (p * m) >> 12;
        }
    }
}
