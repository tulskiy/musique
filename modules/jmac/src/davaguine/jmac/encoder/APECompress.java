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

import davaguine.jmac.info.InputSource;
import davaguine.jmac.info.WaveFormat;
import davaguine.jmac.tools.*;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 08.05.2004
 * Time: 11:17:57
 */
public class APECompress extends IAPECompress {
    public APECompress() {
        m_nBufferHead = 0;
        m_nBufferTail = 0;
        m_nBufferSize = 0;
        m_bBufferLocked = false;
        m_bOwnsOutputIO = false;
        m_pioOutput = null;

        m_spAPECompressCreate = new APECompressCreate();

        m_pBuffer = null;
    }

    protected void finalize() {
        Kill();
    }

    // start encoding
    public void Start(String pOutputFilename, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData, int nHeaderBytes) throws IOException {
        m_pioOutput = File.createFile(pOutputFilename, "rw");
        m_bOwnsOutputIO = true;

        m_spAPECompressCreate.Start(m_pioOutput, pwfeInput, nMaxAudioBytes, nCompressionLevel,
                pHeaderData, nHeaderBytes);

        m_nBufferSize = m_spAPECompressCreate.GetFullFrameBytes();
        m_pBuffer = new byte[m_nBufferSize];
        m_wfeInput = pwfeInput;
    }

    public void StartEx(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData, int nHeaderBytes) throws IOException {
        m_pioOutput = pioOutput;
        m_bOwnsOutputIO = false;

        m_spAPECompressCreate.Start(m_pioOutput, pwfeInput, nMaxAudioBytes, nCompressionLevel,
                pHeaderData, nHeaderBytes);

        m_nBufferSize = m_spAPECompressCreate.GetFullFrameBytes();
        m_pBuffer = new byte[m_nBufferSize];
        m_wfeInput = pwfeInput;
    }

    // add data / compress data

    // allows linear, immediate access to the buffer (fast)
    public int GetBufferBytesAvailable() {
        return m_nBufferSize - m_nBufferTail;
    }

    public void UnlockBuffer(int nBytesAdded, boolean bProcess) throws IOException {
        if (!m_bBufferLocked)
            throw new JMACException("Error Undefined");

        m_nBufferTail += nBytesAdded;
        m_bBufferLocked = false;

        if (bProcess)
            ProcessBuffer();
    }

    private ByteBuffer pBufferPointer = new ByteBuffer();

    public ByteBuffer LockBuffer(IntegerPointer pBytesAvailable) {
        if (m_pBuffer == null) {
            return null;
        }

        if (m_bBufferLocked)
            return null;

        m_bBufferLocked = true;

        if (pBytesAvailable != null)
            pBytesAvailable.value = GetBufferBytesAvailable();

        pBufferPointer.reset(m_pBuffer, m_nBufferTail);
        return pBufferPointer;
    }

    // slower, but easier than locking and unlocking (copies data)
    private IntegerPointer m_nAddDataBytesAvailable = new IntegerPointer();

    public void AddData(byte[] pData, int nBytes) throws IOException {
        int nBytesDone = 0;

        while (nBytesDone < nBytes) {
            // lock the buffer
            m_nAddDataBytesAvailable.value = 0;
            ByteBuffer pBuffer = LockBuffer(m_nAddDataBytesAvailable);
            if (pBuffer == null || m_nAddDataBytesAvailable.value <= 0)
                throw new JMACException("Error Undefined");

            // calculate how many bytes to copy and add that much to the buffer
            int nBytesToProcess = Math.min(m_nAddDataBytesAvailable.value, nBytes - nBytesDone);
            pBuffer.append(pData, nBytesDone, nBytesToProcess);

            // unlock the buffer (fail if not successful)
            UnlockBuffer(nBytesToProcess);

            // update our progress
            nBytesDone += nBytesToProcess;
        }
    }

    // use a CIO (input source) to add data
    private IntegerPointer m_nAddDataFromInputSourceBytesAvailavle = new IntegerPointer();

