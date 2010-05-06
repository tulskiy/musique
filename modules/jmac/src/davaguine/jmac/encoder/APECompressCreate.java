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

import davaguine.jmac.info.*;
import davaguine.jmac.tools.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * Author: Dmitry Vaguine
 * Date: 08.05.2004
 * Time: 12:40:36
 */
public class APECompressCreate {
    APECompressCreate() {
        m_nMaxFrames = 0;
    }

    public void InitializeFile(File pIO, WaveFormat pwfeInput, int nMaxFrames, int nCompressionLevel, byte[] pHeaderData, int nHeaderBytes) throws IOException {
        // error check the parameters
        if (pIO == null || pwfeInput == null || nMaxFrames <= 0)
            throw new JMACException("Bad Parameters");

        APEDescriptor APEDescriptor = new APEDescriptor();
        APEHeaderNew header = new APEHeaderNew();

        // create the descriptor (only fill what we know)
        APEDescriptor.cID = "MAC ";
        APEDescriptor.nVersion = Globals.MAC_VERSION_NUMBER;

        APEDescriptor.nDescriptorBytes = APEDescriptor.APE_DESCRIPTOR_BYTES;
        APEDescriptor.nHeaderBytes = APEHeaderNew.APE_HEADER_BYTES;
        APEDescriptor.nSeekTableBytes = nMaxFrames * 4;
        APEDescriptor.nHeaderDataBytes = (nHeaderBytes == IAPECompress.CREATE_WAV_HEADER_ON_DECOMPRESSION) ? 0 : nHeaderBytes;

        // create the header (only fill what we know now)
        header.nBitsPerSample = pwfeInput.wBitsPerSample;
        header.nChannels = pwfeInput.nChannels;
        header.nSampleRate = pwfeInput.nSamplesPerSec;

        header.nCompressionLevel = nCompressionLevel;
        header.nFormatFlags = (nHeaderBytes == IAPECompress.CREATE_WAV_HEADER_ON_DECOMPRESSION) ? APEHeader.MAC_FORMAT_FLAG_CREATE_WAV_HEADER : 0;

        header.nBlocksPerFrame = m_nSamplesPerFrame;

        // write the data to the file
        ByteArrayWriter writer = new ByteArrayWriter(APEDescriptor.APE_DESCRIPTOR_BYTES + APEHeaderNew.APE_HEADER_BYTES);
        APEDescriptor.write(writer);
        header.write(writer);
        pIO.write(writer.getBytes());

        // write an empty seek table
        m_spSeekTable = new long[nMaxFrames];
        Arrays.fill(m_spSeekTable, 0);
        byte[] zeroTable = new byte[nMaxFrames * 4];
        Arrays.fill(zeroTable, (byte) 0);
        pIO.write(zeroTable);
        m_nMaxFrames = nMaxFrames;

        // write the WAV data
        if ((pHeaderData != null) && (nHeaderBytes > 0) && (nHeaderBytes != IAPECompress.CREATE_WAV_HEADER_ON_DECOMPRESSION)) {
            m_spAPECompressCore.GetBitArray().GetMD5Helper().Update(pHeaderData, nHeaderBytes);
            pIO.write(pHeaderData, 0, nHeaderBytes);
        }
    }

    public void FinalizeFile(File pIO, int nNumberOfFrames, int nFinalFrameBlocks, byte[] pTerminatingData, int nTerminatingBytes, int nWAVTerminatingBytes, int nPeakLevel) throws IOException {
        // store the tail position
        int nTailPosition = (int) pIO.getFilePointer();

        // append the terminating data
        if (nTerminatingBytes > 0) {
            m_spAPECompressCore.GetBitArray().GetMD5Helper().Update(pTerminatingData, nTerminatingBytes);
            pIO.write(pTerminatingData, 0, nTerminatingBytes);
        }

        // go to the beginning and update the information
        pIO.seek(0);

        // get the descriptor
        APEDescriptor descriptor = APEDescriptor.read(pIO);

        // get the header
        APEHeaderNew header = APEHeaderNew.read(pIO);

        // update the header
        header.nFinalFrameBlocks = nFinalFrameBlocks;
        header.nTotalFrames = nNumberOfFrames;

        // update the descriptor
        descriptor.nAPEFrameDataBytes = nTailPosition - (descriptor.nDescriptorBytes + descriptor.nHeaderBytes + descriptor.nSeekTableBytes + descriptor.nHeaderDataBytes);
        descriptor.nAPEFrameDataBytesHigh = 0;
        descriptor.nTerminatingDataBytes = nTerminatingBytes;

        // update the MD5
        ByteArrayWriter writer = new ByteArrayWriter(APEHeaderNew.APE_HEADER_BYTES);
        header.write(writer);
        m_spAPECompressCore.GetBitArray().GetMD5Helper().Update(writer.getBytes());
        writer.reset(m_nMaxFrames * 4);
        for (int i = 0; i < m_nMaxFrames; i++) {
            writer.writeUnsignedInt(m_spSeekTable[i]);
        }
        byte[] seekTable = writer.getBytes();
        m_spAPECompressCore.GetBitArray().GetMD5Helper().Update(seekTable);
        descriptor.cFileMD5 = m_spAPECompressCore.GetBitArray().GetMD5Helper().Final();

        // set the pointer and re-write the updated header and peak level
        pIO.seek(0);
        writer.reset(descriptor.APE_DESCRIPTOR_BYTES + APEHeaderNew.APE_HEADER_BYTES);
        descriptor.write(writer);
        header.write(writer);
        pIO.write(writer.getBytes());
        // write the updated seek table
        pIO.write(seekTable);
    }

