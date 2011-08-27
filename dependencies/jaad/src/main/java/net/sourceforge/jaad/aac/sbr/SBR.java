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
import net.sourceforge.jaad.aac.SampleFrequency;
import net.sourceforge.jaad.aac.ps.PS;
import net.sourceforge.jaad.aac.syntax.BitStream;
import net.sourceforge.jaad.aac.syntax.Constants;
import java.util.Arrays;
import java.util.logging.Level;

public class SBR implements Constants, SBRConstants, SBRTables {

	private final ChannelData[] cd;
	int sampleRate;
	boolean reset;
	//header
	private final SBRHeader header;
	//read
	private boolean coupling;
	int extensionID, extensionData;
	//calculated
	int k0; //first band (=f_master[0])
	int kx, kxPrev;
	int[] mft; //master frequency table
	int[][] ftRes; //0=ftHigh and 1=ftLow
	int[] ftNoise; //frequency border table for noise floors
	int[][] ftLim; //limiter frequency table
	int N_master; //length of MFT
	int N_high, N_low;
	int N_Q;
	int M, Mprev;
	int[] N_L;
	int[] n; //number of frequency bands for low (0) and high (1) frequency
	//patches
	int[] tableMapKToG;
	int patches; //number of patches
	int[] patchNoSubbands, patchStartSubband;
	//filterbank
	private final Filterbank filterBank;
	private final QMFAnalysis[] qmfa;
	private final QMFSynthesis[] qmfs;
	private final HFAdjustment hfAdj;
	private final HFGeneration hfGen;
	//processing buffers
	private float[][][] buffer, bufLeftPS, bufRightPS;
	//PS extension data
	private PS ps;
	private boolean psUsed, psExtensionRead;

	public SBR(SampleFrequency sf, boolean downSampled) {
		//global
		sampleRate = 2*sf.getFrequency();

		cd = new ChannelData[2];
		cd[0] = new ChannelData();
		cd[1] = new ChannelData();
		filterBank = new Filterbank();
		qmfa = new QMFAnalysis[2];
		qmfa[0] = new QMFAnalysis(filterBank, 32);
		qmfs = new QMFSynthesis[2];
		qmfs[0] = new QMFSynthesis(filterBank, downSampled ? 32 : 64);
		hfAdj = new HFAdjustment(this);
		hfGen = new HFGeneration(this);

		patchNoSubbands = new int[64];
		patchStartSubband = new int[64];
		N_L = new int[4];
		n = new int[2];
		tableMapKToG = new int[64];
		mft = new int[64];
		ftRes = new int[2][64];
		ftNoise = new int[64];
		ftLim = new int[4][64];

		//header defaults
		header = new SBRHeader();

		Mprev = 0;
	}

	/* ================== decoding ================== */
	public void decode(BitStream in, int count, boolean stereo, boolean crc) throws AACException {
		final int pos = in.getPosition();

		if(crc) {
			LOGGER.info("SBR CRC bits present");
			in.skipBits(10); //TODO: implement crc check
		}

		if(in.readBool()) header.decode(in);


		if(reset) calculateTables();

		//can't decode before the first header
		if(header.isDecoded()) decodeData(in, stereo);
		//else LOGGER.warning("no SBR header found");

		final int len = in.getPosition()-pos;
		final int bitsLeft = count-len;
		if(bitsLeft>=8) LOGGER.log(Level.WARNING, "SBR: bits left: {0}", bitsLeft);
		else if(bitsLeft<0) throw new AACException("SBR data overread: "+bitsLeft);

		in.skipBits(bitsLeft);
	}

	//calculates the master frequency table
	private void calculateTables() throws AACException {
		k0 = Calculation.getStartChannel(header.getStartFrequency(false), sampleRate);
		final int k2 = Calculation.getStopChannel(header.getStopFrequency(false), sampleRate, k0);

		//check k0 and k2 (MFT length)
		final int len = k2-k0;
		int maxLen;
		if(sampleRate>=48000) maxLen = 32;
		else if(sampleRate<=32000) maxLen = 48;
		else maxLen = 45;

		if(len<=maxLen) {
			int[] table;
			if(header.getFrequencyScale(false)==0) table = Calculation.calculateMasterFrequencyTableFS0(k0, k2, header.isAlterScale(false));
			else table = Calculation.calculateMasterFrequencyTable(k0, k2, header.getFrequencyScale(false), header.isAlterScale(false));
			if(table!=null) {
				mft = table;
				N_master = table.length-1;
			}

			calculateDerivedFrequencyTable(k2);
		}
		else throw new AACException("SBR: master frequency table too long: "+len+", max. length: "+maxLen);
	}

