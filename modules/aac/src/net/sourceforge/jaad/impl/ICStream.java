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
package net.sourceforge.jaad.impl;

import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.ChannelConfiguration;
import net.sourceforge.jaad.DecoderConfig;
import net.sourceforge.jaad.impl.error.HCR;
import net.sourceforge.jaad.impl.error.RVLC;
import net.sourceforge.jaad.impl.gain.GainControl;
import net.sourceforge.jaad.impl.huffman.HCB;
import net.sourceforge.jaad.impl.huffman.Huffman;
import net.sourceforge.jaad.impl.invquant.InvQuant;
import net.sourceforge.jaad.impl.noise.TNS;
import java.util.logging.Level;

public class ICStream implements Constants, HCB {

	private static final int SF_DELTA = 60;
	private final int frameLength;
	//always needed
	private final ICSInfo info;
	private final SectionData sectionData;
	private final short[] data;
	private final float[] invQuant;
	private int[][] scaleFactors;
	private int globalGain;
	private boolean pulseDataPresent, tnsDataPresent, gainControlPresent;
	//only allocated if needed
	private TNS tns;
	private GainControl gainControl;
	private int[] pulseOffset, pulseAmp;
	private int pulseCount;
	private int pulseStartSWB;
	//error resilience
	private boolean noiseUsed;
	private int reorderedSpectralDataLen, longestCodewordLen;
	private RVLC rvlc;

	public ICStream(int frameLength) {
		this.frameLength = frameLength;
		info = new ICSInfo(frameLength);
		sectionData = new SectionData();
		data = new short[frameLength];
		invQuant = new float[frameLength];
	}

	/* ========= decoding ========== */
	public void decode(BitStream in, boolean commonWindow, DecoderConfig conf) throws AACException {
		if(conf.isScalefactorResilienceUsed()&&rvlc==null) rvlc = new RVLC();
		final boolean er = conf.getProfile().isErrorResilientProfile();

		globalGain = in.readBits(8);

		if(!commonWindow) info.decode(in, conf, commonWindow);

		sectionData.decode(in, info, conf.isSectionDataResilienceUsed());
		if(conf.isScalefactorResilienceUsed()) rvlc.decode(in, this, scaleFactors);
		else decodeScaleFactors(in);

		pulseDataPresent = in.readBool();
		if(pulseDataPresent) {
			if(info.isEightShortFrame()) throw new AACException("pulse data not allowed for short frames");
			LOGGER.log(Level.FINE, "PULSE");
			decodePulseData(in);
		}

		tnsDataPresent = in.readBool();
		if(tnsDataPresent&&!er) {
			if(tns==null) tns = new TNS();
			tns.decode(in, info);
		}

		gainControlPresent = in.readBool();
		if(gainControlPresent) {
			if(gainControl==null) gainControl = new GainControl(frameLength);
			LOGGER.log(Level.FINE, "GAIN");
			gainControl.decode(in, info.getWindowSequence());
		}

		//RVLC spectral data
		//if(conf.isScalefactorResilienceUsed()) rvlc.decodeScalefactors(this, in, scaleFactors);

		if(conf.isSpectralDataResilienceUsed()) {
			int max = (conf.getChannelConfiguration()==ChannelConfiguration.CHANNEL_CONFIG_STEREO) ? 6144 : 12288;
			reorderedSpectralDataLen = Math.max(in.readBits(14), max);
			longestCodewordLen = Math.max(in.readBits(6), 49);
			HCR.decodeReorderedSpectralData(this, in, data, conf.isSectionDataResilienceUsed());
		}
		else decodeSpectralData(in);
	}

	private void decodePulseData(BitStream in) throws AACException {
		pulseCount = in.readBits(2)+1;
		pulseStartSWB = in.readBits(6);
		if(pulseStartSWB>=info.getSWBCount()) throw new AACException("pulse SWB out of range: "+pulseStartSWB+" > "+info.getSWBCount());

		if(pulseOffset==null||pulseCount!=pulseOffset.length) {
			//only reallocate if needed
			pulseOffset = new int[pulseCount];
			pulseAmp = new int[pulseCount];
		}

		pulseOffset[0] = info.getSWBOffsets()[pulseStartSWB];
		pulseOffset[0] += in.readBits(5);
		pulseAmp[0] = in.readBits(4);
		for(int i = 1; i<pulseCount; i++) {
			pulseOffset[i] = in.readBits(5)+pulseOffset[i-1];
			if(pulseOffset[i]>1023) throw new AACException("pulse offset out of range: "+pulseOffset[0]);
			pulseAmp[i] = in.readBits(4);
		}
	}

