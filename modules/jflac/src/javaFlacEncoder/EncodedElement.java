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
 * EncodedElement class provides is used to store data in the proper bitstream
 * format for FLAC audio. Methods are provided to easily append values to the
 * bitstream. Conceptually, an EncodedElement is a list structure, in which the
 * encoded data is stored in a byte array. It is assumed that any data stored by
 * objects of this class will be retrieved through direct access to the
 * underlying byte array. This byte array is exposed to outside objects
 * primarily to allow faster access than "proper" object-oriented design might
 * allow.
 * 
 * @author Preston Lacey
 */
public class EncodedElement {
   
    /** For Debugging: Higher level equals more debug statements */
    static int DEBUG_LEV = 0;

    /** Previous element in list. At current times, this should not be dependend
     * on to be set */
    EncodedElement previous = null;

    /** Next element in list. */
    EncodedElement next = null;

    /** Data stored by this element. Member usableBits should be used to track
     * the last valid index at a bit level. */
    byte[] data = null;

    /** Use to track the last valid index of the data array at a bit level. For
     * example, a value of "10" would mean the first byte and two low-order bits
     * of the second byte are used. usableBits must always be equal or greater
     * than offset.
     */
    int usableBits = 0;//i.e, the last write location in 'data' array.

    /** Used to signify the index of the first valid bit of the data array. For
     * purposes of speed, it is not always best to pack the data starting at
     * bit zero of first byte. offset must always be less than or equal to
     * usableBits.
     */
    protected int offset;

    /**
     * Constructor, creates an empty element with offset of zero and array size
     * of 100. This array can be replaced with a call to setData(...).
     */
    public EncodedElement() {
        offset = 0;
        usableBits = 0;
        data = new byte[100];
    }

    /**
     * Constructor. Creates an EncodedElement with the given size and offset
     * @param size Size of data array to use(in bytes)
     * @param off Offset to use for this element. Usablebits will also be set
     * to this value.
     */
    public EncodedElement(int size, int off) {
        data = new byte[size];
        usableBits = off;
        offset = off;
    }

    /**
     * Completely clear this element and use the given size and offset for the
     * new data array.
     *
     * @param size Size of data array to use(in bytes)
     * @param off Offset to use for this element. Usablebits will also be set to
     * this value.
     */
    public void clear(int size, int off) {
        next = null;
        previous = null;
        data = new byte[size];
        offset = off;
        usableBits = off;
    }

    /**
     * Set the object previous to this in the list.
     * @param ele   the object to set as previous.
     * @return      <code>void</code>
     * 
     * Precondition: none
     * Post-condition: getPrevious() will now return the given object. Any 
     * existing “previous” was lost.
     */
    void setPrevious(EncodedElement ele) {
        previous = ele;
    }

    /**
     * Set the object next to this in the list.
     * @param ele   the object to set as next.
     * @return      void
     * Pre-condition: none
     * Post-condition: getNext() will now return the given object. Any existing
     * “next” was lost.
     */
    void setNext(EncodedElement ele) {
        next = ele;        
    }

    /**
     * Get the object stored as the previous item in this list.
     *
     * @return EncodedElement
     */
    EncodedElement getPrevious() {
        return previous;
    }
    /**
     * Get the object stored as the next item in this list.
     * 
     * @param EncodedElement;
     */
    EncodedElement getNext() {
        return next;
    }

    /**
     * Set the byte array stored by this object.
     * 
     * @param data  the byte array to store.
     *
     * Pre-condition: None
     * Post-condition: 'data' is now stored by this object. Any previous data
     * stored was lost.
     */   
    void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Set the number of bits of the given array that are usable data. Data is
     * packed from the lower indices to higher.
     *
     * @param bits  the value to store     
     */
    void setUsableBits(int bits) {
        usableBits = bits;
    }

    /**
     * Get the byte array stored by this object(null if not set).
     *
     * @param byte[]    the data stored in this byte[] is likely not all usable.
     *                  Method getUsableBits() should be used to determine such.
     */
    byte[] getData() {
        return data;
    }

    /**
     * get the number of bits usable in the stored array.
     * @return int
     */
    int getUsableBits() {
        return usableBits;
    }