	//calculates the derived frequency border table from the master table
	private void calculateDerivedFrequencyTable(int k2) throws AACException {
		final int xOverBand = header.getXOverBand(false);
		if(N_master<=xOverBand) throw new AACException("SBR: derived frequency table: N_master="+N_master+", xOverBand="+xOverBand);
		int i;

		N_high = N_master-xOverBand;
		N_low = (N_high>>1)+(N_high-((N_high>>1)<<1));

		n[0] = N_low;
		n[1] = N_high;

		//fill high resolution table
		for(i = 0; i<=N_high; i++) {
			ftRes[HI_RES][i] = mft[i+xOverBand];
		}

		M = ftRes[HI_RES][N_high]-ftRes[HI_RES][0];
		kx = ftRes[HI_RES][0];
		if(kx>32) throw new AACException("SBR: kx>32: "+kx);
		if(kx+M>64) throw new AACException("SBR: kx+M>64: "+(kx+M));

		//fill low resolution table
		final int minus = N_high&1;
		int x = 0;
		for(i = 0; i<=N_low; i++) {
			x = (i==0) ? 0 : 2*i-minus;
			ftRes[LO_RES][i] = ftRes[HI_RES][x];
		}

		final int noiseBands = header.getNoiseBands(false);
		if(noiseBands==0) N_Q = 1;
		else N_Q = Math.min(5, Math.max(1, Calculation.findBands(false, noiseBands, kx, k2)));

		//fill noise table
		for(i = 0; i<=N_Q; i++) {
			x = (i==0) ? 0 : x+(N_low-x)/(N_Q+1-i);
			ftNoise[i] = ftRes[LO_RES][x];
		}

		//build table for mapping k to g in HF patching
		int j;
		for(i = 0; i<64; i++) {
			for(j = 0; j<N_Q; j++) {
				if((ftNoise[j]<=i)&&(i<ftNoise[j+1])) {
					tableMapKToG[i] = j;
					break;
				}
			}
		}
	}

	//fills ftLim and N_L
	void calculateLimiterFrequencyTable() {
		ftLim[0][0] = ftRes[LO_RES][0]-kx;
		ftLim[0][1] = ftRes[LO_RES][N_low]-kx;
		N_L[0] = 1;

		int j;
		//calculate patch borders
		final int[] patchBorders = new int[patches+1];
		patchBorders[0] = kx;
		for(j = 1; j<=patches; j++) {
			patchBorders[j] = patchBorders[j-1]+patchNoSubbands[j-1];
		}

		final int[] limTable = new int[patches+N_low];
		int k, limCount;
		float octaves;
		//fill N_L[i]
		for(int i = 1; i<4; i++) {
			//set up limTable
			System.arraycopy(ftRes[LO_RES], 0, limTable, 0, N_low+1);
			System.arraycopy(patchBorders, 1, limTable, N_low+1, patches-1);
			Arrays.sort(limTable, 0, patches+N_low); //needed!

			j = 1;
			limCount = patches+N_low-1;
			if(limCount<0) return;

			while(j<=limCount) {
				octaves = (limTable[j-1]==0) ? 0 : (float) limTable[j]/(float) limTable[j-1];

				if(octaves<LIMITER_BANDS_COMPARE[i-1]) {
					if(limTable[j]!=limTable[j-1]) {
						boolean found = false;
						for(k = 0; k<=patches; k++) {
							if(limTable[j]==patchBorders[k]) {
								found = false;
								for(k = 0; k<=patches; k++) {
									if(limTable[j-1]==patchBorders[k]) found = true;
								}
								if(found) {
									j++;
									continue;
								}
								else {
									//remove (k-1)th element
									limTable[j-1] = ftRes[LO_RES][N_low];
									Arrays.sort(limTable, 0, patches+N_low);
									limCount--;
									continue;
								}
							}
						}
					}

					//remove jth element
					limTable[j] = ftRes[LO_RES][N_low];
					Arrays.sort(limTable, 0, limCount);
					limCount--;
					continue;
				}
				else j++;
			}

			N_L[i] = limCount;
			for(j = 0; j<=limCount; j++) {
				ftLim[i][j] = limTable[j]-kx;
			}
		}
	}

