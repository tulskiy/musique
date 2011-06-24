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

import davaguine.jmac.info.CompressionLevel;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class AntiPredictor {
    //construction/destruction
    public AntiPredictor() {
    }

    //functions
    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements) {
        return;
    }

    public static AntiPredictor CreateAntiPredictor(int nCompressionLevel, int nVersion) {
        AntiPredictor pAntiPredictor = null;

        switch (nCompressionLevel) {
            case CompressionLevel.COMPRESSION_LEVEL_FAST:
                if (nVersion < 3320)
                    pAntiPredictor = new AntiPredictorFast0000To3320();
                else
                    pAntiPredictor = new AntiPredictorFast3320ToCurrent();
                break;

            case CompressionLevel.COMPRESSION_LEVEL_NORMAL:
                if (nVersion < 3320)
                    pAntiPredictor = new AntiPredictorNormal0000To3320();
                else if (nVersion < 3800)
                    pAntiPredictor = new AntiPredictorNormal3320To3800();
                else
                    pAntiPredictor = new AntiPredictorNormal3800ToCurrent();
                break;

            case CompressionLevel.COMPRESSION_LEVEL_HIGH:
                if (nVersion < 3320)
                    pAntiPredictor = new AntiPredictorHigh0000To3320();
                else if (nVersion < 3600)
                    pAntiPredictor = new AntiPredictorHigh3320To3600();
                else if (nVersion < 3700)
                    pAntiPredictor = new AntiPredictorHigh3600To3700();
                else if (nVersion < 3800)
                    pAntiPredictor = new AntiPredictorHigh3700To3800();
                else
                    pAntiPredictor = new AntiPredictorHigh3800ToCurrent();
                break;

            case CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH:
                if (nVersion < 3320)
                    pAntiPredictor = new AntiPredictorExtraHigh0000To3320();
                else if (nVersion < 3600)
                    pAntiPredictor = new AntiPredictorExtraHigh3320To3600();
                else if (nVersion < 3700)
                    pAntiPredictor = new AntiPredictorExtraHigh3600To3700();
                else if (nVersion < 3800)
                    pAntiPredictor = new AntiPredictorExtraHigh3700To3800();
                else
                    pAntiPredictor = new AntiPredictorExtraHigh3800ToCurrent();
                break;
        }

        return pAntiPredictor;
    }
}
