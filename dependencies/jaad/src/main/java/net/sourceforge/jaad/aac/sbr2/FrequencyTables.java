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
import net.sourceforge.jaad.aac.SampleFrequency;

//stores and calculates frequency tables, TODO: make arrays final with max sizes
class FrequencyTables implements SBRConstants {

	private static final int[] MFT_START_MIN = {7, 7, 10, 11, 12, 16, 16, 17, 24};
	private static final int[] MFT_STOP_MIN = {13, 15, 20, 21, 23, 32, 32, 35, 48};
	private static final int[] MFT_SF_OFFSETS = {5, 5, 4, 4, 4, 3, 2, 1, 0};
	private static final int[][] MFT_START_OFFSETS = {
		{-8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7}, //16000
		{-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13}, //22050
		{-5, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16}, //24000
		{-6, -4, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16}, //32000
		{-4, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16, 20}, //44100-64000
		{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 9, 11, 13, 16, 20, 24} //>64000
	};
	private static final int[][] MFT_STOP_OFFSETS = {
		{2, 4, 6, 8, 11, 14, 18, 22, 26, 31, 37, 44, 51},
		{2, 4, 6, 8, 11, 14, 18, 22, 26, 31, 36, 42, 49},
		{2, 4, 6, 9, 11, 14, 17, 21, 25, 29, 34, 39, 44},
		{2, 4, 6, 9, 11, 14, 17, 21, 24, 28, 33, 38, 43},
		{2, 4, 6, 9, 11, 14, 17, 20, 24, 28, 32, 36, 41},
		{2, 4, 6, 8, 10, 12, 14, 17, 20, 23, 26, 29, 32},
		{2, 4, 6, 8, 10, 12, 14, 17, 20, 23, 26, 29, 32},
		{2, 3, 5, 7, 9, 11, 13, 16, 18, 21, 23, 26, 29},
		{1, 2, 3, 4, 6, 7, 8, 9, 11, 12, 13, 15, 16}
	};
	private static final int[] MFT_INPUT1 = {12, 10, 8};
	private static final float[] MFT_INPUT2 = {1.0f, 1.3f};
	private static final float[] LIM_BANDS_PER_OCTAVE_POW = {
		1.32715174233856803909f, //2^(0.49/1.2)
		1.18509277094158210129f, //2^(0.49/2)
		1.11987160404675912501f //2^(0.49/3)
	};
	private static final float GOAL_SB_FACTOR = 2.048E6f;
	int k0, k2; //TODO: private
	//master
	private int[] mft;
	private int nMaster;
	//frequency tables
	private final int[][] fTable;
	private final int[] n;
	private int m, mPrev, kx, kxPrev;
	//noise table
	private int[] fNoise;
	int nq; //TODO: private
	//limiter table
	private int[] fLim;
	private int nl;
	//patches
	private int patchCount;
	private final int[] patchSubbands, patchStartSubband;
	private int[] patchBorders;

	FrequencyTables() {
		n = new int[2];
		fTable = new int[2][];
		patchSubbands = new int[MAX_PATCHES];
		patchStartSubband = new int[MAX_PATCHES];
		kx = 0;
		kxPrev = 0;
		m = 0;
		mPrev = 0;
	}

	void calculate(SBRHeader header, int sampleRate) throws AACException {
		calculateMFT(header, sampleRate);
		calculateFrequencyTables(header);
		calculateNoiseTable(header);
		calculatePatches(sampleRate);
		calculateLimiterTable(header);
	}

