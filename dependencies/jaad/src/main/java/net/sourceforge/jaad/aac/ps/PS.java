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

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.BitStream;
import java.util.Arrays;

public class PS implements PSConstants, PSTables, HuffmanTables {

	//header data
	private boolean header;
	private boolean enableIID, enableICC, enableExt, enableIPDOPD;
	private int iidMode, iccMode;
	private boolean iidQuant;
	private int iidParCount, iccParCount, ipdopdParCount;
	private boolean use34, use34Old;
	//standard data
	private int frameClass;
	private int envCount, envCountOld;
	private final int[] borderPositions;
	//pars
	private final int[][] iidPars, iccPars, ipdPars, opdPars;
	private final int[] iidParsPrev, iccParsPrev, ipdParsPrev, opdParsPrev;
	//processing
	private float[][][] in_buf;
	private float[][][] delay;
	private float[][][][] ap_delay;
	//decorrelation
	private float[] peak_decay_nrg;
	private float[] power_smooth;
	private float[] peak_decay_diff_smooth;
	//stereo processing
	private float[][][] H11, H12, H21, H22;
	private int[] ipd_hist, opd_hist;

	public PS() {
		//ipd/opd is iid/icc sized so that the same functions can handle both
		iidPars = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		iccPars = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		ipdPars = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		opdPars = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		//last envelope of previous frame
		iidParsPrev = new int[MAX_IID_ICC_PARS];
		iccParsPrev = new int[MAX_IID_ICC_PARS];
		ipdParsPrev = new int[MAX_IID_ICC_PARS];
		opdParsPrev = new int[MAX_IID_ICC_PARS];

		borderPositions = new int[MAX_ENVELOPES+1];

		in_buf = new float[5][44][2];
		delay = new float[MAX_SSB][QMF_SLOTS+MAX_DELAY][2];
		ap_delay = new float[MAX_AP_BANDS][AP_LINKS][QMF_SLOTS+MAX_AP_DELAY][2];

		peak_decay_nrg = new float[34];
		power_smooth = new float[34];
		peak_decay_diff_smooth = new float[34];

		H11 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
		H12 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
		H21 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
		H22 = new float[2][MAX_ENVELOPES+1][MAX_IID_ICC_PARS];
		ipd_hist = new int[MAX_IID_ICC_PARS];
		opd_hist = new int[MAX_IID_ICC_PARS];
	}
	//============================ decoding ==============================
	private int frame = 0;

