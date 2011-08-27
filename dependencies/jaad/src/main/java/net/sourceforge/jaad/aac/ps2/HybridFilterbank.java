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
package net.sourceforge.jaad.aac.ps2;

//hybrid analysis filterbank: splits lower frequency bands
class HybridFilterbank implements FilterbankTables {

	//in: 64 x 38 complex, out: 91 x 32 complex
	public static void analyze(float[][][] in, float[][][] out, boolean use34) {
		if(use34) {
			splitBands4(in[0], out, 0, FILTER_34_12, 12);
			splitBands4(in[1], out, 12, FILTER_34_8, 8);
			splitBands4(in[2], out, 20, FILTER_34_4, 4);
			splitBands4(in[3], out, 24, FILTER_34_4, 4);
			splitBands4(in[4], out, 28, FILTER_34_4, 4);
		}
		else {
			splitBands6(in[0], out, 0);
			splitBands2(in[1], out, 6, true);
			splitBands2(in[2], out, 8, false);
		}
	}

	//type B filtering for 2 bands; in: 38 complex, out:32 complex
	private static void splitBands2(float[][] in, float[][][] out, int outOff, boolean reverse) {
		final float[] tmp1 = new float[2];
		final float[] tmp2 = new float[2];
		int i, j;

		for(i = 0; i<32; i++) {
			tmp1[0] = FILTER_20_2[6]*in[i+6][0];
			tmp1[1] = FILTER_20_2[6]*in[i+6][1];
			tmp2[0] = 0.0f;
			tmp2[1] = 0.0f;
			for(j = 0; j<6; j += 2) {
				tmp2[0] += FILTER_20_2[j+1]*(in[i+j+1][0]+in[i+12-j-1][0]);
				tmp2[1] += FILTER_20_2[j+1]*(in[i+j+1][1]+in[i+12-j-1][1]);
			}
			if(reverse) {
				out[outOff+1][i][0] = tmp1[0]+tmp2[0];
				out[outOff+1][i][1] = tmp1[1]+tmp2[1];
				out[outOff][i][0] = tmp1[0]-tmp2[0];
				out[outOff][i][1] = tmp1[1]-tmp2[1];
			}
			else {
				out[outOff][i][0] = tmp1[0]+tmp2[0];
				out[outOff][i][1] = tmp1[1]+tmp2[1];
				out[outOff+1][i][0] = tmp1[0]-tmp2[0];
				out[outOff+1][i][1] = tmp1[1]-tmp2[1];
			}
		}
	}

	//type A filtering for 8 bands with summation; in: 38 complex, out:32 complex
	private static void splitBands6(float[][] in, float[][][] out, int outOff) {
		final float[][] tmp = new float[8][2];
		final float[] sum = new float[2];
		int i, j, k;

		for(i = 0; i<32; i++) {
			for(k = 0; k<8; k++) {
				sum[0] = FILTER_20_8[k][6][0]*in[i+6][0];
				sum[1] = FILTER_20_8[k][6][0]*in[i+6][1];
				for(j = 0; j<6; j++) {
					sum[0] += FILTER_20_8[k][j][0]*(in[i+j][0]+in[i+12-j][0])
							-FILTER_20_8[k][j][1]*(in[i+j][1]-in[i+12-j][1]);
					sum[1] += FILTER_20_8[k][j][0]*(in[i+j][1]+in[i+12-j][1])
							+FILTER_20_8[k][j][1]*(in[i+j][0]-in[i+12-j][0]);
				}
				tmp[k][0] = sum[0];
				tmp[k][1] = sum[1];
			}
			out[outOff+0][i][0] = tmp[6][0];
			out[outOff+0][i][1] = tmp[6][1];
			out[outOff+1][i][0] = tmp[7][0];
			out[outOff+1][i][1] = tmp[7][1];
			out[outOff+2][i][0] = tmp[0][0];
			out[outOff+2][i][1] = tmp[0][1];
			out[outOff+3][i][0] = tmp[1][0];
			out[outOff+3][i][1] = tmp[1][1];
			out[outOff+4][i][0] = tmp[2][0]+tmp[5][0];
			out[outOff+4][i][1] = tmp[2][1]+tmp[5][1];
			out[outOff+5][i][0] = tmp[3][0]+tmp[4][0];
			out[outOff+5][i][1] = tmp[3][1]+tmp[4][1];
		}
	}

	//type A filtering for 4/8/12 bands; in: 38 complex, out:32 complex
	private static void splitBands4(float[][] in, float[][][] out, int outOff, float[][][] filter, int len) {
		final float[] sum = new float[2];
		int i, j, k;

		for(i = 0; i<len; i++) {
			for(k = 0; k<len; k++) {
				sum[0] = filter[k][6][0]*in[i+6][0];
				sum[1] = filter[k][6][0]*in[i+6][1];
				for(j = 0; j<6; j++) {
					sum[0] += filter[k][j][0]*(in[i+j][0]+in[i+12-j][0])
							-filter[k][j][1]*(in[i+j][1]-in[i+12-j][1]);
					sum[1] += filter[k][j][0]*(in[i+j][1]+in[i+12-j][1])
							+filter[k][j][1]*(in[i+j][0]-in[i+12-j][0]);
				}
				out[outOff+k][i][0] = sum[0];
				out[outOff+k][i][1] = sum[1];
			}
		}
	}

	//in: 91 x 32 complex, out: 64 x 32 complex for SBR
	public static void synthesize(float[][][] in, float[][][] out, boolean use34) {
		int n, k;
		if(use34) {
			for(n = 0; n<32; n++) {
				//sum first 32 into 5
				for(k = 0; k<5; k++) {
					out[k][n][0] = 0;
					out[k][n][1] = 0;
				}
				for(k = 0; k<12; k++) {
					out[0][n][0] += in[k][n][0];
					out[0][n][1] += in[k][n][1];
				}
				for(k = 12; k<19; k++) {
					out[1][n][0] += in[k][n][0];
					out[1][n][1] += in[k][n][1];
				}
				for(k = 20; k<24; k++) {
					out[2][n][0] += in[k][n][0];
					out[2][n][1] += in[k][n][1];
					out[3][n][0] += in[k+4][n][0];
					out[3][n][1] += in[k+4][n][1];
					out[4][n][0] += in[k+8][n][0];
					out[4][n][1] += in[k+8][n][1];
				}
				//copy remaining 59
				for(k = 0; k<59; k++) {
					out[k+5][n][0] = in[k+32][n][0];
					out[k+5][n][1] = in[k+32][n][1];
				}
			}
		}
		else {
			for(n = 0; n<32; n++) {
				//sum first 10 into 3
				out[0][n][0] = in[0][n][0]+in[1][n][0]+in[2][n][0]
						+in[3][n][0]+in[4][n][0]+in[5][n][0];
				out[0][n][1] = in[0][n][1]+in[1][n][1]+in[2][n][1]
						+in[3][n][1]+in[4][n][1]+in[5][n][1];
				out[1][n][0] = in[6][n][0]+in[7][n][0];
				out[1][n][1] = in[6][n][1]+in[7][n][1];
				out[2][n][0] = in[8][n][0]+in[9][n][0];
				out[2][n][1] = in[8][n][1]+in[9][n][1];
				//copy remaining 61
				for(k = 0; k<61; k++) {
					out[k+3][n][0] = in[k+10][n][0];
					out[k+3][n][1] = in[k+10][n][1];
				}
			}
		}
	}
}
