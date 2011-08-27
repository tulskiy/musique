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

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.syntax.BitStream;

class SBRHeader {

	private boolean decoded;
	//header fields sorted by decoding order
	private boolean ampRes;
	private int startFrequency, startFrequencyPrev;
	private int stopFrequency, stopFrequencyPrev;
	private int xOverBand, xOverBandPrev;
	private int frequencyScale, frequencyScalePrev;
	private boolean alterScale, alterScalePrev;
	private int noiseBands, noiseBandsPrev;
	private int limiterBands, limiterGains;
	private boolean interpolFrequency;
	private boolean smoothingMode;

	SBRHeader() {
		decoded = false;
	}

	void decode(BitStream in) throws AACException {
		if(!decoded) decoded = true;

		//save previous values
		startFrequencyPrev = startFrequency;
		stopFrequencyPrev = stopFrequency;
		frequencyScalePrev = frequencyScale;
		alterScalePrev = alterScale;
		xOverBandPrev = xOverBand;
		noiseBandsPrev = noiseBands;

		//read new values
		ampRes = in.readBool();
		startFrequency = in.readBits(4);
		stopFrequency = in.readBits(4);
		xOverBand = in.readBits(3);
		in.skipBits(2); //reserved

		final boolean extraHeader1 = in.readBool();
		final boolean extraHeader2 = in.readBool();

		if(extraHeader1) {
			frequencyScale = in.readBits(2);
			alterScale = in.readBool();
			noiseBands = in.readBits(2);
		}
		else {
			frequencyScale = 2;
			alterScale = true;
			noiseBands = 2;
		}

		if(extraHeader2) {
			limiterBands = in.readBits(2);
			limiterGains = in.readBits(2);
			interpolFrequency = in.readBool();
			smoothingMode = in.readBool();
		}
		else {
			limiterBands = 2;
			limiterGains = 2;
			interpolFrequency = true;
			smoothingMode = true;
		}
	}

	public boolean isDecoded() {
		return decoded;
	}

	public boolean getAmpRes() {
		return ampRes;
	}

	public int getStartFrequency(boolean previous) {
		return previous ? startFrequencyPrev : startFrequency;
	}

	public int getStopFrequency(boolean previous) {
		return previous ? stopFrequencyPrev : stopFrequency;
	}

	public int getXOverBand(boolean previous) {
		return previous ? xOverBandPrev : xOverBand;
	}

	public int getFrequencyScale(boolean previous) {
		return previous ? frequencyScalePrev : frequencyScale;
	}

	public boolean isAlterScale(boolean previous) {
		return previous ? alterScalePrev : alterScale;
	}

	public int getNoiseBands(boolean previous) {
		return previous ? noiseBandsPrev : noiseBands;
	}

	public int getLimiterBands() {
		return limiterBands;
	}

	public int getLimiterGains() {
		return limiterGains;
	}

	public boolean hasInterpolFrequency() {
		return interpolFrequency;
	}

	public boolean isSmoothingMode() {
		return smoothingMode;
	}
}
