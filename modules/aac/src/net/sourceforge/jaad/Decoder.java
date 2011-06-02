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
import net.sourceforge.jaad.impl.PCE;
import net.sourceforge.jaad.impl.SyntacticElements;
import net.sourceforge.jaad.impl.filterbank.FilterBank;
import net.sourceforge.jaad.impl.transport.ADIFHeader;
import net.sourceforge.jaad.impl.transport.ADTSFrame;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * Main AAC decoder class
 * @author in-somnia
 */
public class Decoder implements Constants {

	static {
		LOGGER.setLevel(Level.CONFIG);
		final ConsoleHandler h = new ConsoleHandler();
		h.setLevel(Level.FINE);
		LOGGER.addHandler(h);
	}
	private final DecoderConfig config;
	private final SyntacticElements syntacticElements;
	private final FilterBank filterBank;
	private BitStream in;
	private ADIFHeader adifHeader;
	private ADTSFrame adtsFrame;

	/**
	 * The methods returns true, if a profile is supported by the decoder.
	 * @param profile an AAC profile
	 * @return true if the specified profile can be decoded
	 * @see Profile#isDecodingSupported()
	 */
	public static boolean canDecode(Profile profile) {
		return profile.isDecodingSupported();
	}

	/**
	 * Initializes the decoder with a specific configuration.
	 * @param config A previously created decoder configuration.
	 * @throws AACException if the profile, specified by the DecoderConfig, is not supported
	 */
	public Decoder(DecoderConfig config) throws AACException {
		if(config==null) throw new IllegalArgumentException("decoder config must not be null");
		if(!canDecode(config.getProfile())) throw new AACException("unsupported profile: "+config.getProfile().getDescription());

		if(config.isBitStreamStored()) in = config.getBitStream();
		else in = new BitStream();

		this.config = config;
		LOGGER.log(Level.FINE, "profile: {0}", config.getProfile());
		LOGGER.log(Level.FINE, "sf: {0}", config.getSampleFrequency().getFrequency());
		LOGGER.log(Level.FINE, "channels: {0}", config.getChannelConfiguration().getDescription());

		syntacticElements = new SyntacticElements(config);
		filterBank = new FilterBank(config.isSmallFrameUsed(), config.getChannelConfiguration().getChannelCount());
	}

	/**
	 * Decodes one frame of AAC data in frame mode and returns the raw PCM
	 * data.
	 * @param frame the AAC frame
	 * @param buffer a buffer to hold the decoded PCM data
	 * @throws AACException if decoding fails
	 */
	public void decodeFrame(byte[] frame, SampleBuffer buffer) throws AACException {
		if(frame!=null) in.setData(frame);
		decodeFrame(buffer);
	}

	/**
	 * Decodes one frame of AAC data in stream mode and returns the raw PCM
	 * data.
	 * @param buffer a buffer to hold the decoded PCM data
	 * @throws AACException if decoding fails
	 * @return true if a frame could be decoded, false if the stream ended
	 */
	public boolean decodeFrame(SampleBuffer buffer) throws AACException {
		try {
			decode(buffer);
			return true;
		}
		catch(AACException e) {
			if(e.isEndOfStream()) return false;
			else throw e;
		}
	}

	private void decode(SampleBuffer buffer) throws AACException {
		if(ADIFHeader.isPresent(in)) {
			adifHeader = ADIFHeader.readHeader(in);
			final PCE pce = adifHeader.getFirstPCE();
			config.setProfile(pce.getProfile());
			config.setSampleFrequency(pce.getSampleFrequency());
			config.setChannelConfiguration(ChannelConfiguration.forInt(pce.getChannelCount()));
		}
		if(ADTSFrame.isPresent(in)) {
			adtsFrame = ADTSFrame.readFrame(in);
			config.setProfile(adtsFrame.getProfile());
			config.setSampleFrequency(adtsFrame.getSampleFrequency());
			config.setChannelConfiguration(adtsFrame.getChannelConfiguration());
		}

		if(!canDecode(config.getProfile())) throw new AACException("unsupported profile: "+config.getProfile().getDescription());

		syntacticElements.startNewFrame();

		try {
			//1: bitstream parsing and noiseless coding
			syntacticElements.decode(in);
			//2: spectral processing
			syntacticElements.process(filterBank);
			//3: send to output buffer
			syntacticElements.sendToOutput(buffer);
		}
		catch(AACException e) {
			buffer.setData(new byte[0], 0, 0, 0, 0, 0);
			throw e;
		}
		catch(Exception e) {
			buffer.setData(new byte[0], 0, 0, 0, 0, 0);
			throw new AACException(e);
		}
	}
}