	public int decode(BitStream in) throws AACException {
		frame++;
		final int off = in.getPosition();

		if(in.readBool()) {
			header = true;
			if(enableIID = in.readBool()) {
				iidMode = in.readBits(3);
				iidQuant = iidMode>2;
				iidParCount = IID_ICC_PAR_TABLE[iidMode];
				ipdopdParCount = IPDOPD_PAR_TABLE[iidMode];
			}
			if(enableICC = in.readBool()) {
				iccMode = in.readBits(3);
				iccParCount = IID_ICC_PAR_TABLE[iccMode];
			}
			enableExt = in.readBool();
		}

		frameClass = in.readBit();
		envCountOld = envCount;
		envCount = ENV_COUNT_TABLE[frameClass][in.readBits(2)];
		borderPositions[0] = -1;
		if(frameClass==1) {
			for(int i = 1; i<=envCount; i++) {
				borderPositions[i] = in.readBits(5);
			}
		}
		else {
			for(int i = 1; i<=envCount; i++) {
				borderPositions[i] = (i*QMF_SLOTS>>LOG2_TABLE[envCount])-1;
			}
		}

		if(enableIID) {
			boolean time;
			for(int i = 0; i<envCount; i++) {
				time = in.readBool();
				decodeIIDData(in, time, i);
			}
		}
		else Arrays.fill(iidPars, new int[MAX_IID_ICC_PARS]);

		if(enableICC) {
			boolean time;
			for(int i = 0; i<envCount; i++) {
				time = in.readBool();
				decodeICCData(in, time, i);
			}
		}
		else Arrays.fill(iccPars, new int[MAX_IID_ICC_PARS]);

		use34Old = use34;
		if(enableIID||enableICC) use34 = (enableIID&&iidParCount==34)||(enableICC&&iccParCount==34);

		if(enableExt) {
			int size = in.readBits(4);
			if(size==15) size += in.readBits(8);

			int bitsLeft = 8*size;
			int id;
			while(bitsLeft>7) {
				id = in.readBits(2);
				bitsLeft -= 2;
				bitsLeft -= decodeExtension(in, id);
			}

			in.skipBits(bitsLeft);
		}

		//fix up envelopes
		if(envCount==0||borderPositions[envCount]<QMF_SLOTS-1) {
			//create a fake envelope
			int source = envCount!=0 ? envCount-1 : envCountOld-1;
			if(source>=0&&source!=envCount) {
				if(enableIID) System.arraycopy(iidPars[source], 0, iidPars[envCount], 0, iidPars[source].length);
				if(enableICC) System.arraycopy(iccPars[source], 0, iccPars[envCount], 0, iccPars[source].length);
				if(enableIPDOPD) {
					System.arraycopy(ipdPars[source], 0, ipdPars[envCount], 0, ipdPars[source].length);
					System.arraycopy(opdPars[source], 0, opdPars[envCount], 0, opdPars[source].length);
				}
			}
			envCount++;
			borderPositions[envCount] = QMF_SLOTS-1;
		}

		if(enableIPDOPD) {
			Arrays.fill(ipdPars, new int[MAX_IID_ICC_PARS]);
			Arrays.fill(opdPars, new int[MAX_IID_ICC_PARS]);
		}

		//update previous indices
		System.arraycopy(iidPars[envCount-1], 0, iidParsPrev, 0, 34);
		System.arraycopy(iccPars[envCount-1], 0, iccParsPrev, 0, 34);
		System.arraycopy(ipdPars[envCount-1], 0, ipdParsPrev, 0, 17);
		System.arraycopy(opdPars[envCount-1], 0, opdParsPrev, 0, 17);

		final int read = in.getPosition()-off;
		return read;
	}

	private int decodeExtension(BitStream in, int id) throws AACException {
		int off = in.getPosition();
		if(id==0) {
			if(enableIPDOPD = in.readBool()) {
				boolean time;
				for(int i = 0; i<envCount; i++) {
					time = in.readBool();
					decodeIPDData(in, time, i);
					time = in.readBool();
					decodeOPDData(in, time, i);
				}
			}
			in.skipBit(); //reserved
		}
		return in.getPosition()-off;
	}

	private void decodeIIDData(BitStream in, boolean time, int index) throws AACException {
		final int[][] table;
		if(iidQuant) table = time ? HUFFMAN_IID_FINE_DT : HUFFMAN_IID_FINE_DF;
		else table = time ? HUFFMAN_IID_DEFAULT_DT : HUFFMAN_IID_DEFAULT_DF;
		Huffman.decode(in, table, iidPars[index], iidParCount);

		final int[] iidP = (index==0) ? iidParsPrev : iidPars[index-1];
		final int iidSteps = iidQuant ? 15 : 7;
		Utils.deltaDecode(iidPars[index], iidParCount, iidP,
				time, enableIID,
				(iidMode==0||iidMode==3) ? 2 : 1,
				-iidSteps, iidSteps);
	}

	private void decodeICCData(BitStream in, boolean time, int index) throws AACException {
		final int[][] table = time ? HUFFMAN_ICC_DT : HUFFMAN_ICC_DF;
		Huffman.decode(in, table, iccPars[index], iccParCount);

		final int[] iccP = (index==0) ? iccParsPrev : iccPars[index-1];
		Utils.deltaDecode(iccPars[index], iccParCount, iccP,
				time, enableICC,
				(iccMode==0||iccMode==3) ? 2 : 1,
				0, 7);
	}

