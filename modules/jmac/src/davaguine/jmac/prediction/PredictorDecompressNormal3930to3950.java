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

import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class PredictorDecompressNormal3930to3950 extends IPredictorDecompress {
    private final static int HISTORY_ELEMENTS = 8;
    private final static int WINDOW_BLOCKS = 512;

    private final static int BUFFER_COUNT = 1;
    private final static int M_COUNT = 8;

    public PredictorDecompressNormal3930to3950(int nCompressionLevel, int nVersion) {
        super(nCompressionLevel, nVersion);
        m_pBuffer[0] = new int[HISTORY_ELEMENTS + WINDOW_BLOCKS];

        if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_FAST) {
            m_pNNFilter = null;
            m_pNNFilter1 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_NORMAL) {
            m_pNNFilter = new NNFilter16(11, nVersion);
            m_pNNFilter1 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_HIGH) {
            m_pNNFilter = new NNFilter64(11, nVersion);
            m_pNNFilter1 = null;
        } else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH) {
            m_pNNFilter = new NNFilter256(13, nVersion);
            m_pNNFilter1 = new NNFilter32(10, nVersion);
        } else {
            throw new JMACException("Unknown Compression Type");
        }
    }

    public int DecompressValue(int nInput, int notneeded) {
        if (m_nCurrentIndex == WINDOW_BLOCKS) {
            // copy forward and adjust pointers
            System.arraycopy(m_pBuffer[0], WINDOW_BLOCKS, m_pBuffer[0], 0, HISTORY_ELEMENTS);
            m_pInputBuffer_i = 0;
            m_pInputBuffer_j = HISTORY_ELEMENTS;

            m_nCurrentIndex = 0;
        }

        // stage 2: NNFilter
        if (m_pNNFilter1 != null)
            nInput = m_pNNFilter1.Decompress(nInput);
        if (m_pNNFilter != null)
            nInput = m_pNNFilter.Decompress(nInput);

        // stage 1: multiple predictors (order 2 and offset 1)

        int p1 = m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j - 1];
        int p2 = m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j - 1] - m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j - 2];
        int p3 = m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j - 2] - m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j - 3];
        int p4 = m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j - 3] - m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j - 4];

        m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j] = nInput + (((p1 * m_aryM[0]) + (p2 * m_aryM[1]) + (p3 * m_aryM[2]) + (p4 * m_aryM[3])) >> 9);

        if (nInput > 0) {
            m_aryM[0] -= ((p1 >> 30) & 2) - 1;
            m_aryM[1] -= ((p2 >> 30) & 2) - 1;
            m_aryM[2] -= ((p3 >> 30) & 2) - 1;
            m_aryM[3] -= ((p4 >> 30) & 2) - 1;
        } else if (nInput < 0) {
            m_aryM[0] += ((p1 >> 30) & 2) - 1;
            m_aryM[1] += ((p2 >> 30) & 2) - 1;
            m_aryM[2] += ((p3 >> 30) & 2) - 1;
            m_aryM[3] += ((p4 >> 30) & 2) - 1;
        }

        int nRetVal = m_pBuffer[m_pInputBuffer_i][m_pInputBuffer_j] + ((m_nLastValue * 31) >> 5);
        m_nLastValue = nRetVal;

        m_nCurrentIndex++;
        m_pInputBuffer_j++;

        return nRetVal;
    }

    public void Flush() {
        if (m_pNNFilter != null) m_pNNFilter.Flush();
        if (m_pNNFilter1 != null) m_pNNFilter1.Flush();

        Arrays.fill(m_pBuffer[0], 0, HISTORY_ELEMENTS, 0);
        Arrays.fill(m_aryM, 0);

        m_aryM[0] = 360;
        m_aryM[1] = 317;
        m_aryM[2] = -109;
        m_aryM[3] = 98;

        m_pInputBuffer_i = 0;
        m_pInputBuffer_j = HISTORY_ELEMENTS;

        m_nLastValue = 0;
        m_nCurrentIndex = 0;
    }

    // buffer information
    protected int[][] m_pBuffer = new int[BUFFER_COUNT][];

    // adaption
    protected int[] m_aryM = new int[M_COUNT];

    // buffer pointers
    protected int m_pInputBuffer_i;
    protected int m_pInputBuffer_j;

    // other
    protected int m_nCurrentIndex;
    protected int m_nLastValue;
    protected NNFilter m_pNNFilter;
    protected NNFilter m_pNNFilter1;
}
