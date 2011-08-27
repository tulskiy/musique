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
package net.sourceforge.jaad.aac.ps;

class Filterbank implements FilterbankTables {

	private static final int FB_LEN = 32;

	//=============================== analysis =============================
	//out: [91][32][2], buf: [5][44][2], in:[38][64][2]
	static void performAnalysis(float[][][] in, float[][][] out, float[][][] buf, boolean use34) {
		int i, j;
		for(i = 0; i<5; i++) {
			for(j = 0; j<38; j++) {
				buf[i][j+6][0] = in[j][i][0];
				buf[i][j+6][1] = in[j][i][1];
			}
		}
		if(use34) {
			perfomFilter4C(buf[0], out, 0, F34_0_12, 12);
			perfomFilter4C(buf[1], out, 12, F34_1_8, 8);
			perfomFilter4C(buf[2], out, 20, F34_2_4, 4);
			perfomFilter4C(buf[3], out, 24, F34_2_4, 4);
			perfomFilter4C(buf[4], out, 28, F34_2_4, 4);
			for(i = 0; i<59; i++) {
				for(j = 0; j<FB_LEN; j++) {
					out[i+32][j][0] = in[j][i+5][0];
					out[i+32][j][1] = in[j][i+5][1];
				}
			}
		}
		else {
			performFilter6C(buf[0], out, 0, F20_0_8);
			performFilter2R(buf[1], out, 6, G1_Q2, true);
			performFilter2R(buf[2], out, 8, G1_Q2, false);
			for(i = 0; i<61; i++) {
				for(j = 0; j<FB_LEN; j++) {
					out[i+10][j][0] = in[j][i+3][0];
					out[i+10][j][1] = in[j][i+3][1];
				}
			}
		}
		//update in_buf
		for(i = 0; i<5; i++) {
			System.arraycopy(buf[i], 32, buf[i], 0, 6);
			//memcpy(in[i], in[i]+32, 6*sizeof(in[i][0]));
		}
	}

	//splits one subband into 2 sub-subbands with a symmetric real filter
	//the filter must have its non-center even coefficients equal to zero
	private static void performFilter2R(float[][] in, float[][][] out, int outOff, float[] filter, boolean reverse) {
		int i, j;
		int inOff = 0;
		for(i = 0; i<FB_LEN; i++, inOff++) {
			float re_in = filter[6]*in[inOff+6][0];          //real inphase
			float re_op = 0.0f;                          //real out of phase
			float im_in = filter[6]*in[inOff+6][1];          //imag inphase
			float im_op = 0.0f;                          //imag out of phase
			for(j = 0; j<6; j += 2) {
				re_op += filter[j+1]*(in[inOff+j+1][0]+in[inOff+12-j-1][0]);
				im_op += filter[j+1]*(in[inOff+j+1][1]+in[inOff+12-j-1][1]);
			}
			out[outOff+(reverse ? 1 : 0)][i][0] = re_in+re_op;
			out[outOff+(reverse ? 1 : 0)][i][1] = im_in+im_op;
			out[outOff+(reverse ? 0 : 1)][i][0] = re_in-re_op;
			out[outOff+(reverse ? 0 : 1)][i][1] = im_in-im_op;
		}
	}

	//splits one subband into 6 sub-subbands with a complex filter
	private static void performFilter6C(float[][] in, float[][][] out, int outOff, float[][][] filter) {
		final int N = 8;
		final float[][] temp = new float[8][2];

		int i, j, ssb;
		int inOff = 0;
		for(i = 0; i<FB_LEN; i++, inOff++) {
			for(ssb = 0; ssb<N; ssb++) {
				float sum_re = filter[ssb][6][0]*in[inOff+6][0], sum_im = filter[ssb][6][0]*in[inOff+6][1];
				for(j = 0; j<6; j++) {
					float in0_re = in[inOff+j][0];
					float in0_im = in[inOff+j][1];
					float in1_re = in[inOff+12-j][0];
					float in1_im = in[inOff+12-j][1];
					sum_re += filter[ssb][j][0]*(in0_re+in1_re)-filter[ssb][j][1]*(in0_im-in1_im);
					sum_im += filter[ssb][j][0]*(in0_im+in1_im)+filter[ssb][j][1]*(in0_re-in1_re);
				}
				temp[ssb][0] = sum_re;
				temp[ssb][1] = sum_im;
			}
			out[outOff+0][i][0] = temp[6][0];
			out[outOff+0][i][1] = temp[6][1];
			out[outOff+1][i][0] = temp[7][0];
			out[outOff+1][i][1] = temp[7][1];
			out[outOff+2][i][0] = temp[0][0];
			out[outOff+2][i][1] = temp[0][1];
			out[outOff+3][i][0] = temp[1][0];
			out[outOff+3][i][1] = temp[1][1];
			out[outOff+4][i][0] = temp[2][0]+temp[5][0];
			out[outOff+4][i][1] = temp[2][1]+temp[5][1];
			out[outOff+5][i][0] = temp[3][0]+temp[4][0];
			out[outOff+5][i][1] = temp[3][1]+temp[4][1];
		}
	}

