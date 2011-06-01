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
 * This class is used to generate a Frame Header for a FLAC Frame.
 * 
 * @author Preston Lacey
 */
public class FrameHeader {

    /** For Debugging: Higher level equals more debug statements */
    public static int DEBUG_LEV = 0;

    private static final int definedBlockSizes[] = {
      -1,
      192,
      576,
      1152,
      2304,
      4608,
      -1,
      -1,
      256,
      512,
      1024,
      2048,
      4096,
      8192,
      16384,
      32768
    };

    private static final int definedSampleRates[] = {
        0,
        88200,
        176400,
        192000,
        8000,
        16000,
        22050,
        24000,
        32000,
        44100,
        48000,
        96000,
        -1,
        -1,
        -1,
        -1
    };

    /** Maximum size a header can be according to FLAC specification. */
    public static final int MAX_HEADER_SIZE = 128;//in bytes

    /** Synchronization code used at beginning of frame(low-order 14 bits
     *  used) */
    public static final short syncCode = 0x3FFE;//14 bits used

    static final byte reserved = 0;//1 bit used; 0 mandatory value

    byte blockingStrategy = 0;//1 bit used; 0=fixed-blocksize. 1=variable
    byte blockSize = 0xC;//4 bits used; see format docs
    byte sampleRate = 4;//0;//4 bits used; see format docs
    //byte channelAssignment = 0;//4 bits used; see format docs
    byte sampleSize = 4;//3 bits used; see format docs

    static final byte reserved2 = 0;//1 bit used; 0 mandatory value
    long frameNumber = 0;//8-56 bits used; UTF-8 coded sample number
    int blockSizeMod = 0;//if(blocksize bits == 011x)  8/16 bit (blocksize-1)
    int SampleRateMod = 0;//if(sample rate bits == 11xx) 8/16 bit sample rate
    byte crc8 = 0;
    CRC8 crcCalculator;

    /**
     * Constructor creates a new FrameHeader object which is ready to
     * generate headers. We can't use static functions to do this, since the
     * process uses a CRC8 object which must be instantiated.
     *
     */
    public FrameHeader() {
        crcCalculator = new CRC8();
    }

