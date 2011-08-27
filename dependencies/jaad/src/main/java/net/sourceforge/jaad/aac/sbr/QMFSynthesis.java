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

/**
 * Quadrature mirror filter for analysis
 * @author in-somnia
 */
class QMFSynthesis implements FilterbankTables {

	private static final float SCALE = 1.0f/64.0f;
	private final Filterbank filterBank;
	private final float[] v;
	private final float[][] x, tmpIn1, tmpOut1, tmpIn2, tmpOut2;
	private int vIndex;
	private final float[] buf; //buffer for DCT

	QMFSynthesis(Filterbank filterBank, int channels) {
		this.filterBank = filterBank;
		v = new float[40*channels];
		x = new float[2][32]; //reverse complex to pass 1D-array to DCT and DST
		tmpIn1 = new float[32][2];
		tmpOut1 = new float[32][2];
		tmpIn2 = new float[32][2];
		tmpOut2 = new float[32][2];
		vIndex = 0;
		buf = new float[398];
	}

	void performSynthesis32(float[][][] in, float[] out, int len) {
		int n, k, off = 0;
		float d;

		for(int l = 0; l<len; l++) {
			//calculate 64 samples
			//complex pre-twiddle
			for(k = 0; k<32; k++) {
				x[0][k] = (in[l][k][0]*QMF32_PRE_TWIDDLE[k][0])-(in[l][k][1]*QMF32_PRE_TWIDDLE[k][1]);
				x[1][k] = (in[l][k][1]*QMF32_PRE_TWIDDLE[k][0])+(in[l][k][0]*QMF32_PRE_TWIDDLE[k][1]);

				x[0][k] *= SCALE;
				x[1][k] *= SCALE;
			}

			//transform
			computeDCT(x[0]);
			computeDST(x[1]);

			for(n = 0; n<32; n++) {
				d = -x[0][n]+x[1][n];
				v[vIndex+n] = d;
				v[vIndex+640+n] = d;
				d = x[0][n]+x[1][n];
				v[vIndex+63-n] = d;
				v[vIndex+640+63-n] = d;
			}

			//calculate 32 output samples and window
			for(k = 0; k<32; k++) {
				out[off++] = (v[vIndex+k]*QMF_C[2*k])
						+(v[vIndex+96+k]*QMF_C[64+2*k])
						+(v[vIndex+128+k]*QMF_C[128+2*k])
						+(v[vIndex+224+k]*QMF_C[192+2*k])
						+(v[vIndex+256+k]*QMF_C[256+2*k])
						+(v[vIndex+352+k]*QMF_C[320+2*k])
						+(v[vIndex+384+k]*QMF_C[384+2*k])
						+(v[vIndex+480+k]*QMF_C[448+2*k])
						+(v[vIndex+512+k]*QMF_C[512+2*k])
						+(v[vIndex+608+k]*QMF_C[576+2*k]);
			}

			//update ringbuffer index
			vIndex -= 64;
			if(vIndex<0) vIndex = (640-64);
		}
	}

	void performSynthesis64(float[][][] in, float[] out, int len) {
		float[][] pX;
		int buf1, buf2;
		int n, k, off = 0;

		for(int l = 0; l<len; l++) {
			//calculate 128 samples
			pX = in[l];
			tmpIn1[31][1] = SCALE*pX[1][0];
			tmpIn1[0][0] = SCALE*pX[0][0];
			tmpIn2[31][1] = SCALE*pX[63-1][1];
			tmpIn2[0][0] = SCALE*pX[63-0][1];
			for(k = 1; k<31; k++) {
				tmpIn1[31-k][1] = SCALE*pX[2*k+1][0];
				tmpIn1[k][0] = SCALE*pX[2*k][0];
				tmpIn2[31-k][1] = SCALE*pX[63-(2*k+1)][1];
				tmpIn2[k][0] = SCALE*pX[63-(2*k)][1];
			}
			tmpIn1[0][1] = SCALE*pX[63][0];
			tmpIn1[31][0] = SCALE*pX[62][0];
			tmpIn2[0][1] = SCALE*pX[63-63][1];
			tmpIn2[31][0] = SCALE*pX[63-62][1];

			filterBank.computeDCT4Kernel(tmpIn1, tmpOut1);
			filterBank.computeDCT4Kernel(tmpIn2, tmpOut2);

			buf1 = vIndex;
			buf2 = vIndex+1280;

			for(n = 0; n<32; n++) {
				v[buf1+2*n] = v[buf2+2*n] = tmpOut2[n][0]-tmpOut1[n][0];
				v[buf1+127-2*n] = v[buf2+127-2*n] = tmpOut2[n][0]+tmpOut1[n][0];
				v[buf1+2*n+1] = v[buf2+2*n+1] = tmpOut2[31-n][1]+tmpOut1[31-n][1];
				v[buf1+127-(2*n+1)] = v[buf2+127-(2*n+1)] = tmpOut2[31-n][1]-tmpOut1[31-n][1];
			}

			buf1 = vIndex;
			//calculate 64 output samples and window
			for(k = 0; k<64; k++) {
				out[off++] = (v[buf1+k+0]*QMF_C[k+0])
						+(v[buf1+k+192]*QMF_C[k+64])
						+(v[buf1+k+256]*QMF_C[k+128])
						+(v[buf1+k+(256+192)]*QMF_C[k+192])
						+(v[buf1+k+512]*QMF_C[k+256])
						+(v[buf1+k+(512+192)]*QMF_C[k+320])
						+(v[buf1+k+768]*QMF_C[k+384])
						+(v[buf1+k+(768+192)]*QMF_C[k+448])
						+(v[buf1+k+1024]*QMF_C[k+512])
						+(v[buf1+k+(1024+192)]*QMF_C[k+576]);
			}

			//update ringbuffer index
			vIndex -= 128;
			if(vIndex<0) vIndex = (1280-128);
		}
	}

