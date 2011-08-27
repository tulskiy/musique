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

import net.sourceforge.jaad.aac.AACException;

//TODO: patch and chirp factor calculation are ok, next inverse filtering (p229)
class HFGenerator implements SBRConstants {

	private static final float RELAX_COEF = 1.000001f;
	private static final int ALPHA_MAX = 16;
	private static final float[][] CHIRP_COEFS = {{0.75f, 0.25f}, {0.90625f, 0.09375f}};
	//values for bw [invfModePrev][invfMode]
	private static final float[][] BW_COEFS = {
		{0.0f, 0.6f, 0.9f, 0.98f},
		{0.6f, 0.75f, 0.9f, 0.98f},
		{0.0f, 0.75f, 0.9f, 0.98f},
		{0.0f, 0.75f, 0.9f, 0.98f}
	};
	private static final float CHIRP_MIN = 0.015625f;

	//in: 32x40 complex Xlow, out: 23x40 complex Xhigh
	public static void process(FrequencyTables tables, ChannelData cd, float[][][] Xlow, float[][][] Xhigh) throws AACException {
		//calculate chirp factors
		final float[] bwArray = calculateChirpFactors(tables, cd);

		//calculate inverse filter coefficients for bands 0-k0
		final int k0 = tables.getK0();
		final float[][] alpha0 = new float[k0][2];
		final float[][] alpha1 = new float[k0][2];
		calculateIFCoefs(tables, alpha0, alpha1, Xlow);

		//HF generation
		final int patchCount = tables.getPatchCount();
		final int[] patchSubbands = tables.getPatchSubbands();
		final int[] patchStartSubband = tables.getPatchStartSubband();
		final int kx = tables.getKx(false);
		final int m = tables.getM(false);
		final int Nq = tables.getNq();
		final int[] fNoise = tables.getNoiseTable();

		final int[] te = cd.getTe();
		final int start = RATE*te[0];
		final int end = RATE*te[cd.getEnvCount()];

		final float[] alpha = new float[4];
		float square;
		int l, x; //loop indizes
		int k = kx;
		int g = 0;

		for(int j = 0; j<patchCount; j++) {
			for(x = 0; x<patchSubbands[j]; x++, k++) {
				final int p = patchStartSubband[j]+x;
				while(g<=Nq&&k>=fNoise[g]) {
					g++;
				}
				g--;

				if(g<0) throw new AACException("SBR: HFGenerator: no subband found for frequency "+k);

				//fill Xhigh[k] (4.6.18.6.3)
				square = bwArray[g]*bwArray[g];
				alpha[0] = alpha1[p][0]*square;
				alpha[1] = alpha1[p][1]*square;
				alpha[2] = alpha0[p][0]*bwArray[g];
				alpha[3] = alpha0[p][1]*bwArray[g];
				for(l = start; l<end; l++) {
					final int off = l+T_HF_ADJ;
					Xhigh[k][off][0] = alpha[0]*Xlow[p][off-2][0]
							-alpha[1]*Xlow[p][off-2][1]
							+alpha[2]*Xlow[p][off-1][0]
							-alpha[3]*Xlow[p][off-1][1]
							+Xlow[p][off][0];
					Xhigh[k][off][1] = alpha[0]*Xlow[p][off-2][1]
							+alpha[1]*Xlow[p][off-2][0]
							+alpha[2]*Xlow[p][off-1][1]
							+alpha[3]*Xlow[p][off-1][0]
							+Xlow[p][off][1];
				}
			}
		}

		//fill remaining with zero
		while(k<m+kx) {
			for(int j = 0; j<Xhigh[k].length; j++) {
				Xhigh[k][j][0] = 0;
				Xhigh[k][j][1] = 0;
			}
			k++;
		}
	}

	private static float[] calculateChirpFactors(FrequencyTables tables, ChannelData cd) {
		//calculates chirp factors and replaces old ones in ChannelData
		final int nq = tables.getNq();
		final int[] invfMode = cd.getInvfMode(false);
		final int[] invfModePrevious = cd.getInvfMode(true);
		final float[] bwArray = cd.getChirpFactors();

		float tmp;
		float[] chirpCoefs;
		for(int i = 0; i<nq; i++) {
			tmp = BW_COEFS[invfModePrevious[i]][invfMode[i]];
			chirpCoefs = (tmp<bwArray[i]) ? CHIRP_COEFS[0] : CHIRP_COEFS[1];
			bwArray[i] = (chirpCoefs[0]*tmp)+(chirpCoefs[1]*bwArray[i]);
			if(bwArray[i]<CHIRP_MIN) bwArray[i] = 0;
		}

		return bwArray;
	}

