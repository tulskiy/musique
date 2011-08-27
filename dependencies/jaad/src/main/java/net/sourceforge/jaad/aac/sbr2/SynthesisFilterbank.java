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
package net.sourceforge.jaad.aac.sbr2;

class SynthesisFilterbank implements SBRConstants, FilterbankTables {

	private final float[][][] COEFS;
	private final float[][] V;
	private final float[] g;

	SynthesisFilterbank() {
		V = new float[2][1280]; //for both channels
		g = new float[640]; //tmp buffer

		//complex coefficients:
		COEFS = new float[64][128][2];
		final float fac = 1.0f/64.0f;
		double tmp;
		//TODO: optimize loop
		for(int k = 0; k<64; k++) {
			for(int n = 0; n<128; n++) {
				tmp = Math.PI/128.0*(n+0.5)*(2*k-255);
				COEFS[k][n][0] = fac*(float) Math.cos(tmp);
				COEFS[k][n][1] = fac*(float) Math.sin(tmp);
			}
		}
	}

	//in: 64 x 32 complex, out: 2048 time samples
	public void process(float[][][] in, float[] out, int ch) {
		final float[] v = V[ch];
		int n, k, off = 0;

		//each loop creates 64 output samples
		for(int l = 0; l<TIME_SLOTS_RATE; l++) {
			//1. shift buffer
			System.arraycopy(v, 0, v, 128, 1152);

			//2. multiple input by matrix and save in buffer
			for(n = 0; n<128; n++) {
				v[n] = 0.0f;
				for(k = 0; k<64; k++) {
					v[n] += in[k][l][0]*COEFS[k][n][0];
					v[n] -= in[k][l][1]*COEFS[k][n][1];
				}
			}

			//3. extract samples
			for(n = 0; n<5; n++) {
				for(k = 0; k<64; k++) {
					g[128*n+k] = v[256*n+k];
					g[128*n+64+k] = v[256*n+192+k];
				}
			}

			//4. window signal

			for(n = 0; n<=639; n++) {
				g[n] *= (float) WINDOW[n];
			}

			//5. calculate output samples
			for(k = 0; k<=63; k++) {
				out[off] = g[k];
				for(n = 1; n<=9; n++) {
					out[off] += g[64*n+k];
				}
				off++;
			}
		}
	}
}
