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
 * The RiceEncoder class is used to create FLAC-compliant rice-encoded
 * residuals.
 * 
 * @author Preston Lacey
 */
public class RiceEncoder {
    /** For debugging: Higher values equals greater output, generally in
     * increments of 10 */
    public static int DEBUG_LEV = 0;

    private static final int POSITIVE = 0;
    private static final int NEGATIVE = 1;
    private static final int STOP_BIT = 0xFFFFFFFF;
    private static final int UNARY_BIT = 0;
    
    private int[] _dataToEncode = null;
    private int[] _bitsToEncode = null;

    /**
     * Constructor. A RiceEncoder object is used(as opposed to potentially
     * faster static methods), for memory considerations; some temporary,
     * dynamically created arrays are kept between calls to the encode methods
     * to prevent frequently allocating and freeing similarly sized arrays.
     */
    public RiceEncoder() {
        
    }

    /**
     * Create the residual headers for a FLAC stream.
     *
     * @param useFiveBitParam Set TRUE if using a five-bit parameter size, FALSE
     *        for a four-bit parameter
     * @param order Specify order of partitions to be used(actual number of
     *              partitions will be 2^order.
     * @param ele   EncodedElement to write header to.
     * @return total written size of header.
     */
    public static int beginResidual(boolean useFiveBitParam, byte order,
            EncodedElement ele) {
        ele = ele.getEnd();
        int paramSize = (useFiveBitParam) ? 1:0;
        ele.addInt(paramSize, 2);
        ele.addInt(order, 4);
        return 6;
    }

    public static int encodeRicePartitionEscaped(int[] values, int inputOffset,
            int inputStep, int inputCount, EncodedElement destEle,
            int bitParam, boolean fiveBitParam) {
        if(DEBUG_LEV > 0)
            System.err.println("RiceEncoder::encode : Begin");
        /* Currently, we're passing in an EncodedElement with a set byte[], and
         filling that array. We should therefore ensure that we're not writing
         too much to it. We *can* add another element to the given one if need.*/

        //write headers(i.e, write the parameter)
        int bitsWritten = 0;
        byte[] data = destEle.getData();
        int destOffset = destEle.getUsableBits();
        if(fiveBitParam) {
            EncodedElement.addInt(255, 5, destOffset, data);
            destOffset += 5;
            EncodedElement.addInt(16,5,destOffset,data);
            destOffset += 5;
            bitsWritten += 10;
        }
        else {
            EncodedElement.addInt(255, 4, destOffset, data);
            destOffset += 4;
            EncodedElement.addInt(16,5,destOffset,data);
            destOffset += 5;
            bitsWritten += 9;
        }
        int maxBits = data.length*8;
        System.err.println("FIRST IN RICE : " +
                Integer.toHexString(values[0*inputStep+inputOffset]));
        System.err.println("LAST IN RICE : " +
                Integer.toHexString(values[(inputCount-1)*inputStep+inputOffset]));
        for(int i = 0; i < inputCount; i++) {
            if(destOffset + 16 >= maxBits) {
                if(DEBUG_LEV > 10) {
                    System.err.println("-- Adding EncodedElement to destEle");
                }
                destEle.setUsableBits(destOffset);
                EncodedElement temp = new EncodedElement();
                destEle.attachEnd(temp);
                destEle = temp;
                //new size is remaining count, plus 10 for some extra room,
                //  times sign+5 for unary + 1 unary end + bits
                byte[] newData = new byte[(inputCount-i+10)*16];
                destEle.setData(newData);
                maxBits = newData.length*8;
                destOffset = destOffset%8;
                data = newData;
            }
            EncodedElement.addInt(values[i*inputStep+inputOffset], 16, destOffset, data);
            destOffset += 16;
            bitsWritten += 16;
        }

        if(DEBUG_LEV > 0)
            System.err.println("RiceEncoder::encode : End");
        destEle.setUsableBits(destOffset);
        return bitsWritten;
    }

