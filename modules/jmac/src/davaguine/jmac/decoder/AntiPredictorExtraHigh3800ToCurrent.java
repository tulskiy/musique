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
public class AntiPredictorExtraHigh3800ToCurrent extends AntiPredictor {
    private short[] bm = new short[256];
    private AntiPredictorExtraHighHelper Helper = new AntiPredictorExtraHighHelper();
    private int[] FM = new int[9];
    private int[] FP = new int[9];
    private short[] IPAdaptFactor = null;
    private short[] IPShort = null;

    public void AntiPredict(int[] pInputArray, int[] pOutputArray, int NumberOfElements, int nVersion) {
        final int nFilterStageElements = (nVersion < 3830) ? 128 : 256;
        final int nFilterStageShift = (nVersion < 3830) ? 11 : 12;
        final int nMaxElements = (nVersion < 3830) ? 134 : 262;
        final int nFirstElement = (nVersion < 3830) ? 128 : 256;
        final int nStageCShift = (nVersion < 3830) ? 10 : 11;

        //short frame handling
        if (NumberOfElements < nMaxElements) {
            System.arraycopy(pInputArray, 0, pOutputArray, 0, NumberOfElements);
            return;
        }

        //make the first five samples identical in both arrays
        System.arraycopy(pInputArray, 0, pOutputArray, 0, nFirstElement);

        //variable declares and initializations
        Arrays.fill(bm, (short) 0);
        int m2 = 64, m3 = 115, m4 = 64, m5 = 740, m6 = 0;
        int p4 = pInputArray[nFirstElement - 1];
        int p3 = (pInputArray[nFirstElement - 1] - pInputArray[nFirstElement - 2]) << 1;
        int p2 = pInputArray[nFirstElement - 1] + ((pInputArray[nFirstElement - 3] - pInputArray[nFirstElement - 2]) << 3);
        int op = nFirstElement;
        int ip = nFirstElement;
        int IPP2 = pInputArray[ip - 2];
        int p7 = 2 * pInputArray[ip - 1] - pInputArray[ip - 2];
        int opp = pOutputArray[op - 1];
        int Original;

        //undo the initial prediction stuff
        int q; // loop variable
        for (q = 1; q < nFirstElement; q++) {
            pOutputArray[q] += pOutputArray[q - 1];
        }

        //pump the primary loop
        if (IPAdaptFactor == null || IPAdaptFactor.length < NumberOfElements)
            IPAdaptFactor = new short[NumberOfElements];
        if (IPShort == null || IPShort.length < NumberOfElements)
            IPShort = new short[NumberOfElements];
        for (q = 0; q < nFirstElement; q++) {
            IPAdaptFactor[q] = (short) (((pInputArray[q] >> 30) & 2) - 1);
            IPShort[q] = (short) pInputArray[q];
        }

        Arrays.fill(FM, 0);
        Arrays.fill(FP, 0);

        for (q = nFirstElement; op < NumberOfElements; op++, ip++, q++) {
            //CPU load-balancing
            if (nVersion >= 3830) {
                int pFP = 8;
                int pFM = 8;
                int nDotProduct = 0;
                FP[0] = pInputArray[ip];

                if (FP[0] == 0) {
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM--];
                    FP[pFP--] = FP[pFP - 1];
                } else if (FP[0] > 0) {
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] += ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                } else {
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                    nDotProduct += FP[pFP] * FM[pFM];
                    FM[pFM--] -= ((FP[pFP] >> 30) & 2) - 1;
                    FP[pFP--] = FP[pFP - 1];
                }

                pInputArray[ip] -= nDotProduct >> 9;
            }

            Original = pInputArray[ip];

            IPShort[q] = (short) pInputArray[ip];
            IPAdaptFactor[q] = (short) (((pInputArray[ip] >> 30) & 2) - 1);

            pInputArray[ip] -= (Helper.ConventionalDotProduct(IPShort, q - nFirstElement, bm, 0, IPAdaptFactor, q - nFirstElement, Original, nFilterStageElements) >> nFilterStageShift);

            IPShort[q] = (short) pInputArray[ip];
            IPAdaptFactor[q] = (short) (((pInputArray[ip] >> 30) & 2) - 1);

            /////////////////////////////////////////////
            pOutputArray[op] = pInputArray[ip] + (((p2 * m2) + (p3 * m3) + (p4 * m4)) >> 11);

            if (pInputArray[ip] > 0) {
                m2 -= ((p2 >> 30) & 2) - 1;
                m3 -= ((p3 >> 28) & 8) - 4;
                m4 -= ((p4 >> 28) & 8) - 4;
            } else if (pInputArray[ip] < 0) {
                m2 += ((p2 >> 30) & 2) - 1;
                m3 += ((p3 >> 28) & 8) - 4;
                m4 += ((p4 >> 28) & 8) - 4;
            }


            p2 = pOutputArray[op] + ((IPP2 - p4) << 3);
            p3 = (pOutputArray[op] - p4) << 1;
            IPP2 = p4;
            p4 = pOutputArray[op];

            /////////////////////////////////////////////
            pOutputArray[op] += (((p7 * m5) - (opp * m6)) >> nStageCShift);

            if (p4 > 0) {
                m5 -= ((p7 >> 29) & 4) - 2;
                m6 += ((opp >> 30) & 2) - 1;
            } else if (p4 < 0) {
                m5 += ((p7 >> 29) & 4) - 2;
                m6 -= ((opp >> 30) & 2) - 1;
            }

            p7 = 2 * pOutputArray[op] - opp;
            opp = pOutputArray[op];

            /////////////////////////////////////////////
            pOutputArray[op] += ((pOutputArray[op - 1] * 31) >> 5);
        }
    }
}
