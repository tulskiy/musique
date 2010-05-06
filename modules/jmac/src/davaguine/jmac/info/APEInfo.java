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

import davaguine.jmac.tools.File;
import davaguine.jmac.tools.InputStreamFile;
import davaguine.jmac.tools.JMACException;
import davaguine.jmac.tools.RandomAccessFile;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APEInfo {

    // construction and destruction
    public APEInfo(URL url) throws IOException {
        this(url, null);
    }

    public APEInfo(URL url, APETag pTag) throws IOException {
        this(url.openStream(), pTag);
    }

    public APEInfo(java.io.File file) throws IOException {
        this(file, null);
    }

    public APEInfo(java.io.File file, APETag pTag) throws IOException {
        this(new RandomAccessFile(file, "r"), pTag);
    }

    public APEInfo(InputStream pIO) throws IOException {
        this(pIO, null);
    }

    public APEInfo(InputStream pIO, APETag pTag) throws IOException {
        this(new InputStreamFile(pIO), pTag);
    }

    public APEInfo(File pIO) throws IOException {
        this(pIO, null);
    }

    public APEInfo(File pIO, APETag pTag) throws IOException {
        // open the file
        m_spIO = pIO;

        // get the file information
        GetFileInformation();

        // get the tag (do this second so that we don't do it on failure)
        if (pTag == null) {
            // we don't want to analyze right away for non-local files
            // since a single I/O object is shared, we can't tag and read at the same time (i.e. in multiple threads)

            m_spAPETag = new APETag(m_spIO, pIO.isLocal());
        } else
            m_spAPETag = pTag;
    }

    public void close() throws IOException {
        m_APEFileInfo.spWaveHeaderData = null;
        m_APEFileInfo.spSeekBitTable = null;
        m_APEFileInfo.spSeekByteTable = null;
        m_APEFileInfo.spAPEDescriptor = null;
        m_spAPETag = null;

        // re-initialize variables
        m_APEFileInfo.nSeekTableElements = 0;
        m_bHasFileInformationLoaded = false;
    }

    public int getApeInfoFileVersion() {
        return m_APEFileInfo.nVersion;
    }

    public int getApeInfoCompressionLevel() {
        return m_APEFileInfo.nCompressionLevel;
    }

    public int getApeInfoFormatFlags() {
        return m_APEFileInfo.nFormatFlags;
    }

    public int getApeInfoSampleRate() {
        return m_APEFileInfo.nSampleRate;
    }

    public int getApeInfoBitsPerSample() {
        return m_APEFileInfo.nBitsPerSample;
    }

    public int getApeInfoBytesPerSample() {
        return m_APEFileInfo.nBytesPerSample;
    }

    public int getApeInfoChannels() {
        return m_APEFileInfo.nChannels;
    }

    public int getApeInfoBlockAlign() {
        return m_APEFileInfo.nBlockAlign;
    }

    public int getApeInfoBlocksPerFrame() {
        return m_APEFileInfo.nBlocksPerFrame;
    }

    public int getApeInfoFinalFrameBlocks() {
        return m_APEFileInfo.nFinalFrameBlocks;
    }

    public int getApeInfoTotalFrames() {
        return m_APEFileInfo.nTotalFrames;
    }

    public int getApeInfoWavHeaderBytes() {
        return m_APEFileInfo.nWAVHeaderBytes;
    }

    public int getApeInfoWavTerminatingBytes() {
        return m_APEFileInfo.nWAVTerminatingBytes;
    }

    public int getApeInfoWavDataBytes() {
        return m_APEFileInfo.nWAVDataBytes;
    }

    public int getApeInfoWavTotalBytes() {
        return m_APEFileInfo.nWAVTotalBytes;
    }

    public int getApeInfoApeTotalBytes() {
        return m_APEFileInfo.nAPETotalBytes;
    }

    public int getApeInfoTotalBlocks() {
        return m_APEFileInfo.nTotalBlocks;
    }

    public int getApeInfoLengthMs() {
        return m_APEFileInfo.nLengthMS;
    }

    public int getApeInfoAverageBitrate() {
        return m_APEFileInfo.nAverageBitrate;
    }

    public int getApeInfoSeekByte(int nFrame) {
        return (nFrame < 0 || nFrame >= m_APEFileInfo.nTotalFrames) ? 0 : m_APEFileInfo.spSeekByteTable[nFrame] + m_APEFileInfo.nJunkHeaderBytes;
    }

    public int getApeInfoFrameBytes(int nFrame) throws IOException {
        if ((nFrame < 0) || (nFrame >= m_APEFileInfo.nTotalFrames))
            return -1;
        else {
            if (nFrame != (m_APEFileInfo.nTotalFrames - 1))
                return getApeInfoSeekByte(nFrame + 1) - getApeInfoSeekByte(nFrame);
            else {
                if (m_spIO.isLocal())
                    return (int) m_spIO.length() - m_spAPETag.GetTagBytes() - m_APEFileInfo.nWAVTerminatingBytes - getApeInfoSeekByte(nFrame);
                else if (nFrame > 0)
                    return getApeInfoSeekByte(nFrame) - getApeInfoSeekByte(nFrame - 1);
                else
                    return -1;
            }
        }
    }

    public int getApeInfoFrameBlocks(int nFrame) {
        if ((nFrame < 0) || (nFrame >= m_APEFileInfo.nTotalFrames))
            return -1;
        else {
            if (nFrame != (m_APEFileInfo.nTotalFrames - 1))
                return m_APEFileInfo.nBlocksPerFrame;
            else
                return m_APEFileInfo.nFinalFrameBlocks;
        }
    }

    public int getApeInfoFrameBitrate(int nFrame) throws IOException {
        int nFrameBytes = getApeInfoFrameBytes(nFrame);
        int nFrameBlocks = getApeInfoFrameBlocks(nFrame);
        if ((nFrameBytes > 0) && (nFrameBlocks > 0) && m_APEFileInfo.nSampleRate > 0) {
            int nFrameMS = (nFrameBlocks * 1000) / m_APEFileInfo.nSampleRate;
            if (nFrameMS != 0) {
                return (nFrameBytes * 8) / nFrameMS;
            }
        }
        return m_APEFileInfo.nAverageBitrate;
    }

    public int getApeInfoDecompressedBitrate() {
        return m_APEFileInfo.nDecompressedBitrate;
    }

    public int getApeInfoPeakLevel() {
        return m_APEFileInfo.nPeakLevel;
    }

    public int getApeInfoSeekBit(int nFrame) {
        if (getApeInfoFileVersion() > 3800)
            return 0;
        else {
            if (nFrame < 0 || nFrame >= m_APEFileInfo.nTotalFrames)
                return 0;
            else
                return m_APEFileInfo.spSeekBitTable[nFrame];
        }
    }

    public WaveFormat getApeInfoWaveFormatEx() {
        final WaveFormat pWaveFormatEx = new WaveFormat();
        WaveFormat.FillWaveFormatEx(pWaveFormatEx, m_APEFileInfo.nSampleRate, m_APEFileInfo.nBitsPerSample, m_APEFileInfo.nChannels);
        return pWaveFormatEx;
    }

    public byte[] getApeInfoWavHeaderData(int nMaxBytes) {
        if ((m_APEFileInfo.nFormatFlags & APEHeader.MAC_FORMAT_FLAG_CREATE_WAV_HEADER) > 0) {
            if (WaveHeader.WAVE_HEADER_BYTES > nMaxBytes)
                return null;
            else {
                WaveFormat wfeFormat = getApeInfoWaveFormatEx();
                WaveHeader WAVHeader = new WaveHeader();
                WaveHeader.FillWaveHeader(WAVHeader, m_APEFileInfo.nWAVDataBytes, wfeFormat, m_APEFileInfo.nWAVTerminatingBytes);
                return WAVHeader.write();
            }
        } else {
            if (m_APEFileInfo.nWAVHeaderBytes > nMaxBytes)
                return null;
            else {
                byte[] pBuffer = new byte[m_APEFileInfo.nWAVHeaderBytes];
                System.arraycopy(m_APEFileInfo.spWaveHeaderData, 0, pBuffer, 0, m_APEFileInfo.nWAVHeaderBytes);
                return pBuffer;
            }
        }
    }

    public File getApeInfoIoSource() {
        return m_spIO;
    }

    public APETag getApeInfoTag() {
        return m_spAPETag;
    }

    public byte[] getApeInfoWavTerminatingData(int nMaxBytes) throws IOException {
        if (m_APEFileInfo.nWAVTerminatingBytes > nMaxBytes)
            return null;
        else {
            if (m_APEFileInfo.nWAVTerminatingBytes > 0) {
                // variables
                long nOriginalFileLocation = m_spIO.getFilePointer();

                // check for a tag
                m_spIO.seek(m_spIO.length() - (m_spAPETag.GetTagBytes() + m_APEFileInfo.nWAVTerminatingBytes));
                byte[] pBuffer = new byte[m_APEFileInfo.nWAVTerminatingBytes];
                try {
                    m_spIO.readFully(pBuffer);
                } catch (EOFException e) {
                    throw new JMACException("Can't Read WAV Terminating Bytes");
                }

                // restore the file pointer
                m_spIO.seek(nOriginalFileLocation);
                return pBuffer;
            }
            return null;
        }
    }

    public APEFileInfo getApeInfoInternalInfo() {
        return m_APEFileInfo;
    }

    private void GetFileInformation() throws IOException {
        // quit if the file information has already been loaded
        if (m_bHasFileInformationLoaded)
            return;

        // use a CAPEHeader class to help us analyze the file
        APEHeader APEHeader = new APEHeader(m_spIO);
        APEHeader.Analyze(m_APEFileInfo);

        m_bHasFileInformationLoaded = true;
    }

    // internal variables
    private boolean m_bHasFileInformationLoaded;
    private File m_spIO;
    private APETag m_spAPETag;
    private APEFileInfo m_APEFileInfo = new APEFileInfo();
}
