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
package net.sourceforge.jaad;

import net.sourceforge.jaad.impl.BitStream;
import net.sourceforge.jaad.impl.Constants;
import net.sourceforge.jaad.impl.InputBitStream;
import net.sourceforge.jaad.impl.PCE;
import net.sourceforge.jaad.impl.transport.ADIFHeader;
import net.sourceforge.jaad.impl.transport.ADTSFrame;
import java.io.InputStream;

/**
 * DecoderConfig that must be passed to the <code>Decoder</code> constructor.
 * Typically it is created via one of the static parsing methods.
 * @author in-somnia
 */
public class DecoderConfig implements Constants {

	public static final int MAXIMUM_FRAME_SIZE = 6144;
	private final BitStream in;
	private Profile profile;
	private SampleFrequency sampleFrequency;
	private ChannelConfiguration channelConfiguration;
	private boolean frameLengthFlag;
	private boolean dependsOnCoreCoder;
	private int coreCoderDelay;
	private boolean extensionFlag;
	private Profile extProfile;
	//extension: SBR
	private boolean sbrPresent, downSampledSBR;
	//extension: error resilience
	private boolean sectionDataResilience, scalefactorResilience, spectralDataResilience;

	DecoderConfig(BitStream in) {
		this.in = in;
	}

	public DecoderConfig() {
		in = null;
		profile = Profile.AAC_MAIN;
		sampleFrequency = SampleFrequency.SAMPLE_FREQUENCY_NONE;
		channelConfiguration = ChannelConfiguration.CHANNEL_CONFIG_UNSUPPORTED;
		frameLengthFlag = false;
		sbrPresent = false;
		downSampledSBR = false;
		extProfile = Profile.UNKNOWN;
	}

	/* ========== gets/sets ========== */
	public ChannelConfiguration getChannelConfiguration() {
		return channelConfiguration;
	}

	public void setChannelConfiguration(ChannelConfiguration channelConfiguration) {
		this.channelConfiguration = channelConfiguration;
	}

	public int getCoreCoderDelay() {
		return coreCoderDelay;
	}

	public void setCoreCoderDelay(int coreCoderDelay) {
		this.coreCoderDelay = coreCoderDelay;
	}

	public boolean isDependsOnCoreCoder() {
		return dependsOnCoreCoder;
	}

	public void setDependsOnCoreCoder(boolean dependsOnCoreCoder) {
		this.dependsOnCoreCoder = dependsOnCoreCoder;
	}

	public Profile getExtObjectType() {
		return extProfile;
	}

	public void setExtObjectType(Profile extObjectType) {
		this.extProfile = extObjectType;
	}

	public int getFrameLength() {
		return frameLengthFlag ? WINDOW_SMALL_LEN_LONG : WINDOW_LEN_LONG;
	}

	public boolean isSmallFrameUsed() {
		return frameLengthFlag;
	}

