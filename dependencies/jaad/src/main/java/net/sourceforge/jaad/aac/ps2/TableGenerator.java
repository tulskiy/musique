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

class TableGenerator {

	//icc quantization values
	private static final double[] ICC_DEQUANT = {
		1, 0.937, 0.84118, 0.60092, 0.36764, 0, -0.589, -1
	};
	private static final double[] ACOS_ICC_DEQUANT = {
		0, 0.35685527, 0.57133466, 0.92614472, 1.1943263, Math.PI/2, 2.2006171, Math.PI
	};
	//IID_QUANT[b] = 10^(iid[b]/20)
	private static final double[] IID_DEQUANT = {
		//default
		0.05623413251903f, 0.12589254117942f, 0.19952623149689f, 0.31622776601684f,
		0.44668359215096f, 0.63095734448019f, 0.79432823472428f, 1f,
		1.25892541179417f, 1.58489319246111f, 2.23872113856834f, 3.16227766016838f,
		5.01187233627272f, 7.94328234724282f, 17.7827941003892f,
		//fine
		0.00316227766017f, 0.00562341325190f, 0.01f, 0.01778279410039f,
		0.03162277660168f, 0.05623413251903f, 0.07943282347243f, 0.11220184543020f,
		0.15848931924611f, 0.22387211385683f, 0.31622776601684f, 0.39810717055350f,
		0.50118723362727f, 0.63095734448019f, 0.79432823472428f, 1f,
		1.25892541179417f, 1.58489319246111f, 1.99526231496888f, 2.51188643150958f,
		3.16227766016838f, 4.46683592150963f, 6.30957344480193f, 8.91250938133745f,
		12.5892541179417f, 17.7827941003892f, 31.6227766016838f, 56.2341325190349f,
		100f, 177.827941003892f, 316.227766016837f
	};
	private static final double SQRT2 = Math.sqrt(2);
	private static final double SQRT1_2 = Math.sqrt(0.5);
	private static final double Q_PHI = 0.39;
	private static final double[] QM = {0.43, 0.75, 0.347};
	//table 8.40
	private static final double[] F_CENTER_20 = {
		-3.0/8.0, -1.0/8.0, 1.0/8.0, 3.0/8.0, 5.0/8.0, 7.0/8.0, 5.0/4.0,
		7.0/4.0, 9.0/4.0, 11.0/4
	};
	//table 8.41
	private static final double[] F_CENTER_34 = {
		1.0/12.0, 3.0/12.0, 5.0/12.0, 7.0/12.0, 9.0/12.0, 11.0/12.0, 13.0/12.0,
		15.0/12.0, 17.0/12.0, -5.0/12.0, -3.0/12.0, -1.0/12.0, 17.0/8.0,
		19.0/8.0, 5.0/8.0, 7.0/8.0, 9.0/8.0, 11.0/8.0, 13.0/8.0, 15.0/8.0,
		9.0/4.0, 11.0/4.0, 13.0/4.0, 7.0/4.0, 17.0/4.0, 11.0/4.0, 13.0/4.0,
		15.0/4.0, 17.0/4.0, 19.0/4.0, 21.0/4.0, 15.0/4
	};
	private static final double[] IPDOPD_SIN = {0, SQRT1_2, 1, SQRT1_2, 0, -SQRT1_2, -1, -SQRT1_2};
	private static final double[] IPDOPD_COS = {1, SQRT1_2, 0, -SQRT1_2, -1, -SQRT1_2, 0, SQRT1_2};