	private void calculateMFT(SBRHeader header, int sampleRate) throws AACException {
		//lower border k0
		final int sfIndex = SampleFrequency.forFrequency(sampleRate).getIndex();
		final int sfOff = MFT_SF_OFFSETS[sfIndex];
		k0 = MFT_START_MIN[sfIndex]+MFT_START_OFFSETS[sfOff][header.getStartFrequency(false)];
		//higher border k2
		final int stop = header.getStopFrequency(false);
		final int x;
		if(stop==15) x = 3*k0;
		else if(stop==14) x = 2*k0;
		else x = MFT_STOP_MIN[sfIndex]+MFT_STOP_OFFSETS[sfOff][header.getStopFrequency(false)-1];
		k2 = Math.min(MAX_BANDS, x);

		if(k0>=k2) throw new AACException("SBR: MFT borders out of range: lower="+k0+", higher="+k2);
		//check requirement (4.6.18.3.6):
		final int max;
		if(sampleRate==44100) max = 35;
		else if(sampleRate>=48000) max = 32;
		else max = 48;
		if((k2-k0)>max) throw new AACException("SBR: too many subbands: "+(k2-k0)+", maximum number for samplerate "+sampleRate+": "+max);

		//MFT calculation
		if(header.getFrequencyScale(false)==0) calculateMFT1(header, k0, k2);
		else calculateMFT2(header, k0, k2);

		//check requirement (4.6.18.3.6):
		if(header.getXOverBand(false)>=nMaster) throw new AACException("SBR: illegal length of master frequency table: "+nMaster+", xOverBand: "+header.getXOverBand(false));
	}

	//MFT calculation if frequencyScale==0
	private void calculateMFT1(SBRHeader header, int k0, int k2) throws AACException {
		final int dk;
		if(header.isAlterScale(false)) {
			dk = 2;
			nMaster = 2*Math.round((float) (k2-k0)/4.0f);
		}
		else {
			dk = 1;
			nMaster = 2*(int) ((float) (k2-k0)/2.0f);
		}
		//check requirement (4.6.18.6.3):
		if(nMaster<=0) throw new AACException("SBR: illegal number of bands for master frequency table: "+nMaster);

		final int k2Achieved = k0+nMaster*dk;
		int k2Diff = k2-k2Achieved;

		final int[] vDk = new int[nMaster];
		Arrays.fill(vDk, dk);

		if(k2Diff!=0) {
			final int incr = (k2Diff>0) ? -1 : 1;
			int k = (k2Diff>0) ? nMaster-1 : 0;
			while(k2Diff!=0) {
				vDk[k] -= incr;
				k += incr;
				k2Diff += incr;
			}
		}

		mft = new int[nMaster+1];
		mft[0] = k0;
		for(int i = 1; i<=nMaster; i++) {
			mft[i] = mft[i-1]+vDk[i-1];
		}
	}

	//MFT calculation if frequencyScale>0
	private void calculateMFT2(SBRHeader header, int k0, int k2) throws AACException {
		final int bands = MFT_INPUT1[header.getFrequencyScale(false)-1];
		final float warp = MFT_INPUT2[header.isAlterScale(false) ? 1 : 0];

		float div1 = (float) k2/(float) k0;
		final boolean twoRegions;
		final int k1;
		if(div1>2.2449) {
			twoRegions = true;
			k1 = 2*k0;
		}
		else {
			twoRegions = false;
			k1 = k2;
		}

		final float div2 = (float) k1/(float) k0;
		float log = (float) Math.log(div2)/(float) (2*LOG2);
		final int bandCount0 = 2*Math.round(bands*log);
		//check requirement (4.6.18.6.3):
		if(bandCount0<=0) throw new AACException("SBR: illegal band count for master frequency table: "+bandCount0);

		final int[] vDk0 = new int[bandCount0];
		float pow1, pow2;
		for(int i = 0; i<bandCount0; i++) {
			pow1 = (float) Math.pow(div2, (float) (i+1)/bandCount0);
			pow2 = (float) Math.pow(div2, (float) i/bandCount0);
			vDk0[i] = Math.round(k0*pow1)-Math.round(k0*pow2);
			//check requirement (4.6.18.6.3):
			if(vDk0[i]<=0) throw new AACException("SBR: illegal value in master frequency table: "+vDk0[i]);
		}
		Arrays.sort(vDk0);

		final int[] vk0 = new int[bandCount0+1];
		vk0[0] = k0;
		for(int i = 1; i<=bandCount0; i++) {
			vk0[i] = vk0[i-1]+vDk0[i-1];
		}

		if(twoRegions) {
			div1 = (float) k2/(float) k1;
			log = (float) Math.log(div1);
			final int bandCount1 = 2*(int) Math.round(bands*log/(2*LOG2*warp));
			final int[] vDk1 = new int[bandCount1];
			int min = -1;
			for(int i = 0; i<bandCount1; i++) {
				pow1 = (float) Math.pow(div1, (float) (i+1)/bandCount1);
				pow2 = (float) Math.pow(div1, (float) i/bandCount1);
				vDk1[i] = Math.round(k1*pow1)-Math.round(k1*pow2);
				if(min<0||vDk1[i]<min) min = vDk1[i];
				//check requirement (4.6.18.6.3):
				else if(vDk1[i]<=0) throw new AACException("SBR: illegal value in master frequency table: "+vDk1[i]);
			}

			if(min<vDk0[vDk0.length-1]) {
				Arrays.sort(vDk1);
				int change = vDk0[vDk0.length-1]-vDk1[0];
				final int x = (int) (vDk1[bandCount1-1]-(float) vDk1[0]/2.0);
				if(change>x) change = x;
				vDk1[0] += change;
				vDk1[bandCount1-1] -= change;
			}

			Arrays.sort(vDk1);
			final int[] vk1 = new int[bandCount1+1];
			vk1[0] = k1;
			for(int i = 1; i<=bandCount1; i++) {
				vk1[i] = vk1[i-1]+vDk1[i-1];
			}

			nMaster = bandCount0+bandCount1;
			mft = new int[nMaster+1];
			System.arraycopy(vk0, 0, mft, 0, bandCount0+1);
			System.arraycopy(vk1, 1, mft, bandCount0+1, bandCount1);
		}
		else {
			nMaster = bandCount0;
			mft = new int[nMaster+1];
			System.arraycopy(vk0, 0, mft, 0, nMaster+1);
		}
	}