    /**
     * Create the header for a frame with the given parameters. Header data is
     * stored out to an EncodedElement, in the proper form for a FLAC stream.
     *
     * @param fixBlock True to use a fixed block size, false to use variable. At
     *        this time, this *must* be set to True, as variable block size is
     *        not yet implemented.
     * @param blockSize Block Size of this frame.
     * @param sampleRate Sample rate of this frame.
     * @param channelAssign Channel assignment used in this frame's encoding.
     *                      See EncodingConfiguration class documentation for
     *                      more information.
     * @param sampleSize Bits per sample.
     * @param frameNumber For fixed block-size encodings, this is the frame-number
     *                    starting at zero and incrementing by one. For variable
     *                    block encodings, this is the sample number of the
     *                    first sample in the frame.
     * @param channelCount Number of channels in the stream.
     * @return EncodedElement where the header is saved to.
     */
    public EncodedElement createHeader(boolean fixBlock, int blockSize,
            int sampleRate, EncodingConfiguration.ChannelConfig channelAssign,
            int sampleSize, long frameNumber, int channelCount) {
        if(DEBUG_LEV > 0 )
            System.err.println("FrameHeader::createHeader : Begin");

        EncodedElement result = new EncodedElement();
        boolean useEndBlockSize = false;
        boolean useEndSampleRate = false;
        byte[] data = new byte[MAX_HEADER_SIZE];
        int start = 0;
        int nextPos = start;
        //set blocking strategy bits
        int blockingStrat = (fixBlock)? 0:1;
        byte[] encodedFrameNumber = UTF8Modified.convertToExtendedUTF8(frameNumber);
        //set block size bits
        byte encodedBlockSize = encodeBlockSize(blockSize);
        if(encodedBlockSize == 0x6 || encodedBlockSize == 0x7)
            useEndBlockSize = true;

        //set sample rate bits
        byte encodedSampleRate = encodeSampleRate(sampleRate);
        if(encodedSampleRate >= 12 && encodedSampleRate <= 14)
            useEndSampleRate = true;
        
        //set channelAssignment bits
        int channelAssignment = 0;
        if(channelAssign == EncodingConfiguration.ChannelConfig.INDEPENDENT)
            channelAssignment = channelCount-1;
        else if(channelAssign == EncodingConfiguration.ChannelConfig.LEFT_SIDE)
            channelAssignment = 8;
        else if(channelAssign == EncodingConfiguration.ChannelConfig.RIGHT_SIDE)
            channelAssignment = 9;
        else if(channelAssign == EncodingConfiguration.ChannelConfig.MID_SIDE)
            channelAssignment = 10;

        //set sample size bits
        byte encodedSampleSize = 0;
        switch(sampleSize) {
            case 8: encodedSampleSize = 0x1; break;
            case 12: encodedSampleSize = 0x2; break;
            case 16: encodedSampleSize = 0x4; break;
            case 20: encodedSampleSize = 0x5; break;
            case 24: encodedSampleSize = 0x6; break;
            default: encodedSampleSize = 0x0;
        }

        EncodedElement.addInt(syncCode, 14, nextPos, data);
        nextPos +=14;
        EncodedElement.addInt(reserved, 1, nextPos, data);
        nextPos +=1;
        EncodedElement.addInt(blockingStrat, 1, nextPos, data);
        nextPos +=1;
        EncodedElement.addInt(encodedBlockSize, 4, nextPos, data);
        nextPos +=4;
        EncodedElement.addInt(encodedSampleRate, 4, nextPos, data);
        nextPos +=4;
        EncodedElement.addInt(channelAssignment, 4, nextPos, data);
        nextPos +=4;
        EncodedElement.addInt(encodedSampleSize, 3, nextPos, data);        
        nextPos +=3;
        EncodedElement.addInt(reserved2, 1, nextPos, data);
        nextPos +=1;
        
        for(int i = 0; i < encodedFrameNumber.length; i++) {
            EncodedElement.addLong(encodedFrameNumber[i], 8, nextPos, data);
            nextPos +=8;
        }
        
        //write blockSize if needed(two formats possible)
        if(useEndBlockSize) {
            if(encodedBlockSize == 0x6) {
                EncodedElement.addInt(blockSize-1, 8, nextPos, data);
                nextPos += 8;
            }
            else {
                EncodedElement.addInt(blockSize-1, 16, nextPos, data);
                nextPos += 16;
            }
        }
        //write sampleRate if needed(three formats possible)
        if(useEndSampleRate) {
            switch(encodedSampleRate) {
                case 0xC:
                    EncodedElement.addInt(sampleRate/1000, 8, nextPos, data);
                    nextPos += 8;
                    break;
                case 0xD:
                    EncodedElement.addInt(sampleRate, 16, nextPos, data);
                    nextPos += 16;
                    break;
                case 0xE:
                    EncodedElement.addInt(sampleRate/10, 16, nextPos, data);
                    nextPos +=16;
                    break;
            }
        }
        if(DEBUG_LEV > 20 )
            System.err.println("FrameHeader::createHeader : pre-CRC");
        crcCalculator.reset();
        crcCalculator.updateCRC8(data, 0, nextPos/8);
        crc8 = crcCalculator.checksum();
        if(DEBUG_LEV > 20 )
            System.err.println("FrameHeader::createHeader : post-CRC");
        if(DEBUG_LEV > 50 ) {
            System.err.println("Checksum : " + crc8);
            for(int i = 0; i < nextPos/8; i++) {
                System.err.print(data[i]+":");
            }
        }
        EncodedElement.addInt(crc8, 8, nextPos, data);
        nextPos +=8;
        result.setUsableBits(nextPos);
        result.setData(data);
        
        if(DEBUG_LEV > 0 )
            System.err.println("FrameHeader::createHeader : End");
        return result;
    }

    /**
     * Given a block size, select the proper bit settings to use according to
     * the FLAC stream.
     * @param blockSize
     * @return
     */
    private static byte encodeBlockSize(int blockSize) {
        if(DEBUG_LEV > 0 )
            System.err.println("FrameHeader::encodeBlockSize : Begin");
        byte value = 0;
        int i;
        for(i = 0; i < definedBlockSizes.length; i++) {
            if(blockSize == definedBlockSizes[i]) {
                value = (byte)i;
                break;
            }
        }
        if(i >= definedBlockSizes.length) {
            if(blockSize <= 255)
                value = 0x6;
            else
                value = 0x7;
        }

        if(DEBUG_LEV > 0 )
            System.err.println("FrameHeader::encodeBlockSize : End");

        return value;
    }

    
    private static byte encodeSampleRate(int sampleRate) {
        if(DEBUG_LEV > 0 )
            System.err.println("FrameHeader::encodeSampleRate : Begin");
        byte value = 0;
        int i;
        for(i = 0; i < definedSampleRates.length; i++) {
            if(sampleRate == definedSampleRates[i]) {
                value = (byte)i;
                break;
            }
        }
        if(i >= definedSampleRates.length) {            
            if(sampleRate % 1000 == 0 && sampleRate < 256000)
                value = 0xC;
            else if(sampleRate < 65536)
                value = 0xD;
            else if(sampleRate % 10 == 0 && sampleRate <= 655350)
                value = 0xE;
            else
                value = 0x0;
        }
        if(DEBUG_LEV > 0 )
            System.err.println("FrameHeader::encodeSampleRate : End");
        
        return value;
    }
}