    /**
     * Rice-encode a set of values, adding necessary headers for FLAC format. This
     * encodes a single rice-partition. In general, beginResidual(...) should be
     * used before this method.
     *
     * @param values array of integer values to save
     * @param inputOffset start index in input array
     * @param inputStep number of values to skip between target values(for
     *        interleaved data.
     * @param inputCount    number of total values to encode
     * @param bitParam rice-parameter to use. This value should be based upon
     *        the distribution of the input data(roughly speeking, each value
     *        will require at least bitParam+1 bits to save, so this value
     *        should reflect the average magnitude of input values.
     * @param destEle   EncodedElement to save result to.
     * @param fiveBitParam Set true if this header should use a five-bit
     * rice-parameter, false for a four bit parameter.
     * @return total encoded size(including headers)
     */
    public int encodeRicePartition(int[] values, int inputOffset,
            int inputStep, int inputCount, EncodedElement destEle,
            int bitParam, boolean fiveBitParam) {
        //Pack int version of encode partition
        if(DEBUG_LEV > 0) {
            System.err.println("RiceEncoder::encode : Begin");
            System.err.println("-- bitParam: " + bitParam);
        }

        //write headers(i.e, write the parameter)
        int startBits = destEle.getTotalBits();
        if(fiveBitParam) {
            destEle.addInt(bitParam, 5);
        }
        else {
            destEle.addInt(bitParam, 4);
        }
        //encode each input value;
        if(_dataToEncode == null || _bitsToEncode == null) {
            _dataToEncode = new int[values.length*4];
            _bitsToEncode = new int[values.length*4];
        }
        int[] dataToEncode = _dataToEncode;
        int[] bitsToEncode = _bitsToEncode;
        int nextToEncode = 0;
        int maxToEncode = dataToEncode.length;
        //System.err.println("RiceENcoder begin loop:"+bitParam);
        int inputIndex = inputOffset-inputStep;
        for(int i = 0; i < inputCount; i++) {
            inputIndex +=inputStep;
            int value = values[inputIndex];
            value = (value < 0) ? -2*value-1:2*value;
            int upperBits = value >> bitParam;
            //make sure we won't write to much. Handle if we will.
            int dataToEncodeSpaceNeeded = (2+upperBits/32);
            if(upperBits%32 != 0)
                dataToEncodeSpaceNeeded++;
            if(dataToEncodeSpaceNeeded+nextToEncode >= maxToEncode) {
                //write everything we have:
                destEle.packIntByBits(dataToEncode, bitsToEncode, 0, nextToEncode);
                nextToEncode = 0;
            }
            //write unary upper bits:
            int count = 0;
            while(upperBits > 0) {
                int tempVal = (upperBits > 32) ? 32:upperBits;//can only write 32 bits at a time.
                dataToEncode[nextToEncode] = UNARY_BIT;
                bitsToEncode[nextToEncode++] = tempVal;
                upperBits -= tempVal;
                count++;
            }
            //write upperBits ending.
            dataToEncode[nextToEncode] = STOP_BIT;
            bitsToEncode[nextToEncode++] = 1;
            //write lowerBits
            dataToEncode[nextToEncode] = value;
            bitsToEncode[nextToEncode++] = bitParam;

        }
        //System.err.println("end loop");
        //write remaining data to encode
        if(nextToEncode > 0) {
            destEle.packIntByBits(dataToEncode, bitsToEncode, 0, nextToEncode);
            nextToEncode = 0;
        }
        int bitsWritten = destEle.getTotalBits() - startBits;
        //System.err.println("RiceENcoder encode end:");
        return bitsWritten;
    }



    //Dispatcher for development tests
    /*public int encodeRicePartitionDispatcher(int[] values, int inputOffset,
            int inputStep, int inputCount, EncodedElement destEle,
            int bitParam, boolean fiveBitParam) {

        //return RiceEncoder.encodeRicePartitionOld(values, inputOffset, inputStep, inputCount, destEle, bitParam, fiveBitParam);
        //return RiceEncoder.encodeRicePartitionPackInt(values, inputOffset, inputStep, inputCount, destEle, bitParam, fiveBitParam);
        //return this.encodeRicePartitionObjectDest(values, inputOffset, inputStep, inputCount, destEle, bitParam, fiveBitParam);
        return 0;
    }*/

    /**
     * Calculate how large a given set of values will be once it has been
     * rice-encoded. While this method duplicates much of the process of
     * rice-encoding, it is faster than an actual encode since the data is not
     * actually written to the flac bitstream format(a rather costly write).
     *
     * @param values array of integer values to save
     * @param inputOffset start index in input array
     * @param inputStep number of values to skip between target values(for
     *        interleaved data.
     * @param inputCount    number of total values to encode
     * @param bitParam rice-parameter to use. This value should be based upon
     *        the distribution of the input data(roughly speeking, each value
     *        will require at least bitParam+1 bits to save, so this value
     *        should reflect the average magnitude of input values.
     * @return total encoded-size with given data and rice-parameter.
     */
    public static int calculateEncodeSize(int[] values, int inputOffset,
            int inputStep, int inputCount, int bitParam) {
        //Pack int version of encode partition
        if(DEBUG_LEV > 0) {
            System.err.println("RiceEncoder::calculateEncodeSize : Begin");
            System.err.println("-- bitParam: " + bitParam);
        }
        int totalEncodeLength = inputCount*(bitParam+1);
        int index = inputOffset-inputStep;
        for(int i = 0; i < inputCount; i++) {
            index += inputStep;
            int value = values[index];
            value = (value < 0) ? -2*value-1:2*value;
            int upperBits = value >> bitParam;
            totalEncodeLength += upperBits;
        }
        if(bitParam > 14)
            totalEncodeLength += 5+6;
        else
            totalEncodeLength += 4+6;
        return totalEncodeLength;
    }
 
}
