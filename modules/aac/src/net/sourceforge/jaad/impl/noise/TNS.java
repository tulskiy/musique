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

import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.impl.BitStream;
import net.sourceforge.jaad.impl.Constants;
import net.sourceforge.jaad.impl.ICSInfo;
import net.sourceforge.jaad.impl.ICStream;
import net.sourceforge.jaad.SampleFrequency;

/**
 * Temporal Noise Shaping
 * @author in-somnia
 */
public class TNS implements Constants, TNSTables {

	private static final int TNS_MAX_ORDER = 20;
	private static final int[] SHORT_BITS = {1, 4, 3}, LONG_BITS = {2, 6, 5};
	//bitstream
	private int[] nFilt;
	private byte[] coefRes;
	private int[][] length, order;
	private byte[][] coefCompress;
	private boolean[][] direction;
	private byte[][][] coef;
	//processing buffers
	private final double[] lpc;
	private final double[] tmp1, tmp2;

	public TNS() {
		lpc = new double[TNS_MAX_ORDER+1];
		tmp1 = new double[TNS_MAX_ORDER+1];
		tmp2 = new double[TNS_MAX_ORDER+1];
	}

	public void decode(BitStream in, ICSInfo info) throws AACException {
		final int windowCount = info.getWindowCount();
		final int[] bits = info.isEightShortFrame() ? SHORT_BITS : LONG_BITS;

		int filt, i, coefBits, coefLen;
		if(nFilt==null||windowCount!=nFilt.length) {
			//reallocate only if needed
			nFilt = new int[windowCount];
			coefRes = new byte[windowCount];
			length = new int[windowCount][];
			order = new int[windowCount][];
			coefCompress = new byte[windowCount][];
			direction = new boolean[windowCount][];
			coef = new byte[windowCount][][];
		}

		int x;
		for(int w = 0; w<windowCount; w++) {
			if((x = in.readBits(bits[0]))!=0) {
				if(x!=nFilt[w]) {
					//reallocate only if needed
					nFilt[w] = x;
					length[w] = new int[x];
					order[w] = new int[x];
					direction[w] = new boolean[x];
					coefCompress[w] = new byte[x];
					coef[w] = new byte[x][];
				}

				coefRes[w] = (byte) in.readBit();
				coefBits = 3+coefRes[w];

				for(filt = 0; filt<nFilt[w]; filt++) {
					length[w][filt] = in.readBits(bits[1]);
					if((x = in.readBits(bits[2]))>TNS_MAX_ORDER) throw new AACException("TNS filter order out of range: "+order[w][filt]);
					if(x!=0) {
						if(x!=order[w][filt]) {
							//reallocate only if needed
							order[w][filt] = x;
							coef[w][filt] = new byte[x];
						}
						direction[w][filt] = in.readBool();
						coefCompress[w][filt] = (byte) in.readBit();
						coefLen = coefBits-coefCompress[w][filt];
						for(i = 0; i<order[w][filt]; i++) {
							coef[w][filt][i] = (byte) in.readBits(coefLen);
						}
					}
				}
			}
		}
	}

	public void process(ICStream ics, float[] spec, SampleFrequency sf, boolean forward) {
		final ICSInfo info = ics.getInfo();
		final int maxSFB = info.getMaxSFB();
		final int windowCount = info.getWindowCount();
		final int swbCount = info.getSWBCount();
		final int[] swbOffsets = info.getSWBOffsets();
		final int swbOffsetMax = info.getSWBOffsetMax();
		final int maxTNSSFB = sf.getMaximalTNS_SFB(info.isEightShortFrame());

		final int shortFrameLen = spec.length/8;

		int f, tnsOrder;
		int inc;
		int size;
		int bottom, top, start, end;

		for(int w = 0; w<windowCount; w++) {
			bottom = swbCount;

			for(f = 0; f<nFilt[w]; f++) {
				top = bottom;
				bottom = Math.max(top-length[w][f], 0);
				tnsOrder = Math.min(order[w][f], TNS_MAX_ORDER);
				if(tnsOrder==0) continue;

				decodeCoef(coef[w][f], tnsOrder, coefRes[w], coefCompress[w][f]);

				start = Math.min(bottom, maxTNSSFB);
				start = Math.min(start, maxSFB);
				start = Math.min(swbOffsets[start], swbOffsetMax);

				end = Math.min(top, maxTNSSFB);
				end = Math.min(end, maxSFB);
				end = Math.min(swbOffsets[end], swbOffsetMax);

				size = end-start;
				if(size<=0) continue;

				if(direction[w][f]) {
					inc = -1;
					start = end-1;
				}
				else inc = 1;

				if(forward) applyZeroFilter(spec, (w*shortFrameLen)+start, size, inc, tnsOrder);
				else applyPoleFilter(spec, (w*shortFrameLen)+start, size, inc, tnsOrder);
			}
		}
	}

	//decodes coefs in input array and stores them in lpc-buffer
	private void decodeCoef(byte[] in, int order, byte coefRes, byte coefCompress) {
		int i;

		//conversion to TNS coefs
		final double[] table = TNS_TABLES[2*coefCompress+coefRes];
		for(i = 0; i<order; i++) {
			tmp1[i] = table[in[i]];
		}

		//conversion to LPC coefs
		lpc[0] = 1.0;
		for(int m = 1; m<=order; m++) {
			for(i = 1; i<m; i++) {
				tmp2[i] = lpc[i]+(tmp1[m-1]*lpc[m-i]);
			}
			for(i = 1; i<m; i++) {
				lpc[i] = tmp2[i];
			}
			lpc[m] = tmp1[m-1];
		}
	}

	private void applyPoleFilter(float[] spec, int off, int size, int inc, int order) {
		final double[] state = new double[2*TNS_MAX_ORDER];
		int index = 0;

		int j;
		float y;
		for(int i = 0; i<size; i++) {
			y = spec[off];

			for(j = 0; j<order; j++) {
				y -= state[index+j]*lpc[j+1];
			}

			index--;
			if(index<0) index = order-1;
			state[index] = y;
			state[index+order] = y;

			spec[off] = y;
			off += inc;
		}
	}

	private void applyZeroFilter(float[] spec, int off, int size, int inc, int order) {
		int j, i;
		float y;
		final double[] state = new double[2*TNS_MAX_ORDER];
		int index = 0;

		for(i = 0; i<size; i++) {
			y = spec[off];

			for(j = 0; j<order; j++) {
				y += state[index+j]*lpc[j+1];
			}

			index--;
			if(index<0) index = order-1;
			state[index] = spec[off];
			state[index+order] = spec[off];

			spec[off] = y;
			off += inc;
		}
	}
}
