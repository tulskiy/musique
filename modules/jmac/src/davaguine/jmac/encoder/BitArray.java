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

import davaguine.jmac.tools.ByteArrayWriter;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;
import davaguine.jmac.tools.MD5;

import java.io.IOException;
import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 04.05.2004
 * Time: 16:41:39
 */
public class BitArray {
    private final static int BIT_ARRAY_ELEMENTS = 4096;						// the number of elements in the bit array (4 MB)
    private final static int BIT_ARRAY_BYTES = BIT_ARRAY_ELEMENTS * 4;  	// the number of bytes in the bit array
    private final static int BIT_ARRAY_BITS = BIT_ARRAY_BYTES * 8;	// the number of bits in the bit array
    private final static int MAX_ELEMENT_BITS = 128;
    private final static int REFILL_BIT_THRESHOLD = BIT_ARRAY_BITS - MAX_ELEMENT_BITS;

    private final static long CODE_BITS = 32;
    private final static long TOP_VALUE = (((long) 1) << (CODE_BITS - 1));
    private final static long SHIFT_BITS = (CODE_BITS - 9);
    private final static long BOTTOM_VALUE = (TOP_VALUE >> 8);

    private final static long[] K_SUM_MIN_BOUNDARY = {0, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L, 0, 0, 0, 0};

    private final static long[] RANGE_TOTAL = {0, 19578, 36160, 48417, 56323, 60899, 63265, 64435, 64971, 65232, 65351, 65416, 65447, 65466, 65476, 65482, 65485, 65488, 65490, 65491, 65492, 65493, 65494, 65495, 65496, 65497, 65498, 65499, 65500, 65501, 65502, 65503, 65504, 65505, 65506, 65507, 65508, 65509, 65510, 65511, 65512, 65513, 65514, 65515, 65516, 65517, 65518, 65519, 65520, 65521, 65522, 65523, 65524, 65525, 65526, 65527, 65528, 65529, 65530, 65531, 65532, 65533, 65534, 65535};
    private final static long[] RANGE_WIDTH = {19578, 16582, 12257, 7906, 4576, 2366, 1170, 536, 261, 119, 65, 31, 19, 10, 6, 3, 3, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    private final static int MODEL_ELEMENTS = 64;
    private final static int RANGE_OVERFLOW_SHIFT = 16;

    // construction / destruction
    public BitArray(File pIO) {
        // allocate memory for the bit array
        m_pBitArray = new int[BIT_ARRAY_ELEMENTS];
        Arrays.fill(m_pBitArray, 0);

        // initialize other variables
        m_nCurrentBitIndex = 0;
        m_pIO = pIO;
    }

    protected void finalize() {
        m_pBitArray = null;
    }

    private void NormalizeRangeCoder() {
        while (m_RangeCoderInfo.range <= BOTTOM_VALUE) {
            if (m_RangeCoderInfo.low < (0xFF << SHIFT_BITS)) {
                putc(m_RangeCoderInfo.buffer);
                for (; m_RangeCoderInfo.help > 0; m_RangeCoderInfo.help--) {
                    putc_nocap(0xFF);
                }
                m_RangeCoderInfo.buffer = (short) ((m_RangeCoderInfo.low >> SHIFT_BITS) & 0xff);
            } else if ((m_RangeCoderInfo.low & TOP_VALUE) > 0) {
                putc(m_RangeCoderInfo.buffer + 1);
                m_nCurrentBitIndex += (m_RangeCoderInfo.help * 8);
                m_RangeCoderInfo.help = 0;
                m_RangeCoderInfo.buffer = (short) ((m_RangeCoderInfo.low >> SHIFT_BITS) & 0xff);
            } else {
                m_RangeCoderInfo.help++;
            }

            m_RangeCoderInfo.low = (m_RangeCoderInfo.low << 8) & (TOP_VALUE - 1);
            m_RangeCoderInfo.range <<= 8;
        }
    }

    private void EncodeFast(long RANGE_WIDTH, long RANGE_TOTAL, int SHIFT) {
        NormalizeRangeCoder();
        long nTemp = m_RangeCoderInfo.range >> (SHIFT);
        m_RangeCoderInfo.range = nTemp * (RANGE_WIDTH);
        m_RangeCoderInfo.low += nTemp * (RANGE_TOTAL);
    }

    private void EncodeDirect(long VALUE, int SHIFT) {
        NormalizeRangeCoder();
        m_RangeCoderInfo.range = m_RangeCoderInfo.range >> (SHIFT);
        m_RangeCoderInfo.low += m_RangeCoderInfo.range * (VALUE);
    }

    private void putc(long VALUE) {
        m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] |= ((VALUE) & 0xFF) << (24 - (m_nCurrentBitIndex & 31));
        m_nCurrentBitIndex += 8;
    }

