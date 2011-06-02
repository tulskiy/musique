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
package net.sourceforge.jaad.impl.invquant;

import net.sourceforge.jaad.impl.ICSInfo;

/**
 * Standard inverse quantization and scalefactor applying.
 * @author in-somnia
 */
public final class InvQuant implements IQTable, GainTable {

	private InvQuant() {
	}

	public static void process(ICSInfo info, short[] in, float[] out, int[][] scaleFactors) {
		final int windowGroupCount = info.getWindowGroupCount();
		final int[] sectSFBOffsets = info.getSWBOffsets();
		final int maxSFB = info.getMaxSFB();
		final int swbCount = info.getSWBCount();
		int sfb, win;
		int width, bin, wa, wb, j, gInc, winInc, sf;
		float gain;

		int k = 0;
		int gindex = 0;

		for(int g = 0; g<windowGroupCount; g++) {
			j = 0;
			gInc = 0;
			winInc = sectSFBOffsets[swbCount];

			for(sfb = 0; sfb<swbCount&&maxSFB>0; sfb++) {
				width = sectSFBOffsets[sfb+1]-sectSFBOffsets[sfb];
				wa = gindex+j;

				sf = scaleFactors[g][Math.min(sfb, maxSFB-1)];
				if(sf<0||sf>255) gain = 0;
				else gain = computeGain(sf);

				for(win = 0; win<info.getWindowGroupLength(g); win++) {
					for(bin = 0; bin<width; bin++) {
						wb = wa+bin;
						out[wb] = computeInvQuant(in[k])*gain;
						gInc++;
						k++;
					}
					wa += winInc;
				}
				j += width;
			}
			gindex += gInc;
		}
	}

	/**
	 * iq = sgn(q)*abs(q)<sup>4/3</sup>
	 */
	private static float computeInvQuant(int q) {
		float d;
		if(q<0) d = -IQ_TABLE[-q];
		else d = IQ_TABLE[q];
		return d;
	}

	/**
	 * gain = 2<sup>0.25*(sf-100)</sup>
	 */
	public static float computeGain(int sf) {
		return GAIN_TABLE[sf];
	}
}
