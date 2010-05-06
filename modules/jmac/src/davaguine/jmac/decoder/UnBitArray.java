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

import davaguine.jmac.tools.File;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class UnBitArray extends UnBitArrayBase {
    private final static long RANGE_TOTAL_1[] = {0, 14824, 28224, 39348, 47855, 53994, 58171, 60926, 62682, 63786, 64463, 64878, 65126, 65276, 65365, 65419, 65450, 65469, 65480, 65487, 65491, 65493, 65494, 65495, 65496, 65497, 65498, 65499, 65500, 65501, 65502, 65503, 65504, 65505, 65506, 65507, 65508, 65509, 65510, 65511, 65512, 65513, 65514, 65515, 65516, 65517, 65518, 65519, 65520, 65521, 65522, 65523, 65524, 65525, 65526, 65527, 65528, 65529, 65530, 65531, 65532, 65533, 65534, 65535, 65536};
    private final static long RANGE_WIDTH_1[] = {14824, 13400, 11124, 8507, 6139, 4177, 2755, 1756, 1104, 677, 415, 248, 150, 89, 54, 31, 19, 11, 7, 4, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    private final static long RANGE_TOTAL_2[] = {0, 19578, 36160, 48417, 56323, 60899, 63265, 64435, 64971, 65232, 65351, 65416, 65447, 65466, 65476, 65482, 65485, 65488, 65490, 65491, 65492, 65493, 65494, 65495, 65496, 65497, 65498, 65499, 65500, 65501, 65502, 65503, 65504, 65505, 65506, 65507, 65508, 65509, 65510, 65511, 65512, 65513, 65514, 65515, 65516, 65517, 65518, 65519, 65520, 65521, 65522, 65523, 65524, 65525, 65526, 65527, 65528, 65529, 65530, 65531, 65532, 65533, 65534, 65535, 65536};
    private final static long RANGE_WIDTH_2[] = {19578, 16582, 12257, 7906, 4576, 2366, 1170, 536, 261, 119, 65, 31, 19, 10, 6, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, };

    private final static long K_SUM_MIN_BOUNDARY[] = {0, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L, 0, 0, 0, 0};

    private final static long CODE_BITS = 32;
    private final static long TOP_VALUE = ((long) 1 << (CODE_BITS - 1));
    private final static long EXTRA_BITS = ((CODE_BITS - 2) % 8 + 1);
    private final static long BOTTOM_VALUE = (TOP_VALUE >> 8);
    private final static int RANGE_OVERFLOW_SHIFT = 16;

    private final static int MODEL_ELEMENTS = 64;

    //construction/destruction
    public UnBitArray(File pIO, int nVersion) {
        CreateHelper(pIO, 16384, nVersion);
    }

    public long DecodeValue(int DecodeMethod, int nParam1, int nParam2) throws IOException {
        if (DecodeMethod == DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_INT)
            return DecodeValueXBits(32);

        return 0;
    }

    public void GenerateArray(int[] pOutputArray, int nElements) throws IOException {
        GenerateArray(pOutputArray, nElements, -1);
    }

    public void GenerateArray(int[] pOutputArray, int nElements, int nBytesRequired) throws IOException {
        GenerateArrayRange(pOutputArray, nElements);
    }

    public int DecodeValueRange(UnBitArrayState BitArrayState) throws IOException {
        // make sure there is room for the data
        // this is a little slower than ensuring a huge block to start with, but it's safer
        if (m_nCurrentBitIndex > m_nRefillBitThreshold)
            FillBitArray();

        int nValue = 0;

        if (m_nVersion >= 3990) {
            // figure the pivot value
            int nPivotValue = Math.max(BitArrayState.nKSum / 32, 1);

            // get the overflow
            int nOverflow = 0;
            {
                // decode
                int nRangeTotal = RangeDecodeFast(RANGE_OVERFLOW_SHIFT);

                // lookup the symbol (must be a faster way than this)
                long al1[] = RANGE_TOTAL_2;
                if (nRangeTotal > 65416) {
                    int low = 12;
                    nOverflow = 64;
                    int mid = 38;
                    long midVal = al1[38];
                    do {
                        if (midVal < nRangeTotal)
                            low = mid + 1;
                        else if (midVal > nRangeTotal)
                            nOverflow = mid - 1;
                        else {
                            nOverflow = mid;
                            break;
                        }
                        mid = (low + nOverflow) >> 1;
                        midVal = al1[mid];
                    } while (low <= nOverflow);
                } else {
                    nOverflow = 1;
                    while (nRangeTotal >= al1[nOverflow])
                        nOverflow++;
                    nOverflow--;
                }

                // update
                RangeCoderStructDecompress range = m_RangeCoderInfo;
                range.low -= range.range * al1[nOverflow];
                range.range *= RANGE_WIDTH_2[nOverflow];

                // get the working k
                if (nOverflow == (MODEL_ELEMENTS - 1)) {
                    nOverflow = RangeDecodeFastWithUpdate(16);
                    nOverflow <<= 16;
                    nOverflow |= RangeDecodeFastWithUpdate(16);
                }
            }

            // get the value
            int nBase = 0;
            {
                if (nPivotValue >= (1 << 16)) {
                    int nPivotValueBits = 0;
                    while ((nPivotValue >> nPivotValueBits) > 0) {
                        nPivotValueBits++;
                    }
                    int nSplitFactor = 1 << (nPivotValueBits - 16);

                    int nPivotValueA = (nPivotValue / nSplitFactor) + 1;
                    int nPivotValueB = nSplitFactor;

                    RangeCoderStructDecompress range = m_RangeCoderInfo;
                    while (range.range <= BOTTOM_VALUE) {
                        range.buffer = (range.buffer << 8) | ((m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] >> (24 - (m_nCurrentBitIndex & 31))) & 0xFF);
                        m_nCurrentBitIndex += 8;
                        range.low = (range.low << 8) | ((range.buffer >> 1) & 0xFF);
                        range.range <<= 8;
                    }
                    range.range = range.range / nPivotValueA;
                    int nBaseA = (int) (range.low / range.range);
                    range.low -= range.range * nBaseA;

                    while (range.range <= BOTTOM_VALUE) {
                        range.buffer = (range.buffer << 8) | ((m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] >> (24 - (m_nCurrentBitIndex & 31))) & 0xFF);
                        m_nCurrentBitIndex += 8;
                        range.low = (range.low << 8) | ((range.buffer >> 1) & 0xFF);
                        range.range <<= 8;
                    }
                    range.range = range.range / nPivotValueB;
                    int nBaseB = (int) (range.low / range.range);
                    range.low -= range.range * nBaseB;

                    nBase = nBaseA * nSplitFactor + nBaseB;
                } else {
                    RangeCoderStructDecompress range = m_RangeCoderInfo;
                    while (range.range <= BOTTOM_VALUE) {
                        range.buffer = (range.buffer << 8) | ((m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] >> (24 - (m_nCurrentBitIndex & 31))) & 0xFF);
                        m_nCurrentBitIndex += 8;
                        range.low = (range.low << 8) | ((range.buffer >> 1) & 0xFF);
                        range.range <<= 8;
                    }

                    // decode
                    range.range = range.range / nPivotValue;
                    int nBaseLower = (int) (range.low / range.range);
                    range.low -= range.range * nBaseLower;

                    nBase = nBaseLower;
                }
            }

            // build the value
            nValue = nBase + (nOverflow * nPivotValue);
        } else {
            // decode
            int nRangeTotal = RangeDecodeFast(RANGE_OVERFLOW_SHIFT);

            // lookup the symbol (must be a faster way than this)
            long al1[] = RANGE_TOTAL_1;
            int nOverflow;
            if (nRangeTotal > 64878) {
                int low = 12;
                nOverflow = 64;
                int mid = 38;
                long midVal = al1[38];
                do {
                    if (midVal < nRangeTotal)
                        low = mid + 1;
                    else if (midVal > nRangeTotal)
                        nOverflow = mid - 1;
                    else {
                        nOverflow = mid;
                        break;
                    }
                    mid = (low + nOverflow) >> 1;
                    midVal = al1[mid];
                } while (low <= nOverflow);
            } else {
                nOverflow = 1;
                while (nRangeTotal >= al1[nOverflow])
                    nOverflow++;
                nOverflow--;
            }

            // update
            RangeCoderStructDecompress range = m_RangeCoderInfo;
            range.low -= range.range * al1[nOverflow];
            range.range *= RANGE_WIDTH_1[nOverflow];

            // get the working k
            int nTempK;
            if (nOverflow == (MODEL_ELEMENTS - 1)) {
                nTempK = RangeDecodeFastWithUpdate(5);
                nOverflow = 0;
            } else
                nTempK = (BitArrayState.k < 1) ? 0 : BitArrayState.k - 1;

            // figure the extra bits on the left and the left value
            if (nTempK <= 16 || m_nVersion < 3910)
                nValue = RangeDecodeFastWithUpdate(nTempK);
            else {
                int nX1 = RangeDecodeFastWithUpdate(16);
                int nX2 = RangeDecodeFastWithUpdate(nTempK - 16);
                nValue = nX1 | (nX2 << 16);
            }

            // build the value and output it
            nValue += (nOverflow << nTempK);
        }

        // update nKSum
        BitArrayState.nKSum += ((nValue + 1) / 2) - ((BitArrayState.nKSum + 16) >> 5);

        // update k
        if (BitArrayState.nKSum < K_SUM_MIN_BOUNDARY[BitArrayState.k])
            BitArrayState.k--;
        else if (BitArrayState.nKSum >= K_SUM_MIN_BOUNDARY[BitArrayState.k + 1])
            BitArrayState.k++;

        // output the value (converted to signed)
        return (nValue & 1) > 0 ? (nValue >> 1) + 1 : -(nValue >> 1);
    }

    public void FlushState(UnBitArrayState BitArrayState) {
        BitArrayState.k = 10;
        BitArrayState.nKSum = (1 << BitArrayState.k) * 16;
    }

    public void FlushBitArray() {
        AdvanceToByteBoundary();
        RangeCoderStructDecompress struct = m_RangeCoderInfo;
        m_nCurrentBitIndex += 8; // ignore the first byte... (slows compression too much to not output this dummy byte)
        struct.buffer = GetC();
        struct.low = struct.buffer >> (8 - EXTRA_BITS);
        struct.range = (long) 1 << EXTRA_BITS;

        m_nRefillBitThreshold = (m_nBits - 512);
    }

    public void Finalize() {
        RangeCoderStructDecompress struct = m_RangeCoderInfo;
        long i = m_nCurrentBitIndex;
        long range = struct.range;
        // normalize
        while (range <= BOTTOM_VALUE) {
            i += 8;
            range <<= 8;
        }

        // used to back-pedal the last two bytes out
        // this should never have been a problem because we've outputted and normalized beforehand
        // but stopped doing it as of 3.96 in case it accounted for rare decompression failures
        if (m_nVersion <= 3950)
            i -= 16;
        m_nCurrentBitIndex = i;
        struct.range = range;
    }

    private UnBitArrayState GenerateArrayRangeBitArrayState = new UnBitArrayState();

    private void GenerateArrayRange(int[] pOutputArray, int nElements) throws IOException {
        FlushState(GenerateArrayRangeBitArrayState);
        FlushBitArray();

        for (int z = 0; z < nElements; z++)
            pOutputArray[z] = DecodeValueRange(GenerateArrayRangeBitArrayState);

        Finalize();
    }

    private RangeCoderStructDecompress m_RangeCoderInfo = new RangeCoderStructDecompress();

    private long m_nRefillBitThreshold;

    //functions
    private final int RangeDecodeFast(int nShift) {
        RangeCoderStructDecompress struct = m_RangeCoderInfo;
        long a1[] = m_pBitArray;
        long i = m_nCurrentBitIndex;
        long buffer = struct.buffer;
        long low = struct.low;
        long range = struct.range;
        while (range <= BOTTOM_VALUE) {
            buffer = (buffer << 8) | ((a1[(int) (i >> 5)] >> (24 - (i & 31))) & 0xFF);
            i += 8;
            low = (low << 8) | ((buffer >> 1) & 0xFF);
            range <<= 8;
        }
        m_nCurrentBitIndex = i;
        struct.low = low;
        struct.buffer = buffer;

        // decode
        range >>= nShift;
        struct.range = range;
        return (int) (low / range);
    }

    private final int RangeDecodeFastWithUpdate(int nShift) {
        RangeCoderStructDecompress struct = m_RangeCoderInfo;
        long a1[] = m_pBitArray;
        long i = m_nCurrentBitIndex;
        long buffer = struct.buffer;
        long low = struct.low;
        long range = struct.range;
        while (range <= BOTTOM_VALUE) {
            buffer = (buffer << 8L) | ((a1[(int) (i >> 5)] >> (24 - (i & 31))) & 0xFF);
            i += 8;
            low = (low << 8) | ((buffer >> 1) & 0xFF);
            range <<= 8;
        }
        m_nCurrentBitIndex = i;

        // decode
        range >>= nShift;
        int nRetVal = (int) (low / range);
        low -= range * nRetVal;
        struct.range = range;
        struct.low = low;
        struct.buffer = buffer;
        return nRetVal;
    }

    private final short GetC() {
        long l = m_nCurrentBitIndex;
        short nValue = (short) (m_pBitArray[(int) (l >> 5)] >> (24 - (l & 31)));
        m_nCurrentBitIndex = l + 8;
        return nValue;
    }
}
