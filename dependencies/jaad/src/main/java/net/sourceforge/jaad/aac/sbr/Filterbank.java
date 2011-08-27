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
package net.sourceforge.jaad.aac.sbr;

class Filterbank implements FilterbankTables {

	private static final int FFT_LENGTH = 32;
	private final float[] tmp;

	Filterbank() {
		tmp = new float[2];
	}

	//complex DCT-IV of length 64 without reordering
	void computeDCT4Kernel(float[][] in, float[][] out) {
		float f;
		int i, iRev;

		//Step 1: modulate
		for(i = 0; i<32; i++) {
			tmp[0] = in[i][0];
			tmp[1] = in[i][1];
			f = (tmp[0]+tmp[1])*DCT4_64_TABLE[i];
			in[i][0] = (tmp[1]*DCT4_64_TABLE[i+64])+f;
			in[i][1] = (tmp[0]*DCT4_64_TABLE[i+32])+f;
		}

		//Step 2: FFT, but with output in bit reverse order
		computeFFT(in);

		//Step 3: modulate + bitreverse reordering
		for(i = 0; i<16; i++) {
			iRev = BIT_REVERSE_TABLE[i];
			tmp[0] = in[iRev][0];
			tmp[1] = in[iRev][1];

			f = (tmp[0]+tmp[1])*DCT4_64_TABLE[i+3*32];
			out[i][0] = (tmp[1]*DCT4_64_TABLE[i+5*32])+f;
			out[i][1] = (tmp[0]*DCT4_64_TABLE[i+4*32])+f;
		}

		out[16][1] = (in[1][1]-in[1][0])*DCT4_64_TABLE[16+3*32];
		out[16][0] = (in[1][0]+in[1][1])*DCT4_64_TABLE[16+3*32];
		for(i = 17; i<32; i++) {
			iRev = BIT_REVERSE_TABLE[i];
			tmp[0] = in[iRev][0];
			tmp[1] = in[iRev][1];
			f = (tmp[0]+tmp[1])*DCT4_64_TABLE[i+3*32];
			out[i][0] = (tmp[1]*DCT4_64_TABLE[i+5*32])+f;
			out[i][1] = (tmp[0]*DCT4_64_TABLE[i+4*32])+f;
		}
	}

	//32-point FFT: 144 multiplications, 400 additions
	private void computeFFT(float[][] in) {
		float re1, im1, re2, im2;
		int i, j, z;

		//stage 1
		for(i = 0; i<16; i++) {
			re1 = in[i][0];
			im1 = in[i][1];
			z = i+16;
			re2 = in[z][0];
			im2 = in[z][1];

			tmp[0] = FFT_TABLE[i][0];
			tmp[1] = FFT_TABLE[i][1];

			re1 -= re2;
			im1 -= im2;

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = (re1*tmp[0])-(im1*tmp[1]);
			in[z][1] = (re1*tmp[1])+(im1*tmp[0]);
		}
		//stage 2
		int index = 0;
		for(j = 0; j<8; j++) {
			tmp[0] = FFT_TABLE[index][0];
			tmp[1] = FFT_TABLE[index][1];
			index += 2;

			i = j;
			re1 = in[i][0];
			im1 = in[i][1];
			z = i+8;
			re2 = in[z][0];
			im2 = in[z][1];

			re1 -= re2;
			im1 -= im2;

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = (re1*tmp[0])-(im1*tmp[1]);
			in[z][1] = (re1*tmp[1])+(im1*tmp[0]);

			i = j+16;
			re1 = in[i][0];
			im1 = in[i][1];
			z = i+8;
			re2 = in[z][0];
			im2 = in[z][1];

			re1 -= re2;
			im1 -= im2;

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = (re1*tmp[0])-(im1*tmp[1]);
			in[z][1] = (re1*tmp[1])+(im1*tmp[0]);
		}

		//stage 3
		for(i = 0; i<FFT_LENGTH; i += 8) {
			z = i+4;
			re1 = in[i][0];
			im1 = in[i][1];

			re2 = in[z][0];
			im2 = in[z][1];

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = re1-re2;
			in[z][1] = im1-im2;
		}
		tmp[0] = FFT_TABLE[4][0];
		for(i = 1; i<FFT_LENGTH; i += 8) {
			z = i+4;
			re1 = in[i][0];
			im1 = in[i][1];

			re2 = in[z][0];
			im2 = in[z][1];

			re1 -= re2;
			im1 -= im2;

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = (re1+im1)*tmp[0];
			in[z][1] = (im1-re1)*tmp[0];
		}
		for(i = 2; i<FFT_LENGTH; i += 8) {
			z = i+4;
			re1 = in[i][0];
			im1 = in[i][1];

			re2 = in[z][0];
			im2 = in[z][1];

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = im1-im2;
			in[z][1] = re2-re1;
		}
		tmp[0] = FFT_TABLE[12][0];
		for(i = 3; i<FFT_LENGTH; i += 8) {
			z = i+4;
			re1 = in[i][0];
			im1 = in[i][1];

			re2 = in[z][0];
			im2 = in[z][1];

			re1 -= re2;
			im1 -= im2;

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = (re1-im1)*tmp[0];
			in[z][1] = (re1+im1)*tmp[0];
		}

		//stage 4
		for(i = 0; i<FFT_LENGTH; i += 4) {
			z = i+2;
			re1 = in[i][0];
			im1 = in[i][1];

			re2 = in[z][0];
			im2 = in[z][1];

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = re1-re2;
			in[z][1] = im1-im2;
		}
		for(i = 1; i<FFT_LENGTH; i += 4) {
			z = i+2;
			re1 = in[i][0];
			im1 = in[i][1];

			re2 = in[z][0];
			im2 = in[z][1];

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = im1-im2;
			in[z][1] = re2-re1;
		}

		//stage 5
		for(i = 0; i<FFT_LENGTH; i += 2) {
			z = i+1;
			re1 = in[i][0];
			im1 = in[i][1];

			re2 = in[z][0];
			im2 = in[z][1];

			in[i][0] += re2;
			in[i][1] += im2;

			in[z][0] = re1-re2;
			in[z][1] = im1-im2;
		}
	}
}