	public void setSmallFrameUsed(boolean shortFrame) {
		this.frameLengthFlag = shortFrame;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public SampleFrequency getSampleFrequency() {
		return sampleFrequency;
	}

	public void setSampleFrequency(SampleFrequency sampleFrequency) {
		this.sampleFrequency = sampleFrequency;
	}

	//=========== SBR =============
	public boolean isSBRPresent() {
		return sbrPresent;
	}

	public void setSBRPresent(boolean sbrPresent) {
		this.sbrPresent = sbrPresent;
	}

	public boolean isSBRDownSampled() {
		return downSampledSBR;
	}

	//=========== ER =============
	public boolean isScalefactorResilienceUsed() {
		return scalefactorResilience;
	}

	public boolean isSectionDataResilienceUsed() {
		return sectionDataResilience;
	}

	public boolean isSpectralDataResilienceUsed() {
		return spectralDataResilience;
	}

	//=========== ADIF/ADTS header =============
	boolean isBitStreamStored() {
		return in!=null;
	}

	BitStream getBitStream() {
		return in;
	}

	/* ======== static builder ========= */
	/**
	 * Parses the input arrays as a DecoderSpecificInfo, as used in MP4
	 * containers.
	 * @return a DecoderConfig
	 */
	public static DecoderConfig parseMP4DecoderSpecificInfo(byte[] data) throws AACException {
		final BitStream in = new BitStream(data);
		final DecoderConfig config = new DecoderConfig();

		try {
			config.profile = readProfile(in);

			int sf = in.readBits(4);
			if(sf==0xF) {
				throw new AACException("sample rate specified explicitly, not supported yet!");
				//bits.readBits(24);
			}
			else config.sampleFrequency = SampleFrequency.forInt(sf);
			config.channelConfiguration = ChannelConfiguration.forInt(in.readBits(4));

			switch(config.profile) {
				case AAC_SBR:
					config.extProfile = config.profile;
					config.sbrPresent = true;
					sf = in.readBits(4);
					if(sf==0xF) {
						throw new AACException("extended sample rate specified explicitly, not supported yet!");
						//bits.readBits(24);
					}
					//if sample frequencies are the same: downsample SBR
					config.downSampledSBR = config.sampleFrequency.getIndex()==sf;
					config.sampleFrequency = SampleFrequency.forInt(sf);
					config.profile = readProfile(in);
					break;
				case AAC_MAIN:
				case AAC_LC:
				case AAC_SSR:
				case AAC_LTP:
				case ER_AAC_LC:
				case ER_AAC_LTP:
				case ER_AAC_LD:
					//ga-specific info:
					config.frameLengthFlag = in.readBool();
					if(config.frameLengthFlag) throw new AACException("config uses 960-sample frames, not yet supported");
					config.dependsOnCoreCoder = in.readBool();
					if(config.dependsOnCoreCoder) config.coreCoderDelay = in.readBits(14);
					else config.coreCoderDelay = 0;
					config.extensionFlag = in.readBool();

					if(config.channelConfiguration==ChannelConfiguration.CHANNEL_CONFIG_NONE) {
						PCE pce = new PCE();
						pce.decode(in);
						config.profile = pce.getProfile();
						config.sampleFrequency = pce.getSampleFrequency();
						config.channelConfiguration = ChannelConfiguration.forInt(pce.getChannelCount());
					}

					if(config.extensionFlag) {
						if(config.profile.isErrorResilientProfile()) {
							config.sectionDataResilience = in.readBool();
							config.scalefactorResilience = in.readBool();
							config.spectralDataResilience = in.readBool();
						}
						//extensionFlag3
						in.skipBit();
					}
					break;
				default:
					throw new AACException("profile not supported: "+config.profile.getIndex());
			}
			return config;
		}
		finally {
			in.destroy();
		}
	}

	private static Profile readProfile(BitStream in) throws AACException {
		int i = in.readBits(5);
		if(i==31) {
			i = 32+in.readBits(6);
		}
		return Profile.forInt(i);
	}

	/**
	 * Reads and parses a transport header from the InputStream. The method can
	 * detect and parse ADTS and ADIF headers.
	 * The maximum number of bytes to skip can be passed as parameter. This is
	 * useful since some streams (like internet radios) may start within a
	 * frame. The method then skippes until it finds the next header.
	 * @param input the InputStream to read from
	 * @param maxSkip the maximum number of bytes to skip while searching for a header.
	 * @return a DecoderConfig or null if no header was found
	 */
	public static DecoderConfig parseTransportHeader(InputStream input, int maxSkip) throws AACException {
		final InputBitStream in = new InputBitStream(input);
		final DecoderConfig config = new DecoderConfig(in);
		int left = maxSkip;
		do {
			if(ADIFHeader.isPresent(in)) {
				final ADIFHeader adif = ADIFHeader.readHeader(in);
				final PCE pce = adif.getFirstPCE();
				config.profile = pce.getProfile();
				config.sampleFrequency = pce.getSampleFrequency();
				config.channelConfiguration = ChannelConfiguration.forInt(pce.getChannelCount());
				return config;
			}
			else if(ADTSFrame.isPresent(in)) {
				final ADTSFrame adts = ADTSFrame.readFrame(in);
				config.profile = adts.getProfile();
				config.sampleFrequency = adts.getSampleFrequency();
				config.channelConfiguration = adts.getChannelConfiguration();
				return config;
			}
			else if(left>0) in.skipBits(8);
			left--;
		}
		while(left>=0);
		return null;
	}
}