    public void SetSeekByte(int nFrame, int nByteOffset) {
        if (nFrame >= m_nMaxFrames)
            throw new JMACException("APE Compress Too Much Data");
        m_spSeekTable[nFrame] = nByteOffset;
    }

    public void Start(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes) throws IOException {
        Start(pioOutput, pwfeInput, nMaxAudioBytes, CompressionLevel.COMPRESSION_LEVEL_NORMAL, null, IAPECompress.CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void Start(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel) throws IOException {
        Start(pioOutput, pwfeInput, nMaxAudioBytes, nCompressionLevel, null, IAPECompress.CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void Start(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData) throws IOException {
        Start(pioOutput, pwfeInput, nMaxAudioBytes, nCompressionLevel, pHeaderData, IAPECompress.CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void Start(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData, int nHeaderBytes) throws IOException {
        // verify the parameters
        if (pioOutput == null || pwfeInput == null)
            throw new JMACException("Bad Parameters");

        // verify the wave format
        if ((pwfeInput.nChannels != 1) && (pwfeInput.nChannels != 2))
            throw new JMACException("Input File Unsupported Channel Count");
        if ((pwfeInput.wBitsPerSample != 8) && (pwfeInput.wBitsPerSample != 16) && (pwfeInput.wBitsPerSample != 24))
            throw new JMACException("Input File Unsupported Bit Depth");

        // initialize (creates the base classes)
        m_nSamplesPerFrame = 73728;
        if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_EXTRA_HIGH)
            m_nSamplesPerFrame *= 4;
        else if (nCompressionLevel == CompressionLevel.COMPRESSION_LEVEL_INSANE)
            m_nSamplesPerFrame *= 16;

        m_spIO = pioOutput;
        m_spAPECompressCore = new APECompressCore(m_spIO, pwfeInput, m_nSamplesPerFrame, nCompressionLevel);

        // copy the format
        m_wfeInput = pwfeInput;

        // the compression level
        m_nCompressionLevel = nCompressionLevel;
        m_nFrameIndex = 0;
        m_nLastFrameBlocks = m_nSamplesPerFrame;

        // initialize the file
        if (nMaxAudioBytes < 0)
            nMaxAudioBytes = 2147483647;

        long nMaxAudioBlocks = nMaxAudioBytes / pwfeInput.nBlockAlign;
        int nMaxFrames = (int) (nMaxAudioBlocks / m_nSamplesPerFrame);
        if ((nMaxAudioBlocks % m_nSamplesPerFrame) != 0) nMaxFrames++;

        InitializeFile(m_spIO, m_wfeInput, nMaxFrames,
                m_nCompressionLevel, pHeaderData, nHeaderBytes);
    }

    public int GetFullFrameBytes() {
        return m_nSamplesPerFrame * m_wfeInput.nBlockAlign;
    }

    public void EncodeFrame(ByteArrayReader pInputData, int nInputBytes) throws IOException {
        int nInputBlocks = nInputBytes / m_wfeInput.nBlockAlign;

        if ((nInputBlocks < m_nSamplesPerFrame) && (m_nLastFrameBlocks < m_nSamplesPerFrame))
            throw new JMACException("Bad Parameters");

        // update the seek table
        m_spAPECompressCore.GetBitArray().AdvanceToByteBoundary();
        SetSeekByte(m_nFrameIndex, (int) (m_spIO.getFilePointer() + (m_spAPECompressCore.GetBitArray().GetCurrentBitIndex() / 8)));

        // compress
        m_spAPECompressCore.EncodeFrame(pInputData, nInputBytes);

        // update stats
        m_nLastFrameBlocks = nInputBlocks;
        m_nFrameIndex++;
    }

    public void Finish(byte[] pTerminatingData, int nTerminatingBytes, int nWAVTerminatingBytes) throws IOException {
        // clear the bit array
        m_spAPECompressCore.GetBitArray().OutputBitArray(true);

        // finalize the file
        FinalizeFile(m_spIO, m_nFrameIndex, m_nLastFrameBlocks,
                pTerminatingData, nTerminatingBytes, nWAVTerminatingBytes, m_spAPECompressCore.GetPeakLevel());
    }

    private long[] m_spSeekTable;
    private int m_nMaxFrames;

    private File m_spIO;
    private APECompressCore m_spAPECompressCore;

    private WaveFormat m_wfeInput;
    private int m_nCompressionLevel;
    private int m_nSamplesPerFrame;
    private int m_nFrameIndex;
    private int m_nLastFrameBlocks;
}
