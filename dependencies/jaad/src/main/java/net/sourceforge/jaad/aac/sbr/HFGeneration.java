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

class HFGeneration implements SBRConstants {

	private static class ACCoefs {

		float[] r01 = new float[2];
		float[] r02 = new float[2];
		float r11 = 0;
		float[] r12 = new float[2];
		float[] r22 = new float[2];
		float det = 0;
	}
	private static final int[] GOAL_SB_TABLE = {21, 23, 32, 43, 46, 64, 85, 93, 128, 0, 0, 0};
	private static final float[][] BW_VALUES = {
		{0.0f, 0.6f},
		{0.75f, 0.6f},
		{0.9f, 0.9f},
		{0.98f, 0.98f}
	};
	private static final float BW_MIN = 0.015625f;
	private static final float BW_MAX = 0.99609375f;
	private static final float[] CHIRP_COEFS = {0.75f, 0.25f, 0.90625f, 0.09375f};
	private static final float AC_REL = 0.9999990000010001f; //1/(1+1e-6);
	private final SBR sbr;
	private final float[][] alpha0, alpha1;
	private final float[] a0, a1;
	private final ACCoefs ac;

	HFGeneration(SBR sbr) {
		this.sbr = sbr;
		alpha0 = new float[64][2];
		alpha1 = new float[64][2];
		a0 = new float[2];
		a1 = new float[2];
		ac = new ACCoefs();
	}

	void process(float[][][] Xlow, float[][][] Xhigh, int ch, ChannelData cd) {
		final int offset = T_HFADJ;
		final int first = cd.t_E[0];
		final int last = cd.t_E[cd.L_E];

		calculateChirpFactors(cd);

		if(ch==0&&sbr.reset) constructPatches();

		//actual HF generation
		int j, l, off, k, g;
		float bw, bw2;
		for(int i = 0; i<sbr.patches; i++) {
			for(j = 0; j<sbr.patchNoSubbands[i]; j++) {
				//find the low and high band for patching
				k = sbr.kx+j;
				for(l = 0; l<i; l++) {
					k += sbr.patchNoSubbands[l];
				}
				off = sbr.patchStartSubband[i]+j;

				g = sbr.tableMapKToG[k];

				bw = cd.bwArray[g];
				bw2 = bw*bw;

				//do the patching with or without filtering
				if(bw2>0) {
					float temp1_r, temp2_r, temp3_r;
					float temp1_i, temp2_i, temp3_i;
					calculatePredictionCoef(Xlow, off);

					a0[0] = alpha0[off][0]*bw;
					a1[0] = alpha1[off][0]*bw2;
					a0[1] = alpha0[off][1]*bw;
					a1[1] = alpha1[off][1]*bw2;

					temp2_r = Xlow[first-2+offset][off][0];
					temp3_r = Xlow[first-1+offset][off][0];
					temp2_i = Xlow[first-2+offset][off][1];
					temp3_i = Xlow[first-1+offset][off][1];
					for(l = first; l<last; l++) {
						temp1_r = temp2_r;
						temp2_r = temp3_r;
						temp3_r = Xlow[l+offset][off][0];
						temp1_i = temp2_i;
						temp2_i = temp3_i;
						temp3_i = Xlow[l+offset][off][1];

						Xhigh[l+offset][k][0] = temp3_r
								+((a0[0]*temp2_r)-(a0[1]*temp2_i)
								+(a1[0]*temp1_r)-(a1[1]*temp1_i));
						Xhigh[l+offset][k][1] = temp3_i
								+((a0[1]*temp2_r)+(a0[0]*temp2_i)
								+(a1[1]*temp1_r)+(a1[0]*temp1_i));
					}
				}
				else {
					for(l = first; l<last; l++) {
						Xhigh[l+offset][k][0] = Xlow[l+offset][off][0];
						Xhigh[l+offset][k][1] = Xlow[l+offset][off][1];
					}
				}
			}
		}

		if(sbr.reset) sbr.calculateLimiterFrequencyTable();
	}

	private void calculateChirpFactors(ChannelData cd) {
		int off;
		for(int i = 0; i<sbr.N_Q; i++) {
			cd.bwArray[i] = getBW(cd.invfMode[i], cd.invfModePrev[i]);

			off = (cd.bwArray[i]<cd.bwArrayPrev[i]) ? 0 : 2;
			cd.bwArray[i] = (cd.bwArray[i]*CHIRP_COEFS[off])+(cd.bwArrayPrev[i]*CHIRP_COEFS[off+1]);

			if(cd.bwArray[i]<BW_MIN) cd.bwArray[i] = 0.0f;
			if(cd.bwArray[i]>=BW_MAX) cd.bwArray[i] = BW_MAX;

			cd.bwArrayPrev[i] = cd.bwArray[i];
			cd.invfModePrev[i] = cd.invfMode[i];
		}
	}

	private float getBW(int invfMode, int invfModePrev) {
		int sec;
		if(invfMode==0) sec = invfModePrev==1 ? 1 : 0;
		else if(invfMode==1) sec = invfModePrev==0 ? 1 : 0;
		else sec = 0;
		return BW_VALUES[invfMode][sec];
	}

