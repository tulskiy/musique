/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.impl.transport;

import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.Profile;
import net.sourceforge.jaad.impl.BitStream;
import net.sourceforge.jaad.ChannelConfiguration;
import net.sourceforge.jaad.SampleFrequency;

public final class ADTSFrame {

	private static final long ADTS_ID = 0xFFF;
	//fixed
	private boolean id;
	private int layer;
	private boolean protectionAbsent;
	private Profile profile;
	private SampleFrequency sampleFrequency;
	private boolean privateBit;
	private ChannelConfiguration channelConfiguration;
	private boolean copy, home;
	//variable
	private boolean copyrightIDBit, copyrightIDStart;
	private int frameLength, adtsBufferFullness, rawDataBlockCount;
	//error check
	private int[] rawDataBlockPosition;
	private int crcCheck;

	private ADTSFrame() {
	}

	public static boolean isPresent(BitStream in) throws AACException {
		return in.peekBits(12)==ADTS_ID;
	}

	public static ADTSFrame readFrame(BitStream in) throws AACException {
		final ADTSFrame frame = new ADTSFrame();
		frame.decode(in);
		return frame;
	}

	private void decode(BitStream in) throws AACException {
		readFixedHeader(in);
		readVariableHeader(in);
		if(!protectionAbsent) crcCheck = in.readBits(16);
		if(rawDataBlockCount==0) {
			//raw_data_block();
		}
		else {
			int i;
			//header error check
			if(!protectionAbsent) {
				rawDataBlockPosition = new int[rawDataBlockCount];
				for(i = 0; i<rawDataBlockCount; i++) {
					rawDataBlockPosition[i] = in.readBits(16);
				}
				crcCheck = in.readBits(16);
			}
			//raw data blocks
			for(i = 0; i<rawDataBlockCount; i++) {
				//raw_data_block();
				if(!protectionAbsent) crcCheck = in.readBits(16);
			}
		}
	}

	private void readFixedHeader(BitStream in) throws AACException {
		in.readBits(12);
		id = in.readBool();
		layer = in.readBits(2);
		protectionAbsent = in.readBool();
		profile = Profile.forInt(in.readBits(2)+1);
		sampleFrequency = SampleFrequency.forInt(in.readBits(4));
		privateBit = in.readBool();
		channelConfiguration = ChannelConfiguration.forInt(in.readBits(3));
		copy = in.readBool();
		home = in.readBool();
		//int emphasis = in.readBits(2);
	}

	private void readVariableHeader(BitStream in) throws AACException {
		copyrightIDBit = in.readBool();
		copyrightIDStart = in.readBool();
		frameLength = in.readBits(13);
		adtsBufferFullness = in.readBits(11);
		rawDataBlockCount = in.readBits(2);
	}

	/* getter */
	public Profile getProfile() {
		return profile;
	}

	public SampleFrequency getSampleFrequency() {
		return sampleFrequency;
	}

	public ChannelConfiguration getChannelConfiguration() {
		return channelConfiguration;
	}
}
