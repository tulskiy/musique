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
package net.sourceforge.jaad.aac.huffman;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.BitStream;

//TODO: implement decodeSpectralDataER
public class Huffman implements Codebooks {

	private static final boolean[] UNSIGNED = {false, false, true, true, false, false, true, true, true, true, true};
	private static final int QUAD_LEN = 4, PAIR_LEN = 2;

	private Huffman() {
	}

	private static int findOffset(BitStream in, int[][] table) throws AACException {
		int off = 0;
		int len = table[off][0];
		int cw = in.readBits(len);
		int j;
		while(cw!=table[off][1]) {
			off++;
			j = table[off][0]-len;
			len = table[off][0];
			cw <<= j;
			cw |= in.readBits(j);
		}
		return off;
	}

	private static void signValues(BitStream in, int[] data, int off, int len) throws AACException {
		for(int i = off; i<off+len; i++) {
			if(data[i]!=0) {
				if(in.readBool()) data[i] = -data[i];
			}
		}
	}

	private static int getEscape(BitStream in, int s) throws AACException {
		final boolean neg = s<0;

		int i = 4;
		while(in.readBool()) {
			i++;
		}
		final int j = in.readBits(i)|(1<<i);

		return (neg ? -j : j);
	}

	public static int decodeScaleFactor(BitStream in) throws AACException {
		final int offset = findOffset(in, HCB_SF);
		return HCB_SF[offset][2];
	}

	public static void decodeSpectralData(BitStream in, int cb, int[] data, int off) throws AACException {
		int[][] HCB;
		try {
			HCB = CODEBOOKS[cb-1];
		}
		catch(ArrayIndexOutOfBoundsException ex) {
			System.out.println("codebook: "+cb);
			ex.printStackTrace();
			throw ex;
		}

		//find index
		final int offset = findOffset(in, HCB);

		//copy data
		data[off] = HCB[offset][2];
		data[off+1] = HCB[offset][3];
		if(cb<5) {
			data[off+2] = HCB[offset][4];
			data[off+3] = HCB[offset][5];
		}

		//sign & escape
		if(cb<11) {
			if(UNSIGNED[cb-1]) signValues(in, data, off, cb<5 ? QUAD_LEN : PAIR_LEN);
		}
		else if(cb==11||cb>15) {
			signValues(in, data, off, cb<5 ? QUAD_LEN : PAIR_LEN); //virtual codebooks are always unsigned
			if(Math.abs(data[off])==16) data[off] = getEscape(in, data[off]);
			if(Math.abs(data[off+1])==16) data[off+1] = getEscape(in, data[off+1]);
		}
		else throw new AACException("Huffman: unknown spectral codebook: "+cb);
	}
}
