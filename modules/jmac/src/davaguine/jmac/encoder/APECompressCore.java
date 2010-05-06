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
package davaguine.jmac.encoder;

import davaguine.jmac.info.SpecialFrame;
import davaguine.jmac.info.WaveFormat;
import davaguine.jmac.prediction.IPredictorCompress;
import davaguine.jmac.prediction.PredictorCompressNormal;
import davaguine.jmac.tools.*;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 08.05.2004
 * Time: 11:18:47
 */
public class APECompressCore {
    public APECompressCore(File pIO, WaveFormat pwfeInput, int nMaxFrameBlocks, int nCompressionLevel) {
        m_spBitArray = new BitArray(pIO);
        m_spDataX = new int[nMaxFrameBlocks];
        m_spDataY = new int[nMaxFrameBlocks];
        m_spPrepare = new Prepare();
        m_spPredictorX = new PredictorCompressNormal(nCompressionLevel);
        m_spPredictorY = new PredictorCompressNormal(nCompressionLevel);

        m_wfeInput = pwfeInput;
        m_nPeakLevel.value = 0;
    }

    private IntegerPointer m_nSpecialCodes = new IntegerPointer();

    public void EncodeFrame(ByteArrayReader pInputData, int nInputBytes) throws IOException {
        // variables
        int nInputBlocks = nInputBytes / m_wfeInput.nBlockAlign;
        m_nSpecialCodes.value = 0;

        // always start a new frame on a byte boundary
        m_spBitArray.AdvanceToByteBoundary();

        // do the preparation stage
        Prepare(pInputData, nInputBytes, m_nSpecialCodes);

        m_spPredictorX.Flush();
        m_spPredictorY.Flush();

        m_spBitArray.FlushState(m_BitArrayStateX);
        m_spBitArray.FlushState(m_BitArrayStateY);

        m_spBitArray.FlushBitArray();

        if (m_wfeInput.nChannels == 2) {
            boolean bEncodeX = true;
            boolean bEncodeY = true;

            if ((m_nSpecialCodes.value & SpecialFrame.SPECIAL_FRAME_LEFT_SILENCE) > 0 &&
                    (m_nSpecialCodes.value & SpecialFrame.SPECIAL_FRAME_RIGHT_SILENCE) > 0) {
                bEncodeX = false;
                bEncodeY = false;
            }

            if ((m_nSpecialCodes.value & SpecialFrame.SPECIAL_FRAME_PSEUDO_STEREO) > 0) {
                bEncodeY = false;
            }

            if (bEncodeX && bEncodeY) {
                int nLastX = 0;
                for (int z = 0; z < nInputBlocks; z++) {
                    m_spBitArray.EncodeValue(m_spPredictorY.CompressValue(m_spDataY[z], nLastX), m_BitArrayStateY);
                    m_spBitArray.EncodeValue(m_spPredictorX.CompressValue(m_spDataX[z], m_spDataY[z]), m_BitArrayStateX);

                    nLastX = m_spDataX[z];
                }
            } else if (bEncodeX) {
                for (int z = 0; z < nInputBlocks; z++) {
                    m_spBitArray.EncodeValue(m_spPredictorX.CompressValue(m_spDataX[z]), m_BitArrayStateX);
                }
            } else if (bEncodeY) {
                for (int z = 0; z < nInputBlocks; z++) {
                    m_spBitArray.EncodeValue(m_spPredictorY.CompressValue(m_spDataY[z]), m_BitArrayStateY);
                }
            }
        } else if (m_wfeInput.nChannels == 1) {
            if ((m_nSpecialCodes.value & SpecialFrame.SPECIAL_FRAME_MONO_SILENCE) <= 0) {
                for (int z = 0; z < nInputBlocks; z++) {
                    m_spBitArray.EncodeValue(m_spPredictorX.CompressValue(m_spDataX[z]), m_BitArrayStateX);
                }
            }
        }

        m_spBitArray.Finalize();
    }

    public BitArray GetBitArray() {
        return m_spBitArray;
    }

    public int GetPeakLevel() {
        return m_nPeakLevel.value;
    }

    private BitArray m_spBitArray;
    private IPredictorCompress m_spPredictorX;
    private IPredictorCompress m_spPredictorY;

    private BitArrayState m_BitArrayStateX = new BitArrayState();
    private BitArrayState m_BitArrayStateY = new BitArrayState();

    private int[] m_spDataX;
    private int[] m_spDataY;

    private Prepare m_spPrepare;

    private WaveFormat m_wfeInput;

    private IntegerPointer m_nPeakLevel = new IntegerPointer();

    private Crc32 crc = new Crc32();

    private void Prepare(ByteArrayReader pInputData, int nInputBytes, IntegerPointer pSpecialCodes) throws IOException {
        // variable declares
        pSpecialCodes.value = 0;

        // do the preparation
        m_spPrepare.prepare(pInputData, nInputBytes, m_wfeInput, m_spDataX, m_spDataY,
                crc, pSpecialCodes, m_nPeakLevel);

        // store the CRC
        m_spBitArray.EncodeUnsignedLong(crc.getCrc());

        // store any special codes
        if (pSpecialCodes.value != 0)
            m_spBitArray.EncodeUnsignedLong(pSpecialCodes.value);
    }
}
