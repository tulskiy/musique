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

class AnalysisFilterbank implements SBRConstants, FilterbankTables {

	private final float[][][] COEFS;
	private final float[][] X;
	private final float[] z, u;

	AnalysisFilterbank() {
		X = new float[2][320]; //for both channels
		z = new float[320]; //tmp buffer
		u = new float[64]; //tmp buffer

		//complex coefficients:
		COEFS = new float[32][64][2];
		double tmp;
		for(int k = 0; k<32; k++) {
			for(int n = 0; n<64; n++) {
				tmp = Math.PI/64.0*(k+0.5)*(2*n-0.5);
				COEFS[k][n][0] = (float) (2*Math.cos(tmp));
				COEFS[k][n][1] = (float) (2*Math.sin(tmp));
			}
		}
	}

	//in: 1024 time samples, out: 32 x 32 complex
	public void process(float[] in, float[][][] out, int ch) {
		final float[] x = X[ch];
		int n, k, off = 0;

		//each loop creates 32 complex subband samples
		for(int l = 0; l<TIME_SLOTS_RATE; l++) {
			//1. shift buffer
			System.arraycopy(x, 0, x, 32, 288);

			//2. add new samples
			for(n = 31; n>=0; n--) {
				x[n] = in[off];
				off++;
			}

			//3. windowing
			for(n = 0; n<320; n++) {
				//TODO: convert WINDOW to floats
				z[n] = x[n]*(float) WINDOW[2*n];
			}

			//4. sum samples
			for(n = 0; n<64; n++) {
				u[n] = z[n];
				for(k = 1; k<5; k++) {
					u[n] += z[n+k*64];
				}
			}

			//5. calculate subband samples, TODO: replace with FFT?
			for(k = 0; k<32; k++) {
				out[k][l][0] = 0.0f;
				out[k][l][1] = 0.0f;
				for(n = 0; n<64; n++) {
					out[k][l][0] += u[n]*COEFS[k][n][0];
					out[k][l][1] += u[n]*COEFS[k][n][1];
				}
			}
		}
	}
}