	private void decodeIPDData(BitStream in, boolean time, int index) throws AACException {
		final int[][] table = time ? HUFFMAN_IPD_DT : HUFFMAN_IPD_DF;
		Huffman.decode(in, table, ipdPars[index], ipdopdParCount);

		final int[] ipdP = (index==0) ? ipdParsPrev : ipdPars[index-1];
		Utils.deltaModuloDecode(ipdPars[index], ipdopdParCount, ipdP,
				time, enableIPDOPD);
	}

	private void decodeOPDData(BitStream in, boolean time, int index) throws AACException {
		final int[][] table = time ? HUFFMAN_OPD_DT : HUFFMAN_OPD_DF;
		Huffman.decode(in, table, opdPars[index], ipdopdParCount);

		final int[] opdP = (index==0) ? opdParsPrev : opdPars[index-1];
		Utils.deltaModuloDecode(opdPars[index], ipdopdParCount, opdP,
				time, enableIPDOPD);
	}

	public boolean hasHeader() {
		return header;
	}

	//============================ processing ==============================
	//left,right: [38][64][2]
	public void process(float[][][] left, float[][][] right, int top) {
		float[][][] lBuf = new float[91][32][2];
		float[][][] rBuf = new float[91][32][2];
		int use34I = use34 ? 1 : 0;

		top += BANDS[use34I]-64;
		//memset(ps->delay+top, 0, (BANDS[use34I] - top)*sizeof(ps->delay[0]));
		Arrays.fill(delay, top, BANDS[use34I], new float[QMF_SLOTS+MAX_DELAY][2]);
		//memset(ps->ap_delay+top, 0, (ALLPASS_BANDS[use34I]-top)*sizeof(ps->ap_delay[0]));
		if(top<ALLPASS_BANDS[use34I]) Arrays.fill(ap_delay, top, ALLPASS_BANDS[use34I], new float[AP_LINKS][QMF_SLOTS+MAX_AP_DELAY][2]);

		Filterbank.performAnalysis(left, lBuf, in_buf, use34);
		decorrelate(lBuf, rBuf);
		processStereo(lBuf, rBuf);
		Filterbank.performSynthesis(lBuf, left, use34);
		Filterbank.performSynthesis(rBuf, right, use34);
	}

