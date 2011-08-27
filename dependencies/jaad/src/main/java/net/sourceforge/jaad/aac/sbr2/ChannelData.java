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
package net.sourceforge.jaad.aac.sbr2;

import java.util.Arrays;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.BitStream;

class ChannelData implements SBRConstants, HuffmanTables {

	//grid
	private boolean ampRes;
	private int frameClass;
	private int envCount, envCountPrev, noiseCount;
	private final int[] freqRes;
	private int freqResPrevious;
	private int pointer;
	private int la, laPrevious;
	//dtdf
	private final boolean[] dfEnv, dfNoise;
	//invf
	private final int[] invfMode, invfModePrevious;
	//envelopes
	private final float[][] envelopeSF;
	private final float[] envelopeSFPrevious;
	private final int[] te; //envelope time borders, p.214
	private int tePrevious;
	//noise
	private final float[][] noiseFloorData;
	private final float[] noiseFDPrevious; //last of previous frame
	private final int[] tq; //noise floor time borders, p.215
	//sinusoidal
	private boolean sinusoidalsPresent;
	private final boolean[] sinusoidals;
	private final boolean[] sIndexMappedPrevious;
	//chirp factors (calculated by HFGenerator)
	private final float[] bwArray;
	//values stored for assembling HFAdjustment
	private int noiseIndex, sineIndex;
	private int lTemp;
	private final float[][] gTmp, qTmp;

	ChannelData() {
		freqRes = new int[MAX_ENV_COUNT];
		invfMode = new int[MAX_NQ];
		invfModePrevious = new int[MAX_NQ];

		dfEnv = new boolean[MAX_ENV_COUNT];
		dfNoise = new boolean[MAX_NOISE_COUNT];

		envelopeSF = new float[MAX_ENV_COUNT][MAX_BANDS];
		envelopeSFPrevious = new float[MAX_BANDS];
		te = new int[MAX_ENV_COUNT+1];
		tePrevious = 0;

		noiseFloorData = new float[MAX_NOISE_COUNT][MAX_BANDS];
		noiseFDPrevious = new float[MAX_BANDS];
		tq = new int[MAX_NOISE_COUNT+1];

		sinusoidals = new boolean[MAX_BANDS];
		sIndexMappedPrevious = new boolean[MAX_BANDS];
		Arrays.fill(sIndexMappedPrevious, false);

		bwArray = new float[MAX_CHIRP_FACTORS];

		lTemp = 0;
		//TODO: check sizes!
		gTmp = new float[42][48];
		qTmp = new float[42][48];
	}

	void savePreviousData() {
		//lTemp for next frame
		lTemp = RATE*te[envCount]-TIME_SLOTS_RATE;

		//grid
		envCountPrev = envCount;
		freqResPrevious = freqRes[freqRes.length-1];
		laPrevious = la;
		tePrevious = te[envCountPrev];
		//invf
		System.arraycopy(invfMode, 0, invfModePrevious, 0, MAX_NQ);
	}

