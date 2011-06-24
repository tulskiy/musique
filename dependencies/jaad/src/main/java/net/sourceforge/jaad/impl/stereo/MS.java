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

/**
 * Mid/side stereo
 * @author in-somnia
 */
public final class MS implements Constants {

	private MS() {
	}

	public static void process(CPE cpe, float[] specL, float[] specR) {
		final MSMask msMask = cpe.getMSMask();
		final ICStream icsL = cpe.getLeftChannel();
		final ICStream icsR = cpe.getRightChannel();
		final ICSInfo infoL = icsL.getInfo();
		final SectionData sectDataL = icsL.getSectionData(), sectDataR = icsR.getSectionData();
		final int windowGroupCount = infoL.getWindowGroupCount();
		final int maxSFB = infoL.getMaxSFB();
		final int[] swbOffsets = infoL.getSWBOffsets();
		final int swbOffsetMax = infoL.getSWBOffsetMax();

		final int shortFrameLen = specL.length/8;
		
		int off = 0;
		int i, k, b, sfb;
		float l, r;

		for(int g = 0; g<windowGroupCount; g++) {
			for(b = 0; b<infoL.getWindowGroupLength(g); b++) {
				for(sfb = 0; sfb<maxSFB; sfb++) {
					if((cpe.isMSUsed(g, sfb)||msMask.equals(MSMask.TYPE_ALL_1))
							&&(sectDataR.isIntensity(g, sfb)==0)&&!sectDataL.isNoise(g, sfb)) {
						for(i = swbOffsets[sfb]; i<Math.min(swbOffsets[sfb+1], swbOffsetMax); i++) {
							k = (off*shortFrameLen)+i;
							l = specL[k];
							r = specR[k];
							specL[k] = r+l;
							specR[k] = l-r;
						}
					}
				}
				off++;
			}
		}
	}
}
