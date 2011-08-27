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

import java.util.Arrays;
import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.BitStream;

public class PS implements PSConstants, PSTables, HuffmanTables {

	//generated tables
	private final float[][][] HA, HB;
	private final float[][] PHI_FRACT_20, PHI_FRACT_34;
	private final float[][][] Q_FRACT_ALLPASS_20, Q_FRACT_ALLPASS_34;
	private final float[][] SMOOTHING_TABLE;
	//header
	private boolean headerEnabled;
	private final PSHeader header;
	//bitstream variables
	private boolean frameClass;
	private int envCount, envCountPrev;
	private final int[] borderPositions;
	//pars
	private final int[][] iidPars, iccPars, ipdPars, opdPars;
	private final int[][] iidMapped, iccMapped, ipdMapped, opdMapped;
	private final int[] ipdPrev, opdPrev;
	//working buffer
	private final float[][][] lBuf, rBuf;
	//buffers for decorrelation in z-domain
	private final float[] peakDecayNrg, smoothNrg, smoothPeakDecayDiffNrg;
	private final float[][][] delay;
	private final float[][][][] apDelay;
	//buffers for stereo processing
	private final float[][][] H11, H12, H21, H22;

	public PS() {
		HA = new float[46][8][4];
		HB = new float[46][8][4];
		TableGenerator.generateMixingTables(HA, HB);
		PHI_FRACT_20 = new float[30][2];
		Q_FRACT_ALLPASS_20 = new float[30][3][2];
		TableGenerator.generateFractTables20(PHI_FRACT_20, Q_FRACT_ALLPASS_20);
		PHI_FRACT_34 = new float[50][2];
		Q_FRACT_ALLPASS_34 = new float[50][3][2];
		TableGenerator.generateFractTables34(PHI_FRACT_34, Q_FRACT_ALLPASS_34);
		SMOOTHING_TABLE = new float[8*8*8][2];
		TableGenerator.generateIPDOPDSmoothingTables(SMOOTHING_TABLE);

		headerEnabled = false;
		header = new PSHeader();

		borderPositions = new int[MAX_ENVELOPES];

		iidPars = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		iccPars = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		ipdPars = new int[MAX_ENVELOPES][MAX_IPD_OPD_PARS];
		opdPars = new int[MAX_ENVELOPES][MAX_IPD_OPD_PARS];
		iidMapped = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		iccMapped = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		ipdMapped = new int[MAX_ENVELOPES][MAX_IPD_OPD_PARS];
		opdMapped = new int[MAX_ENVELOPES][MAX_IPD_OPD_PARS];
		ipdPrev = new int[MAX_IPD_OPD_PARS];
		opdPrev = new int[MAX_IPD_OPD_PARS];

		lBuf = new float[91][32][2];
		rBuf = new float[91][32][2];
		peakDecayNrg = new float[MAX_IID_ICC_PARS];
		smoothNrg = new float[MAX_IID_ICC_PARS];
		smoothPeakDecayDiffNrg = new float[MAX_IID_ICC_PARS];
		delay = new float[91][32+MAX_DELAY][2];
		apDelay = new float[50][ALLPASS_LINKS][32+MAX_DELAY][2];

		//complex in first dimension makes mapping easier
		H11 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
		H12 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
		H21 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
		H22 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
	}

	/*========================= decoding =========================*/
	public void decode(BitStream in) throws AACException {
		header.startNewFrame();
		if(headerEnabled = in.readBool()) header.decode(in);

		frameClass = in.readBool();
		//envelopes (table 8.29)
		final int envIdx = in.readBits(2);
		envCountPrev = envCount;
		envCount = envIdx+(frameClass ? 1 : 0);
		if(envIdx==3&&!frameClass) envCount++;
		//if envCount==0: no new parameters, use old ones
		if(envCount==0) envCount = envCountPrev;

		//border positions
		int e;
		if(frameClass) {
			for(e = 0; e<envCount; e++) {
				borderPositions[e] = in.readBits(5);
			}
		}
		else {
			//fixed borders (8.6.4.6.2)
			for(e = 0; e<envCount; e++) {
				borderPositions[e] = (32*(e+1))/envCount-1;
			}
		}

		int len;
		boolean dt;
		int[][] table;
		//iid
		if(header.isIIDEnabled()) {
			len = header.getIIDPars();
			final boolean fine = header.useIIDQuantFine();
			for(e = 0; e<envCount; e++) {
				dt = in.readBool();
				table = dt ? (fine ? HUFF_IID_FINE_DT : HUFF_IID_DEFAULT_DT)
						: (fine ? HUFF_IID_FINE_DF : HUFF_IID_DEFAULT_DF);
				decodePars(in, table, iidPars, e, len, dt, false);
			}
		}

		//icc
		if(header.isICCEnabled()) {
			len = header.getICCPars();
			for(e = 0; e<envCount; e++) {
				dt = in.readBool();
				table = dt ? HUFF_ICC_DT : HUFF_ICC_DF;
				decodePars(in, table, iccPars, e, len, dt, false);
			}
		}

		//extension
		if(header.isExtEnabled()) {
			int left = in.readBits(4);
			if(left==15) left += in.readBits(8);
			left *= 8;

			int id;
			while(left>7) {
				id = in.readBits(2);
				left -= 2;
				left -= decodeExtension(in, id);
			}

			in.skipBits(left);
		}
	}