    /**
     * Return the last element of the list given. This is a static funtion to
     * provide minor speed improvement. Loops through all elements' "next"
     * pointers, till the last is found.
     * @param e EncodedElement list to find end of.
     * @return Final element in list.
     */
    protected static EncodedElement getEnd_S(EncodedElement e) {
        if(e == null)
            return null;
        EncodedElement temp = e.next;
        EncodedElement end = e;
        while(temp != null) {
            end = temp;
            temp = temp.next;
        }
        return end;
    }

    /**
     * Return the last element of the list given. Loops through all elements'
     * "next" pointers, till the last is found.
     * @return last element in this list
     */
    public EncodedElement getEnd() {
        EncodedElement temp = next;
        EncodedElement end = this;
        while(temp != null) {
            end = temp;
            temp = temp.next;
        }
        return end;
    }

    /**
     * Attach an element to the end of this list.
     *
     * @param e Element to attach.
     * @return True if element was attached, false otherwise.
     */
    public boolean attachEnd(EncodedElement e) {
        if(DEBUG_LEV > 0)
            System.err.println("EncodedElement::attachEnd : Begin");
        boolean attached = true;
        EncodedElement current = this;
        while(current.getNext() != null) {
            current = current.getNext();            
        }
        current.setNext(e);
        e.setPrevious(current);
        if(DEBUG_LEV > 0)
            System.err.println("EncodedElement::attachEnd : End");
        return attached;
    }