	//out: [32][2]*, in: [32][2]*
	private void decorrelate(float[][][] s, float[][][] out) {
		final float[][] power = new float[34][QMF_SLOTS];


		float[][] transient_gain = new float[34][QMF_SLOTS];
		//float *peak_decay_nrg = ps->peak_decay_nrg;
		//float *power_smooth = ps->power_smooth;
		//float *peak_decay_diff_smooth = ps->peak_decay_diff_smooth;
		//float (*delay)[PS_QMF_TIME_SLOTS + PS_MAX_DELAY][2] = ps->delay;
		//float (*ap_delay)[PS_AP_LINKS][PS_QMF_TIME_SLOTS + PS_MAX_AP_DELAY][2] = ps->ap_delay;
		final int[] k_to_i = use34 ? K_TO_I_34 : K_TO_I_20;
		final int use34I = use34 ? 1 : 0;
		int i, k, m, n;
		int n0 = 0, nL = 32;

		if(use34!=use34Old) {
			//reset
			Arrays.fill(peak_decay_nrg, 0);
			Arrays.fill(power_smooth, 0);
			Arrays.fill(peak_decay_diff_smooth, 0);
			delay = new float[MAX_SSB][QMF_SLOTS+MAX_DELAY][2];
			ap_delay = new float[MAX_AP_BANDS][AP_LINKS][QMF_SLOTS+MAX_AP_DELAY][2];
			//memset(ps->peak_decay_nrg,         0, sizeof(ps->peak_decay_nrg));
			//memset(ps->power_smooth,           0, sizeof(ps->power_smooth));
			//memset(ps->peak_decay_diff_smooth, 0, sizeof(ps->peak_decay_diff_smooth));
			//memset(ps->delay,                  0, sizeof(ps->delay));
			//memset(ps->ap_delay,               0, sizeof(ps->ap_delay));
		}

		//calculate power
		for(n = n0; n<nL; n++) {
			for(k = 0; k<BANDS[use34I]; k++) {
				i = k_to_i[k];
				power[i][n] += (s[k][n][0]*s[k][n][0])+(s[k][n][1]*s[k][n][1]);
			}
		}

		//transient detection
		float decayed_peak, denom;
		for(i = 0; i<PAR_BANDS[use34I]; i++) {
			for(n = n0; n<nL; n++) {
				decayed_peak = PEAK_DECAY_FACTOR*peak_decay_nrg[i];
				peak_decay_nrg[i] = Math.max(decayed_peak, power[i][n]);
				power_smooth[i] += A_SMOOTH*(power[i][n]-power_smooth[i]);
				peak_decay_diff_smooth[i] += A_SMOOTH*(peak_decay_nrg[i]-power[i][n]-peak_decay_diff_smooth[i]);
				denom = TRANSIENT_IMPACT*peak_decay_diff_smooth[i];
				transient_gain[i][n] = (denom>power_smooth[i]) ? power_smooth[i]/denom : 1.0f;
			}
		}

		/* decorrelation and transient reduction
		
		PS_AP_LINKS - 1
		-----
		| |  (Q_FRACT_ALLPASS[k][m]*z^-link_delay[m]) - (a[m]*g_decay_slope[k])
		H[k][z] = z^-2 * PHI_FRACT[k] * | | ----------------------------------------------------------------
		| | 1 - (a[m]*g_decay_slope[k]*Q_FRACT_ALLPASS[k][m]*z^-link_delay[m])
		m = 0
		
		d[k][z](out) = transient_gain_mapped[k][z] * H[k][z] * s[k][z]
		 */
		for(k = 0; k<ALLPASS_BANDS[use34I]; k++) {
			int b = k_to_i[k];
			float g_decay_slope = 1.0f-DECAY_SLOPE*(k-DECAY_CUTOFF[use34I]);
			float[] ag = new float[AP_LINKS];
			g_decay_slope = Math.min(Math.max(g_decay_slope, 0.0f), 1.0f);
			copyArray(delay[k], nL, delay[k], 0, MAX_DELAY);
			copyArray(s[k], 0, delay[k], MAX_DELAY, QMF_SLOTS);
			//memcpy(delay[k], delay[k] + nL, MAX_DELAY * sizeof(delay[k][0]));
			//memcpy(delay[k] + MAX_DELAY, s[k], QMF_SLOTS * sizeof(delay[k][0]));
			for(m = 0; m<AP_LINKS; m++) {
				copyArray(ap_delay[k][m], QMF_SLOTS, ap_delay[k][m], 0, 5);
				//memcpy(ap_delay[k][m], ap_delay[k][m] + QMF_SLOTS, 5 * sizeof(ap_delay[k][m][0]));
				ag[m] = FILTER_COEF_A[m]*g_decay_slope;
			}
			for(n = n0; n<nL; n++) {
				float in_re = delay[k][n+MAX_DELAY-2][0]*PHI_FRACT[use34I][k][0]
						-delay[k][n+MAX_DELAY-2][1]*PHI_FRACT[use34I][k][1];
				float in_im = delay[k][n+MAX_DELAY-2][0]*PHI_FRACT[use34I][k][1]
						+delay[k][n+MAX_DELAY-2][1]*PHI_FRACT[use34I][k][0];
				for(m = 0; m<AP_LINKS; m++) {
					float a_re = ag[m]*in_re;
					float a_im = ag[m]*in_im;
					float link_delay_re = ap_delay[k][m][n+5-LINK_DELAY[m]][0];
					float link_delay_im = ap_delay[k][m][n+5-LINK_DELAY[m]][1];
					float fractional_delay_re = Q_FRACT_ALLPASS[use34I][k][m][0];
					float fractional_delay_im = Q_FRACT_ALLPASS[use34I][k][m][1];
					ap_delay[k][m][n+5][0] = in_re;
					ap_delay[k][m][n+5][1] = in_im;
					in_re = link_delay_re*fractional_delay_re-link_delay_im*fractional_delay_im-a_re;
					in_im = link_delay_re*fractional_delay_im+link_delay_im*fractional_delay_re-a_im;
					ap_delay[k][m][n+5][0] += ag[m]*in_re;
					ap_delay[k][m][n+5][1] += ag[m]*in_im;
				}
				out[k][n][0] = transient_gain[b][n]*in_re;
				out[k][n][1] = transient_gain[b][n]*in_im;
			}
		}
		for(; k<SHORT_DELAY_BAND[use34I]; k++) {
			//memcpy(delay[k], delay[k]+nL, MAX_DELAY*sizeof(delay[k][0]));
			//memcpy(delay[k]+MAX_DELAY, s[k], QMF_SLOTS*sizeof(delay[k][0]));
			copyArray(delay[k], nL, delay[k], 0, MAX_DELAY);
			copyArray(s[k], 0, delay[k], MAX_DELAY, QMF_SLOTS);
			for(n = n0; n<nL; n++) {
				out[k][n][0] = transient_gain[k_to_i[k]][n]*delay[k][n+MAX_DELAY-14][0];
				out[k][n][1] = transient_gain[k_to_i[k]][n]*delay[k][n+MAX_DELAY-14][1];
			}
		}
		for(; k<BANDS[use34I]; k++) {
			//memcpy(delay[k], delay[k]+nL, MAX_DELAY*sizeof(delay[k][0]));
			//memcpy(delay[k]+MAX_DELAY, s[k], QMF_SLOTS*sizeof(delay[k][0]));
			copyArray(delay[k], nL, delay[k], 0, MAX_DELAY);
			copyArray(s[k], 0, delay[k], MAX_DELAY, QMF_SLOTS);
			for(n = n0; n<nL; n++) {
				//H = delay 1
				out[k][n][0] = transient_gain[k_to_i[k]][n]*delay[k][n+MAX_DELAY-1][0];
				out[k][n][1] = transient_gain[k_to_i[k]][n]*delay[k][n+MAX_DELAY-1][1];
			}
		}
	}

