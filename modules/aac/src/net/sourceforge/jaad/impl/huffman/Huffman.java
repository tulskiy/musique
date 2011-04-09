/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.impl.huffman;

import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.impl.BitStream;
import net.sourceforge.jaad.impl.error.BitsBuffer;

public final class Huffman implements Codebooks, HCB_SF {

	private static final int PAIR_LEN = 2, QUAD_LEN = 4;
	private static final int[] BITS = {0, 5, 5, 0, 5, 0, 5, 0, 5, 0, 6, 5};
	private static final int[] VCB11_LAV = {
		16, 31, 47, 63, 95, 127, 159, 191, 223,
		255, 319, 383, 511, 767, 1023, 2047
	};

	private Huffman() {
	}

	public static byte decodeScaleFactor(BitStream in) throws AACException {
		int offset = 0;

		int b;
		while(HCB_SF[offset][1]!=0) {
			b = in.readBit();
			offset += HCB_SF[offset][b];
			if(offset>240) throw new AACException("scale factor out of range: "+offset);
		}

		return HCB_SF[offset][0];
	}

	private static void signBits(BitStream in, short[] data, int off, int len) throws AACException {
		for(int i = 0; i<len; i++) {
			if(data[off+i]!=0) {
				if(in.readBool()) data[off+i] = (short) -data[off+i];
			}
		}
	}

	private static short getEscape(BitStream in, short s) throws AACException {
		if(Math.abs(s)!=16) return s;

		final boolean neg = s<0;

		int i = 4;
		while(in.readBool()) {
			i++;
		}
		final int j = in.readBits(i)|(1<<i);

		return (short) (neg ? -j : j);
	}

	private static void decode2StepQuad(int cb, BitStream in, short[] data, int off) throws AACException {
		final short[][] TABLE1 = TWO_STEP_CODEBOOKS[cb][0];
		final short[][] TABLE2 = TWO_STEP_CODEBOOKS[cb][1];
		final int cw = in.peekBits(BITS[cb]);
		int offset = TABLE1[cw][0];
		final short extraBits = TABLE1[cw][1];

		if(extraBits==0) in.skipBits(TABLE2[offset][0]);
		else {
			in.skipBits(BITS[cb]);
			offset += in.peekBits(extraBits);
			in.skipBits(TABLE2[offset][0]-BITS[cb]);
		}

		if(offset>TABLE2.length) throw new AACException("invalid offset in scalefactor decoding: "+offset+", codebook: "+cb);

		data[off] = TABLE2[offset][1];
		data[off+1] = TABLE2[offset][2];
		data[off+2] = TABLE2[offset][3];
		data[off+3] = TABLE2[offset][4];
	}

	private static void decode2StepQuadSign(int cb, BitStream in, short[] data, int off) throws AACException {
		decode2StepQuad(cb, in, data, off);
		signBits(in, data, off, QUAD_LEN);
	}

	private static void decode2StepPair(int cb, BitStream in, short[] data, int off) throws AACException {
		final short[][] TABLE1 = TWO_STEP_CODEBOOKS[cb][0];
		final short[][] TABLE2 = TWO_STEP_CODEBOOKS[cb][1];
		final int cw = in.peekBits(BITS[cb]);
		int offset = TABLE1[cw][0];
		final int extraBits = TABLE1[cw][1];

		if(extraBits==0) in.skipBits(TABLE2[offset][0]);
		else {
			in.skipBits(BITS[cb]);
			offset += in.peekBits(extraBits);
			in.skipBits(TABLE2[offset][0]-BITS[cb]);
		}

		if(offset>TABLE2.length) throw new AACException("invalid offset in scalefactor decoding: "+offset+", codebook: "+cb);

		data[off] = TABLE2[offset][1];
		data[off+1] = TABLE2[offset][2];
	}

	private static void decode2StepPairSign(int cb, BitStream in, short[] data, int off) throws AACException {
		decode2StepPair(cb, in, data, off);
		signBits(in, data, off, PAIR_LEN);
	}

	private static void decodeBinaryQuad(int cb, BitStream in, short[] data, int off) throws AACException {
		final byte[][] TABLE = BINARY_CODEBOOKS[cb];
		int offset = 0;

		int b;
		while(TABLE[offset][0]==0) {
			b = in.readBit();
			offset += TABLE[offset][b+1];
		}

		if(offset>TABLE.length) throw new AACException("invalid offset in scalefactor decoding: "+offset+", codebook: "+cb);

		data[off] = TABLE[offset][1];
		data[off+1] = TABLE[offset][2];
		data[off+2] = TABLE[offset][3];
		data[off+3] = TABLE[offset][4];
	}

	private static void decodeBinaryQuadSign(int cb, BitStream in, short[] data, int off) throws AACException {
		decodeBinaryQuad(cb, in, data, off);
		signBits(in, data, off, QUAD_LEN);
	}

	private static void decodeBinaryPair(int cb, BitStream in, short[] data, int off) throws AACException {
		final byte[][] TABLE = BINARY_CODEBOOKS[cb];
		int offset = 0;

		int b;
		while(TABLE[offset][0]==0) {
			b = in.readBit();
			offset += TABLE[offset][b+1];
		}

		if(offset>TABLE.length) throw new AACException("invalid offset in scalefactor decoding: "+offset+", codebook: "+cb);

		data[off] = TABLE[offset][1];
		data[off+1] = TABLE[offset][2];
	}

