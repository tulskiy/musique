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

interface PSConstants {

	//maximum values/lengths
	int MAX_ENVELOPES = 4;
	int MAX_IID_ICC_PARS = 34;
	int MAX_IPD_OPD_PARS = 17;
	int MAX_DELAY = 14;
	//band numbers
	int[] BANDS = {71, 91};
	int[] ALLPASS_BANDS = {30, 50};
	int[] PAR_BANDS = {20, 34};
	int[] DECAY_CUTOFF = {10, 32};
	int[] SHORT_DELAY_BANDS = {42, 62};
	int ALLPASS_LINKS = 3;
	//decorrelation factors
	float PEAK_DECAY_FACTOR = 0.76592833836465f;
	float A_SMOOTH = 0.25f;
	float GAMMA = 1.5f;
	float DECAY_SLOPE = 0.05f;
}