	private static void perfomFilter4C(float[][] in, float[][][] out, int outOff, float[][][] filter, int len) {
		int i, j, ssb;
		int inOff = 0;

		for(i = 0; i<FB_LEN; i++, inOff++) {
			for(ssb = 0; ssb<len; ssb++) {
				float sum_re = filter[ssb][6][0]*in[inOff+6][0], sum_im = filter[ssb][6][0]*in[inOff+6][1];
				for(j = 0; j<6; j++) {
					float in0_re = in[inOff+j][0];
					float in0_im = in[inOff+j][1];
					float in1_re = in[inOff+12-j][0];
					float in1_im = in[inOff+12-j][1];
					sum_re += filter[ssb][j][0]*(in0_re+in1_re)-filter[ssb][j][1]*(in0_im-in1_im);
					sum_im += filter[ssb][j][0]*(in0_im+in1_im)+filter[ssb][j][1]*(in0_re-in1_re);
				}
				out[outOff+ssb][i][0] = sum_re;
				out[outOff+ssb][i][1] = sum_im;
			}
		}
	}

	//=============================== synthesis =============================
	//out: [38][64][2], in: [91][32][2]
	static void performSynthesis(float[][][] in, float[][][] out, boolean use34) {
		int i, n;
		if(use34) {
			for(n = 0; n<FB_LEN; n++) {
				for(i = 0; i<5; i++) {
					out[n][i][0] = 0;
					out[n][i][1] = 0;
				}
				for(i = 0; i<12; i++) {
					out[n][0][0] += in[i][n][0];
					out[n][0][1] += in[i][n][1];
				}
				for(i = 0; i<8; i++) {
					out[n][1][0] += in[12+i][n][0];
					out[n][1][1] += in[12+i][n][1];
				}
				for(i = 0; i<4; i++) {
					out[n][2][0] += in[20+i][n][0];
					out[n][2][1] += in[20+i][n][1];
					out[n][3][0] += in[24+i][n][0];
					out[n][3][1] += in[24+i][n][1];
					out[n][4][0] += in[28+i][n][0];
					out[n][4][1] += in[28+i][n][1];
				}
			}
			for(i = 0; i<59; i++) {
				for(n = 0; n<FB_LEN; n++) {
					out[n][i+5][0] = in[i+32][n][0];
					out[n][i+5][1] = in[i+32][n][1];
				}
			}
		}
		else {
			for(n = 0; n<FB_LEN; n++) {
				out[n][0][0] = in[0][n][0]+in[1][n][0]+in[2][n][0]
						+in[3][n][0]+in[4][n][0]+in[5][n][0];
				out[n][0][1] = in[0][n][1]+in[1][n][1]+in[2][n][1]
						+in[3][n][1]+in[4][n][1]+in[5][n][1];
				out[n][1][0] = in[6][n][0]+in[7][n][0];
				out[n][1][1] = in[6][n][1]+in[7][n][1];
				out[n][2][0] = in[8][n][0]+in[9][n][0];
				out[n][2][1] = in[8][n][1]+in[9][n][1];
			}
			for(i = 0; i<61; i++) {
				for(n = 0; n<FB_LEN; n++) {
					out[n][i+3][0] = in[i+10][n][0];
					out[n][i+3][1] = in[i+10][n][1];
				}
			}
		}
	}
}
