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
public class AntiPredictorExtraHighHelper {
    int ConventionalDotProduct(short[] bip, int indexbip, short[] bbm, int indexbbm, short[] pIPAdaptFactor, int indexap, int op, int nNumberOfIterations) {
        //dot product
        int nDotProduct = 0;
        int pMaxBBM = nNumberOfIterations;

        if (op == 0) {
            int i = indexbbm;
            int j = indexbip;
            while (i < (pMaxBBM + indexbbm)) {
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
                nDotProduct += bip[j++] * bbm[i++];
            }
        } else if (op > 0) {
            int i = indexbbm;
            int j = indexbip;
            int k = indexap;
            while (i < (pMaxBBM + indexbbm)) {
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] += pIPAdaptFactor[k++];
            }
        } else {
            int i = indexbbm;
            int j = indexbip;
            int k = indexap;
            while (i < (pMaxBBM + indexbbm)) {
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
                nDotProduct += bip[j++] * bbm[i];
                bbm[i++] -= pIPAdaptFactor[k++];
            }
        }

        //use the dot product
        return nDotProduct;
    }
}
