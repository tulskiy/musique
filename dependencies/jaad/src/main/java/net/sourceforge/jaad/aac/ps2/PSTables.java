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

interface PSTables {

	int[] IID_PARS = {10, 20, 34};
	int[] IPD_OPD_PARS = {5, 11, 17};
	//iid default quantization values: index -7...+7
	float[] IID_QUANT_DEFAULT = {
		-25, -18, -14, -10, -7, -4, -2, 0, 2, 4, 7, 10, 14, 18, 25};
	//iid fine quantization values: index -15...+15
	float[] IID_QUANT_FINE = {
		-50, -45, -40, -35, -30, -25, -22, -19, -16, -13, -10, -8, -6, -4, -2, 0,
		2, 4, 6, 8, 10, 13, 16, 19, 22, 25, 30, 35, 40, 45, 50
	};
	//ipd/opd quantization values: index 0...7
	float[] IPD_OPD_QUANT = {0.0f, 0.7853982f, 1.5707964f, 2.3561945f,
		3.1415927f, 3.926991f, 4.712389f, 5.497787f};
	//mapping k->b(k) for 20-band-mode (table 8.48)
	int[] K_TO_BK_20 = {
		1, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 15,
		15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19
	};
	//mapping k->b(k) for 34-band-mode (table 8.48)
	int[] K_TO_BK_34 = {
		0, 1, 2, 3, 4, 5, 6, 6, 7, 2, 1, 0, 10, 10, 4, 5, 6, 7, 8,
		9, 10, 11, 12, 9, 14, 11, 12, 13, 14, 15, 16, 13, 16, 17, 18, 19, 20, 21,
		22, 22, 23, 23, 24, 24, 25, 25, 26, 26, 27, 27, 27, 28, 28, 28, 29, 29, 29,
		30, 30, 30, 31, 31, 31, 31, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33
	};
	//a(m)
	float[] FILTER_COEFFICIENTS = {0.65143905753106f, 0.56471812200776f,
		0.48954165955695f};
	//d(m)
	int[] LINK_DELAY = {3, 4, 5};
	//mapping 20->71 (table 8.48)
	int[] PARAMETER_MAP_20 = {
		1, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 14, 15, 15, 15,
		16, 16, 16, 16, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18,
		18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19,
		19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19
	};
	//mapping 34->91 (table 8.49)
	int[] PARAMETER_MAP_34 = {
		0, 1, 2, 3, 4, 5, 6, 6, 7, 2, 1, 0, 10, 10, 4, 5, 6, 7, 8, 9, 10, 11,
		12, 9, 14, 11, 12, 13, 14, 15, 16, 13, 16, 17, 18, 19, 20, 21, 22, 22,
		23, 23, 24, 24, 25, 25, 26, 26, 27, 27, 27, 28, 28, 28, 29, 29, 29, 30,
		30, 30, 31, 31, 31, 31, 32, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33,
		33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33
	};
}
