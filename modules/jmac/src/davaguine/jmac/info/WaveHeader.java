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

import java.io.EOFException;
import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class WaveHeader {

    public final static int WAVE_HEADER_BYTES = 44;

    // RIFF header
    public String cRIFFHeader;
    public long nRIFFBytes;

    // data type
    public String cDataTypeID;

    // wave format
    public String cFormatHeader;
    public long nFormatBytes;

    public int nFormatTag;
    public int nChannels;
    public long nSamplesPerSec;
    public long nAvgBytesPerSec;
    public int nBlockAlign;
    public int nBitsPerSample;

    // data chunk header
    public String cDataHeader;
    public long nDataBytes;

    public static void FillWaveHeader(WaveHeader pWAVHeader, int nAudioBytes, WaveFormat pWaveFormatEx, int nTerminatingBytes) {
        // RIFF header
        pWAVHeader.cRIFFHeader = "RIFF";
        pWAVHeader.nRIFFBytes = (nAudioBytes + 44) - 8 + nTerminatingBytes;

        // format header
        pWAVHeader.cDataTypeID = "WAVE";
        pWAVHeader.cFormatHeader = "fmt ";

        // the format chunk is the first 16 bytes of a waveformatex
        pWAVHeader.nFormatBytes = 16;
        pWAVHeader.nFormatTag = pWaveFormatEx.wFormatTag;
        pWAVHeader.nChannels = pWaveFormatEx.nChannels;
        pWAVHeader.nSamplesPerSec = pWaveFormatEx.nSamplesPerSec;
        pWAVHeader.nAvgBytesPerSec = pWaveFormatEx.nAvgBytesPerSec;
        pWAVHeader.nBlockAlign = pWaveFormatEx.nBlockAlign;
        pWAVHeader.nBitsPerSample = pWaveFormatEx.wBitsPerSample;

        // the data header
        pWAVHeader.cDataHeader = "data";
        pWAVHeader.nDataBytes = nAudioBytes;
    }

    public final static WaveHeader read(final File file) throws IOException {
        try {
            final ByteArrayReader reader = new ByteArrayReader(file, WAVE_HEADER_BYTES);
            return read(reader);
        } catch (EOFException e) {
            return null;
        }
    }

    public final static WaveHeader read(final byte[] data) {
        final ByteArrayReader reader = new ByteArrayReader(data);
        return read(reader);
    }

    private final static WaveHeader read(final ByteArrayReader reader) {
        final WaveHeader header = new WaveHeader();
        header.cRIFFHeader = reader.readString(4, "US-ASCII");
        header.nRIFFBytes = reader.readUnsignedInt();
        header.cDataTypeID = reader.readString(4, "US-ASCII");
        header.cFormatHeader = reader.readString(4, "US-ASCII");
        header.nFormatBytes = reader.readUnsignedInt();
        header.nFormatTag = reader.readUnsignedShort();
        header.nChannels = reader.readUnsignedShort();
        header.nSamplesPerSec = reader.readUnsignedInt();
        header.nAvgBytesPerSec = reader.readUnsignedInt();
        header.nBlockAlign = reader.readUnsignedShort();
        header.nBitsPerSample = reader.readUnsignedShort();
        header.cDataHeader = reader.readString(4, "US-ASCII");
        header.nDataBytes = reader.readUnsignedInt();
        return header;
    }

    public final byte[] write() {
        final ByteArrayWriter writer = new ByteArrayWriter(WAVE_HEADER_BYTES);
        writer.writeString(cRIFFHeader, 4);
        writer.writeUnsignedInt(nRIFFBytes);
        writer.writeString(cDataTypeID, 4);
        writer.writeString(cFormatHeader, 4);
        writer.writeUnsignedInt(nFormatBytes);
        writer.writeUnsignedShort(nFormatTag);
        writer.writeUnsignedShort(nChannels);
        writer.writeUnsignedInt(nSamplesPerSec);
        writer.writeUnsignedInt(nAvgBytesPerSec);
        writer.writeUnsignedShort(nBlockAlign);
        writer.writeUnsignedShort(nBitsPerSample);
        writer.writeString(cDataHeader, 4);
        writer.writeUnsignedInt(nDataBytes);
        return writer.getBytes();
    }
}