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

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.Constants;

class ChannelData implements SBRConstants, HuffmanTables {

	private static final int[] LOG2TABLE = {0, 0, 1, 2, 2, 3, 3, 3, 3, 4};
	//read
	int frameClass;
	int pointer;
	private boolean addHarmonicFlag, addHarmonicFlagPrev;
	boolean[] addHarmonic, addHarmonicPrev;
	private int[] relBord, relBord0, relBord1;
	private int rel0, rel1;
	private boolean[] dfEnv;
	private boolean[] dfNoise;
	int[] invfMode, invfModePrev;
	//calculated
	boolean ampRes;
	int[] t_E, tEtmp, t_Q; //envelope/noise time border vectors
	int L_E, L_E_prev, L_Q; //length of t_E/t_Q
	boolean[] f;
	private boolean fPrev;
	float[][] gTempPrev;
	float[][] qTempPrev;
	int gqIndex;
	int[][] E;
	private int[] E_prev;
	float[][] E_orig;
	float[][] E_curr;
	int[][] Q;
	int[] Q_prev;
	float[][] Q_div, Q_div2;
	//
	private int absBordLead, absBordTrail;
	int prevEnvIsShort;
	int l_A;
	float[][][] Xsbr;
	float[] bwArray, bwArrayPrev;
	int indexNoisePrev, psiIsPrev;

	ChannelData() {
		addHarmonic = new boolean[64];
		addHarmonicPrev = new boolean[64];
		relBord = new int[9];
		relBord0 = new int[9];
		relBord1 = new int[9];
		dfEnv = new boolean[9];
		dfNoise = new boolean[3];
		invfMode = new int[MAX_L_E];
		invfModePrev = new int[MAX_L_E];
		t_E = new int[MAX_L_E+1];
		tEtmp = new int[6];
		t_Q = new int[3];
		f = new boolean[MAX_L_E+1];
		gTempPrev = new float[5][64];
		qTempPrev = new float[5][64];
		E = new int[64][MAX_L_E];
		E_prev = new int[64];
		E_orig = new float[64][MAX_L_E];
		E_curr = new float[64][MAX_L_E];
		Q = new int[64][2];
		Q_prev = new int[64];
		Q_div = new float[64][2];
		Q_div2 = new float[64][2];
		bwArray = new float[64];
		bwArrayPrev = new float[64];
		Xsbr = new float[MAX_NTSRHFG][64][2];

		gqIndex = 0;
	}

	/* ================= decoding ================= */
	void decodeGrid(BitStream in) throws AACException {
		final int savedL_E = L_E;
		final int savedL_Q = L_Q;
		final int savedFrameClass = frameClass;

		int envCount = 0;
		int i, x, absBord0, absBord1;
		switch(frameClass = in.readBits(2)) {
			case FIXFIX:
				x = in.readBits(2);

				envCount = Math.min(1<<x, 5);

				final boolean b = in.readBool();
				for(i = 0; i<envCount; i++) {
					f[i] = b;
				}

				absBordLead = 0;
				absBordTrail = TIME_SLOTS;
				//n_rel_lead = bs_num_env-1;
				//n_rel_trail = 0;
				break;

			case FIXVAR:
				absBord0 = in.readBits(2)+TIME_SLOTS;
				envCount = in.readBits(2)+1;

				for(i = 0; i<envCount-1; i++) {
					relBord[i] = 2*in.readBits(2)+2;
				}
				pointer = in.readBits(LOG2TABLE[envCount+1]);

				for(i = 0; i<envCount; i++) {
					f[envCount-i-1] = in.readBool();
				}

				absBordLead = 0;
				absBordTrail = absBord0;
				//n_rel_lead = 0;
				//n_rel_trail = bs_num_env-1;
				break;

			case VARFIX:
				absBord0 = in.readBits(2);
				envCount = in.readBits(2)+1;

				for(i = 0; i<envCount-1; i++) {
					relBord[i] = 2*in.readBits(2)+2;
				}
				x = LOG2TABLE[envCount+1];
				pointer = in.readBits(x);

				for(i = 0; i<envCount; i++) {
					f[i] = in.readBool();
				}

				absBordLead = absBord0;
				absBordTrail = TIME_SLOTS;
				//n_rel_lead = bs_num_env-1;
				//n_rel_trail = 0;
				break;

			case VARVAR:
				absBord0 = in.readBits(2);
				absBord1 = in.readBits(2)+TIME_SLOTS;
				rel0 = in.readBits(2);
				rel1 = in.readBits(2);

				envCount = Math.min(5, rel0+rel1+1);

				for(i = 0; i<rel0; i++) {
					relBord0[i] = 2*in.readBits(2)+2;
				}
				for(i = 0; i<rel1; i++) {
					relBord1[i] = 2*in.readBits(2)+2;
				}
				x = LOG2TABLE[rel0+rel1+2];
				pointer = in.readBits(x);

				for(i = 0; i<envCount; i++) {
					f[i] = in.readBool();
				}

				absBordLead = absBord0;
				absBordTrail = absBord1;
				//n_rel_lead = bs_num_rel_0;
				//n_rel_trail = bs_num_rel_1;
				break;
		}

		if(frameClass==VARVAR) L_E = Math.min(envCount, 5);
		else L_E = Math.min(envCount, 4);

		if(L_E<=0) throw new AACException("L_E out of range: "+L_E);

		if(L_E>1) L_Q = 2;
		else L_Q = 1;

		if(!envelopeTimeBorderVector()) {
			Constants.LOGGER.warning("envelopeTimeBorderVector failed");
			frameClass = savedFrameClass;
			L_E = savedL_E;
			L_Q = savedL_Q;
		}
		noiseFloorTimeBorderVector();
	}

