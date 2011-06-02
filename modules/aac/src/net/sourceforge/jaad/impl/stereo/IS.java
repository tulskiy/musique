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
package net.sourceforge.jaad.impl.stereo;

import net.sourceforge.jaad.impl.CPE;
import net.sourceforge.jaad.impl.Constants;
import net.sourceforge.jaad.impl.ICSInfo;
import net.sourceforge.jaad.impl.ICStream;
import net.sourceforge.jaad.impl.SectionData;
import net.sourceforge.jaad.impl.prediction.ICPrediction;

/**
 * Intensity stereo
 * @author in-somnia
 */
public final class IS implements Constants, ISScaleTable {

	private IS() {
	}

	public static void process(CPE cpe, float[] specL, float[] specR) {
		final ICStream icsL = cpe.getLeftChannel();
		final ICStream icsR = cpe.getRightChannel();
		final ICSInfo infoL = icsL.getInfo();
		final ICSInfo infoR = icsR.getInfo();
		final SectionData sectDataR = icsR.getSectionData();
		final int windowGroupCount = infoR.getWindowGroupCount();
		final int maxSFB = infoR.getMaxSFB();
		final int[][] scaleFactors = icsR.getScaleFactors();
		final int[] swbOffsets = infoR.getSWBOffsets();
		final int swbOffsetMax = infoL.getSWBOffsetMax();
		final ICPrediction predL = infoL.getICPrediction();
		final ICPrediction predR = infoR.getICPrediction();

		final int shortFrameLen = specL.length/8;

		int sfb, b, i, max;
		float scale;
		int group = 0;

		for(int g = 0; g<windowGroupCount; g++) {
			for(b = 0; b<infoR.getWindowGroupLength(g); b++) {
				for(sfb = 0; sfb<maxSFB; sfb++) {
					max = Math.min(swbOffsets[sfb+1], swbOffsetMax);
					if(sectDataR.isIntensity(g, sfb)!=0) {
						predL.setPredictionUnused(sfb);
						predR.setPredictionUnused(sfb);

						scale = SCALE_TABLE[scaleFactors[g][sfb]];

						for(i = swbOffsets[sfb]; i<max; i++) {
							specR[(group*shortFrameLen)+i] = specL[(group*shortFrameLen)+i]*scale;
							if(sectDataR.isIntensity(g, sfb)!=invertIntensity(cpe, g, sfb)) specR[(group*shortFrameLen)+i] = -specR[(group*shortFrameLen)+i];
						}
					}
				}
				group++;
			}
		}
	}

	private static int invertIntensity(CPE cpe, int g, int sfb) {
		int i;
		if(cpe.getMSMask().equals(MSMask.TYPE_USED)) i = (1-2*(cpe.isMSUsed(g, sfb) ? 1 : 0));
		else i = 1;
		return i;
	}
}
