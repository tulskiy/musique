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
package davaguine.jmac.core;

import davaguine.jmac.decoder.IAPEDecompress;
import davaguine.jmac.encoder.IAPECompress;
import davaguine.jmac.info.APEFileInfo;
import davaguine.jmac.info.InputSource;
import davaguine.jmac.info.WaveFormat;
import davaguine.jmac.tools.*;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Author: Dmitry Vaguine
 * Date: 11.05.2004
 * Time: 16:26:19
 */
public class APESimple {
    public final static int UNMAC_DECODER_OUTPUT_APE = 2;
    public final static int UNMAC_DECODER_OUTPUT_WAV = 1;
    public final static int UNMAC_DECODER_OUTPUT_NONE = 0;
    public final static int BLOCKS_PER_DECODE = 9216;

    public static void VerifyFile(String pInputFilename, ProgressCallback progressor) throws IOException, JMACSkippedException, JMACStoppedByUserException {
        // error check the function parameters
        if (pInputFilename == null)
            throw new JMACException("Bad Parameters");

        // see if we can quick verify
        File file = File.createFile(pInputFilename, "r");
        IAPEDecompress spAPEDecompress = null;
        try {
            spAPEDecompress = IAPEDecompress.CreateIAPEDecompress(file);

            APEFileInfo pInfo = spAPEDecompress.getApeInfoInternalInfo();

            if ((pInfo.nVersion < 3980) || (pInfo.spAPEDescriptor == null))
                DecompressCore(spAPEDecompress, null, UNMAC_DECODER_OUTPUT_NONE, -1, progressor);
            else {
                MD5 MD5Helper = new MD5();

                File pIO = spAPEDecompress.getApeInfoIoSource();

                int nHead = (int) (pInfo.nJunkHeaderBytes + pInfo.spAPEDescriptor.nDescriptorBytes);
                int nStart = (int) (nHead + pInfo.spAPEDescriptor.nHeaderBytes + pInfo.spAPEDescriptor.nSeekTableBytes);

                pIO.seek(nHead);
                int nHeadBytes = nStart - nHead;
                byte[] spHeadBuffer = new byte[nHeadBytes];
                pIO.readFully(spHeadBuffer);

                int nBytesLeft = (int) (pInfo.spAPEDescriptor.nHeaderDataBytes + pInfo.spAPEDescriptor.nAPEFrameDataBytes + pInfo.spAPEDescriptor.nTerminatingDataBytes);
                // create the progress helper
                ProgressHelper spMACProgressHelper = new ProgressHelper(nBytesLeft / 16384, progressor);
                byte[] spBuffer = new byte[16384];
                int nBytesRead = 1;
                while ((nBytesLeft > 0) && (nBytesRead > 0)) {
                    int nBytesToRead = Math.min(16384, nBytesLeft);
                    nBytesRead = pIO.read(spBuffer, 0, nBytesToRead);

                    MD5Helper.Update(spBuffer, nBytesRead);
                    nBytesLeft -= nBytesRead;

                    spMACProgressHelper.UpdateProgress();
                    if (spMACProgressHelper.isKillFlag())
                        throw new JMACStoppedByUserException();
                }

                if (nBytesLeft != 0)
                    throw new JMACException("The File Is Broken");

                MD5Helper.Update(spHeadBuffer, nHeadBytes);

                // fire the "complete" progress notification
                spMACProgressHelper.UpdateProgressComplete();

                byte[] cResult = MD5Helper.Final();
                for (int i = 0; i < 16; i++)
                    if (cResult[i] != pInfo.spAPEDescriptor.cFileMD5[i])
                        throw new JMACException("Invalid Checksum");
            }
        } finally {
            file.close();
        }
    }

    /**
     * **************************************************************************************
     * Convert file
     * ***************************************************************************************
     */
    public static void ConvertFile(String pInputFilename, String pOutputFilename, int nCompressionLevel, ProgressCallback progressor) throws IOException, JMACSkippedException, JMACStoppedByUserException {
        DecompressCore(pInputFilename, pOutputFilename, UNMAC_DECODER_OUTPUT_APE, nCompressionLevel, progressor);
    }