	void copyGrid(ChannelData cd) {
		frameClass = cd.frameClass;
		L_E = cd.L_E;
		L_Q = cd.L_Q;
		pointer = cd.pointer;

		System.arraycopy(cd.t_E, 0, t_E, 0, L_E+1);
		System.arraycopy(cd.f, 0, f, 0, L_E+1);
		System.arraycopy(cd.t_Q, 0, t_Q, 0, L_Q+1);
	}

	void decodeDTDF(BitStream in) throws AACException {
		int i;
		for(i = 0; i<L_E; i++) {
			dfEnv[i] = in.readBool();
		}

		for(i = 0; i<L_Q; i++) {
			dfNoise[i] = in.readBool();
		}
	}

	void decodeInvfMode(BitStream in, int len) throws AACException {
		for(int i = 0; i<len; i++) {
			invfMode[i] = in.readBits(2);
		}
	}

	void copyInvfMode(ChannelData cd, int len) {
		System.arraycopy(cd.invfMode, 0, invfMode, 0, len);
	}

	void decodeSinusoidalCoding(BitStream in, int len) throws AACException {
		if(addHarmonicFlag = in.readBool()) {
			for(int i = 0; i<len; i++) {
				addHarmonic[i] = in.readBool();
			}
		}
	}

	/* ================= huffman ================= */
	void decodeEnvelope(BitStream in, SBR sbr, int ch, boolean coupling, boolean ampRes) throws AACException {
		ampRes = ((L_E==1)&&(frameClass==FIXFIX)) ? false : ampRes;
		final int bits = 7-((coupling&&(ch==1)) ? 1 : 0)-(ampRes ? 1 : 0);

		int delta;
		int[][] huffT, huffF;
		if(coupling&&(ch==1)) {
			delta = 1;
			if(ampRes) {
				huffT = T_HUFFMAN_ENV_BAL_3_0DB;
				huffF = F_HUFFMAN_ENV_BAL_3_0DB;
			}
			else {
				huffT = T_HUFFMAN_ENV_BAL_1_5DB;
				huffF = F_HUFFMAN_ENV_BAL_1_5DB;
			}
		}
		else {
			delta = 0;
			if(ampRes) {
				huffT = T_HUFFMAN_ENV_3_0DB;
				huffF = F_HUFFMAN_ENV_3_0DB;
			}
			else {
				huffT = T_HUFFMAN_ENV_1_5DB;
				huffF = F_HUFFMAN_ENV_1_5DB;
			}
		}

		int j;
		for(int i = 0; i<L_E; i++) {
			if(dfEnv[i]) {
				for(j = 0; j<sbr.n[f[i] ? 1 : 0]; j++) {
					E[j][i] = decodeHuffman(in, huffT)<<delta;
				}
			}
			else {
				E[0][i] = in.readBits(bits)<<delta;

				for(j = 1; j<sbr.n[f[i] ? 1 : 0]; j++) {
					E[j][i] = decodeHuffman(in, huffF)<<delta;
				}
			}
		}

		extractEnvelopeData(sbr);
	}

