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
 * Class to calculate a CRC8 checksum.
 * @author Preston Lacey
 */
public class CRC8 {

    /** For Debugging: Higher level equals more debug statements */
    public static int DEBUG_LEV = 0;

    /** CRC Divisor: 0x107 */
    static final int divisorCRC8 = 0x107 << 23;

    /** working checksum stored between calls to update(..) */
    int workingCRC8;

    /** number of valid bits stored in workingCRC8(valid bits are packed towards
     * high-order side of int */
    int workingCRC8Count;

    /**
     * Constructor. Creates a CRC8 object that is ready to be used. Next step
     * would be to call updateCRC8 with appropriate data.
     */
    public CRC8() {
        reset();
    }

    /**
     * Add data to the crc. Immediately creates checksum on given data. Can be
     * called multiple times as it won't finalize the checksum until the method
     * checksum() is called.
     *
     * @param inSet Array holding data to checksum.
     * @param start Index of array holding first element
     * @param end Index to stop at. Last index used will be end-1.
     * @return intermediate result of checksum to this point. This is *not* a
     * finalized result, as non-summed data must remain in workingCRC8 until
     * checksum() is called.
     */
    public byte updateCRC8(byte[] inSet, int start, int end) {
        //we need at least one byte to work on. And cache between calls.
        //  Follow md5 style, updating many times, finalizing once.

        //Copy in saved value(starts out zero).
        //Shift value, and divisor to top end of int. When we drop below 17 in
        //  working int, "OR" it with another byte, or save and return.
        //  I won't do this, just "assume" it's already there and use instance
        //  variables.
        //While bits available is more than 8:
            //while top bit is zero, and >8 bits in buffer. Shift Left.
            //if >8 bits remain in working int, divide
            //else if 8 bits remain, "OR" in another byte.
        //store working int, return;
        if(DEBUG_LEV > 10 )
            System.err.println("CRC8::updateCRC8: Begin");
        if(DEBUG_LEV > 20) {
            System.err.println("Start:End : "+start+":"+end);
        }
        int current = start;
        int topBit = 0;
        int topMask = 1<<31;
        while(current < end) {//this should leave 8 bits in workingCRC8.
            if(DEBUG_LEV > 40 ) {
                System.err.println("CRC8::updateCRC8: looping bytes. current: "+current);
                System.err.println("workingCRC8Count : " +workingCRC8Count);
            }
            topBit = workingCRC8 & topMask;
            while(workingCRC8Count > 8 && topBit == 0) {
                if(DEBUG_LEV > 40)
                    System.err.println("CRC8::updateCRC8:  shifting left");
                workingCRC8Count--;
                workingCRC8 = workingCRC8 << 1;
                topBit = workingCRC8 & topMask;
            }
            if( workingCRC8Count > 8 ) {
                workingCRC8 = workingCRC8 ^ divisorCRC8;
            }
            else {//workingCRC8Count < 9
                if(DEBUG_LEV > 30) {
                    System.err.println("CRC8: Adding byte with workingCRC of: "+
                            (workingCRC8 >>> 24));
                }
                int temp = inSet[current++];
                temp = temp << 24;
                temp = temp >>> 8;
                workingCRC8 = workingCRC8 | temp;
                workingCRC8Count+=8;
            }
        }
        return (byte)(workingCRC8 >>> 24);
    }

    /**
     * Finalize the checksum, and return the value. After this is called, you
     * must call reset() before attempting a new checksum.
     * @return finalized checksum.
     */
    public byte checksum() {
        if(DEBUG_LEV > 10 )
            System.err.println("CRC8::checksum : Begin");
        //add 8 to the count, 
        //call update with a byte constructed to add no more.
        byte[] fake = {0};
        workingCRC8Count += 8;
        byte val = updateCRC8(fake, 0, 1);
        if(DEBUG_LEV > 10 )
            System.err.println("CRC8::checksum : End");
        return val;
    }

    /**
     * Resets all stored data, preparing object for a new checksum.
     */
    public void reset() {
        workingCRC8 = 0;
        workingCRC8Count = 8;
    }
}