	//real DCT-IV of length 32, inplace
	private void computeDCT(float[] in) {
		buf[0] = in[15]-in[16];
		buf[1] = in[15]+in[16];
		buf[2] = DCT_TABLE[0]*buf[1];
		buf[3] = DCT_TABLE[1]*buf[0];
		buf[4] = in[8]-in[23];
		buf[5] = in[8]+in[23];
		buf[6] = DCT_TABLE[2]*buf[5];
		buf[7] = DCT_TABLE[3]*buf[4];
		buf[8] = in[12]-in[19];
		buf[9] = in[12]+in[19];
		buf[10] = DCT_TABLE[4]*buf[9];
		buf[11] = DCT_TABLE[5]*buf[8];
		buf[12] = in[11]-in[20];
		buf[13] = in[11]+in[20];
		buf[14] = DCT_TABLE[6]*buf[13];
		buf[15] = DCT_TABLE[7]*buf[12];
		buf[16] = in[14]-in[17];
		buf[17] = in[14]+in[17];
		buf[18] = DCT_TABLE[8]*buf[17];
		buf[19] = DCT_TABLE[9]*buf[16];
		buf[20] = in[9]-in[22];
		buf[21] = in[9]+in[22];
		buf[22] = DCT_TABLE[10]*buf[21];
		buf[23] = DCT_TABLE[11]*buf[20];
		buf[24] = in[13]-in[18];
		buf[25] = in[13]+in[18];
		buf[26] = DCT_TABLE[12]*buf[25];
		buf[27] = DCT_TABLE[13]*buf[24];
		buf[28] = in[10]-in[21];
		buf[29] = in[10]+in[21];
		buf[30] = DCT_TABLE[14]*buf[29];
		buf[31] = DCT_TABLE[15]*buf[28];
		buf[32] = in[0]-buf[2];
		buf[33] = in[0]+buf[2];
		buf[34] = in[31]-buf[3];
		buf[35] = in[31]+buf[3];
		buf[36] = in[7]-buf[6];
		buf[37] = in[7]+buf[6];
		buf[38] = in[24]-buf[7];
		buf[39] = in[24]+buf[7];
		buf[40] = in[3]-buf[10];
		buf[41] = in[3]+buf[10];
		buf[42] = in[28]-buf[11];
		buf[43] = in[28]+buf[11];
		buf[44] = in[4]-buf[14];
		buf[45] = in[4]+buf[14];
		buf[46] = in[27]-buf[15];
		buf[47] = in[27]+buf[15];
		buf[48] = in[1]-buf[18];
		buf[49] = in[1]+buf[18];
		buf[50] = in[30]-buf[19];
		buf[51] = in[30]+buf[19];
		buf[52] = in[6]-buf[22];
		buf[53] = in[6]+buf[22];
		buf[54] = in[25]-buf[23];
		buf[55] = in[25]+buf[23];
		buf[56] = in[2]-buf[26];
		buf[57] = in[2]+buf[26];
		buf[58] = in[29]-buf[27];
		buf[59] = in[29]+buf[27];
		buf[60] = in[5]-buf[30];
		buf[61] = in[5]+buf[30];
		buf[62] = in[26]-buf[31];
		buf[63] = in[26]+buf[31];
		buf[64] = buf[39]+buf[37];
		buf[65] = DCT_TABLE[16]*buf[39];
		buf[66] = DCT_TABLE[17]*buf[64];
		buf[67] = DCT_TABLE[18]*buf[37];
		buf[68] = buf[65]+buf[66];
		buf[69] = buf[67]-buf[66];
		buf[70] = buf[38]+buf[36];
		buf[71] = DCT_TABLE[19]*buf[38];
		buf[72] = DCT_TABLE[20]*buf[70];
		buf[73] = DCT_TABLE[21]*buf[36];
		buf[74] = buf[71]+buf[72];
		buf[75] = buf[73]-buf[72];
		buf[76] = buf[47]+buf[45];
		buf[77] = DCT_TABLE[22]*buf[47];
		buf[78] = DCT_TABLE[23]*buf[76];
		buf[79] = DCT_TABLE[24]*buf[45];
		buf[80] = buf[77]+buf[78];
		buf[81] = buf[79]-buf[78];
		buf[82] = buf[46]+buf[44];
		buf[83] = DCT_TABLE[25]*buf[46];
		buf[84] = DCT_TABLE[26]*buf[82];
		buf[85] = DCT_TABLE[27]*buf[44];
		buf[86] = buf[83]+buf[84];
		buf[87] = buf[85]-buf[84];
		buf[88] = buf[55]+buf[53];
		buf[89] = DCT_TABLE[28]*buf[55];
		buf[90] = DCT_TABLE[29]*buf[88];
		buf[91] = DCT_TABLE[30]*buf[53];
		buf[92] = buf[89]+buf[90];
		buf[93] = buf[91]-buf[90];
		buf[94] = buf[54]+buf[52];
		buf[95] = DCT_TABLE[31]*buf[54];
		buf[96] = DCT_TABLE[32]*buf[94];
		buf[97] = DCT_TABLE[33]*buf[52];
		buf[98] = buf[95]+buf[96];
		buf[99] = buf[97]-buf[96];
		buf[100] = buf[63]+buf[61];
		buf[101] = DCT_TABLE[34]*buf[63];
		buf[102] = DCT_TABLE[35]*buf[100];
		buf[103] = DCT_TABLE[36]*buf[61];
		buf[104] = buf[101]+buf[102];
		buf[105] = buf[103]-buf[102];
		buf[106] = buf[62]+buf[60];
		buf[107] = DCT_TABLE[37]*buf[62];
		buf[108] = DCT_TABLE[38]*buf[106];
		buf[109] = DCT_TABLE[39]*buf[60];
		buf[110] = buf[107]+buf[108];
		buf[111] = buf[109]-buf[108];
		buf[112] = buf[33]-buf[68];
		buf[113] = buf[33]+buf[68];
		buf[114] = buf[35]-buf[69];
		buf[115] = buf[35]+buf[69];
		buf[116] = buf[32]-buf[74];
		buf[117] = buf[32]+buf[74];
		buf[118] = buf[34]-buf[75];
		buf[119] = buf[34]+buf[75];
		buf[120] = buf[41]-buf[80];
		buf[121] = buf[41]+buf[80];
		buf[122] = buf[43]-buf[81];
		buf[123] = buf[43]+buf[81];
		buf[124] = buf[40]-buf[86];
		buf[125] = buf[40]+buf[86];
		buf[126] = buf[42]-buf[87];
		buf[127] = buf[42]+buf[87];
		buf[128] = buf[49]-buf[92];
		buf[129] = buf[49]+buf[92];
		buf[130] = buf[51]-buf[93];
		buf[131] = buf[51]+buf[93];
		buf[132] = buf[48]-buf[98];
		buf[133] = buf[48]+buf[98];
		buf[134] = buf[50]-buf[99];
		buf[135] = buf[50]+buf[99];
		buf[136] = buf[57]-buf[104];
		buf[137] = buf[57]+buf[104];
		buf[138] = buf[59]-buf[105];
		buf[139] = buf[59]+buf[105];
		buf[140] = buf[56]-buf[110];
		buf[141] = buf[56]+buf[110];
		buf[142] = buf[58]-buf[111];
		buf[143] = buf[58]+buf[111];
		buf[144] = buf[123]+buf[121];
		buf[145] = DCT_TABLE[40]*buf[123];
		buf[146] = DCT_TABLE[41]*buf[144];
		buf[147] = DCT_TABLE[42]*buf[121];
		buf[148] = buf[145]+buf[146];
		buf[149] = buf[147]-buf[146];
		buf[150] = buf[127]+buf[125];
		buf[151] = DCT_TABLE[43]*buf[127];
		buf[152] = DCT_TABLE[44]*buf[150];
		buf[153] = DCT_TABLE[45]*buf[125];
		buf[154] = buf[151]+buf[152];
		buf[155] = buf[153]-buf[152];
		buf[156] = buf[122]+buf[120];
		buf[157] = DCT_TABLE[46]*buf[122];
		buf[158] = DCT_TABLE[47]*buf[156];
		buf[159] = DCT_TABLE[48]*buf[120];
		buf[160] = buf[157]+buf[158];
		buf[161] = buf[159]-buf[158];
		buf[162] = buf[126]+buf[124];
		buf[163] = DCT_TABLE[49]*buf[126];
		buf[164] = DCT_TABLE[50]*buf[162];
		buf[165] = DCT_TABLE[51]*buf[124];
		buf[166] = buf[163]+buf[164];
		buf[167] = buf[165]-buf[164];
		buf[168] = buf[139]+buf[137];
		buf[169] = DCT_TABLE[52]*buf[139];
		buf[170] = DCT_TABLE[53]*buf[168];
		buf[171] = DCT_TABLE[54]*buf[137];
		buf[172] = buf[169]+buf[170];
		buf[173] = buf[171]-buf[170];
		buf[174] = buf[143]+buf[141];
		buf[175] = DCT_TABLE[55]*buf[143];
		buf[176] = DCT_TABLE[56]*buf[174];
		buf[177] = DCT_TABLE[57]*buf[141];
		buf[178] = buf[175]+buf[176];
		buf[179] = buf[177]-buf[176];
		buf[180] = buf[138]+buf[136];
		buf[181] = DCT_TABLE[58]*buf[138];
		buf[182] = DCT_TABLE[59]*buf[180];
		buf[183] = DCT_TABLE[60]*buf[136];
		buf[184] = buf[181]+buf[182];
		buf[185] = buf[183]-buf[182];
		buf[186] = buf[142]+buf[140];
		buf[187] = DCT_TABLE[61]*buf[142];
		buf[188] = DCT_TABLE[62]*buf[186];
		buf[189] = DCT_TABLE[63]*buf[140];
		buf[190] = buf[187]+buf[188];
		buf[191] = buf[189]-buf[188];
		buf[192] = buf[113]-buf[148];
		buf[193] = buf[113]+buf[148];
		buf[194] = buf[115]-buf[149];
		buf[195] = buf[115]+buf[149];
		buf[196] = buf[117]-buf[154];
		buf[197] = buf[117]+buf[154];
		buf[198] = buf[119]-buf[155];
		buf[199] = buf[119]+buf[155];
		buf[200] = buf[112]-buf[160];
		buf[201] = buf[112]+buf[160];
		buf[202] = buf[114]-buf[161];
		buf[203] = buf[114]+buf[161];
		buf[204] = buf[116]-buf[166];
		buf[205] = buf[116]+buf[166];
		buf[206] = buf[118]-buf[167];
		buf[207] = buf[118]+buf[167];
		buf[208] = buf[129]-buf[172];
		buf[209] = buf[129]+buf[172];
		buf[210] = buf[131]-buf[173];
		buf[211] = buf[131]+buf[173];
		buf[212] = buf[133]-buf[178];
		buf[213] = buf[133]+buf[178];
		buf[214] = buf[135]-buf[179];
		buf[215] = buf[135]+buf[179];
		buf[216] = buf[128]-buf[184];
		buf[217] = buf[128]+buf[184];
		buf[218] = buf[130]-buf[185];
		buf[219] = buf[130]+buf[185];
		buf[220] = buf[132]-buf[190];
		buf[221] = buf[132]+buf[190];
		buf[222] = buf[134]-buf[191];
		buf[223] = buf[134]+buf[191];
		buf[224] = buf[211]+buf[209];
		buf[225] = DCT_TABLE[64]*buf[211];
		buf[226] = DCT_TABLE[65]*buf[224];
		buf[227] = DCT_TABLE[66]*buf[209];
		buf[228] = buf[225]+buf[226];
		buf[229] = buf[227]-buf[226];
		buf[230] = buf[215]+buf[213];
		buf[231] = DCT_TABLE[67]*buf[215];
		buf[232] = DCT_TABLE[68]*buf[230];
		buf[233] = DCT_TABLE[69]*buf[213];
		buf[234] = buf[231]+buf[232];
		buf[235] = buf[233]-buf[232];
		buf[236] = buf[219]+buf[217];
		buf[237] = DCT_TABLE[70]*buf[219];
		buf[238] = DCT_TABLE[71]*buf[236];
		buf[239] = DCT_TABLE[72]*buf[217];
		buf[240] = buf[237]+buf[238];
		buf[241] = buf[239]-buf[238];
		buf[242] = buf[223]+buf[221];
		buf[243] = DCT_TABLE[73]*buf[223];
		buf[244] = DCT_TABLE[74]*buf[242];
		buf[245] = DCT_TABLE[75]*buf[221];
		buf[246] = buf[243]+buf[244];
		buf[247] = buf[245]-buf[244];
		buf[248] = buf[210]+buf[208];
		buf[249] = DCT_TABLE[76]*buf[210];
		buf[250] = DCT_TABLE[77]*buf[248];
		buf[251] = DCT_TABLE[78]*buf[208];
		buf[252] = buf[249]+buf[250];
		buf[253] = buf[251]-buf[250];
		buf[254] = buf[214]+buf[212];
		buf[255] = DCT_TABLE[79]*buf[214];
		buf[256] = DCT_TABLE[80]*buf[254];
		buf[257] = DCT_TABLE[81]*buf[212];
		buf[258] = buf[255]+buf[256];
		buf[259] = buf[257]-buf[256];
		buf[260] = buf[218]+buf[216];
		buf[261] = DCT_TABLE[82]*buf[218];
		buf[262] = DCT_TABLE[83]*buf[260];
		buf[263] = DCT_TABLE[84]*buf[216];
		buf[264] = buf[261]+buf[262];
		buf[265] = buf[263]-buf[262];
		buf[266] = buf[222]+buf[220];
		buf[267] = DCT_TABLE[85]*buf[222];
		buf[268] = DCT_TABLE[86]*buf[266];
		buf[269] = DCT_TABLE[87]*buf[220];
		buf[270] = buf[267]+buf[268];
		buf[271] = buf[269]-buf[268];
		buf[272] = buf[193]-buf[228];
		buf[273] = buf[193]+buf[228];
		buf[274] = buf[195]-buf[229];
		buf[275] = buf[195]+buf[229];
		buf[276] = buf[197]-buf[234];
		buf[277] = buf[197]+buf[234];
		buf[278] = buf[199]-buf[235];
		buf[279] = buf[199]+buf[235];
		buf[280] = buf[201]-buf[240];
		buf[281] = buf[201]+buf[240];
		buf[282] = buf[203]-buf[241];
		buf[283] = buf[203]+buf[241];
		buf[284] = buf[205]-buf[246];
		buf[285] = buf[205]+buf[246];
		buf[286] = buf[207]-buf[247];
		buf[287] = buf[207]+buf[247];
		buf[288] = buf[192]-buf[252];
		buf[289] = buf[192]+buf[252];
		buf[290] = buf[194]-buf[253];
		buf[291] = buf[194]+buf[253];
		buf[292] = buf[196]-buf[258];
		buf[293] = buf[196]+buf[258];
		buf[294] = buf[198]-buf[259];
		buf[295] = buf[198]+buf[259];
		buf[296] = buf[200]-buf[264];
		buf[297] = buf[200]+buf[264];
		buf[298] = buf[202]-buf[265];
		buf[299] = buf[202]+buf[265];
		buf[300] = buf[204]-buf[270];
		buf[301] = buf[204]+buf[270];
		buf[302] = buf[206]-buf[271];
		buf[303] = buf[206]+buf[271];
		buf[304] = buf[275]+buf[273];
		buf[305] = DCT_TABLE[88]*buf[275];
		buf[306] = DCT_TABLE[89]*buf[304];
		buf[307] = DCT_TABLE[90]*buf[273];
		in[0] = buf[305]+buf[306];
		in[31] = buf[307]-buf[306];
		buf[310] = buf[279]+buf[277];
		buf[311] = DCT_TABLE[91]*buf[279];
		buf[312] = DCT_TABLE[92]*buf[310];
		buf[313] = DCT_TABLE[93]*buf[277];
		in[2] = buf[311]+buf[312];
		in[29] = buf[313]-buf[312];
		buf[316] = buf[283]+buf[281];
		buf[317] = DCT_TABLE[94]*buf[283];
		buf[318] = DCT_TABLE[95]*buf[316];
		buf[319] = DCT_TABLE[96]*buf[281];
		in[4] = buf[317]+buf[318];
		in[27] = buf[319]-buf[318];
		buf[322] = buf[287]+buf[285];
		buf[323] = DCT_TABLE[97]*buf[287];
		buf[324] = DCT_TABLE[98]*buf[322];
		buf[325] = DCT_TABLE[99]*buf[285];
		in[6] = buf[323]+buf[324];
		in[25] = buf[325]-buf[324];
		buf[328] = buf[291]+buf[289];
		buf[329] = DCT_TABLE[100]*buf[291];
		buf[330] = DCT_TABLE[101]*buf[328];
		buf[331] = DCT_TABLE[102]*buf[289];
		in[8] = buf[329]+buf[330];
		in[23] = buf[331]-buf[330];
		buf[334] = buf[295]+buf[293];
		buf[335] = DCT_TABLE[103]*buf[295];
		buf[336] = DCT_TABLE[104]*buf[334];
		buf[337] = DCT_TABLE[105]*buf[293];
		in[10] = buf[335]+buf[336];
		in[21] = buf[337]-buf[336];
		buf[340] = buf[299]+buf[297];
		buf[341] = DCT_TABLE[106]*buf[299];
		buf[342] = DCT_TABLE[107]*buf[340];
		buf[343] = DCT_TABLE[108]*buf[297];
		in[12] = buf[341]+buf[342];
		in[19] = buf[343]-buf[342];
		buf[346] = buf[303]+buf[301];
		buf[347] = DCT_TABLE[109]*buf[303];
		buf[348] = DCT_TABLE[110]*buf[346];
		buf[349] = DCT_TABLE[111]*buf[301];
		in[14] = buf[347]+buf[348];
		in[17] = buf[349]-buf[348];
		buf[352] = buf[274]+buf[272];
		buf[353] = DCT_TABLE[112]*buf[274];
		buf[354] = DCT_TABLE[113]*buf[352];
		buf[355] = DCT_TABLE[114]*buf[272];
		in[16] = buf[353]+buf[354];
		in[15] = buf[355]-buf[354];
		buf[358] = buf[278]+buf[276];
		buf[359] = DCT_TABLE[115]*buf[278];
		buf[360] = DCT_TABLE[116]*buf[358];
		buf[361] = DCT_TABLE[117]*buf[276];
		in[18] = buf[359]+buf[360];
		in[13] = buf[361]-buf[360];
		buf[364] = buf[282]+buf[280];
		buf[365] = DCT_TABLE[118]*buf[282];
		buf[366] = DCT_TABLE[119]*buf[364];
		buf[367] = DCT_TABLE[120]*buf[280];
		in[20] = buf[365]+buf[366];
		in[11] = buf[367]-buf[366];
		buf[370] = buf[286]+buf[284];
		buf[371] = DCT_TABLE[121]*buf[286];
		buf[372] = DCT_TABLE[122]*buf[370];
		buf[373] = DCT_TABLE[123]*buf[284];
		in[22] = buf[371]+buf[372];
		in[9] = buf[373]-buf[372];
		buf[376] = buf[290]+buf[288];
		buf[377] = DCT_TABLE[124]*buf[290];
		buf[378] = DCT_TABLE[125]*buf[376];
		buf[379] = DCT_TABLE[126]*buf[288];
		in[24] = buf[377]+buf[378];
		in[7] = buf[379]-buf[378];
		buf[382] = buf[294]+buf[292];
		buf[383] = DCT_TABLE[127]*buf[294];
		buf[384] = DCT_TABLE[128]*buf[382];
		buf[385] = DCT_TABLE[129]*buf[292];
		in[26] = buf[383]+buf[384];
		in[5] = buf[385]-buf[384];
		buf[388] = buf[298]+buf[296];
		buf[389] = DCT_TABLE[130]*buf[298];
		buf[390] = DCT_TABLE[131]*buf[388];
		buf[391] = DCT_TABLE[132]*buf[296];
		in[28] = buf[389]+buf[390];
		in[3] = buf[391]-buf[390];
		buf[394] = buf[302]+buf[300];
		buf[395] = DCT_TABLE[133]*buf[302];
		buf[396] = DCT_TABLE[134]*buf[394];
		buf[397] = DCT_TABLE[135]*buf[300];
		in[30] = buf[395]+buf[396];
		in[1] = buf[397]-buf[396];
	}

