/*
 *  Copyright (C) 2011 in-somnia
 * 
 *  This file is part of JAAD.
 * 
 *  JAAD is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation; either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  JAAD is distributed in the hope that it will be useful, but WITHOUT 
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.adts;

import java.io.DataInputStream;
import java.io.IOException;
import net.sourceforge.jaad.aac.ChannelConfiguration;
import net.sourceforge.jaad.aac.SampleFrequency;

class ADTSFrame {

	//fixed
	private boolean id, protectionAbsent, privateBit, copy, home;
	private int layer, profile, sampleFrequency, channelConfiguration;
	//variable
	private boolean copyrightIDBit, copyrightIDStart;
	private int frameLength, adtsBufferFullness, rawDataBlockCount;
	//error check
	private int[] rawDataBlockPosition;
	private int crcCheck;
	//decoder specific info
	private byte[] info;

	ADTSFrame(DataInputStream in) throws IOException {
		readHeader(in);

		if(!protectionAbsent) crcCheck = in.readUnsignedShort();
		if(rawDataBlockCount==0) {
			//raw_data_block();
		}
		else {
			int i;
			//header error check
			if(!protectionAbsent) {
				rawDataBlockPosition = new int[rawDataBlockCount];
				for(i = 0; i<rawDataBlockCount; i++) {
					rawDataBlockPosition[i] = in.readUnsignedShort();
				}
				crcCheck = in.readUnsignedShort();
			}
			//raw data blocks
			for(i = 0; i<rawDataBlockCount; i++) {
				//raw_data_block();
				if(!protectionAbsent) crcCheck = in.readUnsignedShort();
			}
		}
	}

	private void readHeader(DataInputStream in) throws IOException {
		//fixed header:
		//1 bit ID, 2 bits layer, 1 bit protection absent
		int i = in.read();
		id = ((i>>3)&0x1)==1;
		layer = (i>>1)&0x3;
		protectionAbsent = (i&0x1)==1;
		if(!protectionAbsent) System.out.println("\t\tCRC!!!");

		//2 bits profile, 4 bits sample frequency, 1 bit private bit
		i = in.read();
		profile = ((i>>6)&0x3)+1;
		sampleFrequency = (i>>2)&0xF;
		privateBit = ((i>>1)&0x1)==1;

		//3 bits channel configuration, 1 bit copy, 1 bit home
		i = (i<<8)|in.read();
		channelConfiguration = ((i>>6)&0x7);
		copy = ((i>>5)&0x1)==1;
		home = ((i>>4)&0x1)==1;
		//int emphasis = in.readBits(2);

		//variable header:
		//1 bit copyrightIDBit, 1 bit copyrightIDStart, 13 bits frame length,
		//11 bits adtsBufferFullness, 2 bits rawDataBlockCount
		copyrightIDBit = ((i>>3)&0x1)==1;
		copyrightIDStart = ((i>>2)&0x1)==1;
		i = (i<<16)|in.readUnsignedShort();
		frameLength = (i>>5)&0x1FFF;
		i = (i<<8)|in.read();
		adtsBufferFullness = (i>>2)&0x7FF;
		rawDataBlockCount = i&0x3;
	}

	int getFrameLength() {
		return frameLength-(protectionAbsent ? 7 : 9);
	}

	byte[] createDecoderSpecificInfo() {
		if(info==null) {
			//5 bits profile, 4 bits sample frequency, 4 bits channel configuration
			info = new byte[2];
			info[0] = (byte) (profile<<3);
			info[0] |= (sampleFrequency>>1)&0x7;
			info[1] = (byte) ((sampleFrequency&0x1)<<7);
			info[1] |= (channelConfiguration<<3);
			/*1 bit frame length flag, 1 bit depends on core coder,
			1 bit extension flag (all three currently 0)*/
		}

		return info;
	}

	int getSampleFrequency() {
		return SampleFrequency.forInt(sampleFrequency).getFrequency();
	}

	int getChannelCount() {
		return ChannelConfiguration.forInt(channelConfiguration).getChannelCount();
	}
}
