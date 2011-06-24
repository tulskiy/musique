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
 * EncodedElement which uses an integer array as a backing, rather than the
 * byte array.
 *
 * @author Preston Lacey
 */
public class EncodedElement_32 extends EncodedElement {
   int[] data_32 = null;

   public EncodedElement_32() {
      offset = 0;
      usableBits = 0;
      data = null;
      data_32 = new int[100];
   }

   public EncodedElement_32(int size, int off) {
      data = null;
      usableBits = off;
      offset = off;
      data_32 = new int[size];
   }

   @Override
   public void clear(int size, int off) {
      next = null;
      previous = null;
      data = null;
      data_32 = new int[size];
      offset = off;
      usableBits = off;
   }


    /**
     * This method adds a given number of bits of an int to a byte array.
     * @param value int to store bits from
     * @param count number of low-order bits to store
     * @param startPos start bit location in array to begin writing
     * @param dest array to store bits in. dest MUST have enough space to store
     * the given data, or this function will fail.
     */
    public static void addInt(int value, int count, int startPos, int[] dest) {
        assert(count <= 32);
        assert(count > 0);
        assert(startPos >= 0);
        //System.err.println("addInt("+value+", "+count + ", "+startPos+")");
        /*
         * Because we're using both 32 bit input and 32 bit output, we only have
         * two cases to handle:
         * 1) All bits fit in one index, appropriately shifted up first.
         *      Mask upper bits we'll merge with(Ones in upper).
         *      Mask lower bits we'll merge with(Ones in lower).
         *      destMask = OR Upper and lower masks together.
         *      currentIndex = currentIndex & destMask
         *      destMask = ~destMask;
         *      upshift by 32-count-currentOffset;
         *      inputValue = inputValue & destMask;
         *      currentIndex = currentIndex | inputValue
         * 2) Some bits merge in top of one index, remaining bits merge in
         * bottom of second index
         *      A) CurrentIndex
         *          Create inputValueHigh, with high order bits which will enter
         *              currentIndex.
         *          Handle this as we handled case one(pretend it's a new value)
         *      B) CurrentIndex++
         *          We fill from upper edge. No upper mask needed.
         *          upshift value by 32-count;
         *          Mask lower bits we'll merge with(Ones in lower)
         *          currentIndex = currentIndex & destMask
         *          currentIndex = currentIndex | destMask
         */
        int currentIndex = startPos/32;
        int currentOffset = startPos%32;
        
        int totalSize = count+currentOffset;

        if(totalSize > 32) {
            //System.err.println("totalSize > 32");
            int secondIndex = currentIndex + 1;
            int secondSize = totalSize - 32;
            int secondValue = value << (32-secondSize);
            int lowerMask = -1 >>> secondSize;
            int temp = dest[secondIndex] & lowerMask;
            dest[secondIndex] = temp | secondValue;
            totalSize = 32;
            value = value >>> secondSize;
        }

        if(totalSize <= 32) {//Case 1
            int upperMask = -2 << (31-currentOffset);
            //int lowerMask = Integer.MAX_VALUE >>> (totalSize-1);
            int lowerMask = 0x7FFFFFFF >>> (totalSize-1);
            int destMask = upperMask | lowerMask;
            int temp = dest[currentIndex] & destMask;
            destMask = ~destMask;
            value = value << (32-totalSize);
            value = value & destMask;
            dest[currentIndex] = temp | value;
        }
    }

    public int[] getData32() { return data_32; }
    private static byte[] convertIntArrayToByteArray(int[] input) {
        byte[] result = new byte[input.length*4];
        for(int i = 0; i < input.length; i++) {
            //byte 3 = byte 0
            //byte 2 = byte 1
            //byte 1 = byte 2
            //byte 0 = byte 3
            int byteBase = i*4;
            int value = input[i];
            result[byteBase+0] = (byte)(value >> 24);
            result[byteBase+1] = (byte)(value >> 16);
            result[byteBase+2] = (byte)(value >> 8);
            result[byteBase+3] = (byte)(value);
        }
        return result;
    }



