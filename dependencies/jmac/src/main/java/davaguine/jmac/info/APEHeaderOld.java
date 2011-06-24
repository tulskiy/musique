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
public class APEHeaderOld {
    public String cID;					// should equal 'MAC '
    public int nVersion;				// version number * 1000 (3.81 = 3810)
    public int nCompressionLevel;	    // the compression level
    public int nFormatFlags;			// any format flags (for future use)
    public int nChannels;			    // the number of channels (1 or 2)
    public long nSampleRate;			// the sample rate (typically 44100)
    public long nHeaderBytes;			// the bytes after the MAC header that compose the WAV header
    public long nTerminatingBytes;		// the bytes after that raw data (for extended info)
    public long nTotalFrames;			// the number of frames in the file
    public long nFinalFrameBlocks;		// the number of samples in the final frame

    public final static int APE_HEADER_OLD_BYTES = 32;

    public static APEHeaderOld read(final File file) throws IOException {
        try {
            APEHeaderOld header = new APEHeaderOld();
            final ByteArrayReader reader = new ByteArrayReader(file, APE_HEADER_OLD_BYTES);
            header.cID = reader.readString(4, "US-ASCII");
            header.nVersion = reader.readUnsignedShort();
            header.nCompressionLevel = reader.readUnsignedShort();
            header.nFormatFlags = reader.readUnsignedShort();
            header.nChannels = reader.readUnsignedShort();
            header.nSampleRate = reader.readUnsignedInt();
            header.nHeaderBytes = reader.readUnsignedInt();
            header.nTerminatingBytes = reader.readUnsignedInt();
            header.nTotalFrames = reader.readUnsignedInt();
            header.nFinalFrameBlocks = reader.readUnsignedInt();
            return header;
        } catch (EOFException e) {
            throw new JMACException("Unsupported Format");
        }
    }
}