	public void decodeScaleFactors(BitStream in) throws AACException {
		noiseUsed = false;
		final int maxSFB = info.getMaxSFB();
		final int windowGroupCount = info.getWindowGroupCount();
		final int[][] sfbCB = sectionData.getSfbCB();
		if(scaleFactors==null||windowGroupCount!=scaleFactors.length||maxSFB!=scaleFactors[0].length) {
			//only reallocate if needed
			scaleFactors = new int[windowGroupCount][maxSFB];
		}

		int scaleFactor = globalGain;
		int isPosition = 0;
		int noiseEnergy = globalGain-90-256;
		boolean noisePCM = true;
		int sfb;
		for(int g = 0; g<windowGroupCount; g++) {
			for(sfb = 0; sfb<maxSFB; sfb++) {
				switch(sfbCB[g][sfb]) {
					case ZERO_HCB:
						scaleFactors[g][sfb] = 0;
						break;
					case INTENSITY_HCB:
					case INTENSITY_HCB2:
						isPosition += Huffman.decodeScaleFactor(in)-60;
						scaleFactors[g][sfb] = isPosition;
						break;
					case NOISE_HCB:
						if(!noiseUsed) noiseUsed = true;
						if(noisePCM) {
							noisePCM = false;
							noiseEnergy += in.readBits(9);
						}
						else noiseEnergy += Huffman.decodeScaleFactor(in)-SF_DELTA;
						scaleFactors[g][sfb] = noiseEnergy;
						break;
					default:
						scaleFactor += Huffman.decodeScaleFactor(in)-SF_DELTA;
						scaleFactors[g][sfb] = scaleFactor;
						break;
				}
			}
		}
	}

	private void decodeSpectralData(BitStream in) throws AACException {
		final int[] numSec = sectionData.getNumSec();
		final int[][] sectCB = sectionData.getSectCB();
		final int[][] sectSFBOffset = info.getSectSFBOffsets();
		final int[][] sectStart = sectionData.getSectStart();
		final int[][] sectEnd = sectionData.getSectEnd();
		final int shortFrameLen = data.length/8;
		int i, k, inc, hcb, p;
		int start, end, startOff, endOff;
		int wins = 0;

		for(int g = 0; g<info.getWindowGroupCount(); g++) {
			p = wins*shortFrameLen;

			for(i = 0; i<numSec[g]; i++) {
				hcb = sectCB[g][i];
				inc = (hcb>=FIRST_PAIR_HCB) ? 2 : 4;
				start = sectStart[g][i];
				end = sectEnd[g][i];
				startOff = sectSFBOffset[g][start];
				endOff = sectSFBOffset[g][end];

				switch(hcb) {
					case ZERO_HCB:
					case NOISE_HCB:
					case INTENSITY_HCB:
					case INTENSITY_HCB2:
						p += (endOff-startOff);
						break;
					default:
						for(k = startOff; k<endOff; k += inc) {
							Huffman.decodeSpectralData(in, hcb, data, p);
							p += inc;
						}
						break;
				}
			}
			wins += info.getWindowGroupLength(g);
		}
	}

	/* ========= processing ========= */
	/**
	 * Does inverse quantization and applies the scale factors on the decoded
	 * data. After this the noiseless decoding is finished and the decoded data
	 * is returned.
	 * @return the inverse quantized and scaled data
	 */
	public float[] getInvQuantData() throws AACException {
		if(pulseDataPresent) {
			int k = Math.min(info.getSWBOffsets()[pulseStartSWB], info.getSWBOffsetMax());
			for(int i = 0; i<=pulseCount; i++) {
				k += pulseOffset[i];
				if(k>=data.length) throw new AACException("pulse offset out of range");

				if(data[k]>0) data[k] += pulseAmp[i];
				else data[k] -= pulseAmp[i];
			}
		}
		InvQuant.process(info, data, invQuant, scaleFactors);
		return invQuant;
	}

	/* =========== gets ============ */
	public ICSInfo getInfo() {
		return info;
	}

	public SectionData getSectionData() {
		return sectionData;
	}

	public int[][] getScaleFactors() {
		return scaleFactors;
	}

	public boolean isTNSDataPresent() {
		return tnsDataPresent;
	}

	public TNS getTNS() {
		return tns;
	}

	public int getGlobalGain() {
		return globalGain;
	}

	public boolean isNoiseUsed() {
		return noiseUsed;
	}

	public int getLongestCodewordLength() {
		return longestCodewordLen;
	}

	public int getReorderedSpectralDataLength() {
		return reorderedSpectralDataLen;
	}

	public boolean isGainControlPresent() {
		return gainControlPresent;
	}

	public GainControl getGainControl() {
		return gainControl;
	}
}
