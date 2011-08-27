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
package net.sourceforge.jaad.aac.error;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.huffman.HCB;
import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.ICSInfo;
import net.sourceforge.jaad.aac.syntax.ICStream;

/**
 * Reversable variable length coding
 * Decodes scalefactors if error resilience is used.
 */
public class RVLC implements RVLCTables {

	private static final int ESCAPE_FLAG = 7;

	public void decode(BitStream in, ICStream ics, int[][] scaleFactors) throws AACException {
		final int bits = (ics.getInfo().isEightShortFrame()) ? 11 : 9;
		final boolean sfConcealment = in.readBool();
		final int revGlobalGain = in.readBits(8);
		final int rvlcSFLen = in.readBits(bits);

		final ICSInfo info = ics.getInfo();
		final int windowGroupCount = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		final int[][] sfbCB = null; //ics.getSectionData().getSfbCB();

		int sf = ics.getGlobalGain();
		int intensityPosition = 0;
		int noiseEnergy = sf-90-256;
		boolean intensityUsed = false, noiseUsed = false;

		int sfb;
		for(int g = 0; g<windowGroupCount; g++) {
			for(sfb = 0; sfb<maxSFB; sfb++) {
				switch(sfbCB[g][sfb]) {
					case HCB.ZERO_HCB:
						scaleFactors[g][sfb] = 0;
						break;
					case HCB.INTENSITY_HCB:
					case HCB.INTENSITY_HCB2:
						if(!intensityUsed) intensityUsed = true;
						intensityPosition += decodeHuffman(in);
						scaleFactors[g][sfb] = intensityPosition;
						break;
					case HCB.NOISE_HCB:
						if(noiseUsed) {
							noiseEnergy += decodeHuffman(in);
							scaleFactors[g][sfb] = noiseEnergy;
						}
						else {
							noiseUsed = true;
							noiseEnergy = decodeHuffman(in);
						}
						break;
					default:
						sf += decodeHuffman(in);
						scaleFactors[g][sfb] = sf;
						break;
				}
			}
		}

		int lastIntensityPosition = 0;
		if(intensityUsed) lastIntensityPosition = decodeHuffman(in);
		noiseUsed = false;
		if(in.readBool()) decodeEscapes(in, ics, scaleFactors);
	}

	private void decodeEscapes(BitStream in, ICStream ics, int[][] scaleFactors) throws AACException {
		final ICSInfo info = ics.getInfo();
		final int windowGroupCount = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		final int[][] sfbCB = null; //ics.getSectionData().getSfbCB();

		final int escapesLen = in.readBits(8);

		boolean noiseUsed = false;

		int sfb, val;
		for(int g = 0; g<windowGroupCount; g++) {
			for(sfb = 0; sfb<maxSFB; sfb++) {
				if(sfbCB[g][sfb]==HCB.NOISE_HCB&&!noiseUsed) noiseUsed = true;
				else if(Math.abs(sfbCB[g][sfb])==ESCAPE_FLAG) {
					val = decodeHuffmanEscape(in);
					if(sfbCB[g][sfb]==-ESCAPE_FLAG) scaleFactors[g][sfb] -= val;
					else scaleFactors[g][sfb] += val;
				}
			}
		}
	}

	private int decodeHuffman(BitStream in) throws AACException {
		int off = 0;
		int i = RVLC_BOOK[off][1];
		int cw = in.readBits(i);

		int j;
		while((cw!=RVLC_BOOK[off][2])&&(i<10)) {
			off++;
			j = RVLC_BOOK[off][1]-i;
			i += j;
			cw <<= j;
			cw |= in.readBits(j);
		}

		return RVLC_BOOK[off][0];
	}

	private int decodeHuffmanEscape(BitStream in) throws AACException {
		int off = 0;
		int i = ESCAPE_BOOK[off][1];
		int cw = in.readBits(i);

		int j;
		while((cw!=ESCAPE_BOOK[off][2])&&(i<21)) {
			off++;
			j = ESCAPE_BOOK[off][1]-i;
			i += j;
			cw <<= j;
			cw |= in.readBits(j);
		}

		return ESCAPE_BOOK[off][0];
	}
}
