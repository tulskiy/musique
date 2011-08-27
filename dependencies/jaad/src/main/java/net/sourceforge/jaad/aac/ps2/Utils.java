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

class Utils {

	public static void map10To34(int[] in, int[] out, boolean full) {
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

	public static void map20To34(int[] in, int[] out, boolean full) {
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

	public static void map10To20(int[] in, int[] out, boolean full) {
		int i;
		if(full) i = 9;
		else {
			i = 4;
			out[10] = 0;
		}
		for(; i>=0; i--) {
			out[2*i+1] = in[i];
			out[2*i] = in[i];
		}
	}

	public static void map34To20(int[] in, int[] out, boolean full) {
		out[0] = (2*in[0]+in[1])/3;
		out[1] = (in[1]+2*in[2])/3;
		out[2] = (2*in[3]+in[4])/3;
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

	public static void map20To34(float[] par) {
		par[33] = par[19];
		par[32] = par[19];
		par[31] = par[18];
		par[30] = par[18];
		par[29] = par[18];
		par[28] = par[18];
		par[27] = par[17];
		par[26] = par[17];
		par[25] = par[16];
		par[24] = par[16];
		par[23] = par[15];
		par[22] = par[15];
		par[21] = par[14];
		par[20] = par[14];
		par[19] = par[13];
		par[18] = par[12];
		par[17] = par[11];
		par[16] = par[10];
		par[15] = par[9];
		par[14] = par[9];
		par[13] = par[8];
		par[12] = par[8];
		par[11] = par[7];
		par[10] = par[6];
		par[9] = par[5];
		par[8] = par[5];
		par[7] = par[4];
		par[6] = par[4];
		par[5] = par[3];
		par[4] = (par[2]+par[3])*0.5f;
		par[3] = par[2];
		par[2] = par[1];
		par[1] = (par[0]+par[1])*0.5f;
		par[0] = par[0];
	}

	public static void map34To20(float[] par) {
		par[0] = (2*par[0]+par[1])*0.33333333f;
		par[1] = (par[1]+2*par[2])*0.33333333f;
		par[2] = (2*par[3]+par[4])*0.33333333f;
		par[3] = (par[4]+2*par[5])*0.33333333f;
		par[4] = (par[6]+par[7])*0.5f;
		par[5] = (par[8]+par[9])*0.5f;
		par[6] = par[10];
		par[7] = par[11];
		par[8] = (par[12]+par[13])*0.5f;
		par[9] = (par[14]+par[15])*0.5f;
		par[10] = par[16];
		par[11] = par[17];
		par[12] = par[18];
		par[13] = par[19];
		par[14] = (par[20]+par[21])*0.5f;
		par[15] = (par[22]+par[23])*0.5f;
		par[16] = (par[24]+par[25])*0.5f;
		par[17] = (par[26]+par[27])*0.5f;
		par[18] = (par[28]+par[29]+par[30]+par[31])*0.25f;
		par[19] = (par[32]+par[33])*0.5f;
	}
}