	private void constructPatches() {
		final int goalSb = GOAL_SB_TABLE[Calculation.getSampleRateIndex(sbr.sampleRate)]; //(2.048e6/sbr.sample_rate + 0.5);
		sbr.patches = 0;

		int k;
		if(goalSb<(sbr.kx+sbr.M)) {
			int i;
			for(i = 0, k = 0; sbr.mft[i]<goalSb; i++) {
				k = i+1;
			}
		}
		else k = sbr.N_master;

		if(sbr.N_master==0) {
			sbr.patchNoSubbands[0] = 0;
			sbr.patchStartSubband[0] = 0;
			return;
		}

		int msb = sbr.k0;
		int usb = sbr.kx;
		int j, odd, sb;
		do {
			j = k+1;
			do {
				j--;

				sb = sbr.mft[j];
				odd = (sb-2+sbr.k0)%2;
			}
			while(sb>(sbr.k0-1+msb-odd));

			sbr.patchNoSubbands[sbr.patches] = Math.max(sb-usb, 0);
			sbr.patchStartSubband[sbr.patches] = sbr.k0-odd-sbr.patchNoSubbands[sbr.patches];

			if(sbr.patchNoSubbands[sbr.patches]>0) {
				usb = sb;
				msb = sb;
				sbr.patches++;
			}
			else msb = sbr.kx;

			if(sbr.mft[k]-sb<3) k = sbr.N_master;
		}
		while(sb!=(sbr.kx+sbr.M));

		if((sbr.patchNoSubbands[sbr.patches-1]<3)&&(sbr.patches>1)) sbr.patches--;

		sbr.patches = Math.min(sbr.patches, 5);
	}

	private void calculatePredictionCoef(float[][][] Xlow, int k) {
		float tmp;
		calculateAutoCorrelation(Xlow, k, TIME_SLOTS_RATE+6);

		if(ac.det==0) {
			alpha1[k][0] = 0;
			alpha1[k][1] = 0;
		}
		else {
			tmp = 1.0f/ac.det;
			alpha1[k][0] = ((ac.r01[0]*ac.r12[0])-(ac.r01[1]*ac.r12[1])-(ac.r02[0]*ac.r11))*tmp;
			alpha1[k][1] = ((ac.r01[1]*ac.r12[0])+(ac.r01[0]*ac.r12[1])-(ac.r02[1]*ac.r11))*tmp;
		}

		if(ac.r11==0) {
			alpha0[k][0] = 0;
			alpha0[k][1] = 0;
		}
		else {
			tmp = 1.0f/ac.r11;
			alpha0[k][0] = -(ac.r01[0]+(alpha1[k][0]*ac.r12[0])+(alpha1[k][1]*ac.r12[1]))*tmp;
			alpha0[k][1] = -(ac.r01[1]+(alpha1[k][1]*ac.r12[0])-(alpha1[k][0]*ac.r12[1]))*tmp;
		}

		if(((alpha0[k][0]*alpha0[k][0])+(alpha0[k][1]*alpha0[k][1])>=16)
				||((alpha1[k][0]*alpha1[k][0])+(alpha1[k][1]*alpha1[k][1])>=16)) {
			alpha0[k][0] = 0;
			alpha0[k][1] = 0;
			alpha1[k][0] = 0;
			alpha1[k][1] = 0;
		}
	}

	private ACCoefs calculateAutoCorrelation(float[][][] buffer, int bd, int len) {
		final int offset = T_HFADJ;

		float temp2r = buffer[offset-2][bd][0];
		float temp2i = buffer[offset-2][bd][1];
		float temp3r = buffer[offset-1][bd][0];
		float temp3i = buffer[offset-1][bd][1];
		//save these because they are needed after loop
		final float[] temp4 = {temp2r, temp2i};
		final float[] temp5 = {temp3r, temp3i};

		final float[] r01 = new float[2];
		final float[] r02 = new float[2];
		float r11 = 0;

		final float[] temp1 = new float[2];
		for(int i = offset; i<len+offset; i++) {
			temp1[0] = temp2r;
			temp1[1] = temp2i;
			temp2r = temp3r;
			temp2i = temp3i;
			temp3r = buffer[i][bd][0];
			temp3i = buffer[i][bd][1];
			r01[0] += temp3r*temp2r+temp3i*temp2i;
			r01[1] += temp3i*temp2r-temp3r*temp2i;
			r02[0] += temp3r*temp1[0]+temp3i*temp1[1];
			r02[1] += temp3i*temp1[0]-temp3r*temp1[1];
			r11 += temp2r*temp2r+temp2i*temp2i;
		}

		ac.r12[0] = r01[0]-(temp3r*temp2r+temp3i*temp2i)
				+(temp5[0]*temp4[0]+temp5[1]*temp4[1]);
		ac.r12[1] = r01[1]-(temp3i*temp2r-temp3r*temp2i)
				+(temp5[1]*temp4[0]-temp5[0]*temp4[1]);
		ac.r22[0] = r11-(temp2r*temp2r+temp2i*temp2i)
				+(temp4[0]*temp4[0]+temp4[1]*temp4[1]);

		ac.r01 = r01;
		ac.r02 = r02;
		ac.r11 = r11;
		ac.det = (ac.r11*ac.r22[0])-(AC_REL*((ac.r12[0]*ac.r12[0])+(ac.r12[1]*ac.r12[1])));
		return ac;
	}
}
