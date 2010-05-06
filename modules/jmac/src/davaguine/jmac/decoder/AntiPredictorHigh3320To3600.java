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
public class AntiPredictorHigh3320To3600 extends AntiPredictor {
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {
        //short frame handling
        if (NumberOfElements < 8) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        //do the offset anti-prediction
        AntiPredictorOffset.AntiPredict(pInputArray, pOutputArray, NumberOfElements, 2, 12);
        AntiPredictorOffset.AntiPredict(pOutputArray, pInputArray, NumberOfElements, 3, 12);

        AntiPredictorOffset.AntiPredict(pInputArray, pOutputArray, NumberOfElements, 4, 12);
        AntiPredictorOffset.AntiPredict(pOutputArray, pInputArray, NumberOfElements, 5, 12);

        AntiPredictorOffset.AntiPredict(pInputArray, pOutputArray, NumberOfElements, 6, 12);
        AntiPredictorOffset.AntiPredict(pOutputArray, pInputArray, NumberOfElements, 7, 12);


        //use the normal mode
        AntiPredictor.AntiPredict(pInputArray, pOutputArray, NumberOfElements);
    }

    private AntiPredictorOffset AntiPredictorOffset = new AntiPredictorOffset();
    private AntiPredictorNormal3320To3800 AntiPredictor = new AntiPredictorNormal3320To3800();
}
