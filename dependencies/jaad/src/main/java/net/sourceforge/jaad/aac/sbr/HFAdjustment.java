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

class HFAdjustment implements SBRConstants, NoiseTable {

	private static final float[] LIM_GAIN = {0.5f, 1.0f, 2.0f, 1e10f};
	private static final float EPS = 1e-12f;
	private static final float[] H_SMOOTH = {
		0.03183050093751f, 0.11516383427084f,
		0.21816949906249f, 0.30150283239582f,
		0.33333333333333f
	};
	private static final int[] PHI_REAL = {1, 0, -1, 0};
	private static final int[] PHI_IMAG = {0, 1, 0, -1};
	private static final float G_BOOST_MAX = 2.51188643f; //1.584893192^2
	private static final float MAXIMUM_GAIN = 1e10f;

	private static class AdjustmentParams {

		final float[][] G_lim_boost = new float[MAX_L_E][MAX_M];
		final float[][] Q_M_lim_boost = new float[MAX_L_E][MAX_M];
		final float[][] S_M_boost = new float[MAX_L_E][MAX_M];

		private void reset() {
			int j;
			for(int i = 0; i<MAX_L_E; i++) {
				for(j = 0; j<MAX_M; j++) {
					G_lim_boost[i][j] = 0;
					Q_M_lim_boost[i][j] = 0;
					S_M_boost[i][j] = 0;
				}
			}
		}
	}
	private final SBR sbr;
	private final AdjustmentParams adj;

	HFAdjustment(SBR sbr) {
		this.sbr = sbr;
		adj = new AdjustmentParams();
	}

	void process(float[][][] Xsbr, ChannelData cd, SBRHeader header) {
		if(cd.frameClass==FIXFIX) cd.l_A = -1;
		else if(cd.frameClass==VARFIX) cd.l_A = (cd.pointer>1) ? -1 : cd.pointer-1;
		else cd.l_A = (cd.pointer==0) ? -1 : cd.L_E+1-cd.pointer;

		adj.reset(); //TODO: needed?
		estimateCurrentEnvelope(Xsbr, cd, header.hasInterpolFrequency());
		calculateGain(cd, header.getLimiterBands(), header.getLimiterGains());
		assembleHF(Xsbr, cd, header.isSmoothingMode());
	}

	private void estimateCurrentEnvelope(float[][][] Xsbr, ChannelData cd, boolean interpolFrequency) {
		int i, j, k, l, m, curr, next, low, high;
		float nrg, div;

		if(interpolFrequency) {
			for(i = 0; i<cd.L_E; i++) {
				curr = cd.t_E[i];
				next = cd.t_E[i+1];

				div = (float) (next-curr);
				if(div==0) div = 1;

				for(j = 0; j<sbr.M; j++) {
					nrg = 0;

					for(l = curr+T_HFADJ; l<next+T_HFADJ; l++) {
						nrg += (Xsbr[l][j+sbr.kx][0]*Xsbr[l][j+sbr.kx][0])
								+(Xsbr[l][j+sbr.kx][1]*Xsbr[l][j+sbr.kx][1]);
					}

					cd.E_curr[j][i] = nrg/div;
				}
			}
		}
		else {
			for(i = 0; i<cd.L_E; i++) {
				for(j = 0; j<sbr.n[cd.f[i] ? 1 : 0]; j++) {
					low = sbr.ftRes[cd.f[i] ? 1 : 0][j];
					high = sbr.ftRes[cd.f[i] ? 1 : 0][j+1];

					for(k = low; k<high; k++) {
						curr = cd.t_E[i];
						next = cd.t_E[i+1];

						div = (float) ((next-curr)*(high-low));

						if(div==0) div = 1;

						nrg = 0;
						for(l = curr+T_HFADJ; l<next+T_HFADJ; l++) {
							for(m = low; m<high; m++) {
								nrg += (Xsbr[l][m][0]*Xsbr[l][m][0])
										+(Xsbr[l][m][1]*Xsbr[l][m][1]);
							}
						}

						cd.E_curr[k-sbr.kx][i] = nrg/div;
					}
				}
			}
		}
	}

