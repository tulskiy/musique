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
 * This is a utility class that provides methods to both encode to and decode
 * from the extended version of UTF8 used by the FLAC format. All functions
 * should work with standard UTF8 as well, since this only extends it to handle
 * larger input values.
 *
 * @author Preston Lacey
 */
public class UTF8Modified {
    static final long oneByteLimit = (long)Math.pow(2, 7);
    static final long twoByteLimit = (long)Math.pow(2, 11);
    static final long threeByteLimit = (long)Math.pow(2, 16);
    static final long fourByteLimit = (long)Math.pow(2, 21);
    static final long fiveByteLimit = (long)Math.pow(2, 26);
    static final long sixByteLimit = (long)Math.pow(2, 31);
    static final long sevenByteLimit = (long)Math.pow(2, 36);
    static long[] limits = {
        oneByteLimit,
        twoByteLimit,
        threeByteLimit,
        fourByteLimit,
        fiveByteLimit,
        sixByteLimit,
        sevenByteLimit
    };

    /** For debugging: Higher value equals more output, generally by increments
     * of 10 */
    public static int DEBUG_LEV = 0;

    /**
     * Constructor. This Class provides only static methods and static fields.
     */
    public UTF8Modified() {
    }

    /**
     * Decode an extended UTF8(as used in FLAC), to a long value.
     * @param input extended UTF8 encoded value.
     * @return value represented by the UTF8 input.
     */
    public static long decodeFromExtendedUTF8(byte[] input) {
        int leadOnes = 0;
        int leadMask = 128;
        int work = input[0];
        while((work & leadMask) > 0) {
            leadOnes++;
            work = work << 1;
        }
        int valMask = 255 >>> (leadOnes+1);
        long val = input[0] & valMask;
        for(int i = 1; i < leadOnes; i++) {
            int midMask = 0x3F;
            val = val << 6;
            val = (input[i] & midMask) | val;
        }
        return val;
    }

    /**
     * Convert a value to an extended UTF8 format(as used in FLAC).
     * @param value value to convert to extended UTF8(value must be positive
     * and 36 bits or less in size)
     * @return extended UTF8 encoded value(array size is equal to the number of
     * usable bytes)
     */
    public static byte[] convertToExtendedUTF8(long value) {
        //calculate bytes needed
        int bytesNeeded = 1;
        for(int i = 0; i < 7; i++) {
            if(value >= limits[i] ) {
                bytesNeeded++;         
            }
        }
        //create space
        byte [] result = new byte[bytesNeeded];
        int byteIndex = 0;
        int inputIndex = 0;
        int bytesLeft = bytesNeeded;
        while(bytesLeft > 1) {
            int midByteMarker = 0x80;//10 in leftmost bits
            int midByteMask = 0x3F;//00111111
            int val = ((int)(value >>> inputIndex) & midByteMask) | midByteMarker;
            result[byteIndex++] = (byte)val;
            inputIndex += 6;
            bytesLeft--;
        }
        int onesNeeded = inputIndex/6;
        if(onesNeeded > 0)
            onesNeeded++;
        int startMask = 255 >>> (onesNeeded + 1);
        int ones = 255 << (8-onesNeeded);
        int val = ((int)(value >>> inputIndex) & startMask) | ones;
        result[byteIndex++] = (byte)val;

        byte[] finalResult = new byte[bytesNeeded];
        for(int i = 0; i < bytesNeeded; i++) {
            int sourceIndex = bytesNeeded-1-i;
            int destIndex = i;
            finalResult[destIndex] = result[sourceIndex];
        }
        if(DEBUG_LEV > 10) {
            System.err.print("input:result_length:result :: " +value+":"+finalResult.length+"::");
            for(int i = 0; i < finalResult.length; i++)
                System.err.print(Integer.toHexString(finalResult[i])+":");
            System.err.println();
        }
        return finalResult;
    }
}