    public int AddDataFromInputSource(InputSource pInputSource, int nMaxBytes) throws IOException {
        // error check the parameters
        if (pInputSource == null)
            throw new JMACException("Bad Parameters");

        // initialize
        int pBytesAdded = 0;
        int nBytesRead = 0;

        // lock the buffer
        m_nAddDataFromInputSourceBytesAvailavle.value = 0;
        ByteBuffer pBuffer = LockBuffer(m_nAddDataFromInputSourceBytesAvailavle);

        // calculate the 'ideal' number of bytes
        int nIdealBytes = m_spAPECompressCreate.GetFullFrameBytes() - (m_nBufferTail - m_nBufferHead);
        if (nIdealBytes > 0) {
            // get the data
            int nBytesToAdd = m_nAddDataFromInputSourceBytesAvailavle.value;

            if (nMaxBytes > 0) {
                if (nBytesToAdd > nMaxBytes) nBytesToAdd = nMaxBytes;
            }

            if (nBytesToAdd > nIdealBytes) nBytesToAdd = nIdealBytes;

            // always make requests along block boundaries
            while ((nBytesToAdd % m_wfeInput.nBlockAlign) != 0)
                nBytesToAdd--;

            int nBlocksToAdd = nBytesToAdd / m_wfeInput.nBlockAlign;

            // get data
            int nBlocksAdded = pInputSource.GetData(pBuffer, nBlocksToAdd);
            nBytesRead = (nBlocksAdded * m_wfeInput.nBlockAlign);

            // store the bytes read
            pBytesAdded = nBytesRead;
        }

        // unlock the data and process
        UnlockBuffer(nBytesRead, true);

        return pBytesAdded;
    }

    // finish / kill
    public void Finish(byte[] pTerminatingData, int nTerminatingBytes, int nWAVTerminatingBytes) throws IOException {
        ProcessBuffer(true);
        m_spAPECompressCreate.Finish(pTerminatingData, nTerminatingBytes, nWAVTerminatingBytes);
    }

    public void Kill() {
        if (m_pioOutput != null) {
            try {
                if (m_bOwnsOutputIO)
                    m_pioOutput.close();
            } catch (IOException e) {
                throw new JMACException("Error while closing output stream", e);
            }
        }
        m_pioOutput = null;
    }

    private void ProcessBuffer() throws IOException {
        ProcessBuffer(false);
    }

    private ByteArrayReader pByteReader = new ByteArrayReader();

    private void ProcessBuffer(boolean bFinalize) throws IOException {
        if (m_pBuffer == null)
            throw new JMACException("Error Undefined");

        // process as much as possible
        int nThreshold = (bFinalize) ? 0 : m_spAPECompressCreate.GetFullFrameBytes();

        while ((m_nBufferTail - m_nBufferHead) >= nThreshold) {
            int nFrameBytes = Math.min(m_spAPECompressCreate.GetFullFrameBytes(), m_nBufferTail - m_nBufferHead);

            if (nFrameBytes == 0)
                break;

            pByteReader.reset(m_pBuffer, m_nBufferHead);
            m_spAPECompressCreate.EncodeFrame(pByteReader, nFrameBytes);

            m_nBufferHead += nFrameBytes;
        }

        // shift the buffer
        if (m_nBufferHead != 0) {
            int nBytesLeft = m_nBufferTail - m_nBufferHead;

            if (nBytesLeft != 0)
                System.arraycopy(m_pBuffer, m_nBufferHead, m_pBuffer, 0, nBytesLeft);

            m_nBufferTail -= m_nBufferHead;
            m_nBufferHead = 0;
        }
    }

    private APECompressCreate m_spAPECompressCreate;

    private int m_nBufferHead;
    private int m_nBufferTail;
    private int m_nBufferSize;
    private byte[] m_pBuffer;
    private boolean m_bBufferLocked;

    private File m_pioOutput;
    private boolean m_bOwnsOutputIO;
    private WaveFormat m_wfeInput;
}