	/* ======================= decoding ======================*/
	void decodeGrid(BitStream in, SBRHeader header, FrequencyTables tables) throws AACException {
		//read bitstream and fill envelope borders
		int absBordTrail = TIME_SLOTS;
		int relLead, relTrail;

		ampRes = header.getAmpRes();

		switch(frameClass = in.readBits(2)) {
			case FIXFIX:
				envCount = 1<<in.readBits(2);
				relLead = envCount-1;
				if(envCount==1) ampRes = false;
				//check requirement (4.6.18.6.3):
				else if(envCount>4) throw new AACException("SBR: too many envelopes: "+envCount);

				Arrays.fill(freqRes, in.readBit());

				te[0] = 0;
				te[envCount] = absBordTrail;
				absBordTrail = (absBordTrail+(envCount>>1))/envCount;
				for(int i = 0; i<relLead; i++) {
					te[i+1] = te[i]+absBordTrail;
				}

				break;
			case FIXVAR:
				absBordTrail += in.readBits(2);
				relTrail = in.readBits(2);
				envCount = relTrail+1;
				
				te[0] = 0;
				te[envCount] = absBordTrail;
				for(int i = 0; i<relTrail; i++) {
					te[envCount-1-i] = te[envCount-i]-2*in.readBits(2)-2;
				}

				pointer = in.readBits(CEIL_LOG2[envCount]);

				for(int i = 0; i<envCount; i++) {
					freqRes[envCount-1-i] = in.readBit();
				}
				break;
			case VARFIX:
				te[0] = in.readBits(2);
				relLead = in.readBits(2);
				envCount = relLead+1;

				te[envCount] = absBordTrail;
				for(int i = 0; i<relLead; i++) {
					te[i+1] = te[i]+2*in.readBits(2)+2;
				}

				pointer = in.readBits(CEIL_LOG2[envCount]);

				for(int i = 0; i<envCount; i++) {
					freqRes[i] = in.readBit();
				}
				break;
			default: //VARVAR
				te[0] = in.readBits(2);
				absBordTrail += in.readBits(2);
				relLead = in.readBits(2);
				relTrail = in.readBits(2);
				envCount = relLead+relTrail+1;
				if(envCount>5) throw new AACException("SBR: too many envelopes: "+envCount);

				te[envCount] = absBordTrail;
				for(int i = 0; i<relLead; i++) {
					te[i+1] = te[i]+2*in.readBits(2)+2;
				}
				for(int i = 0; i<relTrail; i++) {
					te[envCount-1-i] = te[envCount-i]-2*in.readBits(2)-2;
				}

				pointer = in.readBits(CEIL_LOG2[envCount]);

				for(int i = 0; i<envCount; i++) {
					freqRes[i] = in.readBit();
				}
				break;
		}

		//fill noise floor time borders (4.6.18.3.3)
		noiseCount = (envCount>1) ? 2 : 1;
		tq[0] = te[0];
		tq[noiseCount] = te[envCount];
		if(envCount==1) tq[1] = te[1];
		else {
			final int middleBorder;
			switch(frameClass) {
				case FIXFIX:
					middleBorder = envCount/2;
					break;
				case VARFIX:
					if(pointer==0) middleBorder = 1;
					else if(pointer==1) middleBorder = envCount-1;
					else middleBorder = pointer-1;
					break;
				default:
					if(pointer>1) middleBorder = envCount+1-pointer;
					else middleBorder = envCount-1;
					break;
			}

			tq[1] = te[middleBorder];
		}

		//calculate La (table 4.157)
		if((frameClass==FIXVAR||frameClass==VARVAR)&&pointer>0) la = envCount+1-pointer;
		else if(frameClass==VARFIX&&pointer>1) la = pointer-1;
		else la = -1;
	}

	void decodeDTDF(BitStream in) throws AACException {
		for(int i = 0; i<envCount; i++) {
			dfEnv[i] = in.readBool();
		}

		for(int i = 0; i<noiseCount; i++) {
			dfNoise[i] = in.readBool();
		}
	}

	void decodeInvf(BitStream in, SBRHeader header, FrequencyTables tables) throws AACException {
		for(int i = 0; i<tables.getNq(); i++) {
			invfMode[i] = in.readBits(2);
		}
	}

