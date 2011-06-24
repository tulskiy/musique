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
import davaguine.jmac.tools.ByteArrayWriter;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;

import java.io.EOFException;
import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APEHeaderNew {
    public int nCompressionLevel;	    // the compression level (unsigned short)
    public int nFormatFlags;			// any format flags (for future use) (unsigned short)

    public long nBlocksPerFrame;		// the number of audio blocks in one frame (unsigned int)
    public long nFinalFrameBlocks;		// the number of audio blocks in the final frame (unsigned int)
    public long nTotalFrames;			// the total number of frames (unsigned int)

    public int nBitsPerSample;			// the bits per sample (typically 16) (unsigned short)
    public int nChannels;				// the number of channels (1 or 2) (unsigned short)
    public long nSampleRate;			// the sample rate (typically 44100) (unsigned int)

    public final static int APE_HEADER_BYTES = 24;

    public static APEHeaderNew read(final File file) throws IOException {
        try {
            APEHeaderNew header = new APEHeaderNew();
            final ByteArrayReader reader = new ByteArrayReader(file, APE_HEADER_BYTES);
            header.nCompressionLevel = reader.readUnsignedShort();
            header.nFormatFlags = reader.readUnsignedShort();
            header.nBlocksPerFrame = reader.readUnsignedInt();
            header.nFinalFrameBlocks = reader.readUnsignedInt();
            header.nTotalFrames = reader.readUnsignedInt();
            header.nBitsPerSample = reader.readUnsignedShort();
            header.nChannels = reader.readUnsignedShort();
            header.nSampleRate = reader.readUnsignedInt();
            return header;
        } catch (EOFException e) {
            throw new JMACException("Unsupported Format");
        }
    }

    public void write(ByteArrayWriter writer) {
        writer.writeUnsignedShort(nCompressionLevel);
        writer.writeUnsignedShort(nFormatFlags);
        writer.writeUnsignedInt(nBlocksPerFrame);
        writer.writeUnsignedInt(nFinalFrameBlocks);
        writer.writeUnsignedInt(nTotalFrames);
        writer.writeUnsignedShort(nBitsPerSample);
        writer.writeUnsignedShort(nChannels);
        writer.writeUnsignedInt(nSampleRate);
    }
}
