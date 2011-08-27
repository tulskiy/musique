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
class QMFAnalysis implements FilterbankTables {

	private final Filterbank filterBank;
	private final float[] x, sum;
	private final float[][] tmpIn, tmpOut;
	private int xIndex;

	QMFAnalysis(Filterbank filterBank, int channels) {
		this.filterBank = filterBank;
		x = new float[20*channels];
		sum = new float[64];
		tmpIn = new float[32][2];
		tmpOut = new float[32][2];
		xIndex = 0;
	}

	void performAnalysis32(float[] in, float[][][] out, int offset, int kx, int len) {
		int off = 0;

		int n;
		for(int l = 0; l<len; l++) {
			//add new samples to input buffer x
			for(n = 32-1; n>=0; n--) {
				x[xIndex+n] = in[off];
				x[xIndex+n+320] = in[off];
				off++;
			}

			//window and summation to create array u
			for(n = 0; n<64; n++) {
				sum[n] = (x[xIndex+n]*QMF_C[2*n])
						+(x[xIndex+n+64]*QMF_C[2*(n+64)])
						+(x[xIndex+n+128]*QMF_C[2*(n+128)])
						+(x[xIndex+n+192]*QMF_C[2*(n+192)])
						+(x[xIndex+n+256]*QMF_C[2*(n+256)]);
			}

			//update ringbuffer index
			xIndex -= 32;
			if(xIndex<0) xIndex = (320-32);

			//reordering
			tmpIn[31][1] = sum[1];
			tmpIn[0][0] = sum[0];
			for(n = 1; n<31; n++) {
				tmpIn[31-n][1] = sum[n+1];
				tmpIn[n][0] = -sum[64-n];
			}
			tmpIn[0][1] = sum[32];
			tmpIn[31][0] = -sum[33];

			filterBank.computeDCT4Kernel(tmpIn, tmpOut);

			//reordering
			for(n = 0; n<16; n++) {
				if(2*n+1<kx) {
					out[l+offset][2*n][0] = 2.0f*tmpOut[n][0];
					out[l+offset][2*n][1] = 2.0f*tmpOut[n][1];
					out[l+offset][2*n+1][0] = -2.0f*tmpOut[31-n][1];
					out[l+offset][2*n+1][1] = -2.0f*tmpOut[31-n][0];
				}
				else {
					if(2*n<kx) {
						out[l+offset][2*n][0] = 2.0f*tmpOut[n][0];
						out[l+offset][2*n][1] = 2.0f*tmpOut[n][1];
					}
					else {
						out[l+offset][2*n][0] = 0;
						out[l+offset][2*n][1] = 0;
					}
					out[l+offset][2*n+1][0] = 0;
					out[l+offset][2*n+1][1] = 0;
				}
			}
		}
	}
}
