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
package net.sourceforge.jaad.aac.sbr;

import java.util.Arrays;

//static calculation methods
class Calculation implements SBRTables {

	static int getStartChannel(int startFrequency, int sampleRate) {
		final int index = getSampleRateIndex(sampleRate);
		return START_MIN_TABLE[index]+START_OFFSETS[OFFSET_INDEX_TABLE[index]][startFrequency];
	}

	static int getStopChannel(int stopFrequency, int sampleRate, int k0) {
		if(stopFrequency==15) return Math.min(64, k0*3);
		else if(stopFrequency==14) return Math.min(64, k0*2);
		else {
			//stopFrequency<=13
			final int index = getSampleRateIndex(sampleRate);
			return Math.min(64, STOP_MIN_TABLE[index]+STOP_OFFSETS[index][Math.min(stopFrequency, 13)]);
		}
	}

	//TODO: replace with SampleFrequency.forFrequency ??
	static int getSampleRateIndex(int samplerate) {
		if(92017<=samplerate) return 0;
		else if(75132<=samplerate) return 1;
		else if(55426<=samplerate) return 2;
		else if(46009<=samplerate) return 3;
		else if(37566<=samplerate) return 4;
		else if(27713<=samplerate) return 5;
		else if(23004<=samplerate) return 6;
		else if(18783<=samplerate) return 7;
		else if(13856<=samplerate) return 8;
		else if(11502<=samplerate) return 9;
		else if(9391<=samplerate) return 10;
		else return 11;
	}

	//calculates the master frequency table from k0, k2 and alterScale for frequencyScale = 0
	static int[] calculateMasterFrequencyTableFS0(int k0, int k2, boolean alterScale) {
		if(k2<=k0) return null;
		final int dk = alterScale ? 2 : 1;
		int i;

		int nrBands = alterScale ? (((k2-k0+2)>>2)<<1) : (((k2-k0)>>1)<<1);
		nrBands = Math.min(nrBands, 63);
		if(nrBands<=0) return new int[0];

		final int k2Achieved = k0+nrBands*dk;
		int k2Diff = k2-k2Achieved;

		//fill vDk
		final int[] vDk = new int[64];
		for(i = 0; i<nrBands; i++) {
			vDk[i] = dk;
		}

		if(k2Diff!=0) {
			final int incr = (k2Diff>0) ? -1 : 1;
			i = ((k2Diff>0) ? (nrBands-1) : 0);

			while(k2Diff!=0) {
				vDk[i] -= incr;
				i += incr;
				k2Diff += incr;
			}
		}

		//fill table
		final int len = Math.min(nrBands+1, 64);
		int[] table = new int[len];
		table[0] = k0;
		for(i = 1; i<len; i++) {
			table[i] = table[i-1]+vDk[i-1];
		}
		return table;
	}

	//calculates the master frequency table from k0, k2 and alterScale for frequencyScale > 0
	static int[] calculateMasterFrequencyTable(int k0, int k2, int frequencyScale,
			boolean alterScale) {
		if(k2<=k0) return null;
		final int bands = MFT_BANDS_COUNT[frequencyScale-1];
		int i;

		int k1;
		boolean twoRegions;
		if(((float) k2/(float) k0)>2.2449) {
			twoRegions = true;
			k1 = k0<<1;
		}
		else {
			twoRegions = false;
			k1 = k2;
		}

		final int nrBand0 = Math.min(2*findBands(false, bands, k0, k1), 63);
		if(nrBand0<=0) return new int[0];

		//fill vDk0
		final int[] vDk0 = new int[64];
		double q = findInitialPower(nrBand0, k0, k1);
		double qk = k0;
		int A_1 = (int) (qk+0.5);
		int A_0;
		for(i = 0; i<=nrBand0; i++) {
			A_0 = A_1;
			qk *= q;
			A_1 = (int) Math.round(qk);
			vDk0[i] = A_1-A_0;
		}

		Arrays.sort(vDk0, 0, nrBand0); //needed??

		//fill vk0
		final int[] vk0 = new int[64];
		vk0[0] = k0;
		for(i = 1; i<=nrBand0; i++) {
			vk0[i] = vk0[i-1]+vDk0[i-1];
			if(vDk0[i-1]==0) return new int[0];
		}

		int[] ret;
		if(twoRegions) {
			//two region: create vk1 and append it to vk0
			final int nrBand1 = Math.min(2*findBands(true, bands, k1, k2), 63);

			//fill vDk1
			final int[] vDk1 = new int[64];
			q = findInitialPower(nrBand1, k1, k2);
			qk = k1;
			A_1 = (int) (qk+.5);
			for(i = 0; i<=nrBand1-1; i++) {
				A_0 = A_1;
				qk *= q;
				A_1 = (int) (qk+0.5);
				vDk1[i] = A_1-A_0;
			}

			if(vDk1[0]<vDk0[nrBand0-1]) {
				Arrays.sort(vDk1, 0, nrBand1+1); //needed??
				final int change = vDk0[nrBand0-1]-vDk1[0];
				vDk1[0] = vDk0[nrBand0-1];
				vDk1[nrBand1-1] = vDk1[nrBand1-1]-change;
			}

			//fill vk1
			final int[] vk1 = new int[64];
			Arrays.sort(vDk1, 0, nrBand1); //needed??
			vk1[0] = k1;
			for(i = 1; i<=nrBand1; i++) {
				vk1[i] = vk1[i-1]+vDk1[i-1];
				if(vDk1[i-1]==0) return new int[0];
			}

			final int off = nrBand0+1;
			final int len = Math.min(off+nrBand1, 64);
			ret = new int[len];
			System.arraycopy(vk0, 0, ret, 0, off);
			System.arraycopy(vk1, 1, ret, off, nrBand1);
		}
		else {
			//one region: just copy vk0
			final int len = Math.min(nrBand0+1, 64);
			ret = new int[len];
			System.arraycopy(vk0, 0, ret, 0, len);
		}
		return ret;
	}

	/* finds the number of bands by:
	 * bands * log(a1/a0)/log(2.0) + 0.5 */
	static int findBands(boolean warp, int bands, int a0, int a1) {
		double div = Math.log(2.0);
		if(warp) div *= 1.3;
		return (int) (bands*Math.log((double) a1/(double) a0)/div+0.5);
	}

	private static double findInitialPower(int bands, int a0, int a1) {
		return Math.pow((double) a1/(double) a0, 1.0/(double) bands);
	}
}