	private void decodeData(BitStream in, boolean stereo) throws AACException {
		if(stereo) decodeChannelPairElement(in);
		else decodeSingleChannelElement(in);

		//extended data
		if(in.readBool()) {
			psExtensionRead = false;
			int count = in.readBits(4);
			if(count==15) count += in.readBits(8);

			int bitsLeft = 8*count;
			while(bitsLeft>7) {
				bitsLeft -= 2;
				extensionID = in.readBits(2);
				if(extensionID==EXTENSION_ID_PS&&psExtensionRead) extensionID = 3;
				bitsLeft -= decodeExtension(in, extensionID);
			}
			if(bitsLeft>0) in.skipBits(bitsLeft);
		}
	}

	private void decodeSingleChannelElement(BitStream in) throws AACException {
		if(in.readBool()) in.skipBits(4); //reserved

		cd[0].decodeGrid(in);
		cd[0].decodeDTDF(in);
		cd[0].decodeInvfMode(in, N_Q);
		cd[0].decodeEnvelope(in, this, 0, coupling, header.getAmpRes());
		cd[0].decodeNoise(in, this, 0, coupling);
		cd[0].decodeSinusoidalCoding(in, N_high);

		dequantEnvelopeNoise(0);
	}

	private void decodeChannelPairElement(BitStream in) throws AACException {
		if(in.readBool()) in.skipBits(8); //reserved

		final boolean ampRes = header.getAmpRes();
		if(coupling = in.readBool()) {
			cd[0].decodeGrid(in);
			cd[1].copyGrid(cd[0]);
			cd[0].decodeDTDF(in);
			cd[1].decodeDTDF(in);
			cd[0].decodeInvfMode(in, N_Q);
			cd[1].copyInvfMode(cd[0], N_Q);
			cd[0].decodeEnvelope(in, this, 0, coupling, ampRes);
			cd[0].decodeNoise(in, this, 0, coupling);
			cd[1].decodeEnvelope(in, this, 1, coupling, ampRes);
			cd[1].decodeNoise(in, this, 1, coupling);
		}
		else {
			cd[0].decodeGrid(in);
			cd[1].decodeGrid(in);
			cd[0].decodeDTDF(in);
			cd[1].decodeDTDF(in);
			cd[0].decodeInvfMode(in, N_Q);
			cd[1].decodeInvfMode(in, N_Q);
			cd[0].decodeEnvelope(in, this, 0, coupling, ampRes);
			cd[1].decodeEnvelope(in, this, 1, coupling, ampRes);
			cd[0].decodeNoise(in, this, 0, coupling);
			cd[1].decodeNoise(in, this, 1, coupling);
		}

		cd[0].decodeSinusoidalCoding(in, N_high);
		cd[1].decodeSinusoidalCoding(in, N_high);

		dequantEnvelopeNoise(0);
		dequantEnvelopeNoise(1);

		if(coupling) unmapEnvelopeNoise();
	}

	private int decodeExtension(BitStream in, int extensionID) throws AACException {
		int ret;

		switch(extensionID) {
			case EXTENSION_ID_PS:
				if(!psExtensionRead) {
					psExtensionRead = true;
					if(ps==null) ps = new PS();
					ret = ps.decode(in);
					if(!psUsed&&ps.hasHeader()) psUsed = true;
				}
				else ret = 0;
				break;
			default:
				extensionData = in.readBits(6);
				ret = 6;
				break;
		}
		return ret;
	}

	/* ================== dequant/unmap ================== */
	//dequantizes envelope and noise values
	private void dequantEnvelopeNoise(int ch) {
		final ChannelData c = cd[ch];
		if(!coupling) {
			final int amp = c.ampRes ? 0 : 1;
			int exp, i, j;

			for(i = 0; i<c.L_E; i++) {
				for(j = 0; j<n[c.f[i] ? 1 : 0]; j++) {
					exp = c.E[j][i]>>amp;
					if((exp<0)||(exp>=64)) c.E_orig[j][i] = 0;
					else {
						c.E_orig[j][i] = ENVELOPE_DEQUANT_TABLE[exp];
						if(amp!=0&&(c.E[j][i]&1)==1) c.E_orig[j][i] *= SQRT2;
					}
				}
			}

			for(i = 0; i<c.L_Q; i++) {
				for(j = 0; j<N_Q; j++) {
					c.Q_div[j][i] = calculateQDiv(ch, j, i);
					c.Q_div2[j][i] = calculateQDiv2(ch, j, i);
				}
			}
		}
	}

