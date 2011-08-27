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

interface SBRConstants {

	int MAX_L_E = 5;
	int MAX_M = 49;
	int FIXFIX = 0;
	int FIXVAR = 1;
	int VARFIX = 2;
	int VARVAR = 3;
	int LO_RES = 0;
	int HI_RES = 1;
	int MAX_NTSRHFG = 40;
	int T_HFGEN = 8;
	int T_HFADJ = 2;
	int RATE = 2;
	int TIME_SLOTS = 16;
	int TIME_SLOTS_RATE = RATE*TIME_SLOTS;
	//extension IDs
	int EXTENSION_ID_PS = 2;
}