	//real DST-IV of length 32, inplace
	private void computeDST(float[] in) {
		buf[0] = in[0]-in[1];
		buf[1] = in[2]-in[1];
		buf[2] = in[2]-in[3];
		buf[3] = in[4]-in[3];
		buf[4] = in[4]-in[5];
		buf[5] = in[6]-in[5];
		buf[6] = in[6]-in[7];
		buf[7] = in[8]-in[7];
		buf[8] = in[8]-in[9];
		buf[9] = in[10]-in[9];
		buf[10] = in[10]-in[11];
		buf[11] = in[12]-in[11];
		buf[12] = in[12]-in[13];
		buf[13] = in[14]-in[13];
		buf[14] = in[14]-in[15];
		buf[15] = in[16]-in[15];
		buf[16] = in[16]-in[17];
		buf[17] = in[18]-in[17];
		buf[18] = in[18]-in[19];
		buf[19] = in[20]-in[19];
		buf[20] = in[20]-in[21];
		buf[21] = in[22]-in[21];
		buf[22] = in[22]-in[23];
		buf[23] = in[24]-in[23];
		buf[24] = in[24]-in[25];
		buf[25] = in[26]-in[25];
		buf[26] = in[26]-in[27];
		buf[27] = in[28]-in[27];
		buf[28] = in[28]-in[29];
		buf[29] = in[30]-in[29];
		buf[30] = in[30]-in[31];
		buf[31] = DST_TABLE[0]*buf[15];
		buf[32] = in[0]-buf[31];
		buf[33] = in[0]+buf[31];
		buf[34] = buf[7]+buf[23];
		buf[35] = DST_TABLE[1]*buf[7];
		buf[36] = DST_TABLE[2]*buf[34];
		buf[37] = DST_TABLE[3]*buf[23];
		buf[38] = buf[35]+buf[36];
		buf[39] = buf[37]-buf[36];
		buf[40] = buf[33]-buf[39];
		buf[41] = buf[33]+buf[39];
		buf[42] = buf[32]-buf[38];
		buf[43] = buf[32]+buf[38];
		buf[44] = buf[11]-buf[19];
		buf[45] = buf[11]+buf[19];
		buf[46] = DST_TABLE[4]*buf[45];
		buf[47] = buf[3]-buf[46];
		buf[48] = buf[3]+buf[46];
		buf[49] = DST_TABLE[5]*buf[44];
		buf[50] = buf[49]-buf[27];
		buf[51] = buf[49]+buf[27];
		buf[52] = buf[51]+buf[48];
		buf[53] = DST_TABLE[6]*buf[51];
		buf[54] = DST_TABLE[7]*buf[52];
		buf[55] = DST_TABLE[8]*buf[48];
		buf[56] = buf[53]+buf[54];
		buf[57] = buf[55]-buf[54];
		buf[58] = buf[50]+buf[47];
		buf[59] = DST_TABLE[9]*buf[50];
		buf[60] = DST_TABLE[10]*buf[58];
		buf[61] = DST_TABLE[11]*buf[47];
		buf[62] = buf[59]+buf[60];
		buf[63] = buf[61]-buf[60];
		buf[64] = buf[41]-buf[56];
		buf[65] = buf[41]+buf[56];
		buf[66] = buf[43]-buf[62];
		buf[67] = buf[43]+buf[62];
		buf[68] = buf[42]-buf[63];
		buf[69] = buf[42]+buf[63];
		buf[70] = buf[40]-buf[57];
		buf[71] = buf[40]+buf[57];
		buf[72] = buf[5]-buf[9];
		buf[73] = buf[5]+buf[9];
		buf[74] = buf[13]-buf[17];
		buf[75] = buf[13]+buf[17];
		buf[76] = buf[21]-buf[25];
		buf[77] = buf[21]+buf[25];
		buf[78] = DST_TABLE[12]*buf[75];
		buf[79] = buf[1]-buf[78];
		buf[80] = buf[1]+buf[78];
		buf[81] = buf[73]+buf[77];
		buf[82] = DST_TABLE[13]*buf[73];
		buf[83] = DST_TABLE[14]*buf[81];
		buf[84] = DST_TABLE[15]*buf[77];
		buf[85] = buf[82]+buf[83];
		buf[86] = buf[84]-buf[83];
		buf[87] = buf[80]-buf[86];
		buf[88] = buf[80]+buf[86];
		buf[89] = buf[79]-buf[85];
		buf[90] = buf[79]+buf[85];
		buf[91] = DST_TABLE[16]*buf[74];
		buf[92] = buf[29]-buf[91];
		buf[93] = buf[29]+buf[91];
		buf[94] = buf[76]+buf[72];
		buf[95] = DST_TABLE[17]*buf[76];
		buf[96] = DST_TABLE[18]*buf[94];
		buf[97] = DST_TABLE[19]*buf[72];
		buf[98] = buf[95]+buf[96];
		buf[99] = buf[97]-buf[96];
		buf[100] = buf[93]-buf[99];
		buf[101] = buf[93]+buf[99];
		buf[102] = buf[92]-buf[98];
		buf[103] = buf[92]+buf[98];
		buf[104] = buf[101]+buf[88];
		buf[105] = DST_TABLE[20]*buf[101];
		buf[106] = DST_TABLE[21]*buf[104];
		buf[107] = DST_TABLE[22]*buf[88];
		buf[108] = buf[105]+buf[106];
		buf[109] = buf[107]-buf[106];
		buf[110] = buf[90]-buf[103];
		buf[111] = DST_TABLE[23]*buf[103];
		buf[112] = DST_TABLE[24]*buf[110];
		buf[113] = DST_TABLE[25]*buf[90];
		buf[114] = buf[112]-buf[111];
		buf[115] = buf[113]-buf[112];
		buf[116] = buf[102]+buf[89];
		buf[117] = DST_TABLE[26]*buf[102];
		buf[118] = DST_TABLE[27]*buf[116];
		buf[119] = DST_TABLE[28]*buf[89];
		buf[120] = buf[117]+buf[118];
		buf[121] = buf[119]-buf[118];
		buf[122] = buf[87]-buf[100];
		buf[123] = DST_TABLE[29]*buf[100];
		buf[124] = DST_TABLE[30]*buf[122];
		buf[125] = DST_TABLE[31]*buf[87];
		buf[126] = buf[124]-buf[123];
		buf[127] = buf[125]-buf[124];
		buf[128] = buf[65]-buf[108];
		buf[129] = buf[65]+buf[108];
		buf[130] = buf[67]-buf[114];
		buf[131] = buf[67]+buf[114];
		buf[132] = buf[69]-buf[120];
		buf[133] = buf[69]+buf[120];
		buf[134] = buf[71]-buf[126];
		buf[135] = buf[71]+buf[126];
		buf[136] = buf[70]-buf[127];
		buf[137] = buf[70]+buf[127];
		buf[138] = buf[68]-buf[121];
		buf[139] = buf[68]+buf[121];
		buf[140] = buf[66]-buf[115];
		buf[141] = buf[66]+buf[115];
		buf[142] = buf[64]-buf[109];
		buf[143] = buf[64]+buf[109];
		buf[144] = buf[0]+buf[30];
		buf[145] = DST_TABLE[32]*buf[0];
		buf[146] = DST_TABLE[33]*buf[144];
		buf[147] = DST_TABLE[34]*buf[30];
		buf[148] = buf[145]+buf[146];
		buf[149] = buf[147]-buf[146];
		buf[150] = buf[4]+buf[26];
		buf[151] = DST_TABLE[35]*buf[4];
		buf[152] = DST_TABLE[36]*buf[150];
		buf[153] = DST_TABLE[37]*buf[26];
		buf[154] = buf[151]+buf[152];
		buf[155] = buf[153]-buf[152];
		buf[156] = buf[8]+buf[22];
		buf[157] = DST_TABLE[38]*buf[8];
		buf[158] = DST_TABLE[39]*buf[156];
		buf[159] = DST_TABLE[40]*buf[22];
		buf[160] = buf[157]+buf[158];
		buf[161] = buf[159]-buf[158];
		buf[162] = buf[12]+buf[18];
		buf[163] = DST_TABLE[41]*buf[12];
		buf[164] = DST_TABLE[42]*buf[162];
		buf[165] = DST_TABLE[43]*buf[18];
		buf[166] = buf[163]+buf[164];
		buf[167] = buf[165]-buf[164];
		buf[168] = buf[16]+buf[14];
		buf[169] = DST_TABLE[44]*buf[16];
		buf[170] = DST_TABLE[45]*buf[168];
		buf[171] = DST_TABLE[46]*buf[14];
		buf[172] = buf[169]+buf[170];
		buf[173] = buf[171]-buf[170];
		buf[174] = buf[20]+buf[10];
		buf[175] = DST_TABLE[47]*buf[20];
		buf[176] = DST_TABLE[48]*buf[174];
		buf[177] = DST_TABLE[49]*buf[10];
		buf[178] = buf[175]+buf[176];
		buf[179] = buf[177]-buf[176];
		buf[180] = buf[24]+buf[6];
		buf[181] = DST_TABLE[50]*buf[24];
		buf[182] = DST_TABLE[51]*buf[180];
		buf[183] = DST_TABLE[52]*buf[6];
		buf[184] = buf[181]+buf[182];
		buf[185] = buf[183]-buf[182];
		buf[186] = buf[28]+buf[2];
		buf[187] = DST_TABLE[53]*buf[28];
		buf[188] = DST_TABLE[54]*buf[186];
		buf[189] = DST_TABLE[55]*buf[2];
		buf[190] = buf[187]+buf[188];
		buf[191] = buf[189]-buf[188];
		buf[192] = buf[149]-buf[173];
		buf[193] = buf[149]+buf[173];
		buf[194] = buf[148]-buf[172];
		buf[195] = buf[148]+buf[172];
		buf[196] = buf[155]-buf[179];
		buf[197] = buf[155]+buf[179];
		buf[198] = buf[154]-buf[178];
		buf[199] = buf[154]+buf[178];
		buf[200] = buf[161]-buf[185];
		buf[201] = buf[161]+buf[185];
		buf[202] = buf[160]-buf[184];
		buf[203] = buf[160]+buf[184];
		buf[204] = buf[167]-buf[191];
		buf[205] = buf[167]+buf[191];
		buf[206] = buf[166]-buf[190];
		buf[207] = buf[166]+buf[190];
		buf[208] = buf[192]+buf[194];
		buf[209] = DST_TABLE[56]*buf[192];
		buf[210] = DST_TABLE[57]*buf[208];
		buf[211] = DST_TABLE[58]*buf[194];
		buf[212] = buf[209]+buf[210];
		buf[213] = buf[211]-buf[210];
		buf[214] = buf[196]+buf[198];
		buf[215] = DST_TABLE[59]*buf[196];
		buf[216] = DST_TABLE[60]*buf[214];
		buf[217] = DST_TABLE[61]*buf[198];
		buf[218] = buf[215]+buf[216];
		buf[219] = buf[217]-buf[216];
		buf[220] = buf[200]+buf[202];
		buf[221] = DST_TABLE[62]*buf[200];
		buf[222] = DST_TABLE[63]*buf[220];
		buf[223] = DST_TABLE[64]*buf[202];
		buf[224] = buf[221]+buf[222];
		buf[225] = buf[223]-buf[222];
		buf[226] = buf[204]+buf[206];
		buf[227] = DST_TABLE[65]*buf[204];
		buf[228] = DST_TABLE[66]*buf[226];
		buf[229] = DST_TABLE[67]*buf[206];
		buf[230] = buf[227]+buf[228];
		buf[231] = buf[229]-buf[228];
		buf[232] = buf[193]-buf[201];
		buf[233] = buf[193]+buf[201];
		buf[234] = buf[195]-buf[203];
		buf[235] = buf[195]+buf[203];
		buf[236] = buf[197]-buf[205];
		buf[237] = buf[197]+buf[205];
		buf[238] = buf[199]-buf[207];
		buf[239] = buf[199]+buf[207];
		buf[240] = buf[213]-buf[225];
		buf[241] = buf[213]+buf[225];
		buf[242] = buf[212]-buf[224];
		buf[243] = buf[212]+buf[224];
		buf[244] = buf[219]-buf[231];
		buf[245] = buf[219]+buf[231];
		buf[246] = buf[218]-buf[230];
		buf[247] = buf[218]+buf[230];
		buf[248] = buf[232]+buf[234];
		buf[249] = DST_TABLE[68]*buf[232];
		buf[250] = DST_TABLE[69]*buf[248];
		buf[251] = DST_TABLE[70]*buf[234];
		buf[252] = buf[249]+buf[250];
		buf[253] = buf[251]-buf[250];
		buf[254] = buf[236]+buf[238];
		buf[255] = DST_TABLE[71]*buf[236];
		buf[256] = DST_TABLE[72]*buf[254];
		buf[257] = DST_TABLE[73]*buf[238];
		buf[258] = buf[255]+buf[256];
		buf[259] = buf[257]-buf[256];
		buf[260] = buf[240]+buf[242];
		buf[261] = DST_TABLE[74]*buf[240];
		buf[262] = DST_TABLE[75]*buf[260];
		buf[263] = DST_TABLE[76]*buf[242];
		buf[264] = buf[261]+buf[262];
		buf[265] = buf[263]-buf[262];
		buf[266] = buf[244]+buf[246];
		buf[267] = DST_TABLE[77]*buf[244];
		buf[268] = DST_TABLE[78]*buf[266];
		buf[269] = DST_TABLE[79]*buf[246];
		buf[270] = buf[267]+buf[268];
		buf[271] = buf[269]-buf[268];
		buf[272] = buf[233]-buf[237];
		buf[273] = buf[233]+buf[237];
		buf[274] = buf[235]-buf[239];
		buf[275] = buf[235]+buf[239];
		buf[276] = buf[253]-buf[259];
		buf[277] = buf[253]+buf[259];
		buf[278] = buf[252]-buf[258];
		buf[279] = buf[252]+buf[258];
		buf[280] = buf[241]-buf[245];
		buf[281] = buf[241]+buf[245];
		buf[282] = buf[243]-buf[247];
		buf[283] = buf[243]+buf[247];
		buf[284] = buf[265]-buf[271];
		buf[285] = buf[265]+buf[271];
		buf[286] = buf[264]-buf[270];
		buf[287] = buf[264]+buf[270];
		buf[288] = buf[272]-buf[274];
		buf[289] = buf[272]+buf[274];
		buf[290] = DST_TABLE[80]*buf[288];
		buf[291] = DST_TABLE[81]*buf[289];
		buf[292] = buf[276]-buf[278];
		buf[293] = buf[276]+buf[278];
		buf[294] = DST_TABLE[82]*buf[292];
		buf[295] = DST_TABLE[83]*buf[293];
		buf[296] = buf[280]-buf[282];
		buf[297] = buf[280]+buf[282];
		buf[298] = DST_TABLE[84]*buf[296];
		buf[299] = DST_TABLE[85]*buf[297];
		buf[300] = buf[284]-buf[286];
		buf[301] = buf[284]+buf[286];
		buf[302] = DST_TABLE[86]*buf[300];
		buf[303] = DST_TABLE[87]*buf[301];
		buf[304] = buf[129]-buf[273];
		buf[305] = buf[129]+buf[273];
		buf[306] = buf[131]-buf[281];
		buf[307] = buf[131]+buf[281];
		buf[308] = buf[133]-buf[285];
		buf[309] = buf[133]+buf[285];
		buf[310] = buf[135]-buf[277];
		buf[311] = buf[135]+buf[277];
		buf[312] = buf[137]-buf[295];
		buf[313] = buf[137]+buf[295];
		buf[314] = buf[139]-buf[303];
		buf[315] = buf[139]+buf[303];
		buf[316] = buf[141]-buf[299];
		buf[317] = buf[141]+buf[299];
		buf[318] = buf[143]-buf[291];
		buf[319] = buf[143]+buf[291];
		buf[320] = buf[142]-buf[290];
		buf[321] = buf[142]+buf[290];
		buf[322] = buf[140]-buf[298];
		buf[323] = buf[140]+buf[298];
		buf[324] = buf[138]-buf[302];
		buf[325] = buf[138]+buf[302];
		buf[326] = buf[136]-buf[294];
		buf[327] = buf[136]+buf[294];
		buf[328] = buf[134]-buf[279];
		buf[329] = buf[134]+buf[279];
		buf[330] = buf[132]-buf[287];
		buf[331] = buf[132]+buf[287];
		buf[332] = buf[130]-buf[283];
		buf[333] = buf[130]+buf[283];
		buf[334] = buf[128]-buf[275];
		buf[335] = buf[128]+buf[275];
		in[31] = DST_TABLE[88]*buf[305];
		in[30] = DST_TABLE[89]*buf[307];
		in[29] = DST_TABLE[90]*buf[309];
		in[28] = DST_TABLE[91]*buf[311];
		in[27] = DST_TABLE[92]*buf[313];
		in[26] = DST_TABLE[93]*buf[315];
		in[25] = DST_TABLE[94]*buf[317];
		in[24] = DST_TABLE[95]*buf[319];
		in[23] = DST_TABLE[96]*buf[321];
		in[22] = DST_TABLE[97]*buf[323];
		in[21] = DST_TABLE[98]*buf[325];
		in[20] = DST_TABLE[99]*buf[327];
		in[19] = DST_TABLE[100]*buf[329];
		in[18] = DST_TABLE[101]*buf[331];
		in[17] = DST_TABLE[102]*buf[333];
		in[16] = DST_TABLE[103]*buf[335];
		in[15] = DST_TABLE[104]*buf[334];
		in[14] = DST_TABLE[105]*buf[332];
		in[13] = DST_TABLE[106]*buf[330];
		in[12] = DST_TABLE[107]*buf[328];
		in[11] = DST_TABLE[108]*buf[326];
		in[10] = DST_TABLE[109]*buf[324];
		in[9] = DST_TABLE[110]*buf[322];
		in[8] = DST_TABLE[111]*buf[320];
		in[7] = DST_TABLE[112]*buf[318];
		in[6] = DST_TABLE[113]*buf[316];
		in[5] = DST_TABLE[114]*buf[314];
		in[4] = DST_TABLE[115]*buf[312];
		in[3] = DST_TABLE[116]*buf[310];
		in[2] = DST_TABLE[117]*buf[308];
		in[1] = DST_TABLE[118]*buf[306];
		in[0] = DST_TABLE[119]*buf[304];
	}
}
