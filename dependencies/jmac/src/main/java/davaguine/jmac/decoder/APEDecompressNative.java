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

import davaguine.jmac.info.APEInfo;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APEDecompressNative extends APEDecompress {
    private final int ID;
    private int m_nRealFrame;

    static {
        System.loadLibrary("jmac");
    }

    public APEDecompressNative(APEInfo pAPEInfo) {
        this(pAPEInfo, -1, -1);
    }

    public APEDecompressNative(APEInfo pAPEInfo, int nStartBlock) {
        this(pAPEInfo, nStartBlock, -1);
    }

    public APEDecompressNative(APEInfo pAPEInfo, int nStartBlock, int nFinishBlock) {
        super(pAPEInfo, nStartBlock, nFinishBlock);
        ID = registerDecoder(this.getApeInfoIoSource(), this.getApeInfoFileVersion(), this.getApeInfoCompressionLevel(), nStartBlock, nFinishBlock, this.getApeInfoTotalBlocks(), this.getApeInfoBlockAlign(), this.getApeInfoBlocksPerFrame(), this.getApeInfoSampleRate(), this.getApeInfoBitsPerSample(), this.getApeInfoChannels());
        if (ID < 0)
            throw new JMACException("The Native APE Decoder Can't Be Instantiated");
        m_nRealFrame = 0;
    }

    public void finalize() {
        finalize(ID, this.getApeInfoIoSource());
    }

    public int GetData(byte[] pBuffer, int nBlocks) throws IOException {
        int nBlocksRetrieved = GetData(ID, this.getApeInfoIoSource(), pBuffer, nBlocks);
        m_nCurrentBlock += nBlocksRetrieved;
        return nBlocksRetrieved;
    }

    public void Seek(int nBlockOffset) throws IOException {
        Seek(ID, this.getApeInfoIoSource(), nBlockOffset);
    }

    private native int registerDecoder(File io, int nVersion, int nCompressionLevel, int nStartBlock, int nFinishBlock, int nTotalBlocks, int nBlockAlign, int nBlocksPerFrame, int nSampleRate, int nBitsPerSample, int nChannels);

    private native void finalize(int ID, File io);

    private native int GetData(int ID, File io, byte[] pBuffer, int nBlocks);

    private native void Seek(int ID, File io, int nBlockOffset) throws IOException;
}