    /**
     * Pack a number of bits from each int of an array(within given limits)to
     * the end of this list.
     *
     * @param inputA Array containing input values.
     * @param inputBits Array containing number of bits to use for each index
     * packed. This array should be equal in size to the inputA array.
     * @param inputOffset Index of first usable index.
     * @param countA Number of indices to pack.
     * @param startPosIn First usable bit-level index in destination array(byte
     * index = startPosIn/8, bit within that byte = startPosIn%8)
     * @param dest Destination array to store input values in. This array *must*
     * be large enough to store all values or this method will fail in an
     * undefined manner.
     */
    public static void packIntByBits(int[] inputA, int[] inputBits, int inputOffset,
            int countA, int startPosIn, int[] dest) {
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::packIntByBits : Begin");
        //int offsetCounter = 0;        
        int startPos = startPosIn;//the position to write to in output array
        int inputStop = countA+inputOffset;
        for(int valI = inputOffset; valI < inputStop; valI++) {
            //inputIter = valI+inputOffset;
            //inputIter += valI;
            //int input = inputA[valI];//value to encode
            int value = inputA[valI];
            int count = inputBits[valI];//bits of value to encode
            //EncodedElement_32.addInt(input, count, startPos, dest);
            int currentIndex = startPos/32;
            int currentOffset = startPos%32;

            int totalSize = count+currentOffset;

            if(totalSize > 32) {
                //System.err.println("totalSize > 32");
                int secondIndex = currentIndex + 1;
                int secondSize = totalSize - 32;
                int secondValue = value << (32-secondSize);
                int lowerMask = -1 >>> secondSize;
                int temp = dest[secondIndex] & lowerMask;
                dest[secondIndex] = temp | secondValue;
                totalSize = 32;
                value = value >>> secondSize;
            }

            //if(totalSize <= 32) {//Case 1
            int upperMask = -2 << (31-currentOffset);
            int lowerMask = 0x7FFFFFFF >>> (totalSize-1);
            int destMask = upperMask | lowerMask;
            int temp = dest[currentIndex] & destMask;
            destMask = ~destMask;
            value = value << (32-totalSize);
            value = value & destMask;
            dest[currentIndex] = temp | value;
            startPos += count;//startPos must not be referenced again below here!
        }
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::addInt : End");
    }

    /**
     * Pack a number of bits from each int of an array(within given limits)to
     * the end of this list.
     *
     * @param inputA Array containing input values.
     * @param inputBits Array containing number of bits to use for each index
     * packed. This array should be equal in size to the inputA array.
     * @param inputOffset Index of first usable index.
     * @param countA Number of indices to pack.
     * @return EncodedElement containing end of packed data. Data may flow
     * between multiple EncodedElement's, if an existing element was not large
     * enough for all values.
     */
    @Override
    public EncodedElement packIntByBits(int[] inputA, int[] inputBits, int inputOffset,
            int countA) {
                //go to end if we're not there.
        if(next != null) {
            EncodedElement end = EncodedElement_32.getEnd_S(next);
            return end.packIntByBits(inputA, inputBits, inputOffset, countA);
        }
        //calculate how many we can pack into current.
        int writeBitsRemaining = data_32.length*32 - usableBits;
        int willWrite = 0;
        int writeCount = 0;
        //System.err.println("writeBitsRemaining: " + writeBitsRemaining);
        for(int i = 0; i < countA; i++) {
            writeBitsRemaining -= inputBits[inputOffset+i];
            if(writeBitsRemaining >= 0) {
                writeCount++;
                willWrite += inputBits[inputOffset+i];
            }
            else
                break;
        }
        //pack them and update usable bits.
        if(writeCount > 0) {
            EncodedElement_32.packIntByBits(inputA, inputBits, inputOffset,
                    writeCount, usableBits, data_32);
            usableBits += willWrite;
        }
        //if more remain, create child object and add there
        countA -= writeCount;
        if(countA > 0) {
            inputOffset += writeCount;
            int tOff = usableBits %32;
            int size = data_32.length/2+1;
            //guarantee that our new element can store our given value
            int remainingToWrite = 0;
            for(int i = 0; i < countA; i++) {
                remainingToWrite += inputBits[inputOffset+i];
            }
            remainingToWrite = remainingToWrite / 8 + 1;
            if(size < remainingToWrite) size = remainingToWrite+10;
            //System.err.println("remaining: "+remainingToWrite);
            //System.err.println("creating size/offset : "+size+":"+tOff);
            next = new EncodedElement_32(size, tOff);
            //add int to child
            return next.packIntByBits(inputA, inputBits, inputOffset, countA);
        }
        else {
            //System.err.println("returning....done");
            //return if this is last object we wrote to.
            return this;
        }
    }

