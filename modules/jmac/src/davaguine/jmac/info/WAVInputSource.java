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

import davaguine.jmac.tools.ByteBuffer;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.IntegerPointer;
import davaguine.jmac.tools.JMACException;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 07.05.2004
 * Time: 14:10:50
 */
public class WAVInputSource extends InputSource {
    // construction / destruction
    public WAVInputSource(File pIO, WaveFormat pwfeSource, IntegerPointer pTotalBlocks, IntegerPointer pHeaderBytes, IntegerPointer pTerminatingBytes) throws IOException {
        super(pIO, pwfeSource, pTotalBlocks, pHeaderBytes, pTerminatingBytes);
        m_bIsValid = false;

        if (pIO == null || pwfeSource == null)
            throw new JMACException("Bad Parameters");

        m_spIO = pIO;
        m_bOwnsInputIO = false;

        AnalyzeSource();

        // fill in the parameters
        pwfeSource = m_wfeSource;
        if (pTotalBlocks != null) pTotalBlocks.value = m_nDataBytes / m_wfeSource.nBlockAlign;
        if (pHeaderBytes != null) pHeaderBytes.value = m_nHeaderBytes;
        if (pTerminatingBytes != null) pTerminatingBytes.value = m_nTerminatingBytes;

        m_bIsValid = true;
    }

    public WAVInputSource(String pSourceName, WaveFormat pwfeSource, IntegerPointer pTotalBlocks, IntegerPointer pHeaderBytes, IntegerPointer pTerminatingBytes) throws IOException {
        super(pSourceName, pwfeSource, pTotalBlocks, pHeaderBytes, pTerminatingBytes);
        m_bIsValid = false;

        if (pSourceName == null || pwfeSource == null)
            throw new JMACException("Bad Parameters");

        m_spIO = File.createFile(pSourceName, "r");
        m_bOwnsInputIO = true;

        AnalyzeSource();
        // fill in the parameters
        pwfeSource.wFormatTag = m_wfeSource.wFormatTag;
        pwfeSource.nChannels = m_wfeSource.nChannels;
        pwfeSource.nSamplesPerSec = m_wfeSource.nSamplesPerSec;
        pwfeSource.nAvgBytesPerSec = m_wfeSource.nAvgBytesPerSec;
        pwfeSource.nBlockAlign = m_wfeSource.nBlockAlign;
        pwfeSource.wBitsPerSample = m_wfeSource.wBitsPerSample;
        if (pTotalBlocks != null) pTotalBlocks.value = m_nDataBytes / m_wfeSource.nBlockAlign;
        if (pHeaderBytes != null) pHeaderBytes.value = m_nHeaderBytes;
        if (pTerminatingBytes != null) pTerminatingBytes.value = m_nTerminatingBytes;

        m_bIsValid = true;
    }

    public void Close() throws IOException {
        if (m_bIsValid && m_bOwnsInputIO && m_spIO != null)
            m_spIO.close();
        m_spIO = null;
    }

    protected void finalize() {
        try {
            Close();
        } catch (IOException e) {
            throw new JMACException("Error while closing input stream.");
        }
    }

    // get data
    public int GetData(ByteBuffer pBuffer, int nBlocks) throws IOException {
        if (!m_bIsValid)
            throw new JMACException("Undefined Error");

        int nBytes = (m_wfeSource.nBlockAlign * nBlocks);

        int nBytesRead = m_spIO.read(pBuffer.getBytes(), pBuffer.getIndex(), nBytes);

        return nBytesRead / m_wfeSource.nBlockAlign;
    }

    // get header / terminating data
    public void GetHeaderData(byte[] pBuffer) throws IOException {
        if (!m_bIsValid)
            throw new JMACException("Undefined Error");

        if (m_nHeaderBytes > 0) {
            long nOriginalFileLocation = m_spIO.getFilePointer();

            m_spIO.seek(0);

            if (m_spIO.read(pBuffer, 0, m_nHeaderBytes) != m_nHeaderBytes)
                throw new JMACException("Undefined Error");

            m_spIO.seek(nOriginalFileLocation);
        }
    }

