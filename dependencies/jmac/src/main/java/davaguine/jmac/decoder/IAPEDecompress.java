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
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.Globals;
import davaguine.jmac.tools.JMACException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public abstract class IAPEDecompress {

    //////////////////////////////////////////////////////////////////////////////////////////////
    // GetData(...) - gets raw decompressed audio
    //
    // Parameters:
    //	char * pBuffer
    //		a pointer to a buffer to put the data into
    //	int nBlocks
    //		the number of audio blocks desired (see note at intro about blocks vs. samples)
    //	int * pBlocksRetrieved
    //		the number of blocks actually retrieved (could be less at end of file or on critical failure)
    //////////////////////////////////////////////////////////////////////////////////////////////

    public abstract int GetData(byte[] pBuffer, int nBlocks) throws IOException;

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Seek(...) - seeks
    //
    // Parameters:
    //	int nBlockOffset
    //		the block to seek to (see note at intro about blocks vs. samples)
    //////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void Seek(int nBlockOffset) throws IOException;

    /**
     * ******************************************************************************************
     * Get Information
     * *******************************************************************************************
     */
    public abstract int getApeInfoDecompressCurrentBlock();

    public abstract int getApeInfoDecompressCurrentMS();

    public abstract int getApeInfoDecompressTotalBlocks();

    public abstract int getApeInfoDecompressLengthMS();

    public abstract int getApeInfoDecompressCurrentBitRate() throws IOException;

    public abstract int getApeInfoDecompressAverageBitrate() throws IOException;

    public abstract File getApeInfoIoSource();

    public abstract int getApeInfoBlocksPerFrame();

    public abstract int getApeInfoFileVersion();

    public abstract int getApeInfoCompressionLevel();

    public abstract int getApeInfoFormatFlags();

    public abstract int getApeInfoSampleRate();

    public abstract int getApeInfoBitsPerSample();

    public abstract int getApeInfoBytesPerSample();

    public abstract int getApeInfoChannels();

    public abstract int getApeInfoBlockAlign();

    public abstract int getApeInfoFinalFrameBlocks();

    public abstract int getApeInfoTotalFrames();

    public abstract int getApeInfoWavHeaderBytes();

    public abstract int getApeInfoWavTerminatingBytes();

    public abstract int getApeInfoWavDataBytes();

    public abstract int getApeInfoWavTotalBytes();

    public abstract int getApeInfoApeTotalBytes();

    public abstract int getApeInfoTotalBlocks();

    public abstract int getApeInfoLengthMs();

    public abstract int getApeInfoAverageBitrate();

    public abstract int getApeInfoSeekByte(int nFrame);

    public abstract int getApeInfoFrameBytes(int nFrame) throws IOException;

    public abstract int getApeInfoFrameBlocks(int nFrame);

    public abstract int getApeInfoFrameBitrate(int nFrame) throws IOException;

    public abstract int getApeInfoDecompressedBitrate();

    public abstract int getApeInfoPeakLevel();

    public abstract int getApeInfoSeekBit(int nFrame);

    public abstract WaveFormat getApeInfoWaveFormatEx();

    public abstract byte[] getApeInfoWavHeaderData(int nMaxBytes);

    public abstract APETag getApeInfoTag();

    public abstract byte[] getApeInfoWavTerminatingData(int nMaxBytes) throws IOException;

    public abstract APEFileInfo getApeInfoInternalInfo();

    public static IAPEDecompress CreateIAPEDecompressCore(APEInfo pAPEInfo, int nStartBlock, int nFinishBlock) {
        IAPEDecompress pAPEDecompress = null;
        if (pAPEInfo != null) {
            if (pAPEInfo.getApeInfoFileVersion() >= 3930) {
//                if (Globals.isNative())
//                    pAPEDecompress = new APEDecompressNative(pAPEInfo, nStartBlock, nFinishBlock);
//                else
                pAPEDecompress = new APEDecompress(pAPEInfo, nStartBlock, nFinishBlock);
            } else
                pAPEDecompress = new APEDecompressOld(pAPEInfo, nStartBlock, nFinishBlock);
        }

        return pAPEDecompress;
    }

    public static APEInfo CreateAPEInfo(File in) throws IOException {
        // variables
        APEInfo pAPEInfo = null;

        // get the extension
        if (in.isLocal()) {
            final String pExtension = in.getExtension();

            // take the appropriate action (based on the extension)
            if (pExtension.toLowerCase().equals(".mac") || pExtension.toLowerCase().equals(".ape"))
                // plain .ape file
                pAPEInfo = new APEInfo(in);
        } else
            pAPEInfo = new APEInfo(in);

        // fail if we couldn't get the file information
        if (pAPEInfo == null)
            throw new JMACException("Invalid Input File");
        return pAPEInfo;
    }

    public static IAPEDecompress CreateIAPEDecompress(File in) throws IOException {
        // variables
        APEInfo pAPEInfo = null;
        int nStartBlock = -1;
        int nFinishBlock = -1;

        // get the extension
        if (in.isLocal()) {
            final String pFilename = in.getFilename();
            final String pExtension = in.getExtension();

            // take the appropriate action (based on the extension)
            if (pExtension.toLowerCase().equals(".apl")) {
                // "link" file (.apl linked large APE file)
                APELink APELink = new APELink(pFilename);
                if (APELink.GetIsLinkFile()) {
                    URL url = null;
                    try {
                        url = new URL(APELink.GetImageFilename());
                        pAPEInfo = new APEInfo(url);
                    } catch (MalformedURLException e) {
                        pAPEInfo = new APEInfo(new java.io.File(APELink.GetImageFilename()));
                    }
                    nStartBlock = APELink.GetStartBlock();
                    nFinishBlock = APELink.GetFinishBlock();
                }
            } else if (pExtension.toLowerCase().equals(".mac") || pExtension.toLowerCase().equals(".ape"))
                // plain .ape file
                pAPEInfo = new APEInfo(in);
        } else
            pAPEInfo = new APEInfo(in);

        // fail if we couldn't get the file information
        if (pAPEInfo == null)
            throw new JMACException("Invalid Input File");

        // create and return
        IAPEDecompress pAPEDecompress = CreateIAPEDecompressCore(pAPEInfo, nStartBlock, nFinishBlock);
        return pAPEDecompress;
    }

    public static IAPEDecompress CreateIAPEDecompressEx(APEInfo pAPEInfo, int nStartBlock, int nFinishBlock) {
        return CreateIAPEDecompressCore(pAPEInfo, nStartBlock, nFinishBlock);
    }

}