	void decodeNoise(BitStream in, SBR sbr, int ch, boolean coupling) throws AACException {
		int delta;
		int[][] huffT, huffF;
		if(coupling&&(ch==1)) {
			delta = 1;
			huffT = T_HUFFMAN_NOISE_BAL_3_0DB;
			huffF = F_HUFFMAN_ENV_BAL_3_0DB;
		}
		else {
			delta = 0;
			huffT = T_HUFFMAN_NOISE_3_0DB;
			huffF = F_HUFFMAN_ENV_3_0DB;
		}

		final int len = sbr.N_Q;
		int j;
		for(int i = 0; i<L_Q; i++) {
			if(dfNoise[i]) {
				for(j = 0; j<len; j++) {
					Q[j][i] = decodeHuffman(in, huffT)<<delta;
				}
			}
			else {
				Q[0][i] = in.readBits(5)<<delta;
				for(j = 1; j<len; j++) {
					Q[j][i] = decodeHuffman(in, huffF)<<delta;
				}
			}
		}

		extractNoiseFloorData(sbr);
	}

	private int decodeHuffman(BitStream in, int[][] table) throws AACException {
		int index = 0;
		int bit;
		while(index>=0) {
			bit = in.readBit();
			index = table[index][bit];
		}
		return index+HUFFMAN_OFFSET;
	}

	/* ================== gets ================== */
	public boolean hasHarmonicPrev() {
		return addHarmonicFlagPrev;
	}

	/* ================= computation ================= */
	//constructs new time border vector
	private boolean envelopeTimeBorderVector() {
		for(int i = 0; i<6; i++) {
			tEtmp[i] = 0;
		}

		tEtmp[0] = RATE*absBordLead;
		tEtmp[L_E] = RATE*absBordTrail;

		int i, tmp, border;
		switch(frameClass) {
			case FIXFIX:
				switch(L_E) {
					case 4:
						tmp = (int) (TIME_SLOTS/4.0);
						tEtmp[3] = RATE*3*tmp;
						tEtmp[2] = RATE*2*tmp;
						tEtmp[1] = RATE*tmp;
						break;
					case 2:
						tEtmp[1] = RATE*(TIME_SLOTS/2);
						break;
					default:
						break;
				}
				break;
			case FIXVAR:
				if(L_E>1) {
					tmp = L_E;
					border = absBordTrail;
					for(i = 0; i<(L_E-1); i++) {
						if(border<relBord[i]) return false;
						border -= relBord[i];
						tEtmp[--tmp] = RATE*border;
					}
				}
				break;
			case VARFIX:
				if(L_E>1) {
					tmp = 1;
					border = absBordLead;
					for(i = 0; i<(L_E-1); i++) {
						border += relBord[i];
						if(RATE*border+T_HFADJ>TIME_SLOTS_RATE+T_HFGEN) return false;
						tEtmp[tmp++] = RATE*border;
					}
				}
				break;
			case VARVAR:
				if(rel0>0) {
					tmp = 1;
					border = absBordLead;
					for(i = 0; i<rel0; i++) {
						border += relBord0[i];
						if(RATE*border+T_HFADJ>TIME_SLOTS_RATE+T_HFGEN) return false;
						tEtmp[tmp++] = RATE*border;
					}
				}
				if(rel1>0) {
					tmp = L_E;
					border = absBordTrail;
					for(i = 0; i<rel1; i++) {
						if(border<relBord1[i]) return false;
						border -= relBord1[i];
						tEtmp[--tmp] = RATE*border;
					}
				}
				break;
		}

		//no error occured
		System.arraycopy(tEtmp, 0, t_E, 0, 6);
		return true;
	}