	//unmaps envelope and noise values
	private void unmapEnvelopeNoise() {
		final int amp0 = cd[0].ampRes ? 0 : 1;
		final int amp1 = cd[1].ampRes ? 0 : 1;

		int i, j, exp0, exp1;
		float tmp;
		for(i = 0; i<cd[0].L_E; i++) {
			for(j = 0; j<n[cd[0].f[i] ? 1 : 0]; j++) {
				exp0 = (cd[0].E[j][i]>>amp0)+1;
				exp1 = (cd[1].E[j][i]>>amp1);

				if((exp0<0)||(exp0>=64)||(exp1<0)||(exp1>24)) {
					cd[1].E_orig[j][i] = 0;
					cd[0].E_orig[j][i] = 0;
				}
				else {
					tmp = ENVELOPE_DEQUANT_TABLE[exp0];
					if(amp0!=0&&(cd[0].E[j][i]&1)==1) tmp *= SQRT2;

					//panning
					cd[0].E_orig[j][i] = tmp*ENVELOPE_PANNING_TABLE[exp1];
					cd[1].E_orig[j][i] = tmp*ENVELOPE_PANNING_TABLE[24-exp1];
				}
			}
		}

		for(i = 0; i<cd[0].L_Q; i++) {
			for(j = 0; j<N_Q; j++) {
				cd[0].Q_div[j][i] = calculateQDiv(0, j, i);
				cd[1].Q_div[j][i] = calculateQDiv(1, j, i);
				cd[0].Q_div2[j][i] = calculateQDiv2(0, j, i);
				cd[1].Q_div2[j][i] = calculateQDiv2(1, j, i);
			}
		}
	}

	//calculates 1/(1+Q), [0..1]
	private float calculateQDiv(int ch, int m, int l) {
		if(coupling) {
			if((cd[0].Q[m][l]<0||cd[0].Q[m][l]>30)||(cd[1].Q[m][l]<0||cd[1].Q[m][l]>24)) return 0;
			else {
				if(ch==0) return Q_DIV_TABLE_LEFT[cd[0].Q[m][l]][cd[1].Q[m][l]>>1];
				else return Q_DIV_TABLE_RIGHT[cd[0].Q[m][l]][cd[1].Q[m][l]>>1];
			}
		}
		else {
			if(cd[ch].Q[m][l]<0||cd[ch].Q[m][l]>30) return 0;
			else return Q_DIV_TABLE[cd[ch].Q[m][l]];
		}
	}

	//calculates Q/(1+Q), [0..1]
	private float calculateQDiv2(int ch, int m, int l) {
		if(coupling) {
			if((cd[0].Q[m][l]<0||cd[0].Q[m][l]>30)||(cd[1].Q[m][l]<0||cd[1].Q[m][l]>24)) return 0;
			else {
				if(ch==0) return Q_DIV2_TABLE_LEFT[cd[0].Q[m][l]][cd[1].Q[m][l]>>1];
				else return Q_DIV2_TABLE_RIGHT[cd[0].Q[m][l]][cd[1].Q[m][l]>>1];
			}
		}
		else {
			if(cd[ch].Q[m][l]<0||cd[ch].Q[m][l]>30) return 0;
			else return Q_DIV2_TABLE[cd[ch].Q[m][l]];
		}
	}

	/* ================== processing ================== */
	public boolean isPSUsed() {
		return psUsed;
	}

	public void processSingleFrame(float[] channel, boolean downSampled) {
		if(buffer==null) buffer = new float[TIME_SLOTS_RATE][64][2];
		boolean process = true;

		//can't decode before the first header
		if(!header.isDecoded()) {
			//don't process just upsample
			process = false;
			//re-activate reset for next frame
			//if(reset) header.getStartFrequency(true) = -1;
		}

		processChannel(channel, buffer, 0, process);
		//subband synthesis
		if(downSampled) qmfs[0].performSynthesis32(buffer, channel, TIME_SLOTS_RATE);
		else qmfs[0].performSynthesis64(buffer, channel, TIME_SLOTS_RATE);

		if(header.isDecoded()) savePreviousData(0);

		cd[0].saveMatrix();
	}