    /**
     * Add a number of bits from a long to the end of this list's data. Will
     * add a new element if necessary. The bits stored are taken from the lower-
     * order of input.
     *
     * @param input Long containing bits to append to end.
     * @param bitCount Number of bits to append.
     * @return EncodedElement which actually contains the appended value.
     */
    public EncodedElement addLong(long input, int bitCount) {
        if(next != null) {
            EncodedElement end = EncodedElement.getEnd_S(next);
            return end.addLong(input, bitCount);
        }
        else if(data.length*8 <= usableBits+bitCount) {
            //create child and attach to next.
            //Set child's offset appropriately(i.e, manually set usable bits)
            int tOff = usableBits %8;
            int size = data.length/2+1;
            //guarantee that our new element can store our given value
            if(size < bitCount) size = bitCount*10;
            next = new EncodedElement(size, tOff);
            //add int to child
            return next.addLong(input, bitCount);
        }
        //At this point, we have the space, and we are the end of the chain.
        int startPos = this.usableBits;
        byte[] dest = this.data;
        EncodedElement.addLong(input, bitCount, startPos, dest);
        usableBits +=  bitCount;
        return this;
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
    public EncodedElement addInt(int input, int bitCount) {
        if(next != null) {
            EncodedElement end = EncodedElement.getEnd_S(next);
            return end.addInt(input, bitCount);
        }
        else if(data.length*8 < usableBits+bitCount) {
            //create child and attach to next.
            //Set child's offset appropriately(i.e, manually set usable bits)
            int tOff = usableBits %8;
            //int size = data.length/2+1;
            int size = 1000;
            //guarantee that our new element can store our given value
            //if(size <= bitCount+tOff) size = (size+tOff+bitCount)*10;
            
            next = new EncodedElement(size, tOff);
            System.err.println("creating next node of size:bitCount "+size+
                    ":"+bitCount+":"+usableBits+":"+data.length);
            System.err.println("value: "+input);
                    //+this.toString()+"::"+next.toString());
            //add int to child
            return next.addInt(input, bitCount);
        }
        else {
            //At this point, we have the space, and we are the end of the chain.
            int startPos = this.usableBits;
            byte[] dest = this.data;
            EncodedElement.addInt(input, bitCount, startPos, dest);
            usableBits +=  bitCount;
            return this;
        }
    }

    /**
     * Append an equal number of bits from each int in an array within given
     * limits to the end of this list.
     *
     * @param inputArray Array storing input values.
     * @param bitSize number of bits to store from each value.
     * @param start index of first usable index.
     * @param skip number of indices to skip between values(in case input data
     * is interleaved with non-desirable data).
     * @param countA Number of total indices to store from.
     * @return EncodedElement containing end of packed data. Data may flow
     * between multiple EncodedElement's, if an existing element was not large
     * enough for all values.
     */
    public EncodedElement packInt(int[] inputArray, int bitSize,
            int start, int skip, int countA) {
        //go to end if we're not there.
        if(next != null) {
            EncodedElement end = EncodedElement.getEnd_S(next);
            return end.packInt(inputArray, bitSize, start, skip, countA);
        }
        //calculate how many we can pack into current.
        int writeCount = (data.length*8 - usableBits) / bitSize;
        if(writeCount > countA) writeCount = countA;
        //pack them and update usable bits.
        EncodedElement.packInt(inputArray, bitSize, usableBits, start, skip, countA, data);
        usableBits += writeCount * bitSize;
        //if more remain, create child object and add there
        countA -= writeCount;
        if(countA > 0) {
            int tOff = usableBits %8;
            int size = data.length/2+1;
            //guarantee that our new element can store our given value
            if(size < bitSize*countA) size = bitSize*countA+10;
            next = new EncodedElement(size, tOff);
            //add int to child
            return next.packInt(inputArray, bitSize, start+writeCount*(skip+1), skip, countA);
        }
        else {
            //return last object we write to.
            return this;
        }
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
    public EncodedElement packIntByBits(int[] inputA, int[] inputBits, int inputOffset,
            int countA) {
                //go to end if we're not there.
        if(next != null) {
            EncodedElement end = EncodedElement.getEnd_S(next);
            return end.packIntByBits(inputA, inputBits, inputOffset, countA);
        }
        //calculate how many we can pack into current.
        int writeBitsRemaining = data.length*8 - usableBits;
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
            EncodedElement.packIntByBits(inputA, inputBits, inputOffset,
                    writeCount, usableBits, data);
            usableBits += willWrite;
        }
        //if more remain, create child object and add there
        countA -= writeCount;
        if(countA > 0) {
            inputOffset += writeCount;
            int tOff = usableBits %8;
            int size = data.length/2+1;
            //guarantee that our new element can store our given value
            int remainingToWrite = 0;
            for(int i = 0; i < countA; i++) {
                remainingToWrite += inputBits[inputOffset+i];
            }
            remainingToWrite = remainingToWrite / 8 + 1;
            if(size < remainingToWrite) size = remainingToWrite+10;
            //System.err.println("remaining: "+remainingToWrite);
            //System.err.println("creating size/offset : "+size+":"+tOff);
            next = new EncodedElement(size, tOff);
            //add int to child
            return next.packIntByBits(inputA, inputBits, inputOffset, countA);
        }
        else {
            //System.err.println("returning....done");
            //return if this is last object we wrote to.
            return this;
        }
    }

    /**
     * Total number of usable bits stored by this entire list. This sums the
     * difference of each list element's "usableBits" and "offset".
     * @return Total valid bits in this list.
     */
    public int getTotalBits() {
        //this total calculates and removes the bits reserved for "offset"
        //   between the different children.
        int total = 0;
        EncodedElement iter = this;
        while(iter != null) {
            total += iter.usableBits - iter.offset;
            iter = iter.next;
        }
        return total;
    }

    /**
     * This method adds a given number of bits of an int to a byte array.
     * @param value int to store bits from
     * @param count number of low-order bits to store
     * @param startPos start bit location in array to begin writing
     * @param dest array to store bits in. dest MUST have enough space to store
     * the given data, or this function will fail.
     */
    public static void addInt(int value, int count, int startPos, byte[] dest) {
        int currentByte = startPos/8;
        int currentOffset = startPos%8;
        //int upShift = 32-count;
        //upShift += (8-currentOffset);
        //int upShift = 39-count+currentOffset;
        int upShift = 40-count-currentOffset;
        //get the value to a workable place and clear the extraneous bits
        long upperMask = -1 >>> (63-count);
        long val = (long)value & upperMask;
        
        val = val << upShift;
        //get the bytes ready
        int []bs = new int[5];
        bs[4] = (byte)val;
        val = val >> 8;
        bs[3] = (byte)val;
        val = val >> 8;
        bs[2] = (byte)val;
        val = val >> 8;
        bs[1] = (byte)val;
        val = val >> 8;
        bs[0] = (byte)val;

        //byte One
        int bIndex = 0;
        if(currentOffset > 0) {
            int b1Mask = 255 >> currentOffset;
            int bitRoom = 8-currentOffset;
            int lowerRoom = bitRoom - count;
            if(lowerRoom > 0) {
                b1Mask = b1Mask >>> lowerRoom;
                b1Mask = b1Mask << lowerRoom;
            }
            bs[0] = bs[0] & b1Mask;
            int b1d = dest[currentByte] & ~b1Mask;
            dest[currentByte++] = (byte)(bs[0] | b1d);
            count -= bitRoom;
            bIndex++;
        }
        //int bIndex = 1;
        int midCount = count/8;
        switch(midCount) {
            case 4: dest[currentByte++] = (byte)bs[bIndex++];
            case 3: dest[currentByte++] = (byte)bs[bIndex++];
            case 2: dest[currentByte++] = (byte)bs[bIndex++];
            case 1: dest[currentByte++] = (byte)bs[bIndex++];break;
        }
        count -= midCount*8;
        if(count > 0) {
            //int lastMask = 255 >> 8-count;
            int lastMask = 255 << 8-count;
            dest[currentByte] = (byte)(dest[currentByte] & ~lastMask);
            dest[currentByte] = (byte)(bs[bIndex] | dest[currentByte]);
        }

    }

   /** public static void addIntOld(int input, int count, int startPos, byte[] dest) {
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::addInt : Begin");
        int currentByte = startPos/8;
        int currentOffset = startPos%8;
        int bitRoom;//how many bits can be placed in current byte
        int upMask;//to clear upper bits(lower bits auto-cleared by L-shift
        int downShift;//bits to shift down, isolating top bits of input
        int upShift;//bits to shift up, packing byte from top.
        while(count > 0) {
            //find how many bits can be placed in current byte
            bitRoom = 8-currentOffset;
            //get those bits
            //i.e, take upper 'bitsNeeded' of input, put to lower part of byte.
            downShift = count-bitRoom;
            upMask = 255 >>> currentOffset;
            upShift = 0;
            if(downShift < 0) {
                //upMask = 255 >>> bitRoom-count;
                upShift = bitRoom - count;
                upMask = 255 >>> (currentOffset+upShift);
                downShift = 0;
            }
            if(DEBUG_LEV > 30) {
                System.err.println("count:offset:bitRoom:downShift:upShift:" +
                        count+":"+currentOffset+":"+bitRoom+":"+downShift+":"+upShift);
            }
            int currentBits = (input >>> downShift) & (upMask);
            //shift bits back up to match offset
            currentBits = currentBits << upShift;
            upMask = (byte)upMask << upShift;

            dest[currentByte] = (byte)(dest[currentByte] & (~upMask));
            //merge bytes~
            dest[currentByte] = (byte)(dest[currentByte] | currentBits);
            //System.out.println("new currentByte: " + dest[currentByte]);
            count -= bitRoom;
            currentOffset = 0;
            currentByte++;
        }
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::addInt : End");
    }
**/

    /**
     * This method adds a given number of bits of a long to a byte array.
     * @param input long to store bits from
     * @param count number of low-order bits to store
     * @param startPos start bit location in array to begin writing
     * @param dest array to store bits in. dest MUST have enough space to store
     * the given data, or this function will fail.
     */

    public static void addLong(long input, int count, int startPos, byte[] dest) {
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::addLong : Begin");
        int currentByte = startPos/8;
        int currentOffset = startPos%8;
        int bitRoom;//how many bits can be placed in current byte
        long upMask;//to clear upper bits(lower bits auto-cleared by L-shift
        int downShift;//bits to shift down, isolating top bits of input
        int upShift;//bits to shift up, packing byte from top.
        while(count > 0) {
            //find how many bits can be placed in current byte
            bitRoom = 8-currentOffset;
            //get those bits
            //i.e, take upper 'bitsNeeded' of input, put to lower part of byte.
            downShift = count-bitRoom;
            upMask = 255 >>> currentOffset;
            upShift = 0;
            if(downShift < 0) {
                //upMask = 255 >>> bitRoom-count;
                upShift = bitRoom - count;
                upMask = 255 >>> (currentOffset+upShift);
                downShift = 0;
            }
            if(DEBUG_LEV > 30) {
                System.err.println("count:offset:bitRoom:downShift:upShift:" +
                        count+":"+currentOffset+":"+bitRoom+":"+downShift+":"+upShift);
            }
            long currentBits = (input >>> downShift) & (upMask);
            //shift bits back up to match offset
            currentBits = currentBits << upShift;
            upMask = (byte)upMask << upShift;

            dest[currentByte] = (byte)(dest[currentByte] & (~upMask));
            //merge bytes~
            dest[currentByte] = (byte)(dest[currentByte] | currentBits);
            //System.out.println("new currentByte: " + dest[currentByte]);
            count -= bitRoom;
            currentOffset = 0;
            currentByte++;
        }
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::addLong : End");
    }

   /** public static void packIntOLD_WORKING(int[] inputArray, int startBitSize, int startPos,
            int start, int skip, int count, byte[] dest) {
        if(DEBUG_LEV > 0)
            System.err.println("EncodedElement::packInt : Begin");
        if(DEBUG_LEV > 10)
            System.err.println("start:skip:count : " +start+":"+skip+":"+count);
        for(int i = 0; i < count; i++) {
            addInt(inputArray[i*(skip+1)+start], startBitSize, startPos, dest);
            startPos+=startBitSize;
        }
    }
**/

    /**
     * Append an equal number of bits from each int in an array within given
     * limits to the given byte array.
     *
     * @param inputArray Array storing input values.
     * @param bitSize number of bits to store from each value.
     * @param start index of first usable index.
     * @param skip number of indices to skip between values(in case input data
     * is interleaved with non-desirable data).
     * @param countA Number of total indices to store from.
     * @param startPosIn First usable index in destination array(byte
     * index = startPosIn/8, bit within that byte = startPosIn%8)
     * @param dest Destination array to store input values in. This array *must*
     * be large enough to store all values or this method will fail in an
     * undefined manner.
     */
    public static void packInt(int[] inputArray, int bitSize, int startPosIn,
            int start, int skip, int countA, byte[] dest) {
                if(DEBUG_LEV > 30)
        System.err.println("EncodedElement::packInt : Begin");
        for(int valI = 0; valI < countA; valI++) {
            //int input = inputArray[valI];
            int input = inputArray[valI*(skip+1)+start];
            int count = bitSize;
            int startPos = startPosIn+valI*bitSize;
            int currentByte = startPos/8;
            int currentOffset = startPos%8;
            int bitRoom;//how many bits can be placed in current byte
            int upMask;//to clear upper bits(lower bits auto-cleared by L-shift
            int downShift;//bits to shift down, isolating top bits of input
            int upShift;//bits to shift up, packing byte from top.
            while(count > 0) {
                //find how many bits can be placed in current byte
                bitRoom = 8-currentOffset;
                //get those bits
                //i.e, take upper 'bitsNeeded' of input, put to lower part of byte.
                downShift = count-bitRoom;
                upMask = 255 >>> currentOffset;
                upShift = 0;
                if(downShift < 0) {
                    //upMask = 255 >>> bitRoom-count;
                    upShift = bitRoom - count;
                    upMask = 255 >>> (currentOffset+upShift);
                    downShift = 0;
                }
                if(DEBUG_LEV > 30) {
                    System.err.println("count:offset:bitRoom:downShift:upShift:" +
                            count+":"+currentOffset+":"+bitRoom+":"+downShift+":"+upShift);
                }
                int currentBits = (input >>> downShift) & (upMask);
                //shift bits back up to match offset
                currentBits = currentBits << upShift;
                upMask = (byte)upMask << upShift;

                dest[currentByte] = (byte)(dest[currentByte] & (~upMask));
                //merge bytes~
                dest[currentByte] = (byte)(dest[currentByte] | currentBits);
                //System.out.println("new currentByte: " + dest[currentByte]);
                count -= bitRoom;
                currentOffset = 0;
                currentByte++;
            }
        }
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::packInt: End");

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
            int countA, int startPosIn, byte[] dest) {
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::packIntByBits : Begin");
        //int offsetCounter = 0;
        int inputIter = 0;
        int startPos = startPosIn;//the position to write to in output array
        inputIter = inputOffset;
        int inputStop = countA+inputOffset;
        for(int valI = inputOffset; valI < inputStop; valI++) {
            //inputIter = valI+inputOffset;
            //inputIter += valI;
            int input = inputA[valI];//value to encode
            int count = inputBits[valI];//bits of value to encode
            //int startPos = startPosIn+offsetCounter;//the write bit position
            //offsetCounter += count;
            int currentByte = startPos/8;
            int currentOffset = startPos%8;
            startPos += count;//startPos must not be referenced again below here!
            int bitRoom;//how many bits can be placed in current byte
            int upMask;//to clear upper bits(lower bits auto-cleared by L-shift
            int downShift;//bits to shift down, isolating top bits of input
            int upShift;//bits to shift up, packing byte from top.
            if(currentOffset != 0) {
                bitRoom = 8-currentOffset;
                //get those bits
                //i.e, take upper 'bitsNeeded' of input, put to lower part of byte.
                downShift = count-bitRoom;
                upMask = 255 >>> currentOffset;
                upShift = 0;
                if(downShift < 0) {
                    //upMask = 255 >>> bitRoom-count;
                    upShift = bitRoom - count;
                    upMask = 255 >>> (currentOffset+upShift);
                    downShift = 0;
                }
                if(DEBUG_LEV > 30) {
                    System.err.println("count:offset:bitRoom:downShift:upShift:" +
                            count+":"+currentOffset+":"+bitRoom+":"+downShift+":"+upShift);
                }
                int currentBits = (input >>> downShift) & (upMask);
                //shift bits back up to match offset
                currentBits = currentBits << upShift;
                upMask = (byte)upMask << upShift;

                dest[currentByte] = (byte)(dest[currentByte] & (~upMask));
                //merge bytes~
                dest[currentByte] = (byte)(dest[currentByte] | currentBits);
                //System.out.println("new currentByte: " + dest[currentByte]);
                count -= bitRoom;
                currentOffset = 0;
                currentByte++;
            }
            bitRoom = 8;
            upShift = 0;
            upMask = 255;
            while(count >= 8) {
                //find how many bits can be placed in current byte
                //get those bits
                //i.e, take upper 'bitsNeeded' of input, put to lower part of byte.
                downShift = count-bitRoom;
                if(DEBUG_LEV > 30) {
                    System.err.println("count:offset:bitRoom:downShift:upShift:" +
                            count+":"+currentOffset+":"+bitRoom+":"+downShift+":"+upShift);
                }
                int currentBits = (input >>> downShift) & (upMask);
                dest[currentByte] = (byte)currentBits;
                count -= bitRoom;
                currentByte++;
            }
            if(count > 0) {//while(count > 0) {
                //find how many bits can be placed in current byte
                //bitRoom = 8-currentOffset;
                //get those bits
                //i.e, take upper 'bitsNeeded' of input, put to lower part of byte.
                //downShift = count-bitRoom;
                downShift = 0;
                upShift = bitRoom - count;
                upMask = 255 >>> upShift;

                if(DEBUG_LEV > 30) {
                    System.err.println("count:offset:bitRoom:downShift:upShift:" +
                            count+":"+currentOffset+":"+bitRoom+":"+downShift+":"+upShift);
                }
//                int currentBits = (input >>> downShift) & (upMask);
                int currentBits = input & upMask;
                //shift bits back up to match offset
                currentBits = currentBits << upShift;
                upMask = (byte)upMask << upShift;

                dest[currentByte] = (byte)(dest[currentByte] & (~upMask));
                //merge bytes~
                dest[currentByte] = (byte)(dest[currentByte] | currentBits);
                //System.out.println("new currentByte: " + dest[currentByte]);
                count -= bitRoom;
                currentOffset = 0;
                currentByte++;
            }
        }
        if(DEBUG_LEV > 30)
            System.err.println("EncodedElement::addInt : End");
    }

    /**
     * Force the usable data stored in this list ends on a a byte boundary, by
     * padding to the end with zeros.
     * 
     * @return true if the data was padded, false if it already ended on a byte
     * boundary.
     */
    public boolean padToByte() {
        boolean padded = false;

        //System.err.println("Usable bits: "+tempVal);
        EncodedElement end = EncodedElement.getEnd_S(this);
        int tempVal = end.usableBits;
        if(tempVal % 8 != 0) {
            int toWrite = 8-(tempVal%8);
            end.addInt(0, toWrite);
            /* FOR DEVEL ONLY: */
            if( (this.getTotalBits()+offset) % 8 != 0)
                System.err.println("EncodedElement::padToByte: SERIOUS ERROR! " +
                        "Algorithm implemented is incorrect!!!");
            padded = true;
        }
        return padded;
    }
}
