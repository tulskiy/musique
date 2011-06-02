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
package net.sourceforge.jaad.impl.noise;

import net.sourceforge.jaad.impl.CPE;
import net.sourceforge.jaad.impl.Constants;
import net.sourceforge.jaad.impl.ICSInfo;
import net.sourceforge.jaad.impl.ICStream;
import net.sourceforge.jaad.impl.SectionData;
import net.sourceforge.jaad.impl.stereo.MSMask;

/**
 * Perceptual Noise Substitution
 * @author in-somnia
 */
public final class PNS implements Constants {

	private static final int[] PARITY = {
		0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
		1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
		1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
		0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
		1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0,
		0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
		0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1,
		1, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0
	};
	private static int r1 = 1, r2 = 1;

	private PNS() {
	}

	public static void processSingle(ICStream ics, float[] spec) {
		processCommon(ics, null, null, spec, null, false);
	}

	public static void processPair(CPE cpe, float[] specL, float[] specR) {
		processCommon(cpe.getLeftChannel(), cpe.getRightChannel(), cpe, specL, specR, true);
	}

	private static void processCommon(ICStream icsL, ICStream icsR, CPE cpe, float[] specL, float[] specR, boolean channelPair) {
		final ICSInfo infoL = icsL.getInfo();
		final int windowGroupCount = infoL.getWindowGroupCount();
		final int maxSFB = infoL.getMaxSFB();
		final SectionData sectDataL = icsL.getSectionData();
		final int[] swbOffsetsL = infoL.getSWBOffsets();
		final int swbOffsetMaxL = infoL.getSWBOffsetMax();
		final int[][] scaleFactorsL = icsL.getScaleFactors();

		final int shortFrameLen = specL.length/8;

		ICSInfo infoR = null;
		SectionData sectDataR = null;
		int[] swbOffsetsR = null;
		int swbOffsetMaxR = 0;
		int[][] scaleFactorsR = null;
		if(icsR!=null) {
			infoR = icsR.getInfo();
			sectDataR = icsR.getSectionData();
			swbOffsetsR = infoR.getSWBOffsets();
			swbOffsetMaxR = infoR.getSWBOffsetMax();
			scaleFactorsR = icsR.getScaleFactors();
		}

		MSMask msMask = null;
		if(cpe!=null) msMask = cpe.getMSMask();

		int sfb, b;
		int size, offs;
		int win = 0;

		for(int g = 0; g<windowGroupCount; g++) {
			for(b = 0; b<infoL.getWindowGroupLength(g); b++) {
				for(sfb = 0; sfb<maxSFB; sfb++) {
					if(sectDataL.isNoise(g, sfb)) {
						infoL.unsetPredictionSFB(sfb);

						offs = swbOffsetsL[sfb];
						size = Math.min(swbOffsetsL[sfb+1], swbOffsetMaxL)-offs;

						generateRandomVector(specL, (win*shortFrameLen)+offs, size, scaleFactorsL[g][sfb]);
					}

					if(channelPair&&sectDataR.isNoise(g, sfb)) {
						if((msMask.equals(MSMask.TYPE_USED)&&(cpe.isMSUsed(g, sfb)))
								||(msMask.equals(MSMask.TYPE_ALL_1))) {
							int c;

							offs = swbOffsetsR[sfb];
							size = Math.min(swbOffsetsR[sfb+1], swbOffsetMaxR)-offs;

							for(c = 0; c<size; c++) {
								specR[(win*shortFrameLen)+offs+c] = specR[(win*shortFrameLen)+offs+c];
							}
						}
						else {
							infoR.unsetPredictionSFB(sfb);

							offs = swbOffsetsR[sfb];
							size = Math.min(swbOffsetsR[sfb+1], swbOffsetMaxR)-offs;

							generateRandomVector(specR, (win*shortFrameLen)+offs, size, scaleFactorsR[g][sfb]);
						}
					}
				}
				win++;
			}
		}
	}

	private static void generateRandomVector(float[] spec, int off, int size, int sf) {
		int i;
		float energy = 0.0f;

		float scale = 1.0f/(float) size;

		float tmp;
		for(i = 0; i<size; i++) {
			tmp = scale*(float) nextRandom();
			spec[off+i] = tmp;
			energy += tmp*tmp;
		}

		scale = 1.0f/(float) Math.sqrt(energy);
		scale *= Math.pow(2.0, 0.25*sf);
		for(i = 0; i<size; i++) {
			spec[off+i] *= scale;
		}
	}

	//random number generator based on parity table
	private static int nextRandom() {
		int t1 = r1;
		final int t3 = r1;
		int t2 = r2;
		final int t4 = r2;
		t1 &= 0xF5;
		t2 >>= 25;
		t1 = PARITY[t1];
		t2 &= 0x63;
		t1 <<= 31;
		t2 = PARITY[t2];
		r1 = (t3>>1)|t1;
		r2 = (t4+t4)|t2;

		return r1^r2;
	}
}