	//calculates inverse filter coefficients for bands 0-k0 (4.6.18.6.2)
	private static void calculateIFCoefs(FrequencyTables tables, float[][] alpha0, float[][] alpha1, float[][][] Xlow) {
		final int k0 = tables.getK0();
		final float[] tmp = new float[2];

		float[][][] phi;
		float d;
		for(int k = 0; k<k0; k++) {
			//get covariance matrix
			phi = new float[3][2][2];
			getCovarianceMatrix(Xlow[k], phi, 0);
			getCovarianceMatrix(Xlow[k], phi, 1);
			getCovarianceMatrix(Xlow[k], phi, 2);

			//d(k)
			d = phi[2][1][0]*phi[1][0][0]-(phi[1][1][0]*phi[1][1][0]+phi[1][1][1]*phi[1][1][1])/RELAX_COEF;

			//alpha1
			if(d==0) {
				alpha1[k][0] = 0;
				alpha1[k][1] = 0;
			}
			else {
				tmp[0] = phi[0][0][0]*phi[1][1][0]-phi[0][0][1]*phi[1][1][1]-phi[0][1][0]*phi[1][0][0];
				tmp[1] = phi[0][0][0]*phi[1][1][1]+phi[0][0][1]*phi[1][1][0]-phi[0][1][1]*phi[1][0][0];
				alpha1[k][0] = tmp[0]/d;
				alpha1[k][1] = tmp[1]/d;
			}

			//alpha0
			if(phi[1][0][0]==0) {
				alpha0[k][0] = 0;
				alpha0[k][1] = 0;
			}
			else {
				tmp[0] = phi[0][0][0]+alpha1[k][0]*phi[1][1][0]+alpha1[k][1]*phi[1][1][1];
				tmp[1] = phi[0][0][1]+alpha1[k][1]*phi[1][1][0]-alpha1[k][0]*phi[1][1][1];
				alpha0[k][0] = -tmp[0]/phi[1][0][0];
				alpha0[k][1] = -tmp[1]/phi[1][0][0];
			}

			if(alpha1[k][0]*alpha1[k][0]+alpha1[k][1]*alpha1[k][1]>=ALPHA_MAX
					||alpha0[k][0]*alpha0[k][0]+alpha0[k][1]*alpha0[k][1]>=ALPHA_MAX) {
				alpha1[k][0] = 0;
				alpha1[k][1] = 0;
				alpha0[k][0] = 0;
				alpha0[k][1] = 0;
			}
		}
	}

	//calculates covariance matrix (4.6.18.6.2)
	private static void getCovarianceMatrix(float[][] x, float[][][] phi, int off) {
		final float[] sum = new float[2];
		if(off==0) {
			for(int i = 1; i<38; i++) {
				sum[0] += x[i][0]*x[i][0]+x[i][1]*x[i][1];
			}
			phi[2][1][0] = sum[0]+x[0][0]*x[0][0]+x[0][1]*x[0][1];
			phi[1][0][0] = sum[0]+x[38][0]*x[38][0]+x[38][1]*x[38][1];
		}
		else {
			for(int i = 1; i<38; i++) {
				sum[0] += x[i][0]*x[i+off][0]+x[i][1]*x[i+off][1];
				sum[1] += x[i][0]*x[i+off][1]-x[i][1]*x[i+off][0];
			}
			phi[2-off][1][0] = sum[0]+x[0][0]*x[off][0]+x[0][1]*x[off][1];
			phi[2-off][1][1] = sum[1]+x[0][0]*x[off][1]-x[0][1]*x[off][0];
			if(off==1) {
				phi[0][0][0] = sum[0]+x[38][0]*x[39][0]+x[38][1]*x[39][1];
				phi[0][0][1] = sum[1]+x[38][0]*x[39][1]-x[38][1]*x[39][0];
			}
		}
	}
}
