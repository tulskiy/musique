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

import net.sourceforge.jaad.SampleFrequency;
import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.DecoderConfig;
import net.sourceforge.jaad.Profile;
import net.sourceforge.jaad.impl.stereo.MSMask;

public class CPE extends Element implements Constants {

	private MSMask msMask;
	private boolean[][] msUsed;
	private boolean commonWindow;
	ICStream icsL, icsR;

	CPE(int frameLength) {
		super();
		msUsed = new boolean[MAX_WINDOW_GROUP_COUNT][MAX_SWB_COUNT+1];
		icsL = new ICStream(frameLength);
		icsR = new ICStream(frameLength);
	}

	void decode(BitStream in, DecoderConfig conf) throws AACException {
		final Profile profile = conf.getProfile();
		final SampleFrequency sf = conf.getSampleFrequency();
		if(sf.equals(SampleFrequency.SAMPLE_FREQUENCY_NONE)) throw new AACException("invalid sample frequency");

		readElementInstanceTag(in);

		commonWindow = in.readBool();
		final ICSInfo info = icsL.getInfo();
		if(commonWindow) {
			info.decode(in, conf, commonWindow);
			icsR.getInfo().setData(info);

			msMask = MSMask.forInt(in.readBits(2));
			if(msMask.equals(MSMask.TYPE_RESERVED)) throw new AACException("reserved MS mask type used");
			else if(msMask.equals(MSMask.TYPE_USED)) {
				final int maxSFB = info.getMaxSFB();
				final int windowGroupCount = info.getWindowGroupCount();

				int sfb;
				for(int g = 0; g<windowGroupCount; g++) {
					for(sfb = 0; sfb<maxSFB; sfb++) {
						msUsed[g][sfb] = in.readBool();
					}
				}
			}
		}
		else msMask = MSMask.TYPE_ALL_0;

		if(profile.isErrorResilientProfile()&&(info.isLTPrediction1Present())) {
			if(info.ltpData2Present = in.readBool()) info.getLTPrediction2().decode(in, info, profile);
		}

		icsL.decode(in, commonWindow, conf);
		icsR.decode(in, commonWindow, conf);
	}

	public ICStream getLeftChannel() {
		return icsL;
	}

	public ICStream getRightChannel() {
		return icsR;
	}

	public MSMask getMSMask() {
		return msMask;
	}

	public boolean isMSUsed(int g, int sfb) {
		return msUsed[g][sfb];
	}

	public boolean isMSMaskPresent() {
		return !msMask.equals(MSMask.TYPE_ALL_0);
	}

	public boolean isCommonWindow() {
		return commonWindow;
	}
}