    /**
     * **************************************************************************************
     * Decompress file
     * ***************************************************************************************
     */
    public static void DecompressFile(String pInputFilename, String pOutputFilename, ProgressCallback progressor) throws IOException, JMACSkippedException, JMACStoppedByUserException {
        if (pOutputFilename == null)
            VerifyFile(pInputFilename, progressor);
        else
            DecompressCore(pInputFilename, pOutputFilename, UNMAC_DECODER_OUTPUT_WAV, -1, progressor);
    }

    public static void DecompressCore(String pInputFilename, String pOutputFilename, int nOutputMode, int nCompressionLevel, ProgressCallback progressor) throws IOException, JMACSkippedException, JMACStoppedByUserException {
        // error check the function parameters
        if (pInputFilename == null)
            throw new JMACException("Bad Parameters");

        // variable declares
        IAPEDecompress spAPEDecompress = null;
        File file = File.createFile(pInputFilename, "r");
        try {
            spAPEDecompress = IAPEDecompress.CreateIAPEDecompress(file);
            DecompressCore(spAPEDecompress, pOutputFilename, nOutputMode, nCompressionLevel, progressor);
        } finally {
            file.close();
        }
    }

    public static void DecompressCore(IAPEDecompress spAPEDecompress, String pOutputFilename, int nOutputMode, int nCompressionLevel, ProgressCallback progressor) throws IOException, JMACSkippedException, JMACStoppedByUserException {
        // variable declares
        java.io.RandomAccessFile spioOutput = null;
        IAPECompress spAPECompress = null;

        try {
            // create the core
            WaveFormat wfeInput = spAPEDecompress.getApeInfoWaveFormatEx();

            // allocate space for the header
            byte[] waveHeaderBuffer = spAPEDecompress.getApeInfoWavHeaderData(spAPEDecompress.getApeInfoWavHeaderBytes());

            // initialize the output
            if (nOutputMode == UNMAC_DECODER_OUTPUT_WAV) {
                // create the file
                spioOutput = new RandomAccessFile(pOutputFilename, "rw");

                // output the header
                spioOutput.write(waveHeaderBuffer);
            } else if (nOutputMode == UNMAC_DECODER_OUTPUT_APE) {
                // quit if there is nothing to do
                if (spAPEDecompress.getApeInfoFileVersion() == Globals.MAC_VERSION_NUMBER && spAPEDecompress.getApeInfoCompressionLevel() == nCompressionLevel)
                    throw new JMACSkippedException();

                // create and start the compressor
                spAPECompress = IAPECompress.CreateIAPECompress();
                spAPECompress.Start(pOutputFilename, wfeInput, spAPEDecompress.getApeInfoDecompressTotalBlocks() * spAPEDecompress.getApeInfoBlockAlign(),
                        nCompressionLevel, waveHeaderBuffer, spAPEDecompress.getApeInfoWavHeaderBytes());
            }

            int blockAlign = spAPEDecompress.getApeInfoBlockAlign();
            // allocate space for decompression
            byte[] spTempBuffer = new byte[blockAlign * BLOCKS_PER_DECODE];

            int nBlocksLeft = spAPEDecompress.getApeInfoDecompressTotalBlocks();

            // create the progress helper
            ProgressHelper spMACProgressHelper = new ProgressHelper(nBlocksLeft / BLOCKS_PER_DECODE, progressor);

            // main decoding loop
            while (nBlocksLeft > 0) {
                // decode data
                int nBlocksDecoded = spAPEDecompress.GetData(spTempBuffer, BLOCKS_PER_DECODE);

                // handle the output
                if (nOutputMode == UNMAC_DECODER_OUTPUT_WAV)
                    spioOutput.write(spTempBuffer, 0, nBlocksDecoded * blockAlign);
                else if (nOutputMode == UNMAC_DECODER_OUTPUT_APE)
                    spAPECompress.AddData(spTempBuffer, nBlocksDecoded * spAPEDecompress.getApeInfoBlockAlign());

                // update amount remaining
                nBlocksLeft -= nBlocksDecoded;

                // update progress and kill flag
                spMACProgressHelper.UpdateProgress();
                if (spMACProgressHelper.isKillFlag())
                    throw new JMACStoppedByUserException();
            }

            // terminate the output
            if (nOutputMode == UNMAC_DECODER_OUTPUT_WAV) {
                // write any terminating WAV data
                if (spAPEDecompress.getApeInfoWavTerminatingBytes() > 0) {
                    byte[] termData = spAPEDecompress.getApeInfoWavTerminatingData(spAPEDecompress.getApeInfoWavTerminatingBytes());

                    int nBytesToWrite = spAPEDecompress.getApeInfoWavTerminatingBytes();
                    spioOutput.write(termData, 0, nBytesToWrite);
                }
            } else if (nOutputMode == UNMAC_DECODER_OUTPUT_APE) {
                // write the WAV data and any tag
                int nTagBytes = spAPEDecompress.getApeInfoTag().GetTagBytes();
                boolean bHasTag = (nTagBytes > 0);
                int nTerminatingBytes = nTagBytes;
                nTerminatingBytes += spAPEDecompress.getApeInfoWavTerminatingBytes();

                if (nTerminatingBytes > 0) {
                    spTempBuffer = spAPEDecompress.getApeInfoWavTerminatingData(nTerminatingBytes);

                    if (bHasTag) {
                        spAPEDecompress.getApeInfoIoSource().seek(spAPEDecompress.getApeInfoIoSource().length() - nTagBytes);
                        spAPEDecompress.getApeInfoIoSource().read(spTempBuffer, spAPEDecompress.getApeInfoWavTerminatingBytes(), nTagBytes);
                    }

                    spAPECompress.Finish(spTempBuffer, nTerminatingBytes, spAPEDecompress.getApeInfoWavTerminatingBytes());
                } else
                    spAPECompress.Finish(null, 0, 0);
            }

            // fire the "complete" progress notification
            spMACProgressHelper.UpdateProgressComplete();
        } finally {
            if (spioOutput != null)
                spioOutput.close();
            if (spAPECompress != null)
                spAPECompress.Kill();
        }
    }

