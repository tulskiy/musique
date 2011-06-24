/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package javaFlacEncoder;

/**
 * Implements the Subframe abstract class, providing encoding support for the
 * FLAC Verbatim Subframe.
 *
 * @author Preston Lacey
 */
public class Subframe_Verbatim extends Subframe {
    /** For debugging: Higher value equals more output, typically by increments
     * of 10 */
    public static int DEBUG_LEV = 0;
    /** Subframe type supported by this implementation. */
    public static final EncodingConfiguration.SubframeType type =
            EncodingConfiguration.SubframeType.VERBATIM;

    //int sampleSize = 0;

    /**
     * Constructor. Sets StreamConfiguration to use. If the StreamConfiguration
     * must later be changed, a new Subframe object must be created as well.
     * @param sc StreamConfiguration to use for encoding.
     */
    Subframe_Verbatim(StreamConfiguration sc) {
        super(sc);
        //sampleSize = sc.getBitsPerSample();
    }

    /**
     * This method is used to set the encoding configuration.
     * @param ec    encoding configuration to use.
     * @return      true if configuration was changed, false otherwise
     */
    @Override
    public boolean registerConfiguration(EncodingConfiguration ec) {
        super.registerConfiguration(ec);

        return true;
    }

    
    public int encodeSamples(int[] samples, int count, int start, int skip,
        EncodedElement data, int offset, int bitsPerSample ) {
        if(DEBUG_LEV > 0) {
            System.err.println("Subframe_Verbatim::encodeSamples(...)");
        }
        //int sampleSize = bitsPerSample;
        int encodedSamples = count;
        int bits = bitsPerSample*count+offset+1*8;
        int bytesNeeded = bits/8;
        if(bits%8 != 0)
            bytesNeeded++;
        byte[] dataArray = new byte[bytesNeeded];
        int writePosition = offset;
        //write SubframeHeader
        //EncodedElement.addInt(0, 1, start, dataArray);
        EncodedElement.addInt(0, 1, writePosition, dataArray);
        writePosition++;
        EncodedElement.addInt(1, 6, writePosition, dataArray);
        writePosition += 6;
        EncodedElement.addInt(0, 1, writePosition, dataArray);
        writePosition++;
        EncodedElement.packInt(samples, bitsPerSample, writePosition, start, skip, count,
                dataArray);
        lastEncodedSize = bits-offset;

        data.setData(dataArray);
        data.setUsableBits(bits);
        if(DEBUG_LEV > 0)
            System.err.println("Subframe_Verbatim::encodeSamples(...): End");
        if(DEBUG_LEV > 10) {
            System.err.println("--: bitsUsed : "+bits+"  : Bytes : "+bytesNeeded);
        }
        return encodedSamples;
    }

}
