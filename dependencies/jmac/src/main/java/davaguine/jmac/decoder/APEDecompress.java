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

import davaguine.jmac.info.*;
import davaguine.jmac.prediction.IPredictorDecompress;
import davaguine.jmac.prediction.PredictorDecompress3950toCurrent;
import davaguine.jmac.prediction.PredictorDecompressNormal3930to3950;
import davaguine.jmac.tools.*;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APEDecompress extends IAPEDecompress {

    private final static int DECODE_BLOCK_SIZE = 4096;

    public APEDecompress(APEInfo pAPEInfo) {
        this(pAPEInfo, -1, -1);
    }

    public APEDecompress(APEInfo pAPEInfo, int nStartBlock) {
        this(pAPEInfo, nStartBlock, -1);
    }

    public APEDecompress(APEInfo pAPEInfo, int nStartBlock, int nFinishBlock) {
        // open / analyze the file
        m_spAPEInfo = pAPEInfo;

        // version check (this implementation only works with 3.93 and later files)
        if (m_spAPEInfo.getApeInfoFileVersion() < 3930)
            throw new JMACException("Unsupported Version");

        // get format information
        m_wfeInput = m_spAPEInfo.getApeInfoWaveFormatEx();
        m_nBlockAlign = m_spAPEInfo.getApeInfoBlockAlign();

        // initialize other stuff
        m_bDecompressorInitialized = false;
        m_nCurrentFrame = 0;
        m_nRealFrame = 0;
        m_nCurrentBlock = 0;
        m_nCurrentFrameBufferBlock = 0;
        m_nFrameBufferFinishedBlocks = 0;
        m_bErrorDecodingCurrentFrame = false;

        // set the "real" start and finish blocks
        m_nStartBlock = (nStartBlock < 0) ? 0 : Math.min(nStartBlock, m_spAPEInfo.getApeInfoTotalBlocks());
        m_nFinishBlock = (nFinishBlock < 0) ? m_spAPEInfo.getApeInfoTotalBlocks() : Math.min(nFinishBlock, m_spAPEInfo.getApeInfoTotalBlocks());
        m_bIsRanged = (m_nStartBlock != 0) || (m_nFinishBlock != m_spAPEInfo.getApeInfoTotalBlocks());
    }

    public int GetData(byte[] pBuffer, int nBlocks) throws IOException {
        InitializeDecompressor();

        // cap
        int nBlocksUntilFinish = m_nFinishBlock - m_nCurrentBlock;
        int nBlocksToRetrieve = Math.min(nBlocks, nBlocksUntilFinish);

        // get the data
        int nBlocksLeft = nBlocksToRetrieve;
        int nBlocksThisPass = 1;
        int index = 0;
        while ((nBlocksLeft > 0) && (nBlocksThisPass > 0)) {
            // fill up the frame buffer
            FillFrameBuffer();

            // analyze how much to remove from the buffer
            int nFrameBufferBlocks = m_nFrameBufferFinishedBlocks;
            nBlocksThisPass = Math.min(nBlocksLeft, nFrameBufferBlocks);

            // remove as much as possible
            if (nBlocksThisPass > 0) {
                m_cbFrameBuffer.Get(pBuffer, index, nBlocksThisPass * m_nBlockAlign);
                index += nBlocksThisPass * m_nBlockAlign;
                nBlocksLeft -= nBlocksThisPass;
                m_nFrameBufferFinishedBlocks -= nBlocksThisPass;
            }
        }

        // calculate the blocks retrieved
        int nBlocksRetrieved = nBlocksToRetrieve - nBlocksLeft;

        // update position
        m_nCurrentBlock += nBlocksRetrieved;
        return nBlocksRetrieved;
    }

    public void Seek(int nBlockOffset) throws IOException {
        InitializeDecompressor();

        // use the offset
        nBlockOffset += m_nStartBlock;

        // cap (to prevent seeking too far)
        if (nBlockOffset >= m_nFinishBlock)
            nBlockOffset = m_nFinishBlock - 1;
        if (nBlockOffset < m_nStartBlock)
            nBlockOffset = m_nStartBlock;

        // seek to the perfect location
        int nBaseFrame = nBlockOffset / m_spAPEInfo.getApeInfoBlocksPerFrame();
        int nBlocksToSkip = nBlockOffset % m_spAPEInfo.getApeInfoBlocksPerFrame();
        int nBytesToSkip = nBlocksToSkip * m_nBlockAlign;

        m_nCurrentBlock = nBaseFrame * this.getApeInfoBlocksPerFrame();
        m_nCurrentFrameBufferBlock = nBaseFrame * this.getApeInfoBlocksPerFrame();
        m_nCurrentFrame = nBaseFrame;
        m_nFrameBufferFinishedBlocks = 0;
        m_cbFrameBuffer.Empty();

        SeekToFrame(m_nCurrentFrame);

        // skip necessary blocks
        byte[] spTempBuffer = new byte[nBytesToSkip];

        int nBlocksRetrieved = GetData(spTempBuffer, nBlocksToSkip);
        if (nBlocksRetrieved != nBlocksToSkip)
            throw new JMACException("Undefined Error");
    }

    public int getApeInfoDecompressCurrentBlock() {
        return m_nCurrentBlock - m_nStartBlock;
    }

    public int getApeInfoDecompressCurrentMS() {
        int nSampleRate = m_spAPEInfo.getApeInfoSampleRate();
        if (nSampleRate > 0)
            return (int) ((m_nCurrentBlock * 1000L) / nSampleRate);
        return 0;
    }

    public int getApeInfoDecompressTotalBlocks() {
        return m_nFinishBlock - m_nStartBlock;
    }

    public int getApeInfoDecompressLengthMS() {
        int nSampleRate = m_spAPEInfo.getApeInfoSampleRate();
        if (nSampleRate > 0)
            return (int) (((m_nFinishBlock - m_nStartBlock) * 1000L) / nSampleRate);
        return 0;
    }

    public int getApeInfoDecompressCurrentBitRate() throws IOException {
        return m_spAPEInfo.getApeInfoFrameBitrate(m_nCurrentFrame);
    }

    public int getApeInfoDecompressAverageBitrate() throws IOException {
        if (m_bIsRanged || !m_spAPEInfo.getApeInfoIoSource().isLocal()) {
            // figure the frame range
            int nBlocksPerFrame = m_spAPEInfo.getApeInfoBlocksPerFrame();
            int nStartFrame = m_nStartBlock / nBlocksPerFrame;
            int nFinishFrame = (m_nFinishBlock + nBlocksPerFrame - 1) / nBlocksPerFrame;

            // get the number of bytes in the first and last frame
            int nTotalBytes = (m_spAPEInfo.getApeInfoFrameBytes(nStartFrame) * (m_nStartBlock % nBlocksPerFrame)) / nBlocksPerFrame;
            if (nFinishFrame != nStartFrame)
                nTotalBytes += (m_spAPEInfo.getApeInfoFrameBytes(nFinishFrame) * (m_nFinishBlock % nBlocksPerFrame)) / nBlocksPerFrame;

            // get the number of bytes in between
            int nTotalFrames = m_spAPEInfo.getApeInfoTotalFrames();
            for (int nFrame = nStartFrame + 1; (nFrame < nFinishFrame) && (nFrame < nTotalFrames); nFrame++)
                nTotalBytes += m_spAPEInfo.getApeInfoFrameBytes(nFrame);

            // figure the bitrate
            int nTotalMS = (int) (((m_nFinishBlock - m_nStartBlock) * 1000L) / m_spAPEInfo.getApeInfoSampleRate());
            if (nTotalMS != 0)
                return (nTotalBytes * 8) / nTotalMS;
        } else
            return m_spAPEInfo.getApeInfoAverageBitrate();
        return 0;
    }

    public int getApeInfoWavHeaderBytes() {
        if (m_bIsRanged)
            return WaveHeader.WAVE_HEADER_BYTES;
        return m_spAPEInfo.getApeInfoWavHeaderBytes();
    }

    public byte[] getApeInfoWavHeaderData(int nMaxBytes) {
        if (m_bIsRanged) {
            if (WaveHeader.WAVE_HEADER_BYTES > nMaxBytes)
                return null;
            else {
                WaveFormat wfeFormat = m_spAPEInfo.getApeInfoWaveFormatEx();
                WaveHeader WAVHeader = new WaveHeader();
                WaveHeader.FillWaveHeader(WAVHeader, (m_nFinishBlock - m_nStartBlock) * m_spAPEInfo.getApeInfoBlockAlign(), wfeFormat, 0);
                return WAVHeader.write();
            }
        }
        return m_spAPEInfo.getApeInfoWavHeaderData(nMaxBytes);
    }

    public int getApeInfoWavTerminatingBytes() {
        if (m_bIsRanged)
            return 0;
        else
            return m_spAPEInfo.getApeInfoWavTerminatingBytes();
    }

    public byte[] getApeInfoWavTerminatingData(int nMaxBytes) throws IOException {
        if (m_bIsRanged)
            return null;
        else
            return m_spAPEInfo.getApeInfoWavTerminatingData(nMaxBytes);
    }

    public WaveFormat getApeInfoWaveFormatEx() {
        return m_spAPEInfo.getApeInfoWaveFormatEx();
    }

    public File getApeInfoIoSource() {
        return m_spAPEInfo.getApeInfoIoSource();
    }

    public int getApeInfoBlocksPerFrame() {
        return m_spAPEInfo.getApeInfoBlocksPerFrame();
    }

    public int getApeInfoFileVersion() {
        return m_spAPEInfo.getApeInfoFileVersion();
    }

    public int getApeInfoCompressionLevel() {
        return m_spAPEInfo.getApeInfoCompressionLevel();
    }

    public int getApeInfoFormatFlags() {
        return m_spAPEInfo.getApeInfoFormatFlags();
    }

    public int getApeInfoSampleRate() {
        return m_spAPEInfo.getApeInfoSampleRate();
    }

    public int getApeInfoBitsPerSample() {
        return m_spAPEInfo.getApeInfoBitsPerSample();
    }

    public int getApeInfoBytesPerSample() {
        return m_spAPEInfo.getApeInfoBytesPerSample();
    }

    public int getApeInfoChannels() {
        return m_spAPEInfo.getApeInfoChannels();
    }

    public int getApeInfoBlockAlign() {
        return m_spAPEInfo.getApeInfoBlockAlign();
    }

    public int getApeInfoFinalFrameBlocks() {
        return m_spAPEInfo.getApeInfoFinalFrameBlocks();
    }

    public int getApeInfoTotalFrames() {
        return m_spAPEInfo.getApeInfoTotalFrames();
    }

    public int getApeInfoWavDataBytes() {
        return m_spAPEInfo.getApeInfoWavDataBytes();
    }

    public int getApeInfoWavTotalBytes() {
        return m_spAPEInfo.getApeInfoWavTotalBytes();
    }

    public int getApeInfoApeTotalBytes() {
        return m_spAPEInfo.getApeInfoApeTotalBytes();
    }

    public int getApeInfoTotalBlocks() {
        return m_spAPEInfo.getApeInfoTotalBlocks();
    }

    public int getApeInfoLengthMs() {
        return m_spAPEInfo.getApeInfoLengthMs();
    }

    public int getApeInfoAverageBitrate() {
        return m_spAPEInfo.getApeInfoAverageBitrate();
    }

    public int getApeInfoSeekByte(int nFrame) {
        return m_spAPEInfo.getApeInfoSeekByte(nFrame);
    }

    public int getApeInfoFrameBytes(int nFrame) throws IOException {
        return m_spAPEInfo.getApeInfoFrameBytes(nFrame);
    }

    public int getApeInfoFrameBlocks(int nFrame) {
        return m_spAPEInfo.getApeInfoFrameBlocks(nFrame);
    }

    public int getApeInfoFrameBitrate(int nFrame) throws IOException {
        return m_spAPEInfo.getApeInfoFrameBitrate(nFrame);
    }

    public int getApeInfoDecompressedBitrate() {
        return m_spAPEInfo.getApeInfoDecompressedBitrate();
    }

    public int getApeInfoPeakLevel() {
        return m_spAPEInfo.getApeInfoPeakLevel();
    }

    public int getApeInfoSeekBit(int nFrame) {
        return m_spAPEInfo.getApeInfoSeekBit(nFrame);
    }

    public APETag getApeInfoTag() {
        return m_spAPEInfo.getApeInfoTag();
    }

    public APEFileInfo getApeInfoInternalInfo() {
        return m_spAPEInfo.getApeInfoInternalInfo();
    }

    // file info
    protected int m_nBlockAlign;
    protected int m_nCurrentFrame;
    protected int m_nRealFrame;

    // start / finish information
    protected int m_nStartBlock;
    protected int m_nFinishBlock;
    protected int m_nCurrentBlock;
    protected boolean m_bIsRanged;
    protected boolean m_bDecompressorInitialized;

    // decoding tools
    protected Prepare m_Prepare = new Prepare();
    protected WaveFormat m_wfeInput;
    protected Crc32 m_nCRC;
    protected long m_nStoredCRC;
    protected int m_nSpecialCodes;

    public void SeekToFrame(int nFrameIndex) throws IOException {
        int nSeekRemainder = (m_spAPEInfo.getApeInfoSeekByte(nFrameIndex) - m_spAPEInfo.getApeInfoSeekByte(0)) % 4;
        m_spUnBitArray.FillAndResetBitArray(m_spAPEInfo.getApeInfoSeekByte(nFrameIndex) - nSeekRemainder, nSeekRemainder * 8);
        m_nRealFrame = nFrameIndex;
    }

    protected void DecodeBlocksToFrameBuffer(int nBlocks) throws IOException {
        // decode the samples
        int nBlocksProcessed = 0;

        try {
            if (m_wfeInput.nChannels == 2) {
                if ((m_nSpecialCodes & SpecialFrame.SPECIAL_FRAME_LEFT_SILENCE) > 0 &&
                        (m_nSpecialCodes & SpecialFrame.SPECIAL_FRAME_RIGHT_SILENCE) > 0) {
                    for (nBlocksProcessed = 0; nBlocksProcessed < nBlocks; nBlocksProcessed++) {
                        m_Prepare.unprepare(0, 0, m_wfeInput, m_cbFrameBuffer.GetDirectWritePointer(), m_nCRC);
                        m_cbFrameBuffer.UpdateAfterDirectWrite(m_nBlockAlign);
                    }
                } else if ((m_nSpecialCodes & SpecialFrame.SPECIAL_FRAME_PSEUDO_STEREO) > 0) {
                    for (nBlocksProcessed = 0; nBlocksProcessed < nBlocks; nBlocksProcessed++) {
                        int X = m_spNewPredictorX.DecompressValue(m_spUnBitArray.DecodeValueRange(m_BitArrayStateX));
                        m_Prepare.unprepare(X, 0, m_wfeInput, m_cbFrameBuffer.GetDirectWritePointer(), m_nCRC);
                        m_cbFrameBuffer.UpdateAfterDirectWrite(m_nBlockAlign);
                    }
                } else {
                    if (m_spAPEInfo.getApeInfoFileVersion() >= 3950) {
                        for (nBlocksProcessed = 0; nBlocksProcessed < nBlocks; nBlocksProcessed++) {
                            int nY = m_spUnBitArray.DecodeValueRange(m_BitArrayStateY);
                            int nX = m_spUnBitArray.DecodeValueRange(m_BitArrayStateX);
                            int Y = m_spNewPredictorY.DecompressValue(nY, m_nLastX);
                            int X = m_spNewPredictorX.DecompressValue(nX, Y);
                            m_nLastX = X;

                            m_Prepare.unprepare(X, Y, m_wfeInput, m_cbFrameBuffer.GetDirectWritePointer(), m_nCRC);
                            m_cbFrameBuffer.UpdateAfterDirectWrite(m_nBlockAlign);
                        }
                    } else {
                        for (nBlocksProcessed = 0; nBlocksProcessed < nBlocks; nBlocksProcessed++) {
                            int X = m_spNewPredictorX.DecompressValue(m_spUnBitArray.DecodeValueRange(m_BitArrayStateX));
                            int Y = m_spNewPredictorY.DecompressValue(m_spUnBitArray.DecodeValueRange(m_BitArrayStateY));

                            m_Prepare.unprepare(X, Y, m_wfeInput, m_cbFrameBuffer.GetDirectWritePointer(), m_nCRC);
                            m_cbFrameBuffer.UpdateAfterDirectWrite(m_nBlockAlign);
                        }
                    }
                }
            } else {
                if ((m_nSpecialCodes & SpecialFrame.SPECIAL_FRAME_MONO_SILENCE) > 0) {
                    for (nBlocksProcessed = 0; nBlocksProcessed < nBlocks; nBlocksProcessed++) {
                        m_Prepare.unprepare(0, 0, m_wfeInput, m_cbFrameBuffer.GetDirectWritePointer(), m_nCRC);
                        m_cbFrameBuffer.UpdateAfterDirectWrite(m_nBlockAlign);
                    }
                } else {
                    for (nBlocksProcessed = 0; nBlocksProcessed < nBlocks; nBlocksProcessed++) {
                        int X = m_spNewPredictorX.DecompressValue(m_spUnBitArray.DecodeValueRange(m_BitArrayStateX));
                        m_Prepare.unprepare(X, 0, m_wfeInput, m_cbFrameBuffer.GetDirectWritePointer(), m_nCRC);
                        m_cbFrameBuffer.UpdateAfterDirectWrite(m_nBlockAlign);
                    }
                }
            }
        } catch (JMACException e) {
            m_bErrorDecodingCurrentFrame = true;
        }

        m_nCurrentFrameBufferBlock += nBlocks;
    }

    protected void FillFrameBuffer() throws IOException {
        // determine the maximum blocks we can decode
        // note that we won't do end capping because we can't use data
        // until EndFrame(...) successfully handles the frame
        // that means we may decode a little extra in end capping cases
        // but this allows robust error handling of bad frames
        int nMaxBlocks = m_cbFrameBuffer.MaxAdd() / m_nBlockAlign;

        boolean invalidChecksum = false;

        // loop and decode data
        int nBlocksLeft = nMaxBlocks;
        while (nBlocksLeft > 0) {
            int nFrameBlocks = this.getApeInfoFrameBlocks(m_nCurrentFrame);
            if (nFrameBlocks < 0)
                break;

            int nFrameOffsetBlocks = m_nCurrentFrameBufferBlock % this.getApeInfoBlocksPerFrame();
            int nFrameBlocksLeft = nFrameBlocks - nFrameOffsetBlocks;
            int nBlocksThisPass = Math.min(nFrameBlocksLeft, nBlocksLeft);

            // start the frame if we need to
            if (nFrameOffsetBlocks == 0)
                StartFrame();

            // store the frame buffer bytes before we start
            int nFrameBufferBytes = m_cbFrameBuffer.MaxGet();

            // decode data
            DecodeBlocksToFrameBuffer(nBlocksThisPass);

            // end the frame if we need to
            if ((nFrameOffsetBlocks + nBlocksThisPass) >= nFrameBlocks) {
                EndFrame();
                if (m_bErrorDecodingCurrentFrame) {
                    // remove any decoded data from the buffer
                    m_cbFrameBuffer.RemoveTail(m_cbFrameBuffer.MaxGet() - nFrameBufferBytes);

                    // add silence
                    byte cSilence = (this.getApeInfoBitsPerSample() == 8) ? (byte) 127 : (byte) 0;
                    for (int z = 0; z < nFrameBlocks * m_nBlockAlign; z++) {
                        m_cbFrameBuffer.GetDirectWritePointer().append(cSilence);
                        m_cbFrameBuffer.UpdateAfterDirectWrite(1);
                    }

                    // seek to try to synchronize after an error
                    SeekToFrame(m_nCurrentFrame);

                    // save the return value
                    invalidChecksum = true;
                }
            }

            nBlocksLeft -= nBlocksThisPass;
        }

        if (invalidChecksum)
            throw new JMACException("Invalid Checksum");
    }

    protected void StartFrame() throws IOException {
        m_nCRC = new Crc32();

        // get the frame header
        m_nStoredCRC = m_spUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_INT);
        m_bErrorDecodingCurrentFrame = false;

        //get any 'special' codes if the file uses them (for silence, FALSE stereo, etc.)
        m_nSpecialCodes = 0;
        if (m_spAPEInfo.getApeInfoFileVersion() > 3820) {
            if ((m_nStoredCRC & 0x80000000L) > 0) {
                m_nSpecialCodes = (int) m_spUnBitArray.DecodeValue(DecodeValueMethod.DECODE_VALUE_METHOD_UNSIGNED_INT);
            }
            m_nStoredCRC &= 0x7fffffff;
        }

        m_spNewPredictorX.Flush();
        m_spNewPredictorY.Flush();

        m_spUnBitArray.FlushState(m_BitArrayStateX);
        m_spUnBitArray.FlushState(m_BitArrayStateY);

        m_spUnBitArray.FlushBitArray();

        m_nLastX = 0;
    }

    protected void EndFrame() {
        m_nFrameBufferFinishedBlocks += this.getApeInfoFrameBlocks(m_nCurrentFrame);
        m_nCurrentFrame++;
        // finalize
        m_spUnBitArray.Finalize();

        // check the CRC
        if (m_nCRC.checksum() != m_nStoredCRC)
            m_bErrorDecodingCurrentFrame = true;
    }

    protected void InitializeDecompressor() throws IOException {
        // check if we have anything to do
        if (m_bDecompressorInitialized)
            return;

        // update the initialized flag
        m_bDecompressorInitialized = true;

        // create a frame buffer
        m_cbFrameBuffer.CreateBuffer((this.getApeInfoBlocksPerFrame() + DECODE_BLOCK_SIZE) * m_nBlockAlign, m_nBlockAlign * 64);

        // create decoding components
        m_spUnBitArray = UnBitArrayBase.CreateUnBitArray(this, m_spAPEInfo.getApeInfoFileVersion());

        if (m_spAPEInfo.getApeInfoFileVersion() >= 3950) {
            m_spNewPredictorX = new PredictorDecompress3950toCurrent(m_spAPEInfo.getApeInfoCompressionLevel(), m_spAPEInfo.getApeInfoFileVersion());
            m_spNewPredictorY = new PredictorDecompress3950toCurrent(m_spAPEInfo.getApeInfoCompressionLevel(), m_spAPEInfo.getApeInfoFileVersion());
        } else {
            m_spNewPredictorX = new PredictorDecompressNormal3930to3950(m_spAPEInfo.getApeInfoCompressionLevel(), m_spAPEInfo.getApeInfoFileVersion());
            m_spNewPredictorY = new PredictorDecompressNormal3930to3950(m_spAPEInfo.getApeInfoCompressionLevel(), m_spAPEInfo.getApeInfoFileVersion());
        }

        // seek to the beginning
        Seek(-1);
    }

    // more decoding components
    protected APEInfo m_spAPEInfo;
    protected UnBitArrayBase m_spUnBitArray;
    protected UnBitArrayState m_BitArrayStateX = new UnBitArrayState();
    protected UnBitArrayState m_BitArrayStateY = new UnBitArrayState();

    protected IPredictorDecompress m_spNewPredictorX;
    protected IPredictorDecompress m_spNewPredictorY;

    protected int m_nLastX;

    // decoding buffer
    protected boolean m_bErrorDecodingCurrentFrame;
    protected int m_nCurrentFrameBufferBlock;
    protected int m_nFrameBufferFinishedBlocks;
    protected CircleBuffer m_cbFrameBuffer = new CircleBuffer();
}