	private void calculateFrequencyTables(SBRHeader header) throws AACException {
		final int xover = header.getXOverBand(false);
		n[HIGH] = getNMaster()-xover;
		fTable[HIGH] = new int[n[HIGH]+1];
		System.arraycopy(mft, xover, fTable[HIGH], 0, n[HIGH]+1);

		kxPrev = kx;
		kx = fTable[HIGH][0];
		mPrev = m;
		m = fTable[HIGH][getN(HIGH)]-kx;
		//check requirements (4.6.18.3.6):
		if(kx>32) throw new AACException("SBR: start frequency border out of range: "+kx);
		if((kx+m)>64) throw new AACException("SBR: stop frequency border out of range: "+(kx+m));

		final int half = (int) ((float) n[HIGH]/2.0);
		n[LOW] = half+(n[HIGH]-2*half);
		fTable[LOW] = new int[n[LOW]+1];
		fTable[LOW][0] = fTable[HIGH][0];
		final int div = n[HIGH]&1;
		for(int i = 1; i<=n[LOW]; i++) {
			fTable[LOW][i] = fTable[HIGH][2*i-div];
		}
	}

	private void calculateNoiseTable(SBRHeader header) throws AACException {
		final float log = (float) Math.log((float) k2/(float) kx)/(float) LOG2;
		final int x = Math.round(header.getNoiseBands(false)*log);
		nq = Math.max(1, x);
		//check requirement (4.6.18.6.3):
		if(nq>5) throw new AACException("SBR: too many noise floor scalefactors: "+nq);

		fNoise = new int[nq+1];
		fNoise[0] = fTable[LOW][0];
		int i = 0;
		for(int k = 1; k<=nq; k++) {
			i += (int) ((float) (n[LOW]-i)/(float) (nq+1-k));
			fNoise[k] = fTable[LOW][i];
		}
	}