	private void noiseFloorTimeBorderVector() {
		t_Q[0] = t_E[0];

		if(L_E==1) {
			t_Q[1] = t_E[1];
			t_Q[2] = 0;
		}
		else {
			t_Q[1] = t_E[findMiddleBorder()];
			t_Q[2] = t_E[L_E];
		}
	}

	private int findMiddleBorder() {
		int r = 0;

		switch(frameClass) {
			case FIXFIX:
				r = L_E/2;
				break;
			case VARFIX:
				if(pointer==0) r = 1;
				else if(pointer==1) r = L_E-1;
				else r = pointer-1;
				break;
			case FIXVAR:
			case VARVAR:
				if(pointer>1) r = L_E+1-pointer;
				else r = L_E-1;
				break;
		}

		return r>0 ? r : 0;
	}

	private void extractEnvelopeData(SBR sbr) {
		int j, k;
		boolean g;
		for(int i = 0; i<L_E; i++) {
			if(dfEnv[i]) {
				g = (i==0) ? fPrev : f[i-1];

				if(f[i]==g) {
					for(j = 0; j<sbr.n[f[i] ? 1 : 0]; j++) {
						E[j][i] += (i==0) ? E_prev[j] : E[j][i-1];
					}
				}
				else if(g&&!f[i]) {
					for(j = 0; j<sbr.n[f[i] ? 1 : 0]; j++) {
						for(k = 0; k<sbr.N_high; k++) {
							if(sbr.ftRes[HI_RES][k]==sbr.ftRes[LO_RES][j]) {
								E[j][i] += (i==0) ? E_prev[k] : E[k][i-1];
							}
						}
					}

				}
				else if(!g&&f[i]) {
					for(j = 0; j<sbr.n[f[i] ? 1 : 0]; j++) {
						for(k = 0; k<sbr.N_low; k++) {
							if((sbr.ftRes[LO_RES][k]<=sbr.ftRes[HI_RES][j])
									&&(sbr.ftRes[HI_RES][j]<sbr.ftRes[LO_RES][k+1])) {
								E[j][i] += (i==0) ? E_prev[k] : E[k][i-1];
							}
						}
					}
				}
			}
			else {
				for(j = 1; j<sbr.n[f[i] ? 1 : 0]; j++) {
					E[j][i] += E[j-1][i];
					if(E[j][i]<0) E[j][i] = 0;
				}
			}
		}
	}

	private void extractNoiseFloorData(SBR sbr) {
		final int len = sbr.N_Q;

		int j;
		for(int i = 0; i<L_Q; i++) {
			if(dfNoise[i]) {
				if(i==0) {
					for(j = 0; j<len; j++) {
						Q[j][i] = Q_prev[j]+Q[j][0];
					}
				}
				else {
					for(j = 0; j<len; j++) {
						Q[j][i] += Q[j][i-1];
					}
				}
			}
			else {
				for(j = 1; j<len; j++) {
					Q[j][i] += Q[j-1][i];
				}
			}
		}
	}

	/* ================= processing ================== */
	void savePreviousData() {
		L_E_prev = L_E;
		fPrev = f[L_E-1];

		for(int i = 0; i<MAX_M; i++) {
			E_prev[i] = E[i][L_E-1];
			Q_prev[i] = Q[i][L_Q-1];
		}

		System.arraycopy(addHarmonic, 0, addHarmonicPrev, 0, MAX_M);
		addHarmonicFlagPrev = addHarmonicFlag;

		prevEnvIsShort = (l_A==L_E) ? 0 : -1;
	}

	void saveMatrix() {
		int i, j;
		//copy complex values
		for(i = 0; i<T_HFGEN; i++) {
			for(j = 0; j<64; j++) {
				Xsbr[i][j][0] = Xsbr[i+TIME_SLOTS_RATE][j][0];
				Xsbr[i][j][1] = Xsbr[i+TIME_SLOTS_RATE][j][1];
			}
		}
		for(i = T_HFGEN; i<MAX_NTSRHFG; i++) {
			for(j = 0; j<64; j++) {
				Xsbr[i][j][0] = 0;
				Xsbr[i][j][1] = 0;
			}
		}
	}
}
