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
public class AntiPredictorFast3320ToCurrent extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {

        //short frame handling
        if (NumberOfElements < 3) {
            return;
        }

        //variable declares
        int p;
        int m = 375;
        int ip;
        int IP2 = pInputArray[1];
        int IP3 = pInputArray[0];
        int OP1 = pInputArray[1];

        //the decompression loop (order 2 followed by order 1)
        for (ip = 2; ip < NumberOfElements; ip++) {

            //make a prediction for order 2
            p = IP2 + IP2 - IP3;

            //rollback the values
            IP3 = IP2;
            IP2 = pInputArray[ip] + ((p * m) >> 9);

            //adjust m for the order 2
            if ((pInputArray[ip] ^ p) > 0)
                m++;
            else
                m--;

            //set the output value
            pInputArray[ip] = IP2 + OP1;
            OP1 = pInputArray[ip];
        }
    }
}