	private void decodePars(BitStream in, int[][] table, int[][] pars, int env, int len, boolean dt, boolean mod) throws AACException {
		//huffman delta decoding
		if(dt) {
			final int prev = (env>0) ? env-1 : envCountPrev-1;
			for(int i = 0; i<len; i++) {
				pars[env][i] = pars[prev][i]+decodeHuffman(in, table);
				if(mod) pars[env][i] &= 7;
			}
		}
		else {
			pars[env][0] = decodeHuffman(in, table);
			for(int i = 1; i<len; i++) {
				pars[env][i] = pars[env][i-1]+decodeHuffman(in, table);
				if(mod) pars[env][i] &= 7;
			}
		}
	}

	private int decodeHuffman(BitStream in, int[][] table) throws AACException {
		int off = 0;
		int len = table[off][0];
		int cw = in.readBits(len);
		int j;
		while(cw!=table[off][1]) {
			off++;
			j = table[off][0]-len;
			len = table[off][0];
			cw <<= j;
			cw |= in.readBits(j);
		}
		return table[off][2];
	}

	private int decodeExtension(BitStream in, int id) throws AACException {
		final int start = in.getPosition();

		if(id==0) {
			//ipdopd
			final boolean b = in.readBool();
			header.setIPDOPDEnabled(b);
			if(b) {
				final int len = header.getIPDOPDPars();
				boolean dt;
				int[][] table;

				for(int e = 0; e<envCount; e++) {
					dt = in.readBool();
					table = dt ? HUFF_IPD_DT : HUFF_IPD_DF;
					decodePars(in, table, ipdPars, e, len, dt, true);
					//dequant(ipdPars[e], ipd[e], len, dt ? IPD_OPD_QUANT : ICC_QUANT);

					dt = in.readBool();
					table = dt ? HUFF_OPD_DT : HUFF_OPD_DF;
					decodePars(in, table, opdPars, e, len, dt, true);
					//dequant(opdPars[e], opd[e], len, dt ? IPD_OPD_QUANT : ICC_QUANT);
				}
			}
			in.skipBit(); //reserved
		}

		return in.getPosition()-start;
	}

	/*========================= processing =========================*/
	public boolean hasHeader() {
		return headerEnabled;
	}

	//left: 64 x 38 complex in-/output, left/right: 64 x 38 complex output
	public void process(float[][][] left, float[][][] right) {
		//1. hybrid analysis (in -> lBuf)
		HybridFilterbank.analyze(left, lBuf, header.use34Bands(false));

		//2. decorrelation (lBuf -> rBuf)
		decorrelate();

		//3. stereo processing
		performStereoProcessing();

		//4. hybrid synthesis
		HybridFilterbank.synthesize(lBuf, left, header.use34Bands(false));
		HybridFilterbank.synthesize(rBuf, right, header.use34Bands(false));
	}

