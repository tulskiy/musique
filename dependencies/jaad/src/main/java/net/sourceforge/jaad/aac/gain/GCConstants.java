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
package net.sourceforge.jaad.aac.gain;

interface GCConstants {

	int BANDS = 4;
	int MAX_CHANNELS = 5;
	int NPQFTAPS = 96;
	int NPEPARTS = 64;	//number of pre-echo inhibition parts
	int ID_GAIN = 16;
	int[] LN_GAIN = {
		-4, -3, -2, -1, 0, 1, 2, 3,
		4, 5, 6, 7, 8, 9, 10, 11
	};
}
