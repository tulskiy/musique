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
public class AntiPredictorOffset extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements, int Offset, int DeltaM) {
        System.arraycopy(pInputArray, 0, pOutputArray, 0, Offset);

        int ip = Offset;
        int ipo = 0;
        int op = Offset;
        int m = 0;

        for (; op < NumberOfElements; ip++, ipo++, op++) {
            pOutputArray[op] = pInputArray[ip] + ((pOutputArray[ipo] * m) >> 12);

            if ((pOutputArray[ipo] ^ pInputArray[ip]) > 0)
                m += DeltaM;
            else
                m -= DeltaM;
        }
    }
}