    @Override
    public byte[] getData() {
     //   return convertIntArrayToByteArray(data_32);
        byte[] result = new byte[data_32.length*4];
        for(int i = 0; i < data_32.length; i++) {
            //byte 3 = byte 0
            //byte 2 = byte 1
            //byte 1 = byte 2
            //byte 0 = byte 3
            int byteBase = i*4;
            int value = data_32[i];
            result[byteBase+0] = (byte)(value >> 24);
            result[byteBase+1] = (byte)(value >> 16);
            result[byteBase+2] = (byte)(value >> 8);
            result[byteBase+3] = (byte)(value);
        }
        return result;
    }

    public EncodedElement convertToEncodedElement() {
        EncodedElement ele = new EncodedElement();
        byte[] result = new byte[data_32.length*4];
        int byteBase = -4;
        int valueIndex = 0;
        /*for(int i = 0; i < data_32.length/4; i++) {
            int value0 = data_32[valueIndex++];
            int value1 = data_32[valueIndex++];
            int value2 = data_32[valueIndex++];
            int value3 = data_32[valueIndex++];
            
            byteBase += 7;
            result[byteBase--] = (byte)(value0);
            value0 = value0 >> 8;
            result[byteBase--] = (byte)(value0);
            value0 = value0 >> 8;
            result[byteBase--] = (byte)(value0);
            value0 = value0 >> 8;
            result[byteBase] = (byte)(value0);            
            
            byteBase += 7;
            result[byteBase--] = (byte)(value1);
            value1 = value1 >> 8;
            result[byteBase--] = (byte)(value1);
            value1 = value1 >> 8;
            result[byteBase--] = (byte)(value1);
            value1 = value1 >> 8;
            result[byteBase] = (byte)(value1);
            
            byteBase += 7;
            result[byteBase--] = (byte)(value2);
            value2 = value2 >> 8;
            result[byteBase--] = (byte)(value2);
            value2 = value2 >> 8;
            result[byteBase--] = (byte)(value2);
            value2 = value2 >> 8;
            result[byteBase] = (byte)(value2);
            
            byteBase += 7;
            result[byteBase--] = (byte)(value3);
            value3 = value3 >> 8;
            result[byteBase--] = (byte)(value3);
            value3 = value3 >> 8;
            result[byteBase--] = (byte)(value3);
            value3 = value3 >> 8;
            result[byteBase] = (byte)(value3);
        }*/

        for(int i = 3 ; i < data_32.length*4; i+=7) {
            //byte 3 = byte 0
            //byte 2 = byte 1
            //byte 1 = byte 2
            //byte 0 = byte 3
            //int byteBase = i*4+3;
            //byteBase += 7;
            int value = data_32[valueIndex++];
            result[i--] = (byte)(value);
            value = value >> 8;
            result[i--] = (byte)(value);
            value = value >> 8;
            result[i--] = (byte)(value);
            value = value >> 8;
            result[i] = (byte)(value);

            /*result[byteBase++] = (byte)(value >> 24);
            result[byteBase++] = (byte)(value >> 16);
            result[byteBase++] = (byte)(value >> 8);
            result[byteBase] = (byte)(value);*/
        }

        //ele.data = convertIntArrayToByteArray(data_32);
        ele.data = result;
        ele.offset = offset;
        ele.usableBits = usableBits;
        return ele;
    }
    /**
     * Add a number of bits from an int to the end of this list's data. Will
     * add a new element if necessary. The bits stored are taken from the lower-
     * order of input.
     *
     * @param input Int containing bits to append to end.
     * @param bitCount Number of bits to append.
     * @return EncodedElement which actually contains the appended value.
     */
    @Override
    public EncodedElement addInt(int input, int bitCount) {
        if(next != null) {
            EncodedElement end = EncodedElement_32.getEnd_S(next);
            return end.addInt(input, bitCount);
        }
        else if(data_32.length*32 < usableBits+bitCount) {
            //create child and attach to next.
            //Set child's offset appropriately(i.e, manually set usable bits)
            int tOff = usableBits %32;
            //int size = data.length/2+1;
            int size = 1000;
            //guarantee that our new element can store our given value
            //if(size <= bitCount+tOff) size = (size+tOff+bitCount)*10;

            next = new EncodedElement_32(size, tOff);
            System.err.println("creating next node of size:bitCount "+size+
                    ":"+bitCount+":"+usableBits+":"+data_32.length);
            System.err.println("value: "+input);
                    //+this.toString()+"::"+next.toString());
            //add int to child
            return next.addInt(input, bitCount);
        }
        else {
            //At this point, we have the space, and we are the end of the chain.
            int startPos = this.usableBits;
            int[] dest = this.data_32;
            EncodedElement_32.addInt(input, bitCount, startPos, dest);
            usableBits +=  bitCount;
            return this;
        }
    }
}
