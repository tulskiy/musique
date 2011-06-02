/*
 * Copyright (C) 2false1false in-somnia
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

interface Codebooks {

	short[][][][] TWO_STEP_CODEBOOKS = {
		{null, null},
		{HCB1.HCB1_1, HCB1.HCB1_2},
		{HCB2.HCB2_1, HCB2.HCB2_2},
		{null, null},
		{HCB4.HCB4_1, HCB4.HCB4_2},
		{null, null},
		{HCB6.HCB6_1, HCB6.HCB6_2},
		{null, null},
		{HCB8.HCB8_1, HCB8.HCB8_2},
		{null, null},
		{HCB10.HCB10_1, HCB10.HCB10_2},
		{HCB11.HCB11_1, HCB11.HCB11_2}
	};
	byte[][][] BINARY_CODEBOOKS = {
		null, null, null, HCB3.HCB3, null, HCB5.HCB5, null, HCB7.HCB7, null, HCB9.HCB9, null, null
	};
	boolean UNSIGNED_CODEBOOK[] = {false, false, false, true, true, false, false, true, true, true, true, true, false, false, false, false,
		true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
	};
}
