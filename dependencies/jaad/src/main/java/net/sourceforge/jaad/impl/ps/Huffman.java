package net.sourceforge.jaad.impl.ps;

import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.impl.BitStream;

class Huffman {

	static void decode(BitStream in, int[][] table, int[] out, int len) throws AACException {
		for(int i = 0; i<len; i++) {
			out[i] = decodeHuffman(in, table);
		}
	}

	private static int decodeHuffman(BitStream in, int[][] table) throws AACException {
		int bit;
		int index = 0;

		while(index>=0) {
			bit = in.readBit();
			index = table[index][bit];
		}

		return index+31;
	}
}