	private static void decodeBinaryPairSign(int cb, BitStream in, short[] data, int off) throws AACException {
		decodeBinaryPair(cb, in, data, off);
		signBits(in, data, off, PAIR_LEN);
	}

	public static void decodeSpectralData(BitStream in, int cb, short[] data, int off) throws AACException {
		switch(cb) {
			case 1:
			case 2:
				decode2StepQuad(cb, in, data, off);
				break;
			case 3:
				decodeBinaryQuadSign(cb, in, data, off);
				break;
			case 4:
				decode2StepQuadSign(cb, in, data, off);
				break;
			case 5:
				decodeBinaryPair(cb, in, data, off);
				break;
			case 6:
				decode2StepPair(cb, in, data, off);
				break;
			case 7:
			case 9:
				decodeBinaryPairSign(cb, in, data, off);
				break;
			case 8:
			case 10:
				decode2StepPairSign(cb, in, data, off);
				break;
			case 11:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
			case 25:
			case 26:
			case 27:
			case 28:
			case 29:
			case 30:
			case 31:
				decode2StepPairSign(11, in, data, off);
				data[off] = getEscape(in, data[off]);
				data[off+1] = getEscape(in, data[off+1]);

				//error resilience
				if(cb>11) checkLAV(cb, data, off);
				break;
			default:
				throw new AACException("unknown huffman codebook: "+cb);
		}
	}

	/**
	 * special version for error resilience:
	 * - does not read from a BitStream but a BitsBuffer
	 * - keeps track of the bits decoded and returns the number of bits remaining
	 * - does not read more than in.len, return -1 if codeword would be longer
	 */
	public static int decodeSpectralDataER(BitsBuffer in, int cb, short[] data, int off) {
		int cw, extraBits, i, z;
		int offset = 0, vcb11 = 0;
		short[][] table1, table2;
		byte[][] binTable;
		switch(cb) {
			//2-step method for data quadruples
			case 1:
			case 2:
			case 4:
				table1 = TWO_STEP_CODEBOOKS[cb][0];
				table2 = TWO_STEP_CODEBOOKS[cb][1];
				cw = in.showBits(BITS[cb]);
				offset = table1[cw][0];
				extraBits = table2[cw][1];

				if(extraBits==0) z = 0;
				else {
					if(in.flushBits(BITS[cb])) return -1;
					offset += in.showBits(extraBits);
					z = BITS[cb];
				}
				if(in.flushBits(table2[offset][0]-z)) return -1;

				data[0] = table2[offset][1];
				data[1] = table2[offset][2];
				data[2] = table2[offset][3];
				data[3] = table2[offset][4];
				break;

			//2-step method for data pairs
			case 6:
			case 8:
			case 10:
			case 11:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
			case 25:
			case 26:
			case 27:
			case 28:
			case 29:
			case 30:
			case 31:
				if(cb>=16) {
					vcb11 = cb;
					cb = 11;
				}

				table1 = TWO_STEP_CODEBOOKS[cb][0];
				table2 = TWO_STEP_CODEBOOKS[cb][1];

				cw = in.showBits(BITS[cb]);
				offset = table1[cw][0];
				extraBits = table1[cw][1];

				if(extraBits==0) z = 0;
				else {
					if(in.flushBits(BITS[cb])) return -1;
					offset += in.showBits(extraBits);
					z = BITS[cb];
				}
				if(in.flushBits(table2[offset][0]-z)) return -1;
				data[0] = table2[offset][1];
				data[1] = table2[offset][2];
				break;

			//binary search
			case 3:
			case 5:
			case 7:
			case 9:
				binTable = BINARY_CODEBOOKS[cb];

				int b;
				while(binTable[offset][0]==0) {
					b = in.getBit();
					if(b==-1) return -1;
					offset += binTable[offset][b+1];
				}

				data[0] = binTable[offset][1];
				data[1] = binTable[offset][2];
				if(cb==3) {
					//quad table
					data[2] = binTable[offset][3];
					data[3] = binTable[offset][4];
				}

				break;
		}

		//decode sign bits
		if(UNSIGNED_CODEBOOK[cb]) {
			int b;
			for(i = 0; i<((cb<HCB.FIRST_PAIR_HCB) ? QUAD_LEN : PAIR_LEN); i++) {
				if(data[i]!=0) {
					b = in.getBit();
					if(b==-1) return -1;
					else if(b!=0) data[i] = (short) -data[i];
				}
			}
		}

		//decode huffman escape bits
		if((cb==HCB.ESCAPE_HCB)||(cb>=16)) {
			boolean neg;
			int b, x, j;
			for(int k = 0; k<2; k++) {
				if((data[k]==16)||(data[k]==-16)) {
					neg = data[k]<0;

					for(i = 4;; i++) {
						b = in.getBit();
						if(b==-1) return -1;
						else if(b==0) break;
					}

					x = in.getBits(i);
					if(x==-1) return -1;
					j = x+(1<<i);
					data[k] = (short) (neg ? -j : j);
				}
			}

			if(vcb11!=0) checkLAV(cb, data, off);
		}
		return in.getLength();
	}

	//checks largest absolute value to find errors in the escape signal
	private static void checkLAV(int cb, short[] data, int off) {
		if(cb<16||cb>31) return;
		final int max = VCB11_LAV[cb-16];

		if((Math.abs(data[off])>max)||(Math.abs(data[off+1])>max)) {
			data[0] = 0;
			data[1] = 0;
		}
	}
}