	void decodeEnvelope(BitStream in, SBRHeader header, FrequencyTables tables, boolean secCh, boolean coupling) throws AACException {
		//select huffman codebooks
		final int[][] tHuff, fHuff;
		final int tLav, fLav;
		if(coupling&&secCh) {
			if(ampRes) {
				tHuff = T_HUFFMAN_ENV_BAL_3_0;
				tLav = T_HUFFMAN_ENV_BAL_3_0_LAV;
				fHuff = F_HUFFMAN_ENV_BAL_3_0;
				fLav = F_HUFFMAN_ENV_BAL_3_0_LAV;
			}
			else {
				tHuff = T_HUFFMAN_ENV_BAL_1_5;
				tLav = T_HUFFMAN_ENV_BAL_1_5_LAV;
				fHuff = F_HUFFMAN_ENV_BAL_1_5;
				fLav = F_HUFFMAN_ENV_BAL_1_5_LAV;
			}
		}
		else {
			if(ampRes) {
				tHuff = T_HUFFMAN_ENV_3_0;
				tLav = T_HUFFMAN_ENV_3_0_LAV;
				fHuff = F_HUFFMAN_ENV_3_0;
				fLav = F_HUFFMAN_ENV_3_0_LAV;
			}
			else {
				tHuff = T_HUFFMAN_ENV_1_5;
				tLav = T_HUFFMAN_ENV_1_5_LAV;
				fHuff = F_HUFFMAN_ENV_1_5;
				fLav = F_HUFFMAN_ENV_1_5_LAV;
			}
		}

		//read delta coded huffman data
		final int[] envBands = tables.getN();
		final int bits = 7-((secCh&&coupling) ? 1 : 0)-(ampRes ? 1 : 0);
		final int delta = ((secCh&&coupling) ? 1 : 0)+1;
		final int odd = envBands[1]&1;

		int j, k, frPrev;
		float[] prev;
		for(int i = 0; i<envCount; i++) {
			prev = (i==0) ? envelopeSFPrevious : envelopeSF[i-1];
			frPrev = (i==0) ? freqResPrevious : freqRes[i-1];

			if(dfEnv[i]) {
				if(freqRes[i]==frPrev) {
					for(j = 0; j<envBands[freqRes[i]]; j++) {
						envelopeSF[i][j] = prev[j]+delta*(decodeHuffman(in, tHuff)-tLav);
					}
				}
				else if(freqRes[i]==1) {
					for(j = 0; j<envBands[freqRes[i]]; j++) {
						k = (j+odd)>>1; //fLow[k] <= fHigh[j] < fLow[k + 1]
						envelopeSF[i][j] = prev[k]+delta*(decodeHuffman(in, tHuff)-tLav);
					}
				}
				else {
					for(j = 0; j<envBands[freqRes[i]]; j++) {
						k = (j!=0) ? (2*j-odd) : 0; //fHigh[k] == fLow[j]
						envelopeSF[i][j] = prev[k]+delta*(decodeHuffman(in, tHuff)-tLav);
					}
				}
			}
			else {
				envelopeSF[i][0] = delta*in.readBits(bits);
				for(j = 1; j<envBands[freqRes[i]]; j++) {
					envelopeSF[i][j] = envelopeSF[i][j-1]+delta*(decodeHuffman(in, fHuff)-fLav);
				}
			}
		}

		//save for next frame
		System.arraycopy(envelopeSF[envCount-1], 0, envelopeSFPrevious, 0, MAX_BANDS);
	}

	void decodeNoise(BitStream in, SBRHeader header, FrequencyTables tables, boolean secCh, boolean coupling) throws AACException {
		//select huffman codebooks
		final int[][] tHuff, fHuff;
		final int tLav, fLav;
		if(coupling&&secCh) {
			tHuff = T_HUFFMAN_NOISE_BAL_3_0;
			tLav = T_HUFFMAN_NOISE_BAL_3_0_LAV;
			fHuff = F_HUFFMAN_NOISE_BAL_3_0;
			fLav = F_HUFFMAN_NOISE_BAL_3_0_LAV;
		}
		else {
			tHuff = T_HUFFMAN_NOISE_3_0;
			tLav = T_HUFFMAN_NOISE_3_0_LAV;
			fHuff = F_HUFFMAN_NOISE_3_0;
			fLav = F_HUFFMAN_NOISE_3_0_LAV;
		}

		//read huffman data: i=noise, j=band
		final int noiseBands = tables.getNq();
		final int delta = ((secCh&&coupling) ? 1 : 0)+1;

		int j;
		float[] prev;
		for(int i = 0; i<noiseCount; i++) {
			if(dfNoise[i]) {
				prev = (i==0) ? noiseFDPrevious : noiseFloorData[i-1];
				for(j = 0; j<noiseBands; j++) {
					noiseFloorData[i][j] = prev[j]+delta*(decodeHuffman(in, tHuff)-tLav);
				}
			}
			else {
				noiseFloorData[i][0] = delta*in.readBits(5);
				for(j = 1; j<noiseBands; j++) {
					noiseFloorData[i][j] = noiseFloorData[i][j-1]+delta*(decodeHuffman(in, fHuff)-fLav);
				}
			}
		}

		//save for next frame
		System.arraycopy(noiseFloorData[noiseCount-1], 0, noiseFDPrevious, 0, MAX_BANDS);
	}

