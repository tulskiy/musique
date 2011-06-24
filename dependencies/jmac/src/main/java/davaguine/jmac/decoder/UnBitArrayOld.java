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

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class UnBitArrayOld extends UnBitArrayBase {

    public final static long K_SUM_MIN_BOUNDARY_OLD[] = {0, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L, 0, 0, 0, 0, 0, 0};
    public final static long K_SUM_MAX_BOUNDARY_OLD[] = {128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L, 0, 0, 0, 0, 0, 0, 0};
    public final static long Powers_of_Two[] = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L};
    public final static long Powers_of_Two_Reversed[] = {2147483648L, 1073741824, 536870912, 268435456, 134217728, 67108864, 33554432, 16777216, 8388608, 4194304, 2097152, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1};
    public final static long Powers_of_Two_Minus_One[] = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 131071, 262143, 524287, 1048575, 2097151, 4194303, 8388607, 16777215, 33554431, 67108863, 134217727, 268435455, 536870911, 1073741823, 2147483647, 4294967295L};
    public final static long Powers_of_Two_Minus_One_Reversed[] = {4294967295L, 2147483647, 1073741823, 536870911, 268435455, 134217727, 67108863, 33554431, 16777215, 8388607, 4194303, 2097151, 1048575, 524287, 262143, 131071, 65535, 32767, 16383, 8191, 4095, 2047, 1023, 511, 255, 127, 63, 31, 15, 7, 3, 1, 0};

    public final static long K_SUM_MIN_BOUNDARY[] = {0, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L, 0, 0, 0, 0};
    public final static long K_SUM_MAX_BOUNDARY[] = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648L, 0, 0, 0, 0, 0};

    //construction/destruction
    public UnBitArrayOld(IAPEDecompress pAPEDecompress, int nVersion) {
        int nBitArrayBytes = 262144;

        //calculate the bytes
        if (nVersion <= 3880) {
            int nMaxFrameBytes = (pAPEDecompress.getApeInfoBlocksPerFrame() * 50) / 8;
            nBitArrayBytes = 65536;
            while (nBitArrayBytes < nMaxFrameBytes)
                nBitArrayBytes <<= 1;

            nBitArrayBytes = Math.max(nBitArrayBytes, 262144);
        } else if (nVersion <= 3890)
            nBitArrayBytes = 65536;

        CreateHelper(pAPEDecompress.getApeInfoIoSource(), nBitArrayBytes, nVersion);

        //set the refill threshold
        if (m_nVersion <= 3880)
            m_nRefillBitThreshold = (m_nBits - (16384 * 8));
        else
            m_nRefillBitThreshold = (m_nBits - 512);
    }

    //functions
    public void GenerateArray(int[] pOutputArray, int nElements, int nBytesRequired) throws IOException {
        if (m_nVersion < 3860)
            GenerateArrayOld(pOutputArray, nElements, nBytesRequired);
        else if (m_nVersion <= 3890)
            GenerateArrayRice(pOutputArray, nElements, nBytesRequired);
    }

    public long DecodeValue(int DecodeMethod, int nParam1, int nParam2) throws IOException {
        switch (DecodeMethod) {
            case DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_INT:
                return DecodeValueXBits(32);
            case DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_RICE:
                return DecodeValueRiceUnsigned(nParam1);
            case DecodeValueMethod.DECODE_VALUE_METHOD_X_BITS:
                return DecodeValueXBits(nParam1);
        }

        return 0;
    }

    private void GenerateArrayOld(int[] Output_Array, long Number_of_Elements, int Minimum_nCurrentBitIndex_Array_Bytes) throws IOException {
        //variable declarations
        long K_Sum;
        long q;
        long kmin, kmax;
        long k;
        long Max;
        int p1, p2;

        //fill bit array if necessary
        //could use seek information to determine what the max was...
        long Max_Bits_Needed = Number_of_Elements * 50;

        if (Minimum_nCurrentBitIndex_Array_Bytes > 0)
        //this is actually probably double what is really needed
        //we can only calculate the space needed for both arrays in multichannel
            Max_Bits_Needed = ((Minimum_nCurrentBitIndex_Array_Bytes + 4) * 8);

        if (Max_Bits_Needed > GetBitsRemaining())
            FillBitArray();

        //decode the first 5 elements (all k = 10)
        Max = (Number_of_Elements < 5) ? Number_of_Elements : 5;
        for (q = 0; q < Max; q++)
            Output_Array[(int) q] = (int) DecodeValueRiceUnsigned(10);

        //quit if that was all
        if (Number_of_Elements <= 5) {
            int tvi;
            for (int i = 0; i < Number_of_Elements; i++) {
                tvi = Output_Array[i];
                Output_Array[i] = (tvi & 1) > 0 ? (tvi >> 1) + 1 : -(tvi >> 1);
            }
            return;
        }

        //update k and K_Sum
        K_Sum = Output_Array[0] + Output_Array[1] + Output_Array[2] + Output_Array[3] + Output_Array[4];
        k = Get_K(K_Sum / 10);

        //work through the rest of the elements before the primary loop
        Max = (Number_of_Elements < 64) ? Number_of_Elements : 64;
        for (q = 5; q < Max; q++) {
            Output_Array[(int) q] = (int) DecodeValueRiceUnsigned(k);
            K_Sum += Output_Array[(int) q];
            k = Get_K(K_Sum / (q + 1) / 2);
        }

        //quit if that was all
        if (Number_of_Elements <= 64) {
            int tvi;
            for (int i = 0; i < Number_of_Elements; i++) {
                tvi = Output_Array[i];
                Output_Array[i] = (tvi & 1) > 0 ? (tvi >> 1) + 1 : -(tvi >> 1);
            }
            return;
        }

        // set all of the variables up for the primary loop
        long v, Bit_Array_Index;
        k = Get_K(K_Sum >> 7);
        kmin = K_SUM_MIN_BOUNDARY_OLD[(int) k];
        kmax = K_SUM_MAX_BOUNDARY_OLD[(int) k];

        // the primary loop
        for (p1 = 64, p2 = 0; p1 < Number_of_Elements; p1++, p2++) {
            // plug through the string of 0's (the overflow)
            long Bit_Initial = m_nCurrentBitIndex;
            while ((m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] & Powers_of_Two_Reversed[(int) (m_nCurrentBitIndex++ & 31)]) == 0) ;

            // if k = 0, your done
            if (k == 0)
                v = (m_nCurrentBitIndex - Bit_Initial - 1);
            else {
                // put the overflow value into v
                v = (m_nCurrentBitIndex - Bit_Initial - 1) << k;

                // store the bit information and incement the bit pointer by 'k'
                Bit_Array_Index = m_nCurrentBitIndex >> 5;
                long Bit_Index = m_nCurrentBitIndex & 31;
                m_nCurrentBitIndex += k;

                //figure the extra bits on the left and the left value
                int Left_Extra_Bits = (int) ((32 - k) - Bit_Index);
                long Left_Value = m_pBitArray[(int) Bit_Array_Index] & Powers_of_Two_Minus_One_Reversed[(int) Bit_Index];

                if (Left_Extra_Bits >= 0)
                    v |= (Left_Value >> Left_Extra_Bits);
                else
                    v |= (Left_Value << -Left_Extra_Bits) | (m_pBitArray[(int) (Bit_Array_Index + 1)] >> (32 + Left_Extra_Bits));
            }

            Output_Array[p1] = (int) v;
            K_Sum += Output_Array[p1] - Output_Array[p2];

            // convert *p2 to unsigned
            Output_Array[p2] = (Output_Array[p2] % 2) > 0 ? (Output_Array[p2] >> 1) + 1 : -(Output_Array[p2] >> 1);

            // adjust k if necessary
            if ((K_Sum < kmin) || (K_Sum >= kmax)) {
                if (K_Sum < kmin)
                    while (K_Sum < K_SUM_MIN_BOUNDARY_OLD[(int) (--k)]) ;
                else
                    while (K_Sum >= K_SUM_MAX_BOUNDARY_OLD[(int) (++k)]) ;

                kmax = K_SUM_MAX_BOUNDARY_OLD[(int) k];
                kmin = K_SUM_MIN_BOUNDARY_OLD[(int) k];
            }
        }

        for (; p2 < Number_of_Elements; p2++)
            Output_Array[p2] = (Output_Array[p2] & 1) > 0 ? (Output_Array[p2] >> 1) + 1 : -(Output_Array[p2] >> 1);
    }

    private void GenerateArrayRice(int[] pOutputArray, long NumberOfElements, int MinimumBitArrayBytes) throws IOException {
        /////////////////////////////////////////////////////////////////////////////
        //decode the bit array
        /////////////////////////////////////////////////////////////////////////////

        k = 10;
        K_Sum = 1024 * 16;

        if (m_nVersion <= 3880) {
            //the primary loop
            for (int i = 0; i < NumberOfElements; i++)
                pOutputArray[i] = DecodeValueNew(false);
        } else {
            //the primary loop
            for (int i = 0; i < NumberOfElements; i++)
                pOutputArray[i] = DecodeValueNew(true);
        }
    }

    private long DecodeValueRiceUnsigned(long k) throws IOException {
        //variable declares
        long v;

        //plug through the string of 0's (the overflow)
        long BitInitial = m_nCurrentBitIndex;
        while ((m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] & Powers_of_Two_Reversed[(int) (m_nCurrentBitIndex++ & 31)]) == 0) ;

        //if k = 0, your done
        if (k == 0)
            return (m_nCurrentBitIndex - BitInitial - 1);

        //put the overflow value into v
        v = (m_nCurrentBitIndex - BitInitial - 1) << k;

        return v | DecodeValueXBits(k);
    }

    //data
    private long k;
    private long K_Sum;
    private long m_nRefillBitThreshold;

    //functions
    private int DecodeValueNew(boolean bCapOverflow) throws IOException {
        //make sure there is room for the data
        //this is a little slower than ensuring a huge block to start with, but it's safer
        if (m_nCurrentBitIndex > m_nRefillBitThreshold)
            FillBitArray();

        long v;

        //plug through the string of 0's (the overflow)
        long Bit_Initial = m_nCurrentBitIndex;
        while ((m_pBitArray[(int) (m_nCurrentBitIndex >> 5)] & Powers_of_Two_Reversed[(int) (m_nCurrentBitIndex++ & 31)]) == 0) ;

        int nOverflow = (int) (m_nCurrentBitIndex - Bit_Initial - 1);

        if (bCapOverflow) {
            while (nOverflow >= 16) {
                k += 4;
                nOverflow -= 16;
            }
        }

        //if k = 0, your done
        if (k != 0) {
            //put the overflow value into v
            v = nOverflow << k;

            //store the bit information and incement the bit pointer by 'k'
            long Bit_Array_Index = m_nCurrentBitIndex >> 5;
            long Bit_Index = m_nCurrentBitIndex & 31;
            m_nCurrentBitIndex += k;

            //figure the extra bits on the left and the left value
            int Left_Extra_Bits = (int) ((32 - k) - Bit_Index);
            long Left_Value = m_pBitArray[(int) Bit_Array_Index] & Powers_of_Two_Minus_One_Reversed[(int) Bit_Index];

            if (Left_Extra_Bits >= 0)
                v |= (Left_Value >> Left_Extra_Bits);
            else
                v |= (Left_Value << -Left_Extra_Bits) | (m_pBitArray[(int) (Bit_Array_Index + 1)] >> (32 + Left_Extra_Bits));
        } else
            v = nOverflow;

        //update K_Sum
        K_Sum += v - ((K_Sum + 8) >> 4);

        //update k
        if (K_Sum < K_SUM_MIN_BOUNDARY[(int) k])
            k--;
        else if (K_Sum >= K_SUM_MAX_BOUNDARY[(int) k])
            k++;

        //convert to unsigned and save
        return (v & 1) > 0 ? (int) ((v >> 1) + 1) : -((int) (v >> 1));
    }

    private long GetBitsRemaining() {
        return (m_nElements * 32 - m_nCurrentBitIndex);
    }

    private long Get_K(long x) {
        if (x == 0) return 0;

        long k = 0;
        while (x >= Powers_of_Two[(int) (++k)]) ;

        return k;
    }
}
