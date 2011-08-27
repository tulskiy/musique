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
 * Mid/side stereo
 * @author in-somnia
 */
public final class MS implements Constants, HCB {

	private MS() {
	}

	public static void process(CPE cpe, float[] specL, float[] specR) {
		final ICStream ics = cpe.getLeftChannel();
		final ICSInfo info = ics.getInfo();
		final int[] offsets = info.getSWBOffsets();
		final int windowGroups = info.getWindowGroupCount();
		final int maxSFB = info.getMaxSFB();
		final int[] sfbCBl = ics.getSfbCB();
		final int[] sfbCBr = cpe.getRightChannel().getSfbCB();
		int groupOff = 0;
		int g, i, w, j, idx = 0;

		for(g = 0; g<windowGroups; g++) {
			for(i = 0; i<maxSFB; i++, idx++) {
				if(cpe.isMSUsed(idx)&&sfbCBl[idx]<NOISE_HCB&&sfbCBr[idx]<NOISE_HCB) {
					for(w = 0; w<info.getWindowGroupLength(g); w++) {
						final int off = groupOff+w*128+offsets[i];
						for(j = 0; j<offsets[i+1]-offsets[i]; j++) {
							float t = specL[off+j]-specR[off+j];
							specL[off+j] += specR[off+j];
							specR[off+j] = t;
						}
					}
				}
			}
			groupOff += info.getWindowGroupLength(g)*128;
		}
	}
}