	private void decorrelate() {
		final boolean use34 = header.use34Bands(false);

		//reset if necessary
		if(header.use34Bands(true)!=use34) {
			Arrays.fill(peakDecayNrg, 0);
			Arrays.fill(smoothNrg, 0);
			Arrays.fill(smoothPeakDecayDiffNrg, 0);
			for(int i = 0; i<delay.length; i++) {
				for(int j = 0; j<delay[i].length; j++) {
					delay[i][j][0] = 0;
					delay[i][j][1] = 0;
				}
			}
		}

		final int mode = header.getBandMode();
		final int[] map = use34 ? K_TO_BK_34 : K_TO_BK_20;
		int k, n, m, b;

		//calculate transients
		final int parBands = PAR_BANDS[mode];
		final float[][] power = new float[parBands][32];
		for(k = 0; k<32; k++) {
			Arrays.fill(power[k], 0);
		}
		for(k = 0; k<parBands; k++) {
			for(n = 0; n<32; n++) {
				b = map[k];
				power[b][n] += lBuf[k][n][0]*lBuf[k][n][0]+lBuf[k][n][1]*lBuf[k][n][1];
			}
		}

		//perform transient detection
		final float[][] gTransientRatio = new float[parBands][32];
		float tmp;
		for(k = 0; k<parBands; k++) {
			peakDecayNrg[k] = power[k][0];
			for(n = 0; n<32; n++) {
				peakDecayNrg[k] *= PEAK_DECAY_FACTOR;
				peakDecayNrg[k] = Math.max(peakDecayNrg[k], power[k][n]);
				smoothNrg[k] += A_SMOOTH*(power[k][n]-smoothNrg[k]);
				smoothPeakDecayDiffNrg[k] += A_SMOOTH*(peakDecayNrg[k]-power[k][n]-smoothPeakDecayDiffNrg[k]);
				tmp = GAMMA*smoothPeakDecayDiffNrg[k];
				gTransientRatio[k][n] = (tmp>smoothNrg[k]) ? (smoothNrg[k]/tmp) : 1.0f;
			}
		}
		final float[][] transientGain = new float[parBands][32];
		for(k = 0; k<parBands; k++) {
			for(n = 0; n<32; n++) {
				transientGain[k][n] = gTransientRatio[map[k]][n];
			}
		}

		//calculate transfer function and apply transient reduction
		final int allpassBands = ALLPASS_BANDS[mode];

		final float[][] phiFract = use34 ? PHI_FRACT_34 : PHI_FRACT_20;
		final float[][][] qFract = use34 ? Q_FRACT_ALLPASS_34 : Q_FRACT_ALLPASS_20;
		final float[][][] H = new float[allpassBands][32][2];

		final float[] ag = new float[ALLPASS_LINKS];
		float gDecaySlope, re, im;
		for(k = 0; k<allpassBands; k++) {
			b = map[k];
			gDecaySlope = (k>DECAY_CUTOFF[mode]) ? Math.max(0, 1-DECAY_SLOPE*(k-DECAY_CUTOFF[mode])) : 1;
			for(m = 0; m<ALLPASS_LINKS; m++) {
				for(n = 0; n<5; n++) {
					apDelay[k][m][n][0] = apDelay[k][m][32][0];
					apDelay[k][m][n][1] = apDelay[k][m][32][1];
				}
				ag[m] = FILTER_COEFFICIENTS[m]*gDecaySlope;
			}

			addNewSamples(k);

			for(n = 0; n<32; n++) {
				re = delay[k][n+MAX_DELAY-2][0]*phiFract[k][0]
						-delay[k][n+MAX_DELAY-2][1]*phiFract[k][1];
				im = delay[k][n+MAX_DELAY-2][0]*phiFract[k][1]
						+delay[k][n+MAX_DELAY-2][1]*phiFract[k][0];
				for(m = 0; m<ALLPASS_LINKS; m++) {
					float a_re = ag[m]*re;
					float a_im = ag[m]*im;
					float link_delay_re = apDelay[k][m][n+5-LINK_DELAY[m]][0];
					float link_delay_im = apDelay[k][m][n+5-LINK_DELAY[m]][1];
					float fractional_delay_re = qFract[k][m][0];
					float fractional_delay_im = qFract[k][m][1];
					apDelay[k][m][n+5][0] = re;
					apDelay[k][m][n+5][1] = im;
					re = link_delay_re*fractional_delay_re-link_delay_im*fractional_delay_im-a_re;
					im = link_delay_re*fractional_delay_im+link_delay_im*fractional_delay_re-a_im;
					apDelay[k][m][n+5][0] += ag[m]*re;
					apDelay[k][m][n+5][1] += ag[m]*im;
				}
				rBuf[k][n][0] = transientGain[b][n]*re;
				rBuf[k][n][1] = transientGain[b][n]*im;
			}
		}
		for(k = allpassBands; k<SHORT_DELAY_BANDS[mode]; k++) {
			addNewSamples(k);
			for(n = 0; n<32; n++) {
				rBuf[k][n][0] = transientGain[map[k]][n]*delay[k][n+MAX_DELAY-14][0];
				rBuf[k][n][1] = transientGain[map[k]][n]*delay[k][n+MAX_DELAY-14][1];
			}
		}
		for(k = SHORT_DELAY_BANDS[mode]; k<BANDS[mode]; k++) {
			addNewSamples(k);
			for(n = 0; n<32; n++) {
				//H = delay 1
				rBuf[k][n][0] = transientGain[map[k]][n]*delay[k][n+MAX_DELAY-1][0];
				rBuf[k][n][1] = transientGain[map[k]][n]*delay[k][n+MAX_DELAY-1][1];
			}
		}
	}

