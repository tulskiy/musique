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
import davaguine.jmac.tools.Globals;
import davaguine.jmac.tools.JMACException;
import davaguine.jmac.tools.RollBufferFastInt;
import davaguine.jmac.tools.ScaledFirstOrderFilter;

import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 02.05.2004
 * Time: 13:08:34
 */
public class PredictorCompressNormal extends IPredictorCompress {
    private final static int WINDOW_BLOCKS = 512;

    public PredictorCompressNormal(int nCompressionLevel) {
        super(nCompressionLevel);
        if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_FAST) {
            m_pNNFilter = null;
            m_pNNFilter1 = null;
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_NORMAL) {
            m_pNNFilter = new NNFilter16(11, Globals.MAC_VERSION_NUMBER);
            m_pNNFilter1 = null;
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_HIGH) {
            m_pNNFilter = new NNFilter64(11, Globals.MAC_VERSION_NUMBER);
            m_pNNFilter1 = null;
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH) {
            m_pNNFilter = new NNFilter256(13, Globals.MAC_VERSION_NUMBER);
            m_pNNFilter1 = new NNFilter32(10, Globals.MAC_VERSION_NUMBER);
            m_pNNFilter2 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_INSANE) {
            m_pNNFilter = new NNFilter1280(15, Globals.MAC_VERSION_NUMBER);
            m_pNNFilter1 = new NNFilter256(13, Globals.MAC_VERSION_NUMBER);
            m_pNNFilter2 = new NNFilter16(11, Globals.MAC_VERSION_NUMBER);
        } else {
            throw new JMACException("Unknown Compression Type");
        }
    }

    public int CompressValue(int nA, int nB) {
        // roll the buffers if necessary
        if (m_nCurrentIndex == WINDOW_BLOCKS) {
            m_rbPrediction.Roll();
            m_rbAdapt.Roll();
            m_nCurrentIndex = 0;
        }

        // stage 1: simple, non-adaptive order 1 prediction
        nA = m_Stage1FilterA.Compress(nA);
        nB = m_Stage1FilterB.Compress(nB);

        // stage 2: adaptive offset filter(s)
        int predictIndex = m_rbPrediction.index;
        m_rbPrediction.m_pData[predictIndex] = nA;
        m_rbPrediction.m_pData[predictIndex - 2] = m_rbPrediction.m_pData[predictIndex - 1] - m_rbPrediction.m_pData[predictIndex - 2];

        m_rbPrediction.m_pData[predictIndex - 5] = nB;
        m_rbPrediction.m_pData[predictIndex - 6] = m_rbPrediction.m_pData[predictIndex - 5] - m_rbPrediction.m_pData[predictIndex - 6];

        int nPredictionA = (m_rbPrediction.m_pData[predictIndex - 1] * m_aryM[8]) + (m_rbPrediction.m_pData[predictIndex - 2] * m_aryM[7]) + (m_rbPrediction.m_pData[predictIndex - 3] * m_aryM[6]) + (m_rbPrediction.m_pData[predictIndex - 4] * m_aryM[5]);
        int nPredictionB = (m_rbPrediction.m_pData[predictIndex - 5] * m_aryM[4]) + (m_rbPrediction.m_pData[predictIndex - 6] * m_aryM[3]) + (m_rbPrediction.m_pData[predictIndex - 7] * m_aryM[2]) + (m_rbPrediction.m_pData[predictIndex - 8] * m_aryM[1]) + (m_rbPrediction.m_pData[predictIndex - 9] * m_aryM[0]);

        int nOutput = nA - ((nPredictionA + (nPredictionB >> 1)) >> 10);

        // adapt
        int adaptIndex = m_rbAdapt.index;
        m_rbAdapt.m_pData[adaptIndex] = (m_rbPrediction.m_pData[predictIndex - 1] != 0) ? ((m_rbPrediction.m_pData[predictIndex - 1] >> 30) & 2) - 1 : 0;
        m_rbAdapt.m_pData[adaptIndex - 1] = (m_rbPrediction.m_pData[predictIndex - 2] != 0) ? ((m_rbPrediction.m_pData[predictIndex - 2] >> 30) & 2) - 1 : 0;
        m_rbAdapt.m_pData[adaptIndex - 4] = (m_rbPrediction.m_pData[predictIndex - 5] != 0) ? ((m_rbPrediction.m_pData[predictIndex - 5] >> 30) & 2) - 1 : 0;
        m_rbAdapt.m_pData[adaptIndex - 5] = (m_rbPrediction.m_pData[predictIndex - 6] != 0) ? ((m_rbPrediction.m_pData[predictIndex - 6] >> 30) & 2) - 1 : 0;

        if (nOutput > 0) {
            int indexM = 0;
            int indexA = adaptIndex - 8;
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] -= m_rbAdapt.m_pData[indexA++];
        } else if (nOutput < 0) {
            int indexM = 0;
            int indexA = adaptIndex - 8;
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
            m_aryM[indexM++] += m_rbAdapt.m_pData[indexA++];
        }

        // stage 3: NNFilters
        if (m_pNNFilter != null) {
            nOutput = m_pNNFilter.Compress(nOutput);

            if (m_pNNFilter1 != null) {
                nOutput = m_pNNFilter1.Compress(nOutput);

                if (m_pNNFilter2 != null)
                    nOutput = m_pNNFilter2.Compress(nOutput);
            }
        }

        m_rbPrediction.index++;
        m_rbAdapt.index++;
        m_nCurrentIndex++;

        return nOutput;
    }

    public void Flush() {
        if (m_pNNFilter != null) m_pNNFilter.Flush();
        if (m_pNNFilter1 != null) m_pNNFilter1.Flush();
        if (m_pNNFilter2 != null) m_pNNFilter2.Flush();

        m_rbPrediction.Flush();
        m_rbAdapt.Flush();
        m_Stage1FilterA.Flush();
        m_Stage1FilterB.Flush();

        Arrays.fill(m_aryM, 0);

        m_aryM[8] = 360;
        m_aryM[7] = 317;
        m_aryM[6] = -109;
        m_aryM[5] = 98;

        m_nCurrentIndex = 0;
    }

    // buffer information
    protected RollBufferFastInt m_rbPrediction = new RollBufferFastInt(WINDOW_BLOCKS, 10);
    protected RollBufferFastInt m_rbAdapt = new RollBufferFastInt(WINDOW_BLOCKS, 9);

    protected ScaledFirstOrderFilter m_Stage1FilterA = new ScaledFirstOrderFilter(31, 5);
    protected ScaledFirstOrderFilter m_Stage1FilterB = new ScaledFirstOrderFilter(31, 5);

    // adaption
    protected int[] m_aryM = new int[9];

    // other
    protected int m_nCurrentIndex;
    protected NNFilter m_pNNFilter;
    protected NNFilter m_pNNFilter1;
    protected NNFilter m_pNNFilter2;
}
