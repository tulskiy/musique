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
package net.sourceforge.jaad.aac.gain;

//inverse polyphase quadrature filter
class IPQF implements GCConstants, PQFTables {

	private final float[] buf;
	private final float[][] tmp1, tmp2;

	IPQF() {
		buf = new float[BANDS];
		tmp1 = new float[BANDS/2][NPQFTAPS/BANDS];
		tmp2 = new float[BANDS/2][NPQFTAPS/BANDS];
	}

	void process(float[][] in, int frameLen, int maxBand, float[] out) {
		int i, j;
		for(i = 0; i<frameLen; i++) {
			out[i] = 0.0f;
		}

		for(i = 0; i<frameLen/BANDS; i++) {
			for(j = 0; j<BANDS; j++) {
				buf[j] = in[j][i];
			}
			performSynthesis(buf, out, i*BANDS);
		}
	}

	private void performSynthesis(float[] in, float[] out, int outOff) {
		final int kk = NPQFTAPS/(2*BANDS);
		int i, n, k;
		float acc;

		for(n = 0; n<BANDS/2; ++n) {
			for(k = 0; k<2*kk-1; ++k) {
				tmp1[n][k] = tmp1[n][k+1];
				tmp2[n][k] = tmp2[n][k+1];
			}
		}

		for(n = 0; n<BANDS/2; ++n) {
			acc = 0.0f;
			for(i = 0; i<BANDS; ++i) {
				acc += COEFS_Q0[n][i]*in[i];
			}
			tmp1[n][2*kk-1] = acc;

			acc = 0.0f;
			for(i = 0; i<BANDS; ++i) {
				acc += COEFS_Q1[n][i]*in[i];
			}
			tmp2[n][2*kk-1] = acc;
		}

		for(n = 0; n<BANDS/2; ++n) {
			acc = 0.0f;
			for(k = 0; k<kk; ++k) {
				acc += COEFS_T0[n][k]*tmp1[n][2*kk-1-2*k];
			}
			for(k = 0; k<kk; ++k) {
				acc += COEFS_T1[n][k]*tmp2[n][2*kk-2-2*k];
			}
			out[outOff+n] = acc;

			acc = 0.0f;
			for(k = 0; k<kk; ++k) {
				acc += COEFS_T0[BANDS-1-n][k]*tmp1[n][2*kk-1-2*k];
			}
			for(k = 0; k<kk; ++k) {
				acc -= COEFS_T1[BANDS-1-n][k]*tmp2[n][2*kk-2-2*k];
			}
			out[outOff+BANDS-1-n] = acc;
		}
	}
}