	public static void generateMixingTables(float[][][] HA, float[][][] HB) {
		double c, c1, c2, alpha, beta, gamma, rho, mu;
		double alphac, alphas, gammac, gammas;
		int icc;

		for(int iid = 0; iid<46; iid++) {
			c = IID_DEQUANT[iid];
			c1 = SQRT2/Math.sqrt(1.0f+c*c);
			c2 = c*c1;
			for(icc = 0; icc<8; icc++) {
				//filter A
				alpha = 0.5*ACOS_ICC_DEQUANT[icc];
				beta = alpha*(c1-c2)/(float) SQRT2;
				HA[iid][icc][0] = (float) (c2*Math.cos(alpha+beta));
				HA[iid][icc][1] = (float) (c1*Math.cos(beta-alpha));
				HA[iid][icc][2] = (float) (c2*Math.sin(alpha+beta));
				HA[iid][icc][3] = (float) (c1*Math.sin(beta-alpha));
				//filter B
				rho = Math.max(ICC_DEQUANT[icc], 0.05f);
				alpha = (c==1) ? (Math.PI/4.0) : 0.5*Math.atan2((2.0f*c*rho), (c*c-1.0f));
				if(alpha<0) alpha += Math.PI/2;
				mu = c+1.0f/c;
				mu = Math.sqrt(1+(4*rho*rho-4)/(mu*mu));
				gamma = Math.atan(Math.sqrt((1.0f-mu)/(1.0f+mu)));
				alphac = Math.cos(alpha);
				alphas = Math.sin(alpha);
				gammac = Math.cos(gamma);
				gammas = Math.sin(gamma);
				HB[iid][icc][0] = (float) (SQRT2*alphac*gammac);
				HB[iid][icc][1] = (float) (SQRT2*alphas*gammac);
				HB[iid][icc][2] = (float) (-SQRT2*alphas*gammas);
				HB[iid][icc][3] = (float) (SQRT2*alphac*gammas);
			}
		}
	}

	public static void generateFractTables20(float[][] phiFract, float[][][] qFractAllpass) {
		double fk, tmp;
		int m;
		for(int k = 0; k<30; k++) {
			if(k<F_CENTER_20.length) fk = F_CENTER_20[k];
			else fk = k-6.5;
			tmp = -Math.PI*Q_PHI*fk;
			phiFract[k][0] = (float) Math.cos(tmp);
			phiFract[k][1] = (float) Math.sin(tmp);
			for(m = 0; m<3; m++) {
				tmp = -Math.PI*QM[m]*fk;
				qFractAllpass[k][m][0] = (float) Math.cos(tmp);
				qFractAllpass[k][m][1] = (float) Math.sin(tmp);
			}
		}
	}

	public static void generateFractTables34(float[][] phiFract, float[][][] qFractAllpass) {
		double fk, tmp;
		int m;
		for(int k = 0; k<50; k++) {
			if(k<F_CENTER_34.length) fk = F_CENTER_34[k];
			else fk = k-26.5;
			tmp = -Math.PI*Q_PHI*fk;
			phiFract[k][0] = (float) Math.cos(tmp);
			phiFract[k][1] = (float) Math.sin(tmp);
			for(m = 0; m<3; m++) {
				tmp = -Math.PI*QM[m]*fk;
				qFractAllpass[k][m][0] = (float) Math.cos(tmp);
				qFractAllpass[k][m][1] = (float) Math.sin(tmp);
			}
		}
	}

	public static void generateIPDOPDSmoothingTables(float[][] table) {
		int i, j, k;
		final double[] tmp0 = new double[2];
		final double[] tmp1 = new double[2];
		final double[] tmp2 = new double[2];
		final double[] smooth = new double[2];
		double mag;

		for(i = 0; i<8; i++) {
			tmp0[0] = IPDOPD_COS[i];
			tmp0[1] = IPDOPD_SIN[i];
			for(j = 0; j<8; j++) {
				tmp1[0] = IPDOPD_COS[j];
				tmp1[1] = IPDOPD_SIN[j];
				for(k = 0; k<8; k++) {
					tmp2[0] = IPDOPD_COS[k];
					tmp2[1] = IPDOPD_SIN[k];
					smooth[0] = 0.25f*tmp0[0]+0.5f*tmp1[0]+tmp2[0];
					smooth[1] = 0.25f*tmp0[1]+0.5f*tmp1[1]+tmp2[1];
					mag = 1/Math.sqrt(smooth[1]*smooth[1]+smooth[0]*smooth[0]);
					table[i*64+j*8+k][0] = (float) (smooth[0]*mag);
					table[i*64+j*8+k][1] = (float) (smooth[1]*mag);
				}
			}
		}
	}
}
