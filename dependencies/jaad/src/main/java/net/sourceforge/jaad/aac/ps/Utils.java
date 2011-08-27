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

//mapping methods
class Utils {

	static void remap34(int[][] out, int[][] in, int parCount, int envCount, boolean full) {
		int e;
		if(parCount==20||parCount==11) {
			for(e = 0; e<envCount; e++) {
				map20To34(out[e], in[e], full);
			}
		}
		else if(parCount==10||parCount==5) {
			for(e = 0; e<envCount; e++) {
				map10To34(out[e], in[e], full);
			}
		}
		else {
			//copy
			for(e = 0; e<in.length; e++) {
				System.arraycopy(in[e], 0, out[e], 0, in[e].length);
			}
		}
	}

	static void remap20(int[][] out, int[][] in, int parCount, int envCount, boolean full) {
		int e;
		if(parCount==34||parCount==17) {
			for(e = 0; e<envCount; e++) {
				map34To20(out[e], in[e], full);
			}
		}
		else if(parCount==10||parCount==5) {
			for(e = 0; e<envCount; e++) {
				map10To20(out[e], in[e], full);
			}
		}
		else {
			//copy
			for(e = 0; e<in.length; e++) {
				System.arraycopy(in[e], 0, out[e], 0, in[e].length);
			}
		}
	}

	private static void map20To34(int[] out, int[] in, boolean full) {
		if(full) {
			out[33] = in[19];
			out[32] = in[19];
			out[31] = in[18];
			out[30] = in[18];
			out[29] = in[18];
			out[28] = in[18];
			out[27] = in[17];
			out[26] = in[17];
			out[25] = in[16];
			out[24] = in[16];
			out[23] = in[15];
			out[22] = in[15];
			out[21] = in[14];
			out[20] = in[14];
			out[19] = in[13];
			out[18] = in[12];
			out[17] = in[11];
		}
		out[16] = in[10];
		out[15] = in[9];
		out[14] = in[9];
		out[13] = in[8];
		out[12] = in[8];
		out[11] = in[7];
		out[10] = in[6];
		out[9] = in[5];
		out[8] = in[5];
		out[7] = in[4];
		out[6] = in[4];
		out[5] = in[3];
		out[4] = (in[2]+in[3])/2;
		out[3] = in[2];
		out[2] = in[1];
		out[1] = (in[0]+in[1])/2;
		out[0] = in[0];
	}

	private static void map10To34(int[] out, int[] in, boolean full) {
		if(full) {
			out[33] = in[9];
			out[32] = in[9];
			out[31] = in[9];
			out[30] = in[9];
			out[29] = in[9];
			out[28] = in[9];
			out[27] = in[8];
			out[26] = in[8];
			out[25] = in[8];
			out[24] = in[8];
			out[23] = in[7];
			out[22] = in[7];
			out[21] = in[7];
			out[20] = in[7];
			out[19] = in[6];
			out[18] = in[6];
			out[17] = in[5];
			out[16] = in[5];
		}
		else out[16] = 0;
		out[15] = in[4];
		out[14] = in[4];
		out[13] = in[4];
		out[12] = in[4];
		out[11] = in[3];
		out[10] = in[3];
		out[9] = in[2];
		out[8] = in[2];
		out[7] = in[2];
		out[6] = in[2];
		out[5] = in[1];
		out[4] = in[1];
		out[3] = in[1];
		out[2] = in[0];
		out[1] = in[0];
		out[0] = in[0];
	}

	private static void map34To20(int[] out, int[] in, boolean full) {
		out[0] = (2*in[0]+in[1])/3;
		out[1] = (in[1]+2*in[2])/3;
		out[2] = (2*in[ 3]+in[4])/3;
		out[3] = (in[4]+2*in[5])/3;
		out[4] = (in[6]+in[7])/2;
		out[5] = (in[8]+in[9])/2;
		out[6] = in[10];
		out[7] = in[11];
		out[8] = (in[12]+in[13])/2;
		out[9] = (in[14]+in[15])/2;
		out[10] = in[16];
		if(full) {
			out[11] = in[17];
			out[12] = in[18];
			out[13] = in[19];
			out[14] = (in[20]+in[21])/2;
			out[15] = (in[22]+in[23])/2;
			out[16] = (in[24]+in[25])/2;
			out[17] = (in[26]+in[27])/2;
			out[18] = (in[28]+in[29]+in[30]+in[31])/4;
			out[19] = (in[32]+in[33])/2;
		}
	}

