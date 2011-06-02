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

public interface HCB {

	int ZERO_HCB = 0;
	int ESCAPE_HCB = 11;
	int NOISE_HCB = 13;
	int INTENSITY_HCB2 = 14;
	int INTENSITY_HCB = 15;
	//
	int FIRST_PAIR_HCB = 5;
}