	//utility for copying complex array
	private void copyArray(float[][] from, int fromOff, float[][] to, int toOff, int len) {
		for(int i = 0; i<len; i++) {
			to[toOff+i][0] = from[fromOff+i][0];
			to[toOff+i][1] = from[fromOff+i][1];
		}
	}

	//l, r: [32][2]*
	private void processStereo(float[][][] l, float[][][] r) {
		int e, b, k, n;

		final int[][] iidMapped = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		final int[][] iccMapped = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		final int[][] ipdMapped = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		final int[][] opdMapped = new int[MAX_ENVELOPES][MAX_IID_ICC_PARS];
		final int[] k_to_i = use34 ? K_TO_I_34 : K_TO_I_20;
		final float[][][] H_LUT = iidQuant ? HB : HA;
		final int use34I = use34 ? 1 : 0;

		//remapping
		//memcpy(H11[0][0], H11[0][envCountOld], MAX_IID_ICC_PARS*sizeof(H11[0][0][0]));
		System.arraycopy(H11[0][envCountOld], 0, H11[0][0], 0, MAX_IID_ICC_PARS);
		//memcpy(H11[1][0], H11[1][envCountOld], MAX_IID_ICC_PARS*sizeof(H11[1][0][0]));
		System.arraycopy(H11[1][envCountOld], 0, H11[1][0], 0, MAX_IID_ICC_PARS);
		//memcpy(H12[0][0], H12[0][envCountOld], MAX_IID_ICC_PARS*sizeof(H12[0][0][0]));
		System.arraycopy(H12[0][envCountOld], 0, H12[0][0], 0, MAX_IID_ICC_PARS);
		//memcpy(H12[1][0], H12[1][envCountOld], MAX_IID_ICC_PARS*sizeof(H12[1][0][0]));
		System.arraycopy(H12[1][envCountOld], 0, H12[1][0], 0, MAX_IID_ICC_PARS);
		//memcpy(H21[0][0], H21[0][envCountOld], MAX_IID_ICC_PARS*sizeof(H21[0][0][0]));
		System.arraycopy(H21[0][envCountOld], 0, H21[0][0], 0, MAX_IID_ICC_PARS);
		//memcpy(H21[1][0], H21[1][envCountOld], MAX_IID_ICC_PARS*sizeof(H21[1][0][0]));
		System.arraycopy(H21[1][envCountOld], 0, H21[1][0], 0, MAX_IID_ICC_PARS);
		//memcpy(H22[0][0], H22[0][envCountOld], MAX_IID_ICC_PARS*sizeof(H22[0][0][0]));
		System.arraycopy(H22[0][envCountOld], 0, H22[0][0], 0, MAX_IID_ICC_PARS);
		//memcpy(H22[1][0], H22[1][envCountOld], MAX_IID_ICC_PARS*sizeof(H22[1][0][0]));
		System.arraycopy(H22[1][envCountOld], 0, H22[1][0], 0, MAX_IID_ICC_PARS);
		if(use34) {
			Utils.remap34(iidMapped, iidPars, iidParCount, envCount, true);
			Utils.remap34(iccMapped, iccPars, iccParCount, envCount, true);
			if(enableIPDOPD) {
				Utils.remap34(ipdMapped, ipdPars, ipdopdParCount, envCount, false);
				Utils.remap34(opdMapped, opdPars, ipdopdParCount, envCount, false);
			}
			if(use34Old) {
				Utils.map20To34Float(H11[0][0]);
				Utils.map20To34Float(H11[1][0]);
				Utils.map20To34Float(H12[0][0]);
				Utils.map20To34Float(H12[1][0]);
				Utils.map20To34Float(H21[0][0]);
				Utils.map20To34Float(H21[1][0]);
				Utils.map20To34Float(H22[0][0]);
				Utils.map20To34Float(H22[1][0]);
				resetIPDOPD();
			}
		}
		else {
			Utils.remap20(iidMapped, iidPars, iidParCount, envCount, true);
			Utils.remap20(iccMapped, iccPars, iccParCount, envCount, true);
			if(enableIPDOPD) {
				Utils.remap20(ipdMapped, ipdPars, ipdopdParCount, envCount, false);
				Utils.remap20(opdMapped, opdPars, ipdopdParCount, envCount, false);
			}
			if(use34Old) {
				Utils.map34To20Float(H11[0][0]);
				Utils.map34To20Float(H11[1][0]);
				Utils.map34To20Float(H12[0][0]);
				Utils.map34To20Float(H12[1][0]);
				Utils.map34To20Float(H21[0][0]);
				Utils.map34To20Float(H21[1][0]);
				Utils.map34To20Float(H22[0][0]);
				Utils.map34To20Float(H22[1][0]);
				resetIPDOPD();
			}
		}

		//mixing
		final int iidQuantI = iidQuant ? 1 : 0;
		for(e = 0; e<envCount; e++) {
			for(b = 0; b<PAR_BANDS[use34I]; b++) {
				float h11, h12, h21, h22;
				h11 = H_LUT[iidMapped[e][b]+7+23*iidQuantI][iccMapped[e][b]][0];
				h12 = H_LUT[iidMapped[e][b]+7+23*iidQuantI][iccMapped[e][b]][1];
				h21 = H_LUT[iidMapped[e][b]+7+23*iidQuantI][iccMapped[e][b]][2];
				h22 = H_LUT[iidMapped[e][b]+7+23*iidQuantI][iccMapped[e][b]][3];
				if(enableIPDOPD&&b<ipdopdParCount) {
					float h11i, h12i, h21i, h22i;
					float ipd_adj_re, ipd_adj_im;
					int opd_idx = opd_hist[b]*8+opdMapped[e][b];
					int ipd_idx = ipd_hist[b]*8+ipdMapped[e][b];
					float opd_re = PD_RE_SMOOTH[opd_idx];
					float opd_im = PD_IM_SMOOTH[opd_idx];
					float ipd_re = PD_RE_SMOOTH[ipd_idx];
					float ipd_im = PD_IM_SMOOTH[ipd_idx];
					opd_hist[b] = opd_idx&0x3F;
					ipd_hist[b] = ipd_idx&0x3F;

					ipd_adj_re = opd_re*ipd_re+opd_im*ipd_im;
					ipd_adj_im = opd_im*ipd_re-opd_re*ipd_im;
					h11i = h11*opd_im;
					h11 = h11*opd_re;
					h12i = h12*ipd_adj_im;
					h12 = h12*ipd_adj_re;
					h21i = h21*opd_im;
					h21 = h21*opd_re;
					h22i = h22*ipd_adj_im;
					h22 = h22*ipd_adj_re;
					H11[1][e+1][b] = h11i;
					H12[1][e+1][b] = h12i;
					H21[1][e+1][b] = h21i;
					H22[1][e+1][b] = h22i;
				}
				H11[0][e+1][b] = h11;
				H12[0][e+1][b] = h12;
				H21[0][e+1][b] = h21;
				H22[0][e+1][b] = h22;
			}
			for(k = 0; k<BANDS[use34I]; k++) {
				float h11r, h12r, h21r, h22r;
				float h11i = 0, h12i = 0, h21i = 0, h22i = 0;
				float h11r_step, h12r_step, h21r_step, h22r_step;
				float h11i_step = 0, h12i_step = 0, h21i_step = 0, h22i_step = 0;
				int begin = borderPositions[e];
				int stop = borderPositions[e+1];
				float width = 1.f/(stop-begin);
				b = k_to_i[k];
				h11r = H11[0][e][b];
				h12r = H12[0][e][b];
				h21r = H21[0][e][b];
				h22r = H22[0][e][b];
				if(enableIPDOPD) {
					if((use34&&k<=13&&k>=9)||(!use34&&k<=1)) {
						h11i = -H11[1][e][b];
						h12i = -H12[1][e][b];
						h21i = -H21[1][e][b];
						h22i = -H22[1][e][b];
					}
					else {
						h11i = H11[1][e][b];
						h12i = H12[1][e][b];
						h21i = H21[1][e][b];
						h22i = H22[1][e][b];
					}
				}

				//interpolation
				h11r_step = (H11[0][e+1][b]-h11r)*width;
				h12r_step = (H12[0][e+1][b]-h12r)*width;
				h21r_step = (H21[0][e+1][b]-h21r)*width;
				h22r_step = (H22[0][e+1][b]-h22r)*width;
				if(enableIPDOPD) {
					h11i_step = (H11[1][e+1][b]-h11i)*width;
					h12i_step = (H12[1][e+1][b]-h12i)*width;
					h21i_step = (H21[1][e+1][b]-h21i)*width;
					h22i_step = (H22[1][e+1][b]-h22i)*width;
				}
				for(n = begin+1; n<=stop; n++) {
					//l is s, r is d
					float l_re = l[k][n][0];
					float l_im = l[k][n][1];
					float r_re = r[k][n][0];
					float r_im = r[k][n][1];
					h11r += h11r_step;
					h12r += h12r_step;
					h21r += h21r_step;
					h22r += h22r_step;
					if(enableIPDOPD) {
						h11i += h11i_step;
						h12i += h12i_step;
						h21i += h21i_step;
						h22i += h22i_step;

						l[k][n][0] = h11r*l_re+h21r*r_re-h11i*l_im-h21i*r_im;
						l[k][n][1] = h11r*l_im+h21r*r_im+h11i*l_re+h21i*r_re;
						r[k][n][0] = h12r*l_re+h22r*r_re-h12i*l_im-h22i*r_im;
						r[k][n][1] = h12r*l_im+h22r*r_im+h12i*l_re+h22i*r_re;
					}
					else {
						l[k][n][0] = h11r*l_re+h21r*r_re;
						l[k][n][1] = h11r*l_im+h21r*r_im;
						r[k][n][0] = h12r*l_re+h22r*r_re;
						r[k][n][1] = h12r*l_im+h22r*r_im;
					}
				}
			}
		}
	}

	private void resetIPDOPD() {
		int i;
		for(i = 0; i<MAX_IPD_OPD_PARS; i++) {
			opd_hist[i] = 0;
			ipd_hist[i] = 0;
		}
	}
}