	private void calculateGain(ChannelData cd, int limiterBands, int limiterGains) {
		final float[] qmLim = new float[MAX_M];
		final float[] gLim = new float[MAX_M];
		final float[] sm = new float[MAX_M];

		int j, k;
		int currentTNoiseBand = 0;
		int currFNoiseBand, currResBand, currResBand2, currHiResBand;
		int ml1, ml2;
		boolean sMapped, delta, sIndexMapped;
		float gBoost, gMax, den, acc1, acc2;
		float G, Q_M, Q_div, Q_div2;
		for(int i = 0; i<cd.L_E; i++) {
			currFNoiseBand = 0;
			currResBand = 0;
			currResBand2 = 0;
			currHiResBand = 0;

			delta = i!=cd.l_A&&i!=cd.prevEnvIsShort;

			sMapped = getSMapped(cd, i, currResBand2);

			if(cd.t_E[i+1]>cd.t_Q[currentTNoiseBand+1]) currentTNoiseBand++;

			for(j = 0; j<sbr.N_L[limiterBands]; j++) {
				den = 0;
				acc1 = 0;
				acc2 = 0;

				ml1 = sbr.ftLim[limiterBands][j];
				ml2 = sbr.ftLim[limiterBands][j+1];

				//calculate the accumulated E_orig and E_curr over the limiter band
				for(k = ml1; k<ml2; k++) {
					if((k+sbr.kx)==sbr.ftRes[cd.f[i] ? 1 : 0][currResBand+1]) currResBand++;
					acc1 += cd.E_orig[currResBand][i];
					acc2 += cd.E_curr[k][i];
				}

				//calculate the maximum gain
				gMax = Math.min(MAXIMUM_GAIN, ((EPS+acc1)/(EPS+acc2))*LIM_GAIN[limiterGains]);

				for(k = ml1; k<ml2; k++) {
					//check if m is on a noise band border
					if((k+sbr.kx)==sbr.ftNoise[currFNoiseBand+1]) currFNoiseBand++;

					//check if m is on a resolution band border
					if((k+sbr.kx)==sbr.ftRes[cd.f[i] ? 1 : 0][currResBand2+1]) {
						currResBand2++;
						sMapped = getSMapped(cd, i, currResBand2);
					}

					//check if m is on a HI_RES band border
					if((k+sbr.kx)==sbr.ftRes[HI_RES][currHiResBand+1]) currHiResBand++;

					//find sIndexMapped
					sIndexMapped = false;
					if((i>=cd.l_A)||(cd.addHarmonicPrev[currHiResBand]&&cd.hasHarmonicPrev())
							&&((k+sbr.kx)==(sbr.ftRes[HI_RES][currHiResBand+1]+sbr.ftRes[HI_RES][currHiResBand])>>1)) {
						sIndexMapped = cd.addHarmonic[currHiResBand];
					}

					Q_div = cd.Q_div[currFNoiseBand][currentTNoiseBand];
					Q_div2 = cd.Q_div2[currFNoiseBand][currentTNoiseBand];
					Q_M = cd.E_orig[currResBand2][i]*Q_div2;

					if(sIndexMapped) {
						sm[k] = cd.E_orig[currResBand2][i]*Q_div;
						den += sm[k];
					}
					else sm[k] = 0;

					//calculate gain
					G = cd.E_orig[currResBand2][i]/(1.0f+cd.E_curr[k][i]);
					if((!sMapped)&&delta) G *= Q_div;
					else if(sMapped) G *= Q_div2;

					//limit the additional noise energy level and apply the limiter
					if(gMax>G) {
						qmLim[k] = Q_M;
						gLim[k] = G;
					}
					else {
						qmLim[k] = Q_M*gMax/G;
						gLim[k] = gMax;
					}

					//accumulate the total energy
					den += cd.E_curr[k][i]*gLim[k];
					if((!sIndexMapped)&&(i!=cd.l_A)) den += qmLim[k];
				}

				//gBoost: [0..2.51188643]
				gBoost = Math.min((acc1+EPS)/(den+EPS), G_BOOST_MAX);

				//apply compensation to gain, noise floor sf's and sinusoid levels
				for(k = ml1; k<ml2; k++) {
					adj.G_lim_boost[i][k] = (float) Math.sqrt(gLim[k]*gBoost);
					adj.Q_M_lim_boost[i][k] = (float) Math.sqrt(qmLim[k]*gBoost);
					adj.S_M_boost[i][k] = (float) ((sm[k]==0) ? 0 : Math.sqrt(sm[k]*gBoost));
				}
			}
		}
	}

