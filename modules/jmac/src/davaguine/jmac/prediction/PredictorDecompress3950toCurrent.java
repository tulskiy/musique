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

package davaguine.jmac.prediction;

import davaguine.jmac.info.CompressionLevel;
import davaguine.jmac.tools.JMACException;
import davaguine.jmac.tools.RollBufferFastInt;

import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class PredictorDecompress3950toCurrent extends IPredictorDecompress {
    public final static int M_COUNT = 8;
    private final static int WINDOW_BLOCKS = 512;

    public PredictorDecompress3950toCurrent(int nCompressionLevel, int nVersion) {
        super(nCompressionLevel, nVersion);
        m_nVersion = nVersion;

        if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_FAST) {
            m_pNNFilter = null;
            m_pNNFilter1 = null;
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_NORMAL) {
            m_pNNFilter = new NNFilter16(11, nVersion);
            m_pNNFilter1 = null;
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_HIGH) {
            m_pNNFilter = new NNFilter64(11, nVersion);
            m_pNNFilter1 = null;
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH) {
            m_pNNFilter = new NNFilter256(13, nVersion);
            m_pNNFilter1 = new NNFilter32(10, nVersion);
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_INSANE) {
            m_pNNFilter = new NNFilter1280(15, nVersion);
            m_pNNFilter1 = new NNFilter256(13, nVersion);
            m_pNNFilter2 = new NNFilter16(11, nVersion);
        } else {
            throw new JMACException("Unknown Compression Type");
        }
    }

    public int DecompressValue(int nA) {
        return DecompressValue(nA, 0);
    }

    public int DecompressValue(int nA, int nB) {
        if (m_nCurrentIndex == WINDOW_BLOCKS) {
            // copy forward and adjust pointers
            m_rbPredictionA.Roll();
            m_rbPredictionB.Roll();
            m_rbAdaptA.Roll();
            m_rbAdaptB.Roll();

            m_nCurrentIndex = 0;
        }

        // stage 2: NNFilter
        Object obj;
        if ((obj = m_pNNFilter2) != null)
            nA = ((NNFilter) (obj)).Decompress(nA);
        if ((obj = m_pNNFilter1) != null)
            nA = ((NNFilter) (obj)).Decompress(nA);
        if ((obj = m_pNNFilter) != null)
            nA = ((NNFilter) (obj)).Decompress(nA);

        // stage 1: multiple predictors (order 2 and offset 1)
        int indexA = ((RollBufferFastInt) (obj = m_rbPredictionA)).index;
        RollBufferFastInt predictB;
        int indexB = (predictB = m_rbPredictionB).index;
        int ai[];
        int l;
        (ai = ((RollBufferFastInt) (obj)).m_pData)[indexA] = l = m_nLastValueA;
        int l1 = indexA - 1;
        ai[l1] = l - ai[l1];

        int ai3[] = predictB.m_pData;

        ai3[indexB] = nB - ((scaledFilterBLV * 31) >> 5);
        scaledFilterBLV = nB;

//        ai3[indexB] = m_Stage1FilterB.Compress(nB);
        int k2 = indexB - 1;
        ai3[k2] = ai3[indexB] - ai3[k2];

        int ai2[];
        int ai4[];
        int nPredictionA = (l * (ai2 = m_aryMA)[0]) + (ai[l1] * ai2[1]) + (ai[indexA - 2] * ai2[2]) + (ai[indexA - 3] * ai2[3]);
        int nPredictionB = (ai3[indexB] * (ai4 = m_aryMB)[0]) + (ai3[k2] * ai4[1]) + (ai3[indexB - 2] * ai4[2]) + (ai3[indexB - 3] * ai4[3]) + (ai3[indexB - 4] * ai4[4]);

        int nCurrentA = nA + ((nPredictionA + (nPredictionB >> 1)) >> 10);

        RollBufferFastInt adaptA;
        RollBufferFastInt adaptB;
        int indexAA = (adaptA = m_rbAdaptA).index;
        int indexAB = (adaptB = m_rbAdaptB).index;
        int ai1[];
        (ai1 = m_rbAdaptA.m_pData)[indexAA] = (l != 0) ? ((l >> 30) & 2) - 1 : 0;
        ai1[indexAA - 1] = (ai[l1] != 0) ? ((ai[l1] >> 30) & 2) - 1 : 0;

        int ai5[];
        (ai5 = m_rbAdaptB.m_pData)[indexAB] = (ai3[indexB] != 0) ? ((ai3[indexB] >> 30) & 2) - 1 : 0;
        ai5[indexAB - 1] = (ai3[k2] != 0) ? ((ai3[k2] >> 30) & 2) - 1 : 0;

        if (nA > 0) {
            ai2[0] -= ai1[indexAA];
            ai2[1] -= ai1[indexAA - 1];
            ai2[2] -= ai1[indexAA - 2];
            ai2[3] -= ai1[indexAA - 3];

            ai4[0] -= ai5[indexAB];
            ai4[1] -= ai5[indexAB - 1];
            ai4[2] -= ai5[indexAB - 2];
            ai4[3] -= ai5[indexAB - 3];
            ai4[4] -= ai5[indexAB - 4];
        } else if (nA < 0) {
            ai2[0] += ai1[indexAA];
            ai2[1] += ai1[indexAA - 1];
            ai2[2] += ai1[indexAA - 2];
            ai2[3] += ai1[indexAA - 3];

            ai4[0] += ai5[indexAB];
            ai4[1] += ai5[indexAB - 1];
            ai4[2] += ai5[indexAB - 2];
            ai4[3] += ai5[indexAB - 3];
            ai4[4] += ai5[indexAB - 4];
        }

//        int nRetVal = m_Stage1FilterA.Decompress(nCurrentA);
        scaledFilterALV = nCurrentA + ((scaledFilterALV * 31) >> 5);
        m_nLastValueA = nCurrentA;

        ((RollBufferFastInt) (obj)).index++;
        predictB.index++;
        adaptA.index++;
        adaptB.index++;

        m_nCurrentIndex++;

        return scaledFilterALV;
    }

    public void Flush() {
        NNFilter nnfilter;
        if ((nnfilter = m_pNNFilter) != null)
            nnfilter.Flush();
        if ((nnfilter = m_pNNFilter1) != null)
            nnfilter.Flush();
        if ((nnfilter = m_pNNFilter2) != null)
            nnfilter.Flush();

        Arrays.fill(m_aryMA, 0);
        Arrays.fill(m_aryMB, 0);

        m_rbPredictionA.Flush();
        m_rbPredictionB.Flush();
        m_rbAdaptA.Flush();
        m_rbAdaptB.Flush();

        int ai[];
        (ai = m_aryMA)[0] = 360;
        ai[1] = 317;
        ai[2] = -109;
        ai[3] = 98;

//        m_Stage1FilterA.Flush();
        scaledFilterALV = 0;
//        m_Stage1FilterB.Flush();
        scaledFilterBLV = 0;

        m_nLastValueA = 0;

        m_nCurrentIndex = 0;
    }

    // adaption
    protected int m_aryMA[] = new int[M_COUNT];
    protected int m_aryMB[] = new int[M_COUNT];

    // buffer pointers
    protected RollBufferFastInt m_rbPredictionA = new RollBufferFastInt(WINDOW_BLOCKS, 8);
    protected RollBufferFastInt m_rbPredictionB = new RollBufferFastInt(WINDOW_BLOCKS, 8);

    protected RollBufferFastInt m_rbAdaptA = new RollBufferFastInt(WINDOW_BLOCKS, 8);
    protected RollBufferFastInt m_rbAdaptB = new RollBufferFastInt(WINDOW_BLOCKS, 8);

//    protected ScaledFirstOrderFilter m_Stage1FilterA = new ScaledFirstOrderFilter(31, 5);
    protected int scaledFilterALV;
//    protected ScaledFirstOrderFilter m_Stage1FilterB = new ScaledFirstOrderFilter(31, 5);
    protected int scaledFilterBLV;

    // other
    protected int m_nCurrentIndex;
    protected int m_nLastValueA;
    protected int m_nVersion;
    protected NNFilter m_pNNFilter;
    protected NNFilter m_pNNFilter1;
    protected NNFilter m_pNNFilter2;
}