	private void calculatePatches(int sampleRate) throws AACException {
		//patch construction (flowchart 4.46, p231)
		int msb = k0;
		int usb = kx;
		patchCount = 0;

		int goalSb = Math.round(GOAL_SB_FACTOR/(float) sampleRate); //TODO: replace with table
		int k;
		if(goalSb<kx+m) {
			k = 0;
			for(int i = 0; mft[i]<goalSb; i++) {
				k = i+1;
			}
		}
		else k = nMaster;

		int sb, j, odd;
		do {
			j = k+1;
			do {
				j--;
				sb = mft[j];
				odd = (sb-2+k0)&1;
			}
			while(sb>(k0-1+msb-odd));

			patchSubbands[patchCount] = Math.max(sb-usb, 0);
			patchStartSubband[patchCount] = k0-odd-patchSubbands[patchCount];

			if(patchSubbands[patchCount]>0) {
				usb = sb;
				msb = sb;
				patchCount++;
			}
			else msb = kx;

			if(mft[k]-sb<3) k = nMaster;
		}
		while(sb!=(kx+m));

		if(patchSubbands[patchCount-1]<3&&patchCount>1) patchCount--;

		//check requirement (4.6.18.6.3):
		if(patchCount>5) throw new AACException("SBR: too many patches: "+patchCount);
	}

	private void calculateLimiterTable(SBRHeader header) throws AACException {
		//calculation of fTableLim (figure 4.40, p.213)
		final int bands = header.getLimiterBands();
		if(bands==0) {
			fLim = new int[]{fTable[LOW][0], fTable[LOW][n[LOW]]};
			nl = 1;
			patchBorders = new int[0];
		}
		else {
			final float limBandsPerOctaveWarped = LIM_BANDS_PER_OCTAVE_POW[header.getLimiterBands()-1];

			patchBorders = new int[patchCount+1];
			patchBorders[0] = kx;
			for(int i = 1; i<=patchCount; i++) {
				patchBorders[i] = patchBorders[i-1]+patchSubbands[i-1];
			}

			int[] limTable = new int[n[LOW]+patchCount];
			System.arraycopy(fTable[LOW], 0, limTable, 0, n[LOW]+1);
			if(patchCount>1) System.arraycopy(patchBorders, 1, limTable, n[LOW]+1, patchCount-1);
			Arrays.sort(limTable);

			int in = 1;
			int out = 0;
			int lims = n[LOW]+patchCount-1;
			while(out<lims) {
				if(limTable[in]>=limTable[out]*limBandsPerOctaveWarped) {
					limTable[++out] = limTable[in++];
				}
				else if(limTable[in]==limTable[out]
						||!inArray(patchBorders, limTable[in])) {
					in++;
					lims--;
				}
				else if(!inArray(patchBorders, limTable[out])) {
					limTable[out] = limTable[in++];
					lims--;
				}
				else {
					limTable[++out] = limTable[in++];
				}
			}

			fLim = new int[lims+1];
			System.arraycopy(limTable, 0, fLim, 0, lims+1);
			nl = lims;
		}
	}

	private boolean inArray(int[] a, int x) {
		boolean found = false;
		for(int i = 0; !found&&i<a.length; i++) {
			if(a[i]==x) found = true;
		}
		return found;
	}

	//lower MFT border: k0
	public int getK0() {
		return k0;
	}

	//higher MFT border: k2
	public int getK2() {
		return k2;
	}

	//the master frequency table: fMaster
	public int[] getMFT() {
		return mft;
	}

	//bands in master frequency table: Nmaster
	public int getNMaster() {
		return nMaster;
	}

	//the frequency tables: fTableHigh, fTableLow
	public int[] getFrequencyTable(int i) {
		return fTable[i];
	}

	//bands in frequency tables: Nhigh, Nlow
	public int getN(int i) {
		return n[i];
	}

	//both ns
	public int[] getN() {
		return n;
	}

	//first subband in fTableHigh: kx
	public int getKx(boolean previous) {
		return previous ? kxPrev : kx;
	}

	//number of SBR subbands: M
	public int getM(boolean previous) {
		return previous ? mPrev : m;
	}

	//the noise floor frequency table: fTableNoise
	public int[] getNoiseTable() {
		return fNoise;
	}

	//bands in noise table: Nq
	public int getNq() {
		return nq;
	}

	public int getPatchCount() {
		return patchCount;
	}

	public int[] getPatchSubbands() {
		return patchSubbands;
	}

	public int[] getPatchStartSubband() {
		return patchStartSubband;
	}

	public int[] getPatchBorders() {
		return patchBorders;
	}

	//the limiter frequency band table: fTableLim
	public int[] getLimiterTable() {
		return fLim;
	}

	//bands in limiter table: Nl
	public int getNl() {
		return nl;
	}
}
