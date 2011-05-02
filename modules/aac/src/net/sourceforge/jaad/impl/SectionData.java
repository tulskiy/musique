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
import net.sourceforge.jaad.impl.huffman.HCB;

public class SectionData implements Constants {

	private static final int MAX_SECT_COUNT = 120;
	private static final int BITS_LONG = 5, BITS_SHORT = 3;
	private int[][] sectCB;
	private int[][] sectStart;
	private int[][] sectEnd;
	private int[][] sfbCB;
	private int[] numSec;

	public void decode(BitStream in, final ICSInfo info, boolean sectionDataResilienceUsed) throws AACException {
		final int bitsLen = info.isEightShortFrame() ? BITS_SHORT : BITS_LONG;
		final int escVal = (1<<bitsLen)-1;
		final int sectCBBits = (sectionDataResilienceUsed) ? 5 : 4;

		final int windowGroupCount = info.getWindowGroupCount();
		if(sectCB==null||windowGroupCount!=sectCB.length) {
			//only reallocate if needed
			sectCB = new int[windowGroupCount][MAX_SECT_COUNT];
			sectStart = new int[windowGroupCount][MAX_SECT_COUNT];
			sectEnd = new int[windowGroupCount][MAX_SECT_COUNT];
			sfbCB = new int[windowGroupCount][MAX_SECT_COUNT];
			numSec = new int[windowGroupCount];
		}
		final int maxSFB = info.getMaxSFB();

		int k, i;
		int sectLen, sectLenIncr, end, sfb;
		for(int g = 0; g<windowGroupCount; g++) {
			k = 0;
			i = 0;
			while(k<maxSFB) {
				sectCB[g][i] = in.readBits(sectCBBits);

				if(sectionDataResilienceUsed&&((sectCB[g][i]==11)||((sectCB[g][i]>15)&&(sectCB[g][i]<33)))) sectLenIncr = 1;
				else sectLenIncr = in.readBits(bitsLen);
				sectLen = 0;
				while(sectLenIncr==escVal) {
					sectLen += sectLenIncr;
					sectLenIncr = in.readBits(bitsLen);
				}
				sectLen += sectLenIncr;

				end = k+sectLen;
				sectStart[g][i] = k;
				sectEnd[g][i] = end;

				for(sfb = k; sfb<end; sfb++) {
					sfbCB[g][sfb] = sectCB[g][i];
				}

				k += sectLen;
				i++;
			}
			numSec[g] = i;
		}
	}

	public int[][] getSectEnd() {
		return sectEnd;
	}

	public int[][] getSectStart() {
		return sectStart;
	}

	public int[][] getSfbCB() {
		return sfbCB;
	}

	public int[][] getSectCB() {
		return sectCB;
	}

	public int[] getNumSec() {
		return numSec;
	}

	public boolean isNoise(int g, int sfb) {
		return sfbCB[g][sfb]==HCB.NOISE_HCB;
	}

	public int isIntensity(int g, int sfb) {
		int i;
		switch(sfbCB[g][sfb]) {
			case HCB.INTENSITY_HCB:
				i = 1;
				break;
			case HCB.INTENSITY_HCB2:
				i = -1;
				break;
			default:
				i = 0;
		}
		return i;
	}
}
