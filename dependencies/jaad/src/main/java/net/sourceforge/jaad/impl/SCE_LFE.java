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
package net.sourceforge.jaad.impl;

import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.DecoderConfig;

class SCE_LFE extends Element {

	private final ICStream ics;

	SCE_LFE(int frameLength) {
		super();
		ics = new ICStream(frameLength);
	}

	void decode(BitStream in, DecoderConfig conf) throws AACException {
		readElementInstanceTag(in);
		ics.decode(in, false, conf);
	}

	public ICStream getICStream() {
		return ics;
	}
}