    private void putc_nocap(long VALUE) {
        m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] |= (VALUE) << (24 - (m_nCurrentBitIndex & 31));
        m_nCurrentBitIndex += 8;
    }

    public void checkValue(long value) {
        if (value < 0 || value > 4294967295L)
            throw new JMACException("Wrong Value: " + value);
    }

    // encoding
    public void EncodeUnsignedLong(long n) throws IOException {
        // make sure there are at least 8 bytes in the buffer
        if (m_nCurrentBitIndex > (BIT_ARRAY_BYTES - 8))
            OutputBitArray();

        // encode the value
        int nBitArrayIndex = (int) (m_nCurrentBitIndex >> 5);
        int nBitIndex = (int) (m_nCurrentBitIndex & 31);

        if (nBitIndex == 0)
            m_pBitArray[nBitArrayIndex] = (int) n;
        else {
            m_pBitArray[nBitArrayIndex] |= n >> nBitIndex;
            m_pBitArray[nBitArrayIndex + 1] = (int) (n << (32 - nBitIndex));
        }

        m_nCurrentBitIndex += 32;
    }

    public void EncodeValue(int nEncode, BitArrayState BitArrayState) throws IOException {
        // make sure there is room for the data
        // this is a little slower than ensuring a huge block to start with, but it's safer
        if (m_nCurrentBitIndex > REFILL_BIT_THRESHOLD)
            OutputBitArray();

        // convert to unsigned
        nEncode = (nEncode > 0) ? nEncode * 2 - 1 : -nEncode * 2;

        int nOriginalKSum = BitArrayState.nKSum;

        // update nKSum
        BitArrayState.nKSum += ((nEncode + 1) / 2) - ((BitArrayState.nKSum + 16) >> 5);

        // update k
        if (BitArrayState.nKSum < K_SUM_MIN_BOUNDARY[BitArrayState.k])
            BitArrayState.k--;
        else if (BitArrayState.nKSum >= K_SUM_MIN_BOUNDARY[BitArrayState.k + 1])
            BitArrayState.k++;

        // figure the pivot value
        int nPivotValue = Math.max(nOriginalKSum / 32, 1);
        int nOverflow = nEncode / nPivotValue;
        int nBase = nEncode - (nOverflow * nPivotValue);

        // store the overflow
        if (nOverflow < (MODEL_ELEMENTS - 1))
            EncodeFast(RANGE_WIDTH[nOverflow], RANGE_TOTAL[nOverflow], RANGE_OVERFLOW_SHIFT);
        else {
            // store the "special" overflow (tells that perfect k is encoded next)
            EncodeFast(RANGE_WIDTH[MODEL_ELEMENTS - 1], RANGE_TOTAL[MODEL_ELEMENTS - 1], RANGE_OVERFLOW_SHIFT);

            // code the overflow using straight bits
            EncodeDirect((nOverflow >> 16) & 0xFFFF, 16);
            EncodeDirect(nOverflow & 0xFFFF, 16);
        }

        // code the base
        {
            if (nPivotValue >= (1 << 16)) {
                int nPivotValueBits = 0;
                while ((nPivotValue >> nPivotValueBits) > 0)
                    nPivotValueBits++;
                int nSplitFactor = 1 << (nPivotValueBits - 16);

                // we know that base is smaller than pivot coming into this
                // however, after we divide both by an integer, they could be the same
                // we account by adding one to the pivot, but this hurts compression
                // by (1 / nSplitFactor) -- therefore we maximize the split factor
                // that gets one added to it

                // encode the pivot as two pieces
                int nPivotValueA = (nPivotValue / nSplitFactor) + 1;
                int nPivotValueB = nSplitFactor;

                int nBaseA = nBase / nSplitFactor;
                int nBaseB = nBase % nSplitFactor;

                {
                    NormalizeRangeCoder();
                    long nTemp = m_RangeCoderInfo.range / nPivotValueA;
                    m_RangeCoderInfo.range = nTemp;
                    m_RangeCoderInfo.low += nTemp * nBaseA;
                }

                {
                    NormalizeRangeCoder();
                    long nTemp = m_RangeCoderInfo.range / nPivotValueB;
                    m_RangeCoderInfo.range = nTemp;
                    m_RangeCoderInfo.low += nTemp * nBaseB;
                }
            } else {
                NormalizeRangeCoder();
                long nTemp = m_RangeCoderInfo.range / nPivotValue;
                m_RangeCoderInfo.range = nTemp;
                m_RangeCoderInfo.low += nTemp * nBase;
            }
        }
    }

    public void EncodeBits(long nValue, int nBits) throws IOException {
        // make sure there is room for the data
        // this is a little slower than ensuring a huge block to start with, but it's safer
        if (m_nCurrentBitIndex > REFILL_BIT_THRESHOLD)
            OutputBitArray();

        EncodeDirect(nValue, nBits);
    }

    // output (saving)
    public void OutputBitArray() throws IOException {
        OutputBitArray(false);
    }

    private ByteArrayWriter m_pWriter = new ByteArrayWriter();

    public void OutputBitArray(boolean bFinalize) throws IOException {
        // write the entire file to disk
        long nBytesToWrite = 0;

        m_pWriter.reset(m_pBitArray.length * 4);
        for (int i = 0; i < m_pBitArray.length; i++)
            m_pWriter.writeInt(m_pBitArray[i]);

        if (bFinalize) {
            nBytesToWrite = ((m_nCurrentBitIndex >> 5) * 4) + 4;

            m_MD5.Update(m_pWriter.getBytes(), (int) nBytesToWrite);

            m_pIO.write(m_pWriter.getBytes(), 0, (int) nBytesToWrite);

            // reset the bit pointer
            m_nCurrentBitIndex = 0;
        } else {
            nBytesToWrite = (m_nCurrentBitIndex >> 5) * 4;

            m_MD5.Update(m_pWriter.getBytes(), (int) nBytesToWrite);

            m_pIO.write(m_pWriter.getBytes(), 0, (int) nBytesToWrite);

            // move the last value to the front of the bit array
            m_pBitArray[0] = m_pBitArray[(int) (m_nCurrentBitIndex >> 5)];
            m_nCurrentBitIndex = (m_nCurrentBitIndex & 31);

            // zero the rest of the memory (may not need the +1 because of frame byte alignment)
            Arrays.fill(m_pBitArray, 1, (int) (Math.min(nBytesToWrite + 1, BIT_ARRAY_BYTES - 1) / 4 + 1), 0);
        }
    }

    // other functions
    public void Finalize() {
        NormalizeRangeCoder();

        long nTemp = (m_RangeCoderInfo.low >> SHIFT_BITS) + 1;

        if (nTemp > 0xFF) // we have a carry
        {
            putc(m_RangeCoderInfo.buffer + 1);
            for (; m_RangeCoderInfo.help > 0; m_RangeCoderInfo.help--)
                putc(0);
        } else  // no carry
        {
            putc(m_RangeCoderInfo.buffer);
            for (; m_RangeCoderInfo.help > 0; m_RangeCoderInfo.help--)
                putc(((char) 0xFF));
        }

        // we must output these bytes so the core can properly work at the end of the stream
        putc(nTemp & 0xFF);
        putc(0);
        putc(0);
        putc(0);
    }

    public void AdvanceToByteBoundary() {
        while ((m_nCurrentBitIndex % 8) > 0)
            m_nCurrentBitIndex++;
    }

    public long GetCurrentBitIndex() {
        return m_nCurrentBitIndex;
    }

    public void FlushState(BitArrayState BitArrayState) {
        // k and ksum
        BitArrayState.k = 10;
        BitArrayState.nKSum = (1 << BitArrayState.k) * 16;
    }

    public void FlushBitArray() {
        // advance to a byte boundary (for alignment)
        AdvanceToByteBoundary();

        // the range coder
        m_RangeCoderInfo.low = 0;  // full code range
        m_RangeCoderInfo.range = TOP_VALUE;
        m_RangeCoderInfo.buffer = 0;
        m_RangeCoderInfo.help = 0;  // no bytes to follow
    }

    public MD5 GetMD5Helper() {
        return m_MD5;
    }

    // data members
    private int[] m_pBitArray;
    private File m_pIO;
    private long m_nCurrentBitIndex;
    private RangeCoderStructCompress m_RangeCoderInfo = new RangeCoderStructCompress();
    private MD5 m_MD5 = new MD5();

}