	private static void map10To20(int[] out, int[] in, boolean full) {
		int b;
		if(full) b = 9;
		else {
			b = 4;
			out[10] = 0;
		}
		while(b>=0) {
			out[2*b+1] = out[2*b] = in[b];
			b--;
		}
	}

	static void map20To34Float(float[] f) {
		f[33] = f[19];
		f[32] = f[19];
		f[31] = f[18];
		f[30] = f[18];
		f[29] = f[18];
		f[28] = f[18];
		f[27] = f[17];
		f[26] = f[17];
		f[25] = f[16];
		f[24] = f[16];
		f[23] = f[15];
		f[22] = f[15];
		f[21] = f[14];
		f[20] = f[14];
		f[19] = f[13];
		f[18] = f[12];
		f[17] = f[11];
		f[16] = f[10];
		f[15] = f[9];
		f[14] = f[9];
		f[13] = f[8];
		f[12] = f[8];
		f[11] = f[7];
		f[10] = f[6];
		f[9] = f[5];
		f[8] = f[5];
		f[7] = f[4];
		f[6] = f[4];
		f[5] = f[3];
		f[4] = (f[2]+f[3])*0.5f;
		f[3] = f[2];
		f[2] = f[1];
		f[1] = (f[0]+f[1])*0.5f;
		f[0] = f[0];
	}

	static void map34To20Float(float[] f) {
		f[0] = (2*f[0]+f[1])*0.33333333f;
		f[1] = (f[1]+2*f[2])*0.33333333f;
		f[2] = (2*f[ 3]+f[4])*0.33333333f;
		f[3] = (f[4]+2*f[5])*0.33333333f;
		f[4] = (f[6]+f[7])*0.5f;
		f[5] = (f[8]+f[9])*0.5f;
		f[6] = f[10];
		f[7] = f[11];
		f[8] = (f[12]+f[13])*0.5f;
		f[9] = (f[14]+f[15])*0.5f;
		f[10] = f[16];
		f[11] = f[17];
		f[12] = f[18];
		f[13] = f[19];
		f[14] = (f[20]+f[21])*0.5f;
		f[15] = (f[22]+f[23])*0.5f;
		f[16] = (f[24]+f[25])*0.5f;
		f[17] = (f[26]+f[27])*0.5f;
		f[18] = (f[28]+f[29]+f[30]+f[31])*0.25f;
		f[19] = (f[32]+f[33])*0.5f;
	}

	static void deltaDecode(int[] pars, int parCount, int[] parsPrev,
			boolean time, boolean enabled,
			int stride, int min, int max) {
		int i;

		if(enabled) {
			if(time) {
				for(i = 0; i<parCount; i++) {
					pars[i] = parsPrev[i*stride]+pars[i];
					pars[i] = Math.min(Math.max(pars[i], min), max);
				}
			}
			else {
				pars[0] = Math.min(Math.max(pars[0], min), max);

				for(i = 1; i<parCount; i++) {
					pars[i] = pars[i-1]+pars[i];
					pars[i] = Math.min(Math.max(pars[i], min), max);
				}
			}
		}
		else {
			for(i = 0; i<parCount; i++) {
				pars[i] = 0;
			}
		}

		//coarse
		if(stride==2) {
			for(i = (parCount<<1)-1; i>0; i--) {
				pars[i] = pars[i>>1];
			}
		}
	}

	static void deltaModuloDecode(int[] pars, int parCount, int[] parsPrev,
			boolean time, boolean enable) {
		int i;

		if(enable) {
			if(time) {
				for(i = 0; i<parCount; i++) {
					pars[i] = parsPrev[i]+pars[i];
					pars[i] &= 7;
				}
			}
			else {
				pars[0] &= 7;

				for(i = 1; i<parCount; i++) {
					pars[i] = pars[i-1]+pars[i];
					pars[i] &= 7;
				}
			}
		}
		else {
			for(i = 0; i<parCount; i++) {
				pars[i] = 0;
			}
		}
	}
}
