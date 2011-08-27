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
package net.sourceforge.jaad.aac.tools;

import net.sourceforge.jaad.aac.huffman.HCB;
import net.sourceforge.jaad.aac.syntax.CPE;
import net.sourceforge.jaad.aac.syntax.Constants;
import net.sourceforge.jaad.aac.syntax.ICSInfo;
import net.sourceforge.jaad.aac.syntax.ICStream;

/**
 * Intensity stereo
 * @author in-somnia
 */
public final class IS implements Constants, ISScaleTable, HCB {

	private IS() {
	}

	public static void process(CPE cpe, float[] specL, float[] specR) {
		final ICStream ics = cpe.getRightChannel();
		final ICSInfo info = ics.getInfo();
		final int[] offsets = info.getSWBOffsets();
		final int windowGroups = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		final int[] sfbCB = ics.getSfbCB();
		final int[] sectEnd = ics.getSectEnd();
		final float[] scaleFactors = ics.getScaleFactors();

		int w, i, j, c, end, off;
		int idx = 0, groupOff = 0;
		float scale;
		for(int g = 0; g<windowGroups; g++) {
			for(i = 0; i<maxSFB;) {
				if(sfbCB[idx]==INTENSITY_HCB||sfbCB[idx]==INTENSITY_HCB2) {
					end = sectEnd[idx];
					for(; i<end; i++, idx++) {
						c = sfbCB[idx]==INTENSITY_HCB ? 1 : -1;
						if(cpe.isMSMaskPresent())
							c *= cpe.isMSUsed(idx) ? -1 : 1;
						scale = c*scaleFactors[idx];
						for(w = 0; w<info.getWindowGroupLength(g); w++) {
							off = groupOff+w*128+offsets[i];
							for(j = 0; j<offsets[i+1]-offsets[i]; j++) {
								specR[off+j] = specL[off+j]*scale;
							}
						}
					}
				}
				else {
					end = sectEnd[idx];
					idx += end-i;
					i = end;
				}
			}
			groupOff += info.getWindowGroupLength(g)*128;
		}
	}
}
