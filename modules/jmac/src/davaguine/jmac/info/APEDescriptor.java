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
 * Date: 07.04.2004
 * Time: 14:36:53
 */
public class APEDescriptor {
    public String cID;					// should equal 'MAC ' (char[4])
    public int nVersion;				// version number * 1000 (3.81 = 3810) (unsigned short)

    public long nDescriptorBytes;		// the number of descriptor bytes (allows later expansion of this header) (unsigned int32)
    public long nHeaderBytes;			// the number of header APE_HEADER bytes (unsigned int32)
    public long nSeekTableBytes;		// the number of bytes of the seek table (unsigned int32)
    public long nHeaderDataBytes;		// the number of header data bytes (from original file) (unsigned int32)
    public long nAPEFrameDataBytes;		// the number of bytes of APE frame data (unsigned int32)
    public long nAPEFrameDataBytesHigh;	// the high order number of APE frame data bytes (unsigned int32)
    public long nTerminatingDataBytes;	// the terminating data of the file (not including tag data) (unsigned int32)

    public byte[] cFileMD5 = new byte[16]; // the MD5 hash of the file (see notes for usage... it's a littly tricky) (unsigned char[16])

    public final static int APE_DESCRIPTOR_BYTES = 52;

    public static APEDescriptor read(final File file) throws IOException {
        try {
            APEDescriptor header = new APEDescriptor();
            final ByteArrayReader reader = new ByteArrayReader(file, APE_DESCRIPTOR_BYTES - 16);
            header.cID = reader.readString(4, "US-ASCII");
            header.nVersion = reader.readUnsignedShort();
            reader.skipBytes(2);
            header.nDescriptorBytes = reader.readUnsignedInt();
            header.nHeaderBytes = reader.readUnsignedInt();
            header.nSeekTableBytes = reader.readUnsignedInt();
            header.nHeaderDataBytes = reader.readUnsignedInt();
            header.nAPEFrameDataBytes = reader.readUnsignedInt();
            header.nAPEFrameDataBytesHigh = reader.readUnsignedInt();
            header.nTerminatingDataBytes = reader.readUnsignedInt();
            file.readFully(header.cFileMD5);
            return header;
        } catch (EOFException e) {
            throw new JMACException("Unsupported Format");
        }
    }

    public void write(ByteArrayWriter writer) {
        writer.writeString(cID, 4);
        writer.writeUnsignedShort(nVersion);
        writer.writeUnsignedShort(0);
        writer.writeUnsignedInt(nDescriptorBytes);
        writer.writeUnsignedInt(nHeaderBytes);
        writer.writeUnsignedInt(nSeekTableBytes);
        writer.writeUnsignedInt(nHeaderDataBytes);
        writer.writeUnsignedInt(nAPEFrameDataBytes);
        writer.writeUnsignedInt(nAPEFrameDataBytesHigh);
        writer.writeUnsignedInt(nTerminatingDataBytes);
        writer.writeBytes(cFileMD5);
    }
}
