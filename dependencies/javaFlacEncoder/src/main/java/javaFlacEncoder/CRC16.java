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
 * Class to calculate a CRC16 checksum.
 * @author Preston Lacey
 */
public class CRC16 {
    /** For Debugging: Higher level equals more debug statements */
    public static int DEBUG_LEV = 0;

    /** CRC Divisor: 0x18005 */
    static final int divisorCRC16 = 0x18005 << 15;

    /** working checksum stored between calls to update(..) */
    protected int workingCRC16;

    /** number of valid bits stored in workingCRC16(valid bits are packed towards
     * high-order side of int) */
    protected int workingCRC16Count;

    /**
     * Constructor. Creates a CRC16 object that is ready to be used. Next step
     * would be to call updateCRC16, or getCRC16, with appropriate data.
     */
    public CRC16() {
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
     * finalized result, as non-summed data must remain in workingCRC16 until
     * checksum() is called.
     */
    public short updateCRC16(byte[] inSet, int start, int end) {

        //we need at least two bytes to work on. And cache result between calls.
        //  Follow md5 style, updating many times, finalizing once.

        //Copy in saved value(starts out zero).
        //Shift value, and divisor to top end of int. When we drop below 17 in
        //  working int, "OR" it with another byte, or save and return.
        //  I won't do this, just "assume" it's already there and use instance
        //  variables.
        //While bits available is more than 16:
            //while top bit is zero, and >16 bits in buffer. Shift Left.
            //if >16 bits remain in working int, divide
            //else if 2 bytes remain, "OR" in another byte.
        //store working int, return;
        int current = start;
        int topBit = 0;
        int topMask = 1<<31;
        int shiftMask = 0x0000FF00;
        while(current < end) {//this should leave 16 bits in workingCRC16.
            topBit = workingCRC16 & topMask;
            while(workingCRC16Count > 16 && topBit == 0) {
                workingCRC16Count--;
                workingCRC16 = workingCRC16 << 1;
                topBit = workingCRC16 & topMask;
            }
            
            if( workingCRC16Count > 16 ) {
                workingCRC16 = workingCRC16 ^ divisorCRC16;
            }
            else {//workingCRC16Count <= 16(strictly speeking, count will == 16)
                int temp = inSet[current++];
                //temp = temp << 24;
                temp = temp << 8 & shiftMask;
                //temp = temp >>> 16;
                workingCRC16 = workingCRC16 | temp;
                workingCRC16Count+=8;
            }
        }
        return (short)(workingCRC16 >>> 16);
    }

    /**
     * Finalize the intermediate checksum, and return the value. After this is
     * called, you must call reset() before attempting a new checksum.
     * @return finalized checksum.
     */
    public short checksum() {
        //add 16 to the count,
        //call update with a byte constructed to add no more.
        byte[] fake = {0};
        workingCRC16Count += 16;
        short val = updateCRC16(fake, 0, 1);
        //short val = updateCRC16(fake, 0, 2);
        return val;
    }

    /**
     * Resets all stored data, preparing object for a new checksum.
     */
    public void reset() {
        workingCRC16 = 0;
        workingCRC16Count = 16;
    }


    /**
     * Add data to the given crc. Immediately creates checksum on given data.
     * Can be called multiple times as it won't finalize the checksum until the
     * method checksum() is called. This is a static version supplied for
     * potential speed improvement(simple tests showed this was about 15% faster
     * than the above non-static version when used in this Class's getCRC16
     * method)
     *
     * @param inSet Array holding data to checksum.
     * @param start Index of array holding first element
     * @param end Index to stop at. Last index used will be end-1.
     * @param crc16  CRC16 object to use.
     * @return intermediate result of checksum to this point. This is *not* a
     * finalized result, as non-summed data must remain in workingCRC16 until
     * checksum() is called.
     */
    public static short updateCRC16(byte[] inSet, int start, int end, CRC16 crc16) {
        //**NOTE: the static and non-static version of this function should, in
        //general, be altered together, as they share similar code
        
        //we need at least two bytes to work on. And cache result between calls.
        //  Follow md5 style, updating many times, finalizing once.

        //Copy in saved value(starts out zero).
        //Shift value, and divisor to top end of int. When we drop below 17 in
        //  working int, "OR" it with another byte, or save and return.
        //  I won't do this, just "assume" it's already there and use instance
        //  variables.
        //While bits available is more than 16:
            //while top bit is zero, and >16 bits in buffer. Shift Left.
            //if >16 bits remain in working int, divide
            //else if 2 bytes remain, "OR" in another byte.
        //store working int, return;
        int current = start;
        int topBit = 0;
        int topMask = 1<<31;
        int shiftMask = 0x0000FF00;
        int shiftMask2 = 0x000000FF;
        int workingCRC16 = crc16.workingCRC16;
        int workingCRC16Count = crc16.workingCRC16Count;
        while(current < end) {//this should leave 16 bits in val.
            topBit = workingCRC16 & topMask;
            
            while(workingCRC16Count > 16 && topBit == 0) {
                workingCRC16Count--;
                workingCRC16 = workingCRC16 << 1;
                topBit = workingCRC16 & topMask;
            }
            
            if( workingCRC16Count > 16 ) {
                workingCRC16 = workingCRC16 ^ divisorCRC16;
            }
            else {//workingCRC16Count < 16
                int temp = inSet[current++];
                temp = (temp << 8) & shiftMask;//must mask in case temp is negative
                workingCRC16 = workingCRC16 | temp;
                workingCRC16Count+=8;
                if(current < end) {
                    temp = inSet[current++];
                    temp = temp & shiftMask2;//must mask in case temp is negative
                    workingCRC16 = workingCRC16 | temp;
                    workingCRC16Count+=8;
                }
            }
        }
        crc16.workingCRC16 = workingCRC16;
        crc16.workingCRC16Count = workingCRC16Count;
        return (short)(workingCRC16 >>> 16);
    }


/**
     * This method is provided to conveniently calculate the CRC16 checksum of
     * all data stored by an EncodedElement. It uses static functions to allow
     * for potential speed improvements. It calls crc16.reset(), so mustn't be
     * used on a running checksum.
     *
     * @param header Element storing data to be used.
     * @param crc16 CRC16 object to use. This object's reset() method will be
     * called by this method before calculating checksum.
     * @return CRC16 result
     */
    public static short getCRC16_noninlined(EncodedElement header, CRC16 crc16) {
        if(DEBUG_LEV > 0)
            System.err.println("Frame::getCRC16 : Begin");
        crc16.reset();
        //calculate CRC16
        int offset = 0;
        EncodedElement currentEle = header;
        int currentByte = 0;
        byte[] unfullByte = {0};
        byte[] eleData = null;
        int usableBits = 0;
        int lastByte = 0;
        while(currentEle != null) {
            eleData = currentEle.getData();
            usableBits = currentEle.getUsableBits();
            currentByte = 0;
            //if offset is not zero, merge first byte with existing byte
            if(offset != 0) {
                unfullByte[0] = (byte)(unfullByte[0] | eleData[currentByte++]);
                //----updateCRC begin!!!!
                CRC16.updateCRC16(unfullByte, 0, 1, crc16);
                //crc16.updateCRC16(unfullByte, 0, 1);
            }
            //checksum all full bytes of element.
            lastByte = usableBits/8;
            CRC16.updateCRC16(eleData, currentByte, lastByte, crc16);

            //save non-full byte(if present), and set "offset" for next element.
            offset = usableBits % 8;
            if(offset != 0) {
                //System.err.println("usablebits: " + usableBits);
                unfullByte[0] = eleData[lastByte];
            }
            //update current.
            currentEle = currentEle.getNext();
        }
        if(offset > 0) {
            System.err.println("ERROR: frame was not properly bit padded");
            System.exit(0);
        }
        short crc16Val = crc16.checksum();
        if(DEBUG_LEV > 0) {
            if(DEBUG_LEV > 10)
                System.err.println("Frame::getCRC16: crc16 : "+
                        Integer.toHexString(crc16Val));
            System.err.println("Frame::getCRC16 : End");
        }
        return crc16Val;
    }


    /**
     * This method is provided to conveniently calculate the CRC16 checksum of
     * all data stored by an EncodedElement. It uses static functions to allow
     * for potential speed improvements. It calls crc16.reset(), so mustn't be
     * used on a running checksum.
     *
     * @param header Element which stores the data to checksum.
     * @param crc16 CRC16 object to use. This object's reset() method will be
     * called by this method before calculating checksum.
     * @return CRC16 result
     */
    public static short getCRC16(EncodedElement header, CRC16 crc16) {
        if(DEBUG_LEV > 0)
            System.err.println("Frame::getCRC16 : Begin");
        crc16.reset();
        //calculate CRC16
        int offset = 0;
        EncodedElement currentEle = header;
        int currentByte = 0;
        byte[] unfullByte = {0};
        byte[] eleData = null;
        int usableBits = 0;
        int lastByte = 0;
        while(currentEle != null) {
            eleData = currentEle.getData();
            usableBits = currentEle.getUsableBits();
            currentByte = 0;
            //if offset is not zero, merge first byte with existing byte
            if(offset != 0) {
                unfullByte[0] = (byte)(unfullByte[0] | eleData[currentByte++]);
                //----updateCRC begin!!!!
                //CRC16.updateCRC16(unfullByte, 0, 1, crc16);
               int end = 1;
               byte[] inSet = unfullByte;
               int current = 0;
               //int topBit = 0;
               //int topMask = 1<<31;
               int shiftMask = 0x0000FF00;
               int shiftMask2 = 0x000000FF;
               int workingCRC16 = crc16.workingCRC16;
               int workingCRC16Count = crc16.workingCRC16Count;

               boolean process = true;
               //while(workingCRC16Count > 16) {
               while(process) {
                   int max = workingCRC16Count - 16;
                   int leading = Integer.numberOfLeadingZeros(workingCRC16);
                   if(leading > max)
                      leading = max;
                   workingCRC16Count -= leading;
                   workingCRC16 = workingCRC16 << leading;
                   if(workingCRC16Count > 16) {
                       workingCRC16 = workingCRC16 ^ divisorCRC16;
                   }
                   else if(current < end) {//workingCRC16Count < 16
                      int temp = inSet[current++];
                      temp = (temp << 8) & shiftMask;//must mask in case temp is negative
                      workingCRC16 = workingCRC16 | temp;
                      workingCRC16Count+=8;
                      if(current < end) {
                          temp = inSet[current++];
                          temp = temp & shiftMask2;//must mask in case temp is negative
                          workingCRC16 = workingCRC16 | temp;
                          workingCRC16Count+=8;
                      }
                   }
                   else { 
                       process = false;
                   }

               }
               crc16.workingCRC16 = workingCRC16;
               crc16.workingCRC16Count = workingCRC16Count;
                //----updateCRC end!!!!!!!!

                //crc16.updateCRC16(unfullByte, 0, 1);
            }
            //checksum all full bytes of element.
            lastByte = usableBits/8;
            //---updateCRC16 START!!!!!!!!!!
            //CRC16.updateCRC16(eleData, currentByte, lastByte, crc16);
            int end = lastByte;
            byte[] inSet = eleData;
            int current = currentByte;
            int shiftMask = 0x0000FF00;
            int shiftMask2 = 0x000000FF;
            int workingCRC16 = crc16.workingCRC16;
            int workingCRC16Count = crc16.workingCRC16Count;

            boolean process = true;
            if(workingCRC16Count <= 16) {
               if(current < end) {//workingCRC16Count < 16
                  int temp = inSet[current++];
                  temp = (temp << 8) & shiftMask;//must mask in case temp is negative
                  workingCRC16 = workingCRC16 | temp;
                  workingCRC16Count+=8;
                  if(current < end) {
                     temp = inSet[current++];
                     temp = temp & shiftMask2;//must mask in case temp is negative
                     workingCRC16 = workingCRC16 | temp;
                     workingCRC16Count+=8;
                  }
               }
               if(workingCRC16Count <= 16)
                  process = false;
            }
            while(process) {
                int max = workingCRC16Count - 16;
                int leading = Integer.numberOfLeadingZeros(workingCRC16);
                if(leading > max)
                   leading = max;
                workingCRC16Count -= leading;
                workingCRC16 = workingCRC16 << leading;

                if(workingCRC16Count > 16) {
                    workingCRC16 = workingCRC16 ^ divisorCRC16;
                }
                else if(current < end) {//workingCRC16Count < 16
                   int temp = inSet[current++];
                   temp = (temp << 8) & shiftMask;//must mask in case temp is negative
                   workingCRC16 = workingCRC16 | temp;
                   workingCRC16Count+=8;
                   if(current < end) {
                       temp = inSet[current++];
                       temp = temp & shiftMask2;//must mask in case temp is negative
                       workingCRC16 = workingCRC16 | temp;
                       workingCRC16Count+=8;
                   }
                }
                else {
                    process = false;
                }
            }
            crc16.workingCRC16 = workingCRC16;
            crc16.workingCRC16Count = workingCRC16Count;
                //----updateCRC end!!!!!!!!

            //save non-full byte(if present), and set "offset" for next element.
            offset = usableBits % 8;
            if(offset != 0) {
                //System.err.println("usablebits: " + usableBits);
                unfullByte[0] = eleData[lastByte];
            }
            //update current.
            currentEle = currentEle.getNext();
        }
        if(offset > 0) {
            System.err.println("ERROR: frame was not properly bit padded");
            System.exit(0);
        }
        short crc16Val = crc16.checksum();
        if(DEBUG_LEV > 0) {
            if(DEBUG_LEV > 10)
                System.err.println("Frame::getCRC16: crc16 : "+
                        Integer.toHexString(crc16Val));
            System.err.println("Frame::getCRC16 : End");
        }
        return crc16Val;
    }
}
