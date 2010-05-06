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

package davaguine.jmac.tools;

import davaguine.jmac.info.SpecialFrame;
import davaguine.jmac.info.WaveFormat;


/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class Prepare {

    public void prepare(ByteArrayReader pRawData, int nBytes, final WaveFormat pWaveFormatEx, int[] pOutputX, int[] pOutputY, Crc32 pCRC, IntegerPointer pSpecialCodes, IntegerPointer pPeakLevel) {
        // initialize the pointers that got passed in
        pCRC.init();
        pSpecialCodes.value = 0;

        // variables
        int nTotalBlocks = nBytes / pWaveFormatEx.nBlockAlign;
        int R, L;

        // the prepare code

        if (pWaveFormatEx.wBitsPerSample == 8) {
            if (pWaveFormatEx.nChannels == 2) {
                for (int nBlockIndex = 0; nBlockIndex < nTotalBlocks; nBlockIndex++) {
                    short b1 = pRawData.readUnsignedByte();
                    short b2 = pRawData.readUnsignedByte();
                    R = b1 - 128;
                    L = b2 - 128;

                    pCRC.append((byte) b1);
                    pCRC.append((byte) b2);

                    // check the peak
                    if (Math.abs(L) > pPeakLevel.value)
                        pPeakLevel.value = Math.abs(L);
                    if (Math.abs(R) > pPeakLevel.value)
                        pPeakLevel.value = Math.abs(R);

                    // convert to x,y
                    pOutputY[nBlockIndex] = L - R;
                    pOutputX[nBlockIndex] = R + (pOutputY[nBlockIndex] / 2);
                }
            } else if (pWaveFormatEx.nChannels == 1) {
                for (int nBlockIndex = 0; nBlockIndex < nTotalBlocks; nBlockIndex++) {
                    short b1 = pRawData.readUnsignedByte();
                    R = b1 - 128;

                    pCRC.append((byte) b1);

                    // check the peak
                    if (Math.abs(R) > pPeakLevel.value)
                        pPeakLevel.value = Math.abs(R);

                    // convert to x,y
                    pOutputX[nBlockIndex] = R;
                }
            }
        } else if (pWaveFormatEx.wBitsPerSample == 24) {
            if (pWaveFormatEx.nChannels == 2) {
                for (int nBlockIndex = 0; nBlockIndex < nTotalBlocks; nBlockIndex++) {
                    long nTemp = 0;

                    short b = pRawData.readUnsignedByte();
                    nTemp |= (b << 0);
                    pCRC.append((byte) b);

                    b = pRawData.readUnsignedByte();
                    nTemp |= (b << 8);
                    pCRC.append((byte) b);

                    b = pRawData.readUnsignedByte();
                    nTemp |= (b << 16);
                    pCRC.append((byte) b);

                    if ((nTemp & 0x800000) > 0)
                        R = (int) (nTemp & 0x7fffff) - 0x800000;
                    else
                        R = (int) (nTemp & 0x7fffff);

                    nTemp = 0;

                    b = pRawData.readUnsignedByte();
                    nTemp |= (b << 0);
                    pCRC.append((byte) b);

                    b = pRawData.readUnsignedByte();
                    nTemp |= (b << 8);
                    pCRC.append((byte) b);

                    b = pRawData.readUnsignedByte();
                    nTemp |= (b << 16);
                    pCRC.append((byte) b);

                    if ((nTemp & 0x800000) > 0)
                        L = (int) (nTemp & 0x7fffff) - 0x800000;
                    else
                        L = (int) (nTemp & 0x7fffff);

                    // check the peak
                    if (Math.abs(L) > pPeakLevel.value)
                        pPeakLevel.value = Math.abs(L);
                    if (Math.abs(R) > pPeakLevel.value)
                        pPeakLevel.value = Math.abs(R);

                    // convert to x,y
                    pOutputY[nBlockIndex] = L - R;
                    pOutputX[nBlockIndex] = R + (pOutputY[nBlockIndex] / 2);
                }
            } else if (pWaveFormatEx.nChannels == 1) {
                for (int nBlockIndex = 0; nBlockIndex < nTotalBlocks; nBlockIndex++) {
                    long nTemp = 0;

                    short b = pRawData.readUnsignedByte();
                    nTemp |= (b << 0);
                    pCRC.append((byte) b);

                    b = pRawData.readUnsignedByte();
                    nTemp |= (b << 8);
                    pCRC.append((byte) b);

                    b = pRawData.readUnsignedByte();
                    nTemp |= (b << 16);
                    pCRC.append((byte) b);

                    if ((nTemp & 0x800000) > 0)
                        R = (int) (nTemp & 0x7fffff) - 0x800000;
                    else
                        R = (int) (nTemp & 0x7fffff);

                    // check the peak
                    if (Math.abs(R) > pPeakLevel.value)
                        pPeakLevel.value = Math.abs(R);

                    // convert to x,y
                    pOutputX[nBlockIndex] = R;
                }
            }
        } else {
            if (pWaveFormatEx.nChannels == 2) {
                int LPeak = 0;
                int RPeak = 0;
                int nBlockIndex = 0;
                for (nBlockIndex = 0; nBlockIndex < nTotalBlocks; nBlockIndex++) {
                    R = pRawData.readShort();
                    pCRC.append((short) R);

                    L = pRawData.readShort();
                    pCRC.append((short) L);

                    // check the peak
                    if (Math.abs(L) > LPeak)
                        LPeak = Math.abs(L);
                    if (Math.abs(R) > RPeak)
                        RPeak = Math.abs(R);

                    // convert to x,y
                    pOutputY[nBlockIndex] = L - R;
                    pOutputX[nBlockIndex] = R + (pOutputY[nBlockIndex] / 2);
                }

                if (LPeak == 0)
                    pSpecialCodes.value |= SpecialFrame.SPECIAL_FRAME_LEFT_SILENCE;
                if (RPeak == 0)
                    pSpecialCodes.value |= SpecialFrame.SPECIAL_FRAME_RIGHT_SILENCE;
                if (Math.max(LPeak, RPeak) > pPeakLevel.value)
                    pPeakLevel.value = Math.max(LPeak, RPeak);

                // check for pseudo-stereo files
                nBlockIndex = 0;
                while (pOutputY[nBlockIndex++] == 0) {
                    if (nBlockIndex == (nBytes / 4)) {
                        pSpecialCodes.value |= SpecialFrame.SPECIAL_FRAME_PSEUDO_STEREO;
                        break;
                    }
                }
            } else if (pWaveFormatEx.nChannels == 1) {
                int nPeak = 0;
                for (int nBlockIndex = 0; nBlockIndex < nTotalBlocks; nBlockIndex++) {
                    R = pRawData.readUnsignedShort();
                    pCRC.append((short) R);

                    // check the peak
                    if (Math.abs(R) > nPeak)
                        nPeak = Math.abs(R);

                    //convert to x,y
                    pOutputX[nBlockIndex] = R;
                }

                if (nPeak > pPeakLevel.value)
                    pPeakLevel.value = nPeak;
                if (nPeak == 0)
                    pSpecialCodes.value |= SpecialFrame.SPECIAL_FRAME_MONO_SILENCE;
            }
        }

        pCRC.prefinalizeCrc();

        // add the special code
        pCRC.finalizeCrc();

        if (pSpecialCodes.value != 0)
            pCRC.doSpecial();
    }

    public void unprepare(int X, int Y, final WaveFormat waveFormat, final ByteBuffer output, final Crc32 crc) {
        // decompress and convert from (x,y) -> (l,r)
        // sort of long and ugly.... sorry
        int channels = waveFormat.nChannels;
        int bitsPerSample = waveFormat.wBitsPerSample;
        if (channels == 2) {
            if (bitsPerSample == 16) {
                // get the right and left values
                short nR = (short) (X - (Y / 2));
                short nL = (short) (nR + Y);

                output.append(nR, nL);
                crc.append(nR, nL);
            } else if (bitsPerSample == 8) {
                byte R = (byte) (X - (Y / 2) + 128);
                byte L = (byte) (R + Y);

                output.append(R, L);
                crc.append(R, L);
            } else if (bitsPerSample == 24) {
                int RV = X - (Y / 2);
                int LV = RV + Y;

                if (RV < 0)
                    RV = (RV + 0x800000) | 0x800000;
                if (LV < 0)
                    LV = (LV + 0x800000) | 0x800000;

                output.append24(RV, LV);
                crc.append24(RV, LV);
            }
        } else if (channels == 1) {
            if (bitsPerSample == 16) {
                output.append((short) X);
                crc.append((short) X);
            } else if (bitsPerSample == 8) {
                byte R = (byte) (X + 128);

                output.append(R);
                crc.append(R);
            } else if (bitsPerSample == 24) {
                if (X < 0)
                    X = (X + 0x800000) | 0x800000;

                output.append24(X);
                crc.append24(X);
            }
        }
    }

    public void unprepareOld(int[] pInputX, int[] pInputY, int nBlocks, WaveFormat pWaveFormatEx, ByteBuffer output, Crc32 crc, int nFileVersion) {
        //the CRC that will be figured during decompression
        crc.init();

        //decompress and convert from (x,y) -> (l,r)
        //sort of int and ugly.... sorry
        int channels = pWaveFormatEx.nChannels;
        int bitsPerSample = pWaveFormatEx.wBitsPerSample;
        if (channels == 2) {
            //convert the x,y data to raw data
            if (bitsPerSample == 16) {
                short R;
                int pX = 0;
                int pY = 0;

                for (; pX < nBlocks; pX++, pY++) {
                    R = (short) (pInputX[pX] - (pInputY[pY] / 2));

                    output.append(R);
                    crc.append(R);
                    R += pInputY[pY];
                    output.append(R);
                    crc.append(R);
                }
            } else if (bitsPerSample == 8) {
                byte R;
                if (nFileVersion > 3830) {
                    for (int SampleIndex = 0; SampleIndex < nBlocks; SampleIndex++) {
                        R = (byte) (pInputX[SampleIndex] - (pInputY[SampleIndex] / 2) + 128);
                        output.append(R);
                        crc.append(R);
                        R += pInputY[SampleIndex];
                        output.append(R);
                        crc.append(R);
                    }
                } else {
                    for (int SampleIndex = 0; SampleIndex < nBlocks; SampleIndex++) {
                        R = (byte) (pInputX[SampleIndex] - (pInputY[SampleIndex] / 2));
                        output.append(R);
                        crc.append(R);
                        R += pInputY[SampleIndex];
                        output.append(R);
                        crc.append(R);
                    }
                }
            } else if (bitsPerSample == 24) {
                int RV, LV;

                for (int SampleIndex = 0; SampleIndex < nBlocks; SampleIndex++) {
                    RV = pInputX[SampleIndex] - (pInputY[SampleIndex] / 2);
                    LV = RV + pInputY[SampleIndex];

                    int nTemp = 0;
                    if (RV < 0)
                        nTemp = (RV + 0x800000) | 0x800000;
                    else
                        nTemp = RV;

                    output.append24(nTemp);
                    crc.append24(nTemp);

                    nTemp = 0;
                    if (LV < 0)
                        nTemp = (LV + 0x800000) | 0x800000;
                    else
                        nTemp = LV;

                    output.append24(nTemp);
                    crc.append24(nTemp);
                }
            }
        } else if (channels == 1) {
            //convert to raw data
            if (bitsPerSample == 8) {
                byte R;
                if (nFileVersion > 3830) {
                    for (int SampleIndex = 0; SampleIndex < nBlocks; SampleIndex++) {
                        R = (byte) (pInputX[SampleIndex] + 128);
                        output.append(R);
                        crc.append(R);
                    }
                } else {
                    for (int SampleIndex = 0; SampleIndex < nBlocks; SampleIndex++) {
                        R = (byte) (pInputX[SampleIndex]);
                        output.append(R);
                        crc.append(R);
                    }
                }

            } else if (bitsPerSample == 24) {

                int RV;
                for (int SampleIndex = 0; SampleIndex < nBlocks; SampleIndex++) {
                    RV = pInputX[SampleIndex];

                    int nTemp = 0;
                    if (RV < 0)
                        nTemp = (RV + 0x800000) | 0x800000;
                    else
                        nTemp = RV;

                    output.append24(nTemp);
                    crc.append24(nTemp);
                }
            } else {
                short R;
                for (int SampleIndex = 0; SampleIndex < nBlocks; SampleIndex++) {
                    R = (short) (pInputX[SampleIndex]);
                    output.append(R);
                    crc.append(R);
                }
            }
        }
        crc.prefinalizeCrc();
    }
}