    public void GetTerminatingData(byte[] pBuffer) throws IOException {
        if (!m_bIsValid)
            throw new JMACException("Undefined Error");

        if (m_nTerminatingBytes > 0) {
            long nOriginalFileLocation = m_spIO.getFilePointer();

            m_spIO.seek(m_spIO.length() - m_nTerminatingBytes);

            if (m_spIO.read(pBuffer, 0, m_nTerminatingBytes) != m_nTerminatingBytes)
                throw new JMACException("Undefined Error");

            m_spIO.seek(nOriginalFileLocation);
        }
    }

    private void AnalyzeSource() throws IOException {
        // seek to the beginning (just in case)
        m_spIO.seek(0);

        // get the file size
        m_nFileBytes = (int) m_spIO.length();

        // get the RIFF header
        int riffSignature = m_spIO.readInt();
        int goalSignature = ('R' << 24) | ('I' << 16) | ('F' << 8) | ('F');
        if (riffSignature != goalSignature)
            throw new JMACException("Invalid Input File");

        m_spIO.readInt();

        // read the data type header
        int dataTypeSignature = m_spIO.readInt();
        goalSignature = ('W' << 24) | ('A' << 16) | ('V' << 8) | ('E');
        // make sure it's the right data type
        if (dataTypeSignature != goalSignature)
            throw new JMACException("Invalid Input File");

        // find the 'fmt ' chunk
        RiffChunkHeader RIFFChunkHeader = new RiffChunkHeader();
        RIFFChunkHeader.read(m_spIO);
        goalSignature = (' ' << 24) | ('t' << 16) | ('m' << 8) | ('f');
        while (RIFFChunkHeader.cChunkLabel != goalSignature) {
            // move the file pointer to the end of this chunk
            m_spIO.seek(m_spIO.getFilePointer() + RIFFChunkHeader.nChunkBytes);

            // check again for the data chunk
            RIFFChunkHeader.read(m_spIO);
        }

        // read the format info
        WaveFormat WAVFormatHeader = new WaveFormat();
        WAVFormatHeader.readHeader(m_spIO);

        // error check the header to see if we support it
        if (WAVFormatHeader.wFormatTag != 1)
            throw new JMACException("Invalid Input File");

        // copy the format information to the WAVEFORMATEX passed in
        WaveFormat.FillWaveFormatEx(m_wfeSource, WAVFormatHeader.nSamplesPerSec, WAVFormatHeader.wBitsPerSample, WAVFormatHeader.nChannels);

        // skip over any extra data in the header
        int nWAVFormatHeaderExtra = (int) (RIFFChunkHeader.nChunkBytes - WaveFormat.WAV_HEADER_SIZE);
        if (nWAVFormatHeaderExtra < 0)
            throw new JMACException("Invalid Input File");
        else
            m_spIO.seek(m_spIO.getFilePointer() + nWAVFormatHeaderExtra);

        // find the data chunk
        RIFFChunkHeader.read(m_spIO);
        goalSignature = ('a' << 24) | ('t' << 16) | ('a' << 8) | ('d');

        while (RIFFChunkHeader.cChunkLabel != goalSignature) {
            // move the file pointer to the end of this chunk
            m_spIO.seek(m_spIO.getFilePointer() + RIFFChunkHeader.nChunkBytes);

            // check again for the data chunk
            RIFFChunkHeader.read(m_spIO);
        }

        // we're at the data block
        m_nHeaderBytes = (int) m_spIO.getFilePointer();
        m_nDataBytes = (int) RIFFChunkHeader.nChunkBytes;
        if (m_nDataBytes < 0)
            m_nDataBytes = m_nFileBytes - m_nHeaderBytes;

        // make sure the data bytes is a whole number of blocks
        if ((m_nDataBytes % m_wfeSource.nBlockAlign) != 0)
            throw new JMACException("Invalid Input File");

        // calculate the terminating byts
        m_nTerminatingBytes = m_nFileBytes - m_nDataBytes - m_nHeaderBytes;
    }

    private File m_spIO;
    private boolean m_bOwnsInputIO;

    private WaveFormat m_wfeSource = new WaveFormat();
    private int m_nHeaderBytes;
    private int m_nDataBytes;
    private int m_nTerminatingBytes;
    private int m_nFileBytes;
    private boolean m_bIsValid;
}