	private boolean getSMapped(ChannelData cd, int l, int currentBand) {
		final boolean previousHarmonic = cd.hasHarmonicPrev();
		if((cd.f[l] ? 1 : 0)==HI_RES) {
			//ftRes[HIGH]: just 1 to 1 mapping from addHarmonic[l][k]
			if((l>=cd.l_A)||(cd.addHarmonicPrev[currentBand]&&previousHarmonic)) {
				return cd.addHarmonic[currentBand];
			}
		}
		else {
			/* ftLow: check if any of the HI_RES bands
			 * within this LO_RES band has bs_add_harmonic[l][k] turned on */

			//find first HI_RES band in current LO_RES band
			final int lb = 2*currentBand-(sbr.N_high&1);
			//find first HI_RES band in next LO_RES band
			final int ub = 2*(currentBand+1)-(sbr.N_high&1);

			//check all HI_RES bands in current LO_RES band for sinusoid
			for(int b = lb; b<ub; b++) {
				if(((l>=cd.l_A)||(cd.addHarmonicPrev[b]&&previousHarmonic))&&cd.addHarmonic[b]) return true;
			}
		}

		return false;
	}

	private void assembleHF(float[][][] Xsbr, ChannelData cd, boolean smoothingMode) {
		boolean reset = sbr.reset;
		int fIndexNoise = sbr.reset ? 0 : cd.indexNoisePrev;
		int fIndexSine = cd.psiIsPrev;

		int j, k, l, h_SL, ri, rev;
		boolean noNoise;
		float gFilt, qFilt, currHSmooth;
		for(int i = 0; i<cd.L_E; i++) {
			noNoise = (i==cd.l_A||i==cd.prevEnvIsShort);
			h_SL = noNoise ? 0 : ((smoothingMode) ? 0 : 4);

			if(reset) {
				for(l = 0; l<4; l++) {
					System.arraycopy(adj.G_lim_boost[i], 0, cd.gTempPrev[l], 0, sbr.M);
					System.arraycopy(adj.Q_M_lim_boost[i], 0, cd.qTempPrev[l], 0, sbr.M);
				}
				//reset ringbuffer index
				cd.gqIndex = 4;
				reset = false;
			}

			for(j = cd.t_E[i]; j<cd.t_E[i+1]; j++) {
				//load new values into ringbuffer
				System.arraycopy(adj.G_lim_boost[i], 0, cd.gTempPrev[cd.gqIndex], 0, sbr.M);
				System.arraycopy(adj.Q_M_lim_boost[i], 0, cd.qTempPrev[cd.gqIndex], 0, sbr.M);

				for(k = 0; k<sbr.M; k++) {
					gFilt = 0;
					qFilt = 0;

					if(h_SL==0) {
						gFilt = cd.gTempPrev[cd.gqIndex][k];
						qFilt = cd.qTempPrev[cd.gqIndex][k];
					}
					else {
						ri = cd.gqIndex;
						for(l = 0; l<=4; l++) {
							currHSmooth = H_SMOOTH[l];
							ri++;
							if(ri>=5) ri -= 5;
							gFilt += cd.gTempPrev[ri][k]*currHSmooth;
							qFilt += cd.qTempPrev[ri][k]*currHSmooth;
						}
					}

					qFilt = (adj.S_M_boost[i][k]!=0||noNoise) ? 0 : qFilt;

					//add noise to the output
					fIndexNoise = (fIndexNoise+1)&511;

					//the smoothed gain values are applied to Xsbr
					Xsbr[j+T_HFADJ][k+sbr.kx][0] = gFilt*Xsbr[j+T_HFADJ][k+sbr.kx][0]+(qFilt*NOISE_TABLE[fIndexNoise][0]);
					if(sbr.extensionID==3&&sbr.extensionData==42) Xsbr[j+T_HFADJ][k+sbr.kx][0] = 16428320f;
					Xsbr[j+T_HFADJ][k+sbr.kx][1] = (gFilt*Xsbr[j+T_HFADJ][k+sbr.kx][1])+(qFilt*NOISE_TABLE[fIndexNoise][1]);

					rev = ((k+sbr.kx)&1)==1 ? -1 : 1;
					Xsbr[j+T_HFADJ][k+sbr.kx][0] += adj.S_M_boost[i][k]*PHI_REAL[fIndexSine];
					Xsbr[j+T_HFADJ][k+sbr.kx][1] += rev*adj.S_M_boost[i][k]*PHI_IMAG[fIndexSine];
				}

				fIndexSine = (fIndexSine+1)&3;

				cd.gqIndex++;
				if(cd.gqIndex>=5) cd.gqIndex = 0;
			}
		}

		cd.indexNoisePrev = fIndexNoise;
		cd.psiIsPrev = fIndexSine;
	}
}
