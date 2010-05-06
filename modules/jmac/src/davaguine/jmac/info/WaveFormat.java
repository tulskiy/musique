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
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class WaveFormat {

    public short wFormatTag;         /* format type */
    public short nChannels;          /* number of channels (i.e. mono, stereo...) */
    public int nSamplesPerSec;       /* sample rate */
    public int nAvgBytesPerSec;      /* for buffer estimation */
    public short nBlockAlign;        /* block size of data */
    public short wBitsPerSample;     /* number of bits per sample of mono data */
    public short cbSize;             /* the count in bytes of the size of */

    public final static int WAV_HEADER_SIZE = 16;

    public static void FillWaveFormatEx(WaveFormat pWaveFormatEx, int nSampleRate, int nBitsPerSample, int nChannels) {
        pWaveFormatEx.cbSize = 0;
        pWaveFormatEx.nSamplesPerSec = nSampleRate;
        pWaveFormatEx.wBitsPerSample = (short) nBitsPerSample;
        pWaveFormatEx.nChannels = (short) nChannels;
        pWaveFormatEx.wFormatTag = 1;

        pWaveFormatEx.nBlockAlign = (short) ((pWaveFormatEx.wBitsPerSample / 8) * pWaveFormatEx.nChannels);
        pWaveFormatEx.nAvgBytesPerSec = pWaveFormatEx.nBlockAlign * pWaveFormatEx.nSamplesPerSec;
    }

    public void readHeader(File io) throws IOException {
        ByteArrayReader reader = new ByteArrayReader(io, WAV_HEADER_SIZE);
        wFormatTag = reader.readShort();
        nChannels = reader.readShort();
        nSamplesPerSec = reader.readInt();
        nAvgBytesPerSec = reader.readInt();
        nBlockAlign = reader.readShort();
        wBitsPerSample = reader.readShort();
    }

}
