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
package net.sourceforge.jaad.aac.tools;

import net.sourceforge.jaad.aac.AACException;

/**
 * The MSMask indicates, if MS is applied to a specific ICStream.
 * @author in-somnia
 */
public enum MSMask {

	TYPE_ALL_0(0),
	TYPE_USED(1),
	TYPE_ALL_1(2),
	TYPE_RESERVED(3);

	public static MSMask forInt(int i) throws AACException {
		MSMask m;
		switch(i) {
			case 0:
				m = TYPE_ALL_0;
				break;
			case 1:
				m = TYPE_USED;
				break;
			case 2:
				m = TYPE_ALL_1;
				break;
			case 3:
				m = TYPE_RESERVED;
				break;
			default:
				throw new AACException("unknown MS mask type");
		}
		return m;
	}
	private int num;

	private MSMask(int num) {
		this.num = num;
	}
}
