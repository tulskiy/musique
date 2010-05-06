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

import davaguine.jmac.info.APEHeader;
import davaguine.jmac.info.SpecialFrame;
import davaguine.jmac.info.WaveFormat;
import davaguine.jmac.tools.ByteBuffer;
import davaguine.jmac.tools.Crc32;
import davaguine.jmac.tools.JMACException;
import davaguine.jmac.tools.Prepare;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class UnMAC {
    //construction/destruction
    public UnMAC() {
        //initialize member variables
        m_bInitialized = false;
        m_nRealFrame = 0;
        m_LastDecodedFrameIndex = -1;
        m_pAPEDecompress = null;

        m_pAPEDecompressCore = null;
        m_pPrepare = null;

        m_nBlocksProcessed = 0;
    }

    //functions
    public void Initialize(IAPEDecompress pAPEDecompress) {
        //uninitialize if it is currently initialized
        if (m_bInitialized)
            Uninitialize();

        if (pAPEDecompress == null) {
            Uninitialize();
            throw new JMACException("Error Initializing UnMAC");
        }

        //set the member pointer to the IAPEDecompress class
        m_pAPEDecompress = pAPEDecompress;

        //set the last decode frame to -1 so it forces a seek on start
        m_LastDecodedFrameIndex = -1;

        m_pAPEDecompressCore = new APEDecompressCore(pAPEDecompress);
        m_pPrepare = new Prepare();

        //set the initialized flag to TRUE
        m_bInitialized = true;

        m_wfeInput = m_pAPEDecompress.getApeInfoWaveFormatEx();
    }

    public void Uninitialize() {
        if (m_bInitialized) {
            m_pAPEDecompressCore = null;
            m_pPrepare = null;

            //clear the APE info pointer
            m_pAPEDecompress = null;

            //set the last decoded frame again
            m_LastDecodedFrameIndex = -1;

            //set the initialized flag to FALSE
            m_bInitialized = false;
        }
    }

    public int DecompressFrame(ByteBuffer pOutputData, int FrameIndex) throws IOException {
        return DecompressFrameOld(pOutputData, FrameIndex);
    }

    public void SeekToFrame(int FrameIndex) throws IOException {
        if (m_pAPEDecompress.getApeInfoFileVersion() > 3800) {
            if ((m_LastDecodedFrameIndex == -1) || ((FrameIndex - 1) != m_LastDecodedFrameIndex)) {
                int SeekRemainder = (m_pAPEDecompress.getApeInfoSeekByte(FrameIndex) - m_pAPEDecompress.getApeInfoSeekByte(0)) % 4;
                m_pAPEDecompressCore.GetUnBitArrray().FillAndResetBitArray(m_nRealFrame == FrameIndex ? -1 : m_pAPEDecompress.getApeInfoSeekByte(FrameIndex) - SeekRemainder, SeekRemainder * 8);
                m_nRealFrame = FrameIndex;
            } else
                m_pAPEDecompressCore.GetUnBitArrray().AdvanceToByteBoundary();
        } else {
            if ((m_LastDecodedFrameIndex == -1) || ((FrameIndex - 1) != m_LastDecodedFrameIndex)) {
                m_pAPEDecompressCore.GetUnBitArrray().FillAndResetBitArray(m_nRealFrame == FrameIndex ? -1 : m_pAPEDecompress.getApeInfoSeekByte(FrameIndex), m_pAPEDecompress.getApeInfoSeekBit(FrameIndex));
                m_nRealFrame = FrameIndex;
            }
        }
    }

    //data members
    private boolean m_bInitialized;
    private int m_LastDecodedFrameIndex;
    private int m_nRealFrame;
    private IAPEDecompress m_pAPEDecompress;
    private Prepare m_pPrepare;

    private APEDecompressCore m_pAPEDecompressCore;

    //functions
    private int DecompressFrameOld(ByteBuffer pOutputData, int FrameIndex) throws IOException {
        //error check the parameters (too high of a frame index, etc.)
        if (FrameIndex >= m_pAPEDecompress.getApeInfoTotalFrames())
            return 0;

        //get the number of samples in the frame
        int nBlocks = 0;
        nBlocks = ((FrameIndex + 1) >= m_pAPEDecompress.getApeInfoTotalFrames()) ? m_pAPEDecompress.getApeInfoFinalFrameBlocks() : m_pAPEDecompress.getApeInfoBlocksPerFrame();
        if (nBlocks == 0)
            throw new JMACException("Invalid Frame Index"); //nothing to do (file must be zero length) (have to return error)

        //take care of seeking and frame alignment
        SeekToFrame(FrameIndex);

        //get the checksum
        long nSpecialCodes = 0;
        long nStoredCRC = 0;

        if ((m_pAPEDecompress.getApeInfoFormatFlags() & APEHeader.MAC_FORMAT_FLAG_CRC) <= 0) {
            nStoredCRC = m_pAPEDecompressCore.GetUnBitArrray().DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_RICE, 30);
            if (nStoredCRC == 0)
                nSpecialCodes = SpecialFrame.SPECIAL_FRAME_LEFT_SILENCE | SpecialFrame.SPECIAL_FRAME_RIGHT_SILENCE;
        } else {
            nStoredCRC = m_pAPEDecompressCore.GetUnBitArrray().DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_INT);

            //get any 'special' codes if the file uses them (for silence, FALSE stereo, etc.)
            nSpecialCodes = 0;
            if (m_pAPEDecompress.getApeInfoFileVersion() > 3820) {
                if ((nStoredCRC & 0x80000000) > 0)
                    nSpecialCodes = m_pAPEDecompressCore.GetUnBitArrray().DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_INT);
                nStoredCRC &= 0x7fffffff;
            }
        }

        //decompress and convert from (x,y) -> (l,r)
        //sort of int and ugly.... sorry
        if (m_pAPEDecompress.getApeInfoChannels() == 2) {
            m_pAPEDecompressCore.GenerateDecodedArrays(nBlocks, (int) nSpecialCodes, FrameIndex);

            m_pPrepare.unprepareOld(m_pAPEDecompressCore.m_pDataX, m_pAPEDecompressCore.m_pDataY, nBlocks, m_wfeInput,
                    pOutputData, CRC, m_pAPEDecompress.getApeInfoFileVersion());
        } else if (m_pAPEDecompress.getApeInfoChannels() == 1) {
            m_pAPEDecompressCore.GenerateDecodedArrays(nBlocks, (int) nSpecialCodes, FrameIndex);

            m_pPrepare.unprepareOld(m_pAPEDecompressCore.m_pDataX, null, nBlocks, m_wfeInput,
                    pOutputData, CRC, m_pAPEDecompress.getApeInfoFileVersion());
        }

        if (m_pAPEDecompress.getApeInfoFileVersion() > 3820)
            CRC.finalizeCrc();

        // check the CRC
        if ((m_pAPEDecompress.getApeInfoFormatFlags() & APEHeader.MAC_FORMAT_FLAG_CRC) <= 0) {
            long nChecksum = CalculateOldChecksum(m_pAPEDecompressCore.m_pDataX, m_pAPEDecompressCore.m_pDataY, m_pAPEDecompress.getApeInfoChannels(), nBlocks);
            if (nChecksum != nStoredCRC)
                throw new JMACException("Invalid Checksum");
        } else {
            if (CRC.getCrc() != nStoredCRC)
                throw new JMACException("Invalid Checksum");
        }

        m_LastDecodedFrameIndex = FrameIndex;
        return nBlocks;
    }

    private long CalculateOldChecksum(int[] pDataX, int[] pDataY, int nChannels, int nBlocks) {
        long nChecksum = 0;

        if (nChannels == 2) {
            for (int z = 0; z < nBlocks; z++) {
                int R = pDataX[z] - (pDataY[z] / 2);
                int L = R + pDataY[z];
                nChecksum += (Math.abs(R) + Math.abs(L));
            }
        } else if (nChannels == 1) {
            for (int z = 0; z < nBlocks; z++)
                nChecksum += Math.abs(pDataX[z]);
        }

        return nChecksum;
    }

    public int m_nBlocksProcessed;
    public Crc32 CRC = new Crc32();
    public long m_nStoredCRC;
    public WaveFormat m_wfeInput;
}
