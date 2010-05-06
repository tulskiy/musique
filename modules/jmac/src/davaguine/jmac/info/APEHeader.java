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

package davaguine.jmac.info;

import davaguine.jmac.tools.ByteArrayReader;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;

import java.io.EOFException;
import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APEHeader {

    public final static int MAC_FORMAT_FLAG_8_BIT = 1;              // is 8-bit
    public final static int MAC_FORMAT_FLAG_CRC = 2;                // uses the new CRC32 error detection
    public final static int MAC_FORMAT_FLAG_HAS_PEAK_LEVEL = 4;        // unsigned __int32 Peak_Level after the header
    public final static int MAC_FORMAT_FLAG_24_BIT = 8;                // is 24-bit
    public final static int MAC_FORMAT_FLAG_HAS_SEEK_ELEMENTS = 16;    // has the number of seek elements after the peak level
    public final static int MAC_FORMAT_FLAG_CREATE_WAV_HEADER = 32; // create the wave header on decompression (not stored)

    public APEHeader(final File file) {
        m_pIO = file;
    }

    public void Analyze(APEFileInfo pInfo) throws IOException {
        // find the descriptor
        pInfo.nJunkHeaderBytes = FindDescriptor(true);
        if (pInfo.nJunkHeaderBytes < 0)
            throw new JMACException("Unsupported Format");

        // read the first 8 bytes of the descriptor (ID and version)
        m_pIO.mark(10);
        final ByteArrayReader reader = new ByteArrayReader(m_pIO, 8);
        if (!reader.readString(4, "US-ASCII").equals("MAC "))
            throw new JMACException("Unsupported Format");

        int version = reader.readUnsignedShort();

        m_pIO.reset();

        if (version >= 3980) {
            // current header format
            AnalyzeCurrent(pInfo);
        } else {
            // legacy support
            AnalyzeOld(pInfo);
        }
    }

    protected void AnalyzeCurrent(APEFileInfo m_APEFileInfo) throws IOException {
        m_APEFileInfo.spAPEDescriptor = APEDescriptor.read(m_pIO);

        if ((m_APEFileInfo.spAPEDescriptor.nDescriptorBytes - APEDescriptor.APE_DESCRIPTOR_BYTES) > 0)
            m_pIO.skipBytes((int) (m_APEFileInfo.spAPEDescriptor.nDescriptorBytes - APEDescriptor.APE_DESCRIPTOR_BYTES));

        final APEHeaderNew APEHeader = APEHeaderNew.read(m_pIO);

        if ((m_APEFileInfo.spAPEDescriptor.nHeaderBytes - APEHeaderNew.APE_HEADER_BYTES) > 0)
            m_pIO.skipBytes((int) (m_APEFileInfo.spAPEDescriptor.nHeaderBytes - APEHeaderNew.APE_HEADER_BYTES));

        // fill the APE info structure
        m_APEFileInfo.nVersion = m_APEFileInfo.spAPEDescriptor.nVersion;
        m_APEFileInfo.nCompressionLevel = APEHeader.nCompressionLevel;
        m_APEFileInfo.nFormatFlags = APEHeader.nFormatFlags;
        m_APEFileInfo.nTotalFrames = (int) APEHeader.nTotalFrames;
        m_APEFileInfo.nFinalFrameBlocks = (int) APEHeader.nFinalFrameBlocks;
        m_APEFileInfo.nBlocksPerFrame = (int) APEHeader.nBlocksPerFrame;
        m_APEFileInfo.nChannels = APEHeader.nChannels;
        m_APEFileInfo.nSampleRate = (int) APEHeader.nSampleRate;
        m_APEFileInfo.nBitsPerSample = APEHeader.nBitsPerSample;
        m_APEFileInfo.nBytesPerSample = m_APEFileInfo.nBitsPerSample / 8;
        m_APEFileInfo.nBlockAlign = m_APEFileInfo.nBytesPerSample * m_APEFileInfo.nChannels;
        m_APEFileInfo.nTotalBlocks = (int) ((APEHeader.nTotalFrames == 0) ? 0 : ((APEHeader.nTotalFrames - 1) * m_APEFileInfo.nBlocksPerFrame) + APEHeader.nFinalFrameBlocks);
        m_APEFileInfo.nWAVHeaderBytes = (int) ((APEHeader.nFormatFlags & MAC_FORMAT_FLAG_CREATE_WAV_HEADER) > 0 ? WaveHeader.WAVE_HEADER_BYTES : m_APEFileInfo.spAPEDescriptor.nHeaderDataBytes);
        m_APEFileInfo.nWAVTerminatingBytes = (int) m_APEFileInfo.spAPEDescriptor.nTerminatingDataBytes;
        m_APEFileInfo.nWAVDataBytes = m_APEFileInfo.nTotalBlocks * m_APEFileInfo.nBlockAlign;
        m_APEFileInfo.nWAVTotalBytes = m_APEFileInfo.nWAVDataBytes + m_APEFileInfo.nWAVHeaderBytes + m_APEFileInfo.nWAVTerminatingBytes;
        m_APEFileInfo.nAPETotalBytes = m_pIO.isLocal() ? (int) m_pIO.length() : -1;
        m_APEFileInfo.nLengthMS = (int) ((m_APEFileInfo.nTotalBlocks * 1000L) / m_APEFileInfo.nSampleRate);
        m_APEFileInfo.nAverageBitrate = (m_APEFileInfo.nLengthMS <= 0) ? 0 : (int) ((m_APEFileInfo.nAPETotalBytes * 8L) / m_APEFileInfo.nLengthMS);
        m_APEFileInfo.nDecompressedBitrate = (m_APEFileInfo.nBlockAlign * m_APEFileInfo.nSampleRate * 8) / 1000;
        m_APEFileInfo.nSeekTableElements = (int) (m_APEFileInfo.spAPEDescriptor.nSeekTableBytes / 4);
        m_APEFileInfo.nPeakLevel = -1;

        // get the seek tables (really no reason to get the whole thing if there's extra)
        m_APEFileInfo.spSeekByteTable = new int[m_APEFileInfo.nSeekTableElements];
        for (int i = 0; i < m_APEFileInfo.nSeekTableElements; i++)
            m_APEFileInfo.spSeekByteTable[i] = m_pIO.readIntBack();

        // get the wave header
        if ((APEHeader.nFormatFlags & MAC_FORMAT_FLAG_CREATE_WAV_HEADER) <= 0) {
            if (m_APEFileInfo.nWAVHeaderBytes > Integer.MAX_VALUE)
                throw new JMACException("The HeaderBytes Parameter Is Too Big");
            m_APEFileInfo.spWaveHeaderData = new byte[m_APEFileInfo.nWAVHeaderBytes];
            try {
                m_pIO.readFully(m_APEFileInfo.spWaveHeaderData);
            } catch (EOFException e) {
                throw new JMACException("Can't Read Wave Header Data");
            }
        }
    }

    protected void AnalyzeOld(APEFileInfo m_APEFileInfo) throws IOException {
        APEHeaderOld header = APEHeaderOld.read(m_pIO);

        // fail on 0 length APE files (catches non-finalized APE files)
        if (header.nTotalFrames == 0)
            throw new JMACException("Unsupported Format");

        int nPeakLevel = -1;
        if ((header.nFormatFlags & MAC_FORMAT_FLAG_HAS_PEAK_LEVEL) > 0)
            nPeakLevel = m_pIO.readIntBack();

        if ((header.nFormatFlags & MAC_FORMAT_FLAG_HAS_SEEK_ELEMENTS) > 0)
            m_APEFileInfo.nSeekTableElements = m_pIO.readIntBack();
        else
            m_APEFileInfo.nSeekTableElements = (int) header.nTotalFrames;

        // fill the APE info structure
        m_APEFileInfo.nVersion = header.nVersion;
        m_APEFileInfo.nCompressionLevel = header.nCompressionLevel;
        m_APEFileInfo.nFormatFlags = header.nFormatFlags;
        m_APEFileInfo.nTotalFrames = (int) header.nTotalFrames;
        m_APEFileInfo.nFinalFrameBlocks = (int) header.nFinalFrameBlocks;
        m_APEFileInfo.nBlocksPerFrame = ((header.nVersion >= 3900) || ((header.nVersion >= 3800) && (header.nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH))) ? 73728 : 9216;
        if (header.nVersion >= 3950)
            m_APEFileInfo.nBlocksPerFrame = 73728 * 4;
        m_APEFileInfo.nChannels = header.nChannels;
        m_APEFileInfo.nSampleRate = (int) header.nSampleRate;
        m_APEFileInfo.nBitsPerSample = (m_APEFileInfo.nFormatFlags & MAC_FORMAT_FLAG_8_BIT) > 0 ? 8 : ((m_APEFileInfo.nFormatFlags & MAC_FORMAT_FLAG_24_BIT) > 0 ? 24 : 16);
        m_APEFileInfo.nBytesPerSample = m_APEFileInfo.nBitsPerSample / 8;
        m_APEFileInfo.nBlockAlign = m_APEFileInfo.nBytesPerSample * m_APEFileInfo.nChannels;
        m_APEFileInfo.nTotalBlocks = (int) ((header.nTotalFrames == 0) ? 0 : ((header.nTotalFrames - 1) * m_APEFileInfo.nBlocksPerFrame) + header.nFinalFrameBlocks);
        m_APEFileInfo.nWAVHeaderBytes = (int) ((header.nFormatFlags & MAC_FORMAT_FLAG_CREATE_WAV_HEADER) > 0 ? WaveHeader.WAVE_HEADER_BYTES : header.nHeaderBytes);
        m_APEFileInfo.nWAVTerminatingBytes = (int) header.nTerminatingBytes;
        m_APEFileInfo.nWAVDataBytes = m_APEFileInfo.nTotalBlocks * m_APEFileInfo.nBlockAlign;
        m_APEFileInfo.nWAVTotalBytes = m_APEFileInfo.nWAVDataBytes + m_APEFileInfo.nWAVHeaderBytes + m_APEFileInfo.nWAVTerminatingBytes;
        m_APEFileInfo.nAPETotalBytes = m_pIO.isLocal() ? (int) m_pIO.length() : -1;
        m_APEFileInfo.nLengthMS = (int) ((m_APEFileInfo.nTotalBlocks * 1000L) / m_APEFileInfo.nSampleRate);
        m_APEFileInfo.nAverageBitrate = (int) ((m_APEFileInfo.nLengthMS <= 0) ? 0 : ((m_APEFileInfo.nAPETotalBytes * 8L) / m_APEFileInfo.nLengthMS));
        m_APEFileInfo.nDecompressedBitrate = (m_APEFileInfo.nBlockAlign * m_APEFileInfo.nSampleRate * 8) / 1000;
        m_APEFileInfo.nPeakLevel = nPeakLevel;

        // get the wave header
        if ((header.nFormatFlags & MAC_FORMAT_FLAG_CREATE_WAV_HEADER) <= 0) {
            if (header.nHeaderBytes > Integer.MAX_VALUE)
                throw new JMACException("The HeaderBytes Parameter Is Too Big");
            m_APEFileInfo.spWaveHeaderData = new byte[(int) header.nHeaderBytes];
            try {
                m_pIO.readFully(m_APEFileInfo.spWaveHeaderData);
            } catch (EOFException e) {
                throw new JMACException("Can't Read Wave Header Data");
            }
        }

        // get the seek tables (really no reason to get the whole thing if there's extra)
        m_APEFileInfo.spSeekByteTable = new int[m_APEFileInfo.nSeekTableElements];
        for (int i = 0; i < m_APEFileInfo.nSeekTableElements; i++)
            m_APEFileInfo.spSeekByteTable[i] = m_pIO.readIntBack();

        if (header.nVersion <= 3800) {
            m_APEFileInfo.spSeekBitTable = new byte[m_APEFileInfo.nSeekTableElements];
            try {
                m_pIO.readFully(m_APEFileInfo.spSeekBitTable);
            } catch (EOFException e) {
                throw new JMACException("Can't Read Seek Bit Table");
            }
        }
    }

    protected int FindDescriptor(boolean bSeek) throws IOException {
        int nJunkBytes = 0;

        // We need to limit this method if m_pIO is represented as URL
        // We'll not support ID3 tags for such files
        if (m_pIO.isLocal()) {

            // figure the extra header bytes
            m_pIO.mark(1000);

            // skip an ID3v2 tag (which we really don't support anyway...)
            ByteArrayReader reader = new ByteArrayReader(10);
            reader.reset(m_pIO, 10);
            final String tag = reader.readString(3, "US-ASCII");
            if (tag.equals("ID3")) {
                // why is it so hard to figure the lenght of an ID3v2 tag ?!?
                reader.readByte();
                reader.readByte();
                int byte5 = reader.readUnsignedByte();

                int nSyncSafeLength;
                nSyncSafeLength = (reader.readUnsignedByte() & 127) << 21;
                nSyncSafeLength += (reader.readUnsignedByte() & 127) << 14;
                nSyncSafeLength += (reader.readUnsignedByte() & 127) << 7;
                nSyncSafeLength += (reader.readUnsignedByte() & 127);

                boolean bHasTagFooter = false;

                if ((byte5 & 16) > 0) {
                    bHasTagFooter = true;
                    nJunkBytes = nSyncSafeLength + 20;
                } else {
                    nJunkBytes = nSyncSafeLength + 10;
                }

                // error check
                if ((byte5 & 64) > 0) {
                    // this ID3v2 length calculator algorithm can't cope with extended headers
                    // we should be ok though, because the scan for the MAC header below should
                    // really do the trick
                }

                m_pIO.skipBytes(nJunkBytes - 10);

                // scan for padding (slow and stupid, but who cares here...)
                if (!bHasTagFooter) {
                    while (m_pIO.read() == 0)
                        nJunkBytes++;
                }
            }
            m_pIO.reset();
            m_pIO.skipBytes(nJunkBytes);
        }

        m_pIO.mark(1000);

        // scan until we hit the APE header, the end of the file, or 1 MB later
        int nGoalID = ('M' << 24) | ('A' << 16) | ('C' << 8) | (' ');
        int nReadID = m_pIO.readInt();

        // Also, lets suppose that MAC header placed in beginning of file in case of external source of file
        if(m_pIO.isLocal()) {
            int nScanBytes = 0;
            while (nGoalID != nReadID && nScanBytes < (1024 * 1024)) {
                nReadID = (nReadID << 8) | m_pIO.readByte();
                nJunkBytes++;
                nScanBytes++;
            }
        }

        if (nGoalID != nReadID)
            nJunkBytes = -1;

        // seek to the proper place (depending on result and settings)
        if (bSeek && (nJunkBytes != -1)) {
            // successfully found the start of the file (seek to it and return)
            m_pIO.reset();
            m_pIO.skipBytes(nJunkBytes);
            m_pIO.mark(1000);
        } else {
            // restore the original file pointer
            m_pIO.reset();
        }

        return nJunkBytes;
    }

    protected File m_pIO;
}
