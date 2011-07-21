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

import davaguine.jmac.tools.ByteArrayReader;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class UnBitArrayBase {

    private final static long POWERS_OF_TWO_MINUS_ONE[] = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 131071, 262143, 524287, 1048575, 2097151, 4194303, 8388607, 16777215, 33554431, 67108863, 134217727, 268435455, 536870911, 1073741823, 2147483647, 4294967295L};

    //construction/destruction
    public UnBitArrayBase() {
    }

    //functions
    public void FillBitArray() throws IOException {
        //get the bit array index
        long nBitArrayIndex = m_nCurrentBitIndex >> 5;
        long al[];
        int j;

        //move the remaining data to the front
        System.arraycopy(al = m_pBitArray, j = (int) nBitArrayIndex, al, 0, (int) (al.length - nBitArrayIndex));

        //read the new data
        ByteArrayReader reader = m_pReader;
        reader.reset(m_pIO, j << 2);
        long l1;
        int i = (int) ((l1 = m_nElements) - nBitArrayIndex);
        if ((long) i < l1)
            do {
                al[i] = reader.readUnsignedInt();
                i++;
            } while ((long) i < l1);

        //adjust the m_Bit pointer
        m_nCurrentBitIndex &= 31;
    }

    public void FillAndResetBitArray() throws IOException {
        FillAndResetBitArray(-1, 0);
    }

    public void FillAndResetBitArray(int nFileLocation) throws IOException {
        FillAndResetBitArray(nFileLocation, 0);
    }

    public void FillAndResetBitArray(int nFileLocation, int nNewBitIndex) throws IOException {
        //reset the bit index
        m_nCurrentBitIndex = nNewBitIndex;

        //seek if necessary
        if (nFileLocation != -1)
            m_pIO.seek(nFileLocation);
        else
            System.out.println(m_pIO.getFilePointer());

        //read the new data into the bit array
        ByteArrayReader reader = m_pReader;
        reader.reset(m_pIO, (int) m_nBytes);
        long al[] = m_pBitArray;
        long l = m_nElements;
        for (int i = 0; i < l; i++)
            al[i] = reader.readUnsignedInt();
    }

    public void GenerateArray(int[] pOutputArray, int nElements) throws IOException {
        GenerateArray(pOutputArray, nElements, -1);
    }

    public void GenerateArray(int[] pOutputArray, int nElements, int nBytesRequired) throws IOException {
    }

    public long DecodeValue(int DecodeMethod) throws IOException {
        return DecodeValue(DecodeMethod, 0, 0);
    }

    public long DecodeValue(int DecodeMethod, int nParam1) throws IOException {
        return DecodeValue(DecodeMethod, nParam1, 0);
    }

    public long DecodeValue(int DecodeMethod, int nParam1, int nParam2) throws IOException {
        return 0;
    }

    public void AdvanceToByteBoundary() {
        long nMod = m_nCurrentBitIndex % 8L;
        if (nMod != 0)
            m_nCurrentBitIndex += 8L - nMod;
    }

    public int DecodeValueRange(UnBitArrayState BitArrayState) throws IOException {
        return 0;
    }

    public void FlushState(UnBitArrayState BitArrayState) {
    }

    public void FlushBitArray() {
    }

    public void Finalize() {
    }

    protected void CreateHelper(File pIO, int nBytes, int nVersion) {
        //check the parameters
        if ((pIO == null) || (nBytes <= 0))
            throw new JMACException("Bad Parameter");

        //save the size
        m_nElements = nBytes / 4;
        m_nBytes = m_nElements * 4;
        m_nBits = m_nBytes * 8;

        //set the variables
        m_pIO = pIO;
        m_nVersion = nVersion;
        m_nCurrentBitIndex = 0;

        //create the bitarray
        m_pBitArray = new long[(int) m_nElements];
        m_pReader = new ByteArrayReader((int) m_nBytes);
    }

    protected long DecodeValueXBits(long nBits) throws IOException {
        //get more data if necessary
        long nBitArrayIndex;
        if (((nBitArrayIndex = m_nCurrentBitIndex) + nBits) >= m_nBits)
            FillBitArray();

        //variable declares
        long nLeftBits = 32 - (nBitArrayIndex & 31);
        nBitArrayIndex >>= 5;
        m_nCurrentBitIndex += nBits;

        //if their isn't an overflow to the right value, get the value and exit
        if (nLeftBits >= nBits)
            return ((long) (m_pBitArray[(int) nBitArrayIndex] & (POWERS_OF_TWO_MINUS_ONE[(int) nLeftBits]))) >> (nLeftBits - nBits);

        //must get the "split" value from left and right
        long nRightBits = nBits - nLeftBits;

        long nLeftValue = ((long) (m_pBitArray[(int) nBitArrayIndex] & POWERS_OF_TWO_MINUS_ONE[(int) nLeftBits])) << nRightBits;
        long nRightValue = (m_pBitArray[(int) nBitArrayIndex + 1] >> (32 - nRightBits));
        return (nLeftValue | nRightValue);
    }

    public static UnBitArrayBase CreateUnBitArray(IAPEDecompress pAPEDecompress, int nVersion) {
        if (nVersion >= 3900)
            return (UnBitArrayBase) new UnBitArray(pAPEDecompress.getApeInfoIoSource(), nVersion);
        else
            return (UnBitArrayBase) new UnBitArrayOld(pAPEDecompress, nVersion);
    }

    protected long m_nElements;
    protected long m_nBytes;
    protected long m_nBits;

    protected int m_nVersion;
    protected File m_pIO;

    protected long m_nCurrentBitIndex;
    protected long[] m_pBitArray;
    protected ByteArrayReader m_pReader;
}