	public void processCoupleFrame(float[] left, float[] right, boolean downSampled) {
		if(buffer==null) buffer = new float[TIME_SLOTS_RATE][64][2];
		boolean process = true;

		if(!header.isDecoded()) {
			//don't process just upsample
			process = false;
			//re-activate reset for next frame
			//if(reset) startFrequencyPrev = -1;
		}

		processChannel(left, buffer, 0, process);
		if(downSampled) qmfs[0].performSynthesis32(buffer, left, TIME_SLOTS_RATE);
		else qmfs[0].performSynthesis64(buffer, left, TIME_SLOTS_RATE);

		if(qmfs[1]==null) qmfs[1] = new QMFSynthesis(filterBank, downSampled ? 32 : 64);

		processChannel(right, buffer, 1, process);
		if(downSampled) qmfs[1].performSynthesis32(buffer, right, TIME_SLOTS_RATE);
		else qmfs[1].performSynthesis64(buffer, right, TIME_SLOTS_RATE);

		if(header.isDecoded()) {
			savePreviousData(0);
			savePreviousData(1);
		}

		cd[0].saveMatrix();
		cd[1].saveMatrix();
	}

	public void processSingleFramePS(float[] left, float[] right, boolean downSampled) throws AACException {
		if(bufLeftPS==null) {
			bufLeftPS = new float[38][64][2];
			bufRightPS = new float[38][64][2];
		}
		boolean process = true;
		if(!header.isDecoded()) {
			//don't process just upsample
			process = false;
			//re-activate reset for next frame
			//startFrequencyPrev = -1;
		}

		processChannel(left, bufLeftPS, 0, process);

		//copy extra data for PS
		int k;
		for(int l = TIME_SLOTS_RATE; l<TIME_SLOTS_RATE+6; l++) {
			for(k = 0; k<5; k++) {
				bufLeftPS[l][k][0] = cd[0].Xsbr[T_HFADJ+l][k][0];
				bufLeftPS[l][k][1] = cd[0].Xsbr[T_HFADJ+l][k][1];
			}
		}

		//perform parametric stereo
		ps.process(bufLeftPS, bufRightPS, kx+M);

		if(qmfs[1]==null) qmfs[1] = new QMFSynthesis(filterBank, downSampled ? 32 : 64);
		//subband synthesis
		if(downSampled) {
			qmfs[0].performSynthesis32(bufLeftPS, left, TIME_SLOTS_RATE);
			qmfs[1].performSynthesis32(bufRightPS, right, TIME_SLOTS_RATE);
		}
		else {
			qmfs[0].performSynthesis64(bufLeftPS, left, TIME_SLOTS_RATE);
			qmfs[1].performSynthesis64(bufRightPS, right, TIME_SLOTS_RATE);
		}

		if(header.isDecoded()) savePreviousData(0);
		cd[0].saveMatrix();
	}

	private void processChannel(float[] channel, float[][][] X, int ch, boolean process) {
		int i, j;

		//subband analysis
		final int param = process ? kx : 32;
		if(qmfa[ch]==null) qmfa[ch] = new QMFAnalysis(filterBank, 32);
		qmfa[ch].performAnalysis32(channel, cd[ch].Xsbr, T_HFGEN, param, TIME_SLOTS_RATE);

		if(process) {
			hfGen.process(cd[ch].Xsbr, cd[ch].Xsbr, ch, cd[ch]);
			hfAdj.process(cd[ch].Xsbr, cd[ch], header);

			int kx_band, M_band;
			for(i = 0; i<TIME_SLOTS_RATE; i++) {
				if(i<cd[ch].t_E[0]) {
					kx_band = kxPrev;
					M_band = Mprev;
				}
				else {
					kx_band = kx;
					M_band = M;
				}

				for(j = 0; j<kx_band; j++) {
					X[i][j][0] = cd[ch].Xsbr[i+T_HFADJ][j][0];
					X[i][j][1] = cd[ch].Xsbr[i+T_HFADJ][j][1];
				}
				for(j = kx_band; j<kx_band+M_band; j++) {
					X[i][j][0] = cd[ch].Xsbr[i+T_HFADJ][j][0];
					X[i][j][1] = cd[ch].Xsbr[i+T_HFADJ][j][1];
				}
				for(j = kx_band+M_band; j<64; j++) {
					X[i][j][0] = 0;
					X[i][j][1] = 0;
				}
			}
		}
		else {
			for(i = 0; i<TIME_SLOTS_RATE; i++) {
				for(j = 0; j<32; j++) {
					X[i][j][0] = cd[ch].Xsbr[i+T_HFADJ][j][0];
					X[i][j][1] = cd[ch].Xsbr[i+T_HFADJ][j][1];
				}
				for(j = 32; j<64; j++) {
					X[i][j][0] = 0;
					X[i][j][1] = 0;
				}
			}
		}
	}

	private void savePreviousData(int ch) {
		//save data for next frame
		kxPrev = kx;
		Mprev = M;
		cd[ch].savePreviousData();
	}
}
