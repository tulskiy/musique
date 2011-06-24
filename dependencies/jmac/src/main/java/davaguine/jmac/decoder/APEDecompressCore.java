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
import davaguine.jmac.info.SpecialFrame;
import davaguine.jmac.tools.JMACException;

import java.io.IOException;
import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APEDecompressCore {
    public APEDecompressCore(IAPEDecompress pAPEDecompress) {
        m_pAPEDecompress = pAPEDecompress;

        //initialize the bit array
        m_pUnBitArray = UnBitArrayBase.CreateUnBitArray(pAPEDecompress, pAPEDecompress.getApeInfoFileVersion());

        if (pAPEDecompress.getApeInfoFileVersion() >= 3930)
            throw new JMACException("Wrong Version");

        m_pAntiPredictorX = AntiPredictor.CreateAntiPredictor(pAPEDecompress.getApeInfoCompressionLevel(), pAPEDecompress.getApeInfoFileVersion());
        m_pAntiPredictorY = AntiPredictor.CreateAntiPredictor(pAPEDecompress.getApeInfoCompressionLevel(), pAPEDecompress.getApeInfoFileVersion());

        m_pDataX = new int[pAPEDecompress.getApeInfoBlocksPerFrame() + 16];
        m_pDataY = new int[pAPEDecompress.getApeInfoBlocksPerFrame() + 16];
        m_pTempData = new int[pAPEDecompress.getApeInfoBlocksPerFrame() + 16];

        m_nBlocksProcessed = 0;
    }

    public void GenerateDecodedArrays(int nBlocks, int nSpecialCodes, int nFrameIndex) throws IOException {
        if (m_pAPEDecompress.getApeInfoChannels() == 2) {
            if ((nSpecialCodes & SpecialFrame.SPECIAL_FRAME_LEFT_SILENCE) > 0 && (nSpecialCodes & SpecialFrame.SPECIAL_FRAME_RIGHT_SILENCE) > 0) {
                Arrays.fill(m_pDataX, 0, nBlocks, 0);
                Arrays.fill(m_pDataY, 0, nBlocks, 0);
            } else if ((nSpecialCodes & SpecialFrame.SPECIAL_FRAME_PSEUDO_STEREO) > 0) {
                GenerateDecodedArray(m_pDataX, nBlocks, nFrameIndex, m_pAntiPredictorX);
                Arrays.fill(m_pDataY, 0, nBlocks, 0);
            } else {
                GenerateDecodedArray(m_pDataX, nBlocks, nFrameIndex, m_pAntiPredictorX);
                GenerateDecodedArray(m_pDataY, nBlocks, nFrameIndex, m_pAntiPredictorY);
            }
        } else {
            if ((nSpecialCodes & SpecialFrame.SPECIAL_FRAME_LEFT_SILENCE) > 0)
                Arrays.fill(m_pDataX, 0, nBlocks, 0);
            else
                GenerateDecodedArray(m_pDataX, nBlocks, nFrameIndex, m_pAntiPredictorX);
        }
    }

    public void GenerateDecodedArray(int[] Input_Array, int Number_of_Elements, int Frame_Index, AntiPredictor pAntiPredictor) throws IOException {
        final int nFrameBytes = m_pAPEDecompress.getApeInfoFrameBytes(Frame_Index);

        //run the prediction sequence
        switch (m_pAPEDecompress.getApeInfoCompressionLevel()) {

            case CompressionLevel.COMPRESSION_LEVEL_FAST:
                if (m_pAPEDecompress.getApeInfoFileVersion() < 3320) {
                    m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                    pAntiPredictor.AntiPredict(m_pTempData, Input_Array, Number_of_Elements);
                } else {
                    m_pUnBitArray.GenerateArray(Input_Array, Number_of_Elements, nFrameBytes);
                    pAntiPredictor.AntiPredict(Input_Array, null, Number_of_Elements);
                }

                break;

            case CompressionLevel.COMPRESSION_LEVEL_NORMAL:
                {
                    //get the array from the bitstream
                    m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                    pAntiPredictor.AntiPredict(m_pTempData, Input_Array, Number_of_Elements);
                    break;
                }

            case CompressionLevel.COMPRESSION_LEVEL_HIGH:
                //get the array from the bitstream
                m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                pAntiPredictor.AntiPredict(m_pTempData, Input_Array, Number_of_Elements);
                break;

            case CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH:
                long nNumberOfCoefficients;

                if (m_pAPEDecompress.getApeInfoFileVersion() < 3320) {
                    nNumberOfCoefficients = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 4);
                    for (int z = 0; z <= nNumberOfCoefficients; z++) {
                        aryCoefficientsA[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 6);
                        aryCoefficientsB[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 6);
                    }
                    m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                    ((AntiPredictorExtraHigh0000To3320) pAntiPredictor).AntiPredict(m_pTempData, Input_Array, Number_of_Elements, (int) nNumberOfCoefficients, aryCoefficientsA, aryCoefficientsB);
                } else if (m_pAPEDecompress.getApeInfoFileVersion() < 3600) {
                    nNumberOfCoefficients = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 3);
                    for (int z = 0; z <= nNumberOfCoefficients; z++) {
                        aryCoefficientsA[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 5);
                        aryCoefficientsB[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 5);
                    }
                    m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                    ((AntiPredictorExtraHigh3320To3600) pAntiPredictor).AntiPredict(m_pTempData, Input_Array, Number_of_Elements, (int) nNumberOfCoefficients, aryCoefficientsA, aryCoefficientsB);
                } else if (m_pAPEDecompress.getApeInfoFileVersion() < 3700) {
                    nNumberOfCoefficients = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 3);
                    for (int z = 0; z <= nNumberOfCoefficients; z++) {
                        aryCoefficientsA[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 6);
                        aryCoefficientsB[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 6);
                    }
                    m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                    ((AntiPredictorExtraHigh3600To3700) pAntiPredictor).AntiPredict(m_pTempData, Input_Array, Number_of_Elements, (int) nNumberOfCoefficients, aryCoefficientsA, aryCoefficientsB);
                } else if (m_pAPEDecompress.getApeInfoFileVersion() < 3800) {
                    nNumberOfCoefficients = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 3);
                    for (int z = 0; z <= nNumberOfCoefficients; z++) {
                        aryCoefficientsA[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 6);
                        aryCoefficientsB[z] = m_pUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS, 6);
                    }
                    m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                    ((AntiPredictorExtraHigh3700To3800) pAntiPredictor).AntiPredict(m_pTempData, Input_Array, Number_of_Elements, (int) nNumberOfCoefficients, aryCoefficientsA, aryCoefficientsB);
                } else {
                    m_pUnBitArray.GenerateArray(m_pTempData, Number_of_Elements, nFrameBytes);
                    ((AntiPredictorExtraHigh3800ToCurrent) pAntiPredictor).AntiPredict(m_pTempData, Input_Array, Number_of_Elements, m_pAPEDecompress.getApeInfoFileVersion());
                }

                break;
        }
    }

    public UnBitArrayBase GetUnBitArrray() {
        return m_pUnBitArray;
    }

    private long[] aryCoefficientsA = new long[64];
    private long[] aryCoefficientsB = new long[64];

    public int[] m_pTempData;
    public int[] m_pDataX;
    public int[] m_pDataY;

    public AntiPredictor m_pAntiPredictorX;
    public AntiPredictor m_pAntiPredictorY;

    public UnBitArrayBase m_pUnBitArray;
    public UnBitArrayState m_BitArrayStateX = new UnBitArrayState();
    public UnBitArrayState m_BitArrayStateY = new UnBitArrayState();

    public IAPEDecompress m_pAPEDecompress;

    public int m_nBlocksProcessed;
}
