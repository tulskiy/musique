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

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 07.05.2004
 * Time: 16:59:37
 */
public class RiffChunkHeader {
    public int cChunkLabel;		// should equal "data" indicating the data chunk (4 chars)
    public long nChunkBytes;        // the bytes of the chunk

    private final static int RIFF_CHUNK_HEADER_SIZE = 8;

    public void read(File io) throws IOException {
        ByteArrayReader reader = new ByteArrayReader(io, RIFF_CHUNK_HEADER_SIZE);
        cChunkLabel = reader.readInt();
        nChunkBytes = reader.readUnsignedInt();
    }
}