	void decodeSinusoidal(BitStream in, SBRHeader header, FrequencyTables tables) throws AACException {
		if(sinusoidalsPresent = in.readBool()) {
			for(int i = 0; i<tables.getN(HIGH); i++) {
				sinusoidals[i] = in.readBool();
			}
		}
		else Arrays.fill(sinusoidals, false);
	}

	private int decodeHuffman(BitStream in, int[][] table) throws AACException {
		int off = 0;
		int len = table[off][0];
		int cw = in.readBits(len);
		int j;
		while(cw!=table[off][1]) {
			off++;
			j = table[off][0]-len;
			len = table[off][0];
			cw <<= j;
			cw |= in.readBits(j);
		}
		return table[off][2];
	}

	/* ======================= gets ======================*/
	public boolean getAmpRes() {
		return ampRes;
	}

	public float[][] getEnvelopeScalefactors() {
		return envelopeSF;
	}

	//TODO:combine both
	public int getEnvCount() {
		return envCount;
	}

	public int getEnvCountPrevious() {
		return envCountPrev;
	}

	public int[] getTe() {
		return te;
	}

	public int getTePrevious() {
		return tePrevious;
	}

	public float[][] getNoiseFloorData() {
		return noiseFloorData;
	}

	public int getNoiseCount() {
		return noiseCount;
	}

	public int[] getTq() {
		return tq;
	}

	public int[] getFrequencyResolutions() {
		return freqRes;
	}

	int getFrameClass() {
		return frameClass;
	}

	public int getLa(boolean previous) {
		//laPrevious defined in 4.6.18.7.5
		return previous ? ((laPrevious==envCountPrev) ? 0 : -1) : la;
	}

	int getPointer() {
		return pointer;
	}

	public boolean areSinusoidalsPresent() {
		return sinusoidalsPresent;
	}

	public boolean[] getSinusoidals() {
		return sinusoidals;
	}

	public boolean[] getSIndexMappedPrevious() {
		return sIndexMappedPrevious;
	}

	void setSIndexMappedPrevious(boolean[] sIndexMapped) {
		//used by HFAdjuster to save last sIndexMapped for next frame
		//fill with false, because next frame may be larger than this one
		Arrays.fill(sIndexMappedPrevious, false);
		System.arraycopy(sIndexMapped, 0, sIndexMappedPrevious, 0, sIndexMapped.length);
	}

	int[] getInvfMode(boolean previous) {
		return previous ? invfModePrevious : invfMode;
	}

	float[] getChirpFactors() {
		return bwArray;
	}

	int getNoiseIndex() {
		return noiseIndex;
	}

	void setNoiseIndex(int noiseIndex) {
		this.noiseIndex = noiseIndex;
	}

	int getSineIndex() {
		return sineIndex;
	}

	void setSineIndex(int sineIndex) {
		this.sineIndex = sineIndex;
	}

	public int getLTemp() {
		return lTemp;
	}

	public float[][] getGTmp() {
		return gTmp;
	}

	public float[][] getQTmp() {
		return qTmp;
	}


	/* ======================= copying ======================*/
	void copyGrid(ChannelData cd) {
		frameClass = cd.getFrameClass();
		envCount = cd.getEnvCount();
		noiseCount = cd.getNoiseCount();

		System.arraycopy(cd.getFrequencyResolutions(), 0, freqRes, 0, envCount);
		System.arraycopy(cd.getTe(), 0, te, 0, te.length);
		System.arraycopy(cd.getTq(), 0, tq, 0, tq.length);

		pointer = cd.getPointer();
	}

	void copyInvf(ChannelData cd) {
		System.arraycopy(cd.getInvfMode(false), 0, invfMode, 0, MAX_NQ);
	}
}