    public static void CompressFile(String pInputFilename, String pOutputFilename, int nCompressionLevel, ProgressCallback progressor) throws IOException, JMACStoppedByUserException {
        // declare the variables
        IAPECompress spAPECompress = null;
        InputSource spInputSource = null;

        try {
            byte[] spBuffer = null;

            WaveFormat WaveFormatEx = new WaveFormat();

            // create the input source
            IntegerPointer nAudioBlocks = new IntegerPointer();
            nAudioBlocks.value = 0;
            IntegerPointer nHeaderBytes = new IntegerPointer();
            nHeaderBytes.value = 0;
            IntegerPointer nTerminatingBytes = new IntegerPointer();
            nTerminatingBytes.value = 0;
            spInputSource = InputSource.CreateInputSource(pInputFilename, WaveFormatEx, nAudioBlocks,
                    nHeaderBytes, nTerminatingBytes);

            // create the compressor
            spAPECompress = IAPECompress.CreateIAPECompress();

            // figure the audio bytes
            int nAudioBytes = nAudioBlocks.value * WaveFormatEx.nBlockAlign;

            // start the encoder
            if (nHeaderBytes.value > 0) spBuffer = new byte[nHeaderBytes.value];
            spInputSource.GetHeaderData(spBuffer);
            spAPECompress.Start(pOutputFilename, WaveFormatEx, nAudioBytes,
                    nCompressionLevel, spBuffer, nHeaderBytes.value);

            // set-up the progress
            ProgressHelper spMACProgressHelper = new ProgressHelper(nAudioBytes, progressor);

            // master loop
            int nBytesLeft = nAudioBytes;

            spMACProgressHelper.UpdateStatus("Process data by compressor");

            while (nBytesLeft > 0) {
                int nBytesAdded = spAPECompress.AddDataFromInputSource(spInputSource, nBytesLeft);

                nBytesLeft -= nBytesAdded;

                // update the progress
                spMACProgressHelper.UpdateProgress(nAudioBytes - nBytesLeft);

                // process the kill flag
                if (spMACProgressHelper.isKillFlag())
                    throw new JMACStoppedByUserException();
            }

            spMACProgressHelper.UpdateStatus("Finishing compression");

            // finalize the file
            if (nTerminatingBytes.value > 0) spBuffer = new byte[nTerminatingBytes.value];
            spInputSource.GetTerminatingData(spBuffer);
            spAPECompress.Finish(spBuffer, nTerminatingBytes.value, nTerminatingBytes.value);

            // update the progress to 100%
            spMACProgressHelper.UpdateStatus("Compression finished");
        } finally {
            // kill the compressor if we failed
            if (spAPECompress != null)
                spAPECompress.Kill();
            if (spInputSource != null)
                spInputSource.Close();
        }
    }
}