	//helper method for decorrelation
	private void addNewSamples(int k) {
		//copy previous
		for(int n = 0; n<MAX_DELAY; n++) {
			delay[k][n][0] = delay[k][n+32][0];
			delay[k][n][1] = delay[k][n+32][1];
		}
		//add new
		for(int n = 0; n<32; n++) {
			delay[k][n+MAX_DELAY][0] = lBuf[k][n][0];
			delay[k][n+MAX_DELAY][1] = lBuf[k][n][1];
		}
	}

	private void performStereoProcessing() {
		//remapping
		mapPars(iidPars, iidMapped, header.getIIDPars(), true);
		mapPars(iccPars, iccMapped, header.getICCPars(), true);
		if(header.isIPDOPDEnabled()) {
			final int pars = header.getIPDOPDPars();
			mapPars(ipdPars, ipdMapped, pars, true);
			mapPars(opdPars, opdMapped, pars, true);
		}

		System.arraycopy(H11[0][envCountPrev], 0, H11[0][0], 0, 34);
		System.arraycopy(H11[1][envCountPrev], 0, H11[0][0], 0, 34);
		System.arraycopy(H12[0][envCountPrev], 0, H12[0][0], 0, 34);
		System.arraycopy(H12[1][envCountPrev], 0, H12[0][0], 0, 34);
		System.arraycopy(H21[0][envCountPrev], 0, H21[0][0], 0, 34);
		System.arraycopy(H21[1][envCountPrev], 0, H21[0][0], 0, 34);
		System.arraycopy(H22[0][envCountPrev], 0, H22[0][0], 0, 34);
		System.arraycopy(H22[1][envCountPrev], 0, H22[0][0], 0, 34);
		final boolean use34 = header.use34Bands(false), use34Prev = header.use34Bands(true);
		if(use34&&!use34Prev) {
			Utils.map20To34(H11[0][0]);
			Utils.map20To34(H11[1][0]);
			Utils.map20To34(H12[0][0]);
			Utils.map20To34(H12[1][0]);
			Utils.map20To34(H21[0][0]);
			Utils.map20To34(H21[1][0]);
			Utils.map20To34(H22[0][0]);
			Utils.map20To34(H22[1][0]);
			Arrays.fill(ipdPrev, 0);
			Arrays.fill(opdPrev, 0);
		}
		else if(!use34&&use34Prev) {
			Utils.map34To20(H11[0][0]);
			Utils.map34To20(H11[1][0]);
			Utils.map34To20(H12[0][0]);
			Utils.map34To20(H12[1][0]);
			Utils.map34To20(H21[0][0]);
			Utils.map34To20(H21[1][0]);
			Utils.map34To20(H22[0][0]);
			Utils.map34To20(H22[1][0]);
			Arrays.fill(ipdPrev, 0);
			Arrays.fill(opdPrev, 0);
		}

		//mixing
		final boolean ipdopd = header.isIPDOPDEnabled();
		final int mode = header.getBandMode();
		final float[][][] filter = header.useICCMixingB() ? HB : HA;
		final int[] map = header.use34Bands(false) ? PARAMETER_MAP_34 : PARAMETER_MAP_20;
		final int iidQuant = header.useIIDQuantFine() ? 1 : 0;

		final float[] h11 = new float[2], h12 = new float[2];
		final float[] h21 = new float[2], h22 = new float[2];
		final float[] h11Step = new float[2], h12Step = new float[2];
		final float[] h21Step = new float[2], h22Step = new float[2];
		final float[] tmp = new float[2];
		final float[] l = new float[2], r = new float[2];
		float[] ipd, opd;
		float width;
		int ipdIndex, opdIndex;

		int b, k, n;
		for(int e = 0; e<envCount; e++) {
			for(b = 0; b<PAR_BANDS[mode]; b++) {
				h11[0] = filter[iidMapped[e][b]+7+23*iidQuant][iccMapped[e][b]][0];
				h12[0] = filter[iidMapped[e][b]+7+23*iidQuant][iccMapped[e][b]][1];
				h21[0] = filter[iidMapped[e][b]+7+23*iidQuant][iccMapped[e][b]][2];
				h22[0] = filter[iidMapped[e][b]+7+23*iidQuant][iccMapped[e][b]][3];
				if(ipdopd&&b<header.getIPDOPDPars()) {
					ipdIndex = ipdPrev[b]*8+ipdMapped[e][b];
					opdIndex = opdPrev[b]*8+opdMapped[e][b];
					opdPrev[b] = opdIndex&0x3F;
					ipdPrev[b] = ipdIndex&0x3F;

					ipd = SMOOTHING_TABLE[ipdIndex];
					opd = SMOOTHING_TABLE[opdIndex];
					tmp[0] = opd[0]*ipd[0]+opd[1]*ipd[1];
					tmp[1] = opd[1]*ipd[0]-opd[0]*ipd[1];
					h11[1] = h11[0]*opd[1];
					h11[0] *= opd[0];
					h12[1] = h12[0]*tmp[1];
					h12[0] *= tmp[0];
					h21[1] = h21[0]*opd[1];
					h21[0] *= opd[0];
					h22[1] = h22[0]*tmp[1];
					h22[0] *= tmp[0];
					H11[1][e+1][b] = h11[1];
					H12[1][e+1][b] = h12[1];
					H21[1][e+1][b] = h21[1];
					H22[1][e+1][b] = h22[1];
				}
				H11[0][e+1][b] = h11[0];
				H12[0][e+1][b] = h12[0];
				H21[0][e+1][b] = h21[0];
				H22[0][e+1][b] = h22[0];
			}
			for(k = 0; k<BANDS[mode]; k++) {
				width = 1.f/(borderPositions[e]+1-borderPositions[e+1]);
				b = map[k];

				h11[0] = H11[0][e][b];
				h12[0] = H12[0][e][b];
				h21[0] = H21[0][e][b];
				h22[0] = H22[0][e][b];

				if(ipdopd) {
					if((use34&&k>=9&&k<=13)||(!use34&&k<=1)) {
						h11[1] = -H11[1][e][b];
						h12[1] = -H12[1][e][b];
						h21[1] = -H21[1][e][b];
						h22[1] = -H22[1][e][b];
					}
					else {
						h11[1] = H11[1][e][b];
						h12[1] = H12[1][e][b];
						h21[1] = H21[1][e][b];
						h22[1] = H22[1][e][b];
					}
				}

				//interpolation
				h11Step[0] = (H11[0][e+1][b]-h11[0])*width;
				h12Step[0] = (H12[0][e+1][b]-h12[0])*width;
				h21Step[0] = (H21[0][e+1][b]-h21[0])*width;
				h22Step[0] = (H22[0][e+1][b]-h22[0])*width;
				if(ipdopd) {
					h11Step[1] = (H11[1][e+1][b]-h11[1])*width;
					h12Step[1] = (H12[1][e+1][b]-h12[1])*width;
					h21Step[1] = (H21[1][e+1][b]-h21[1])*width;
					h22Step[1] = (H22[1][e+1][b]-h22[1])*width;
				}

				for(n = borderPositions[e]+1; n<=borderPositions[e+1]; n++) {
					l[0] = lBuf[k][n][0];
					l[1] = lBuf[k][n][1];
					r[0] = rBuf[k][n][0];
					r[1] = rBuf[k][n][1];
					h11[0] += h11Step[0];
					h12[0] += h12Step[0];
					h21[0] += h21Step[0];
					h22[0] += h22Step[0];
					lBuf[k][n][0] = h11[0]*l[0]+h21[0]*r[0];
					lBuf[k][n][1] = h11[0]*l[1]+h21[0]*r[1];
					rBuf[k][n][0] = h12[0]*l[0]+h22[0]*r[0];
					rBuf[k][n][1] = h12[0]*l[1]+h22[0]*r[1];

					if(ipdopd) {
						h11[1] += h11Step[1];
						h12[1] += h12Step[1];
						h21[1] += h21Step[1];
						h22[1] += h22Step[1];
						lBuf[k][n][0] -= h11[1]*l[1]-h21[1]*r[1];
						lBuf[k][n][1] += h11[1]*l[0]+h21[1]*r[0];
						rBuf[k][n][0] -= h12[1]*l[1]-h22[1]*r[1];
						rBuf[k][n][1] += h12[1]*l[0]+h22[1]*r[0];
					}
				}
			}
		}
	}

	private void mapPars(int[][] in, int[][] out, int len, boolean full) {
		int i;

		if(header.use34Bands(false)) {
			if(len==10) {
				for(i = 0; i<envCount; i++) {
					Utils.map10To34(in[i], out[i], full);
				}
			}
			else if(len==20) {
				for(i = 0; i<envCount; i++) {
					Utils.map20To34(in[i], out[i], full);
				}
			}
		}
		else {
			if(len==10) {
				for(i = 0; i<envCount; i++) {
					Utils.map10To20(in[i], out[i], full);
				}
			}
			else if(len==34) {
				for(i = 0; i<envCount; i++) {
					Utils.map34To20(in[i], out[i], full);
				}
			}
		}
	}
}
