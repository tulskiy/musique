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
 * MetadataBlockStreamInfo is used to declare the initial stream parameters,
 * such as Sample Rate, bits per sample, and number of channels, as well as
 * information on the encoded stream such as total number of samples, minimum
 * and maximum block and frame sizes, and md5sum of raw audio samples. A
 * StreamInfo block must be the first meta-data block in a FLAC stream, and only
 * one StreamInfo block may exist.
 *
 * @author Preston Lacey
 */
public class MetadataBlockStreamInfo {
    /** For Debugging: Higher level equals more debug statements */
    static int DEBUG_LEV = 0;
/*    int minimumBlockSize = 4096;//32768;//16 bits used; 16 minimum valid
    int maximumBlockSize = 4096;//32768;//16 bits used; 65535 maximum valid
    int minimumFrameSize = 0;//24 bits used; zero implies unknown
    int maximumFrameSize = 0;//24 bits used; zero implies unknown
    int sampleRate = 8000;//4096;//20 bits used, but 655350Hz max. 0 is invalid.
    byte numberOfChannels = 1;//3 bits used
    byte bitsPerSample = 16;//5 bits used. values 4-32 valid
    long totalSamplesInStream = 0;//36 bits used. 0 implies unknown.
    byte[] md5Hash;
*/
    /**
     * Constructor. This class defines only static methods and fields.
     */
    public MetadataBlockStreamInfo() {
        
    }

    /**
     * Create a FLAC StreamInfo metadata block with the given parameters. Because
     * of the data stored in a StreamInfo block, this should generally be created
     * only after all encoding is done.
     *
     * @param sc StreamConfiguration used in this FLAC stream.
     * @param minFrameSize Size of smallest frame in FLAC stream.
     * @param maxFrameSize Size of largest frame in FLAC stream.
     * @param samplesInStream Total number of inter-channel audio samples in
     * FLAC stream.
     * @param md5Hash MD5 hash of the raw audio samples.
     * @return EncodedElement containing created StreamInfo block.
     */
    public static EncodedElement getStreamInfo(StreamConfiguration sc,
            int minFrameSize, int maxFrameSize, long samplesInStream,
            byte[] md5Hash) {
        
        EncodedElement ele = new EncodedElement();
        int bytes = getByteSize();
        byte data[] = new byte[bytes];
        ele.setData(data);
        ele.setUsableBits(bytes*8);
        int encodedBitsPerSample = sc.getBitsPerSample()-1;
        //System.out.println("BitsPerSample : " + encodedBitsPerSample);
        int index = 0;
        EncodedElement.addInt(sc.getMinBlockSize(), 16, index, data);
        index += 16;
        EncodedElement.addInt(sc.getMaxBlockSize(), 16, index, data);
        index += 16;
        EncodedElement.addInt(minFrameSize, 24, index, data);
        index += 24;
        EncodedElement.addInt(maxFrameSize, 24, index, data);
        index += 24;
        EncodedElement.addInt(sc.getSampleRate(), 20, index, data);
        index += 20;
        EncodedElement.addInt(sc.getChannelCount()-1, 3, index, data);
        index += 3;
        EncodedElement.addInt(encodedBitsPerSample, 5, index, data);
        index +=5;
        EncodedElement.addLong(samplesInStream, 36, index, data);
        index +=36;
        //obs.addLong(md5Begin, 64, obs.getUsedLength());
        //obs.addLong(md5End, 64, obs.getUsedLength());
        for(int i = 0; i < 16; i++) {
            EncodedElement.addInt(md5Hash[i], 8, index, data);
            index += 8;
        }
        ele.setUsableBits(index);
        return ele;
    }

    /**
     * Get the expected size of a properly formed STREAMINFO metadata block.
     * 
     * @return size of properly formed FLAC STREAMINFO metadata block.
     */
    static public int getByteSize() {
        int size = 0;
        size += 16;
        size += 16;
        size += 24;
        size += 24;
        size += 20;
        size += 3;
        size += 5;
        size += 36;
        size += 64;
        size += 64;
        size = size/8;
        return size;
    }


}

