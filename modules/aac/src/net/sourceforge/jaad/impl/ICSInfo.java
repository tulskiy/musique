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
import net.sourceforge.jaad.impl.prediction.ICPrediction;
import net.sourceforge.jaad.impl.prediction.LTPrediction;
import java.util.Arrays;

public class ICSInfo implements Constants, ScaleFactorBands {

	public static final int WINDOW_SHAPE_SINE = 0;
	public static final int WINDOW_SHAPE_KAISER = 1;
	public static final int PREVIOUS = 0;
	public static final int CURRENT = 1;

	public static enum WindowSequence {

		ONLY_LONG_SEQUENCE,
		LONG_START_SEQUENCE,
		EIGHT_SHORT_SEQUENCE,
		LONG_STOP_SEQUENCE;

		public static WindowSequence forInt(int i) throws AACException {
			WindowSequence w;
			switch(i) {
				case 0:
					w = ONLY_LONG_SEQUENCE;
					break;
				case 1:
					w = LONG_START_SEQUENCE;
					break;
				case 2:
					w = EIGHT_SHORT_SEQUENCE;
					break;
				case 3:
					w = LONG_STOP_SEQUENCE;
					break;
				default:
					throw new AACException("unknown window sequence type");
			}
			return w;
		}
	}
	private final int frameLength, shortFrameLen;
	private WindowSequence windowSequence;
	private int[] windowShape;
	private int maxSFB;
	//prediction
	private boolean predictionDataPresent;
	private ICPrediction icPredict;
	boolean ltpData1Present, ltpData2Present;
	private LTPrediction ltPredict1, ltPredict2;
	//windows/sfbs
	private int windowCount;
	private int windowGroupCount;
	private int[] windowGroupLength;
	private int swbCount;
	private int[] swbOffsets;
	private int[][] sectSFBOffsets;

	public ICSInfo(int frameLength) {
		this.frameLength = frameLength;
		shortFrameLen = frameLength/8;
		windowShape = new int[2];
		windowSequence = WindowSequence.ONLY_LONG_SEQUENCE;
		windowGroupLength = new int[MAX_WINDOW_GROUP_COUNT];
		sectSFBOffsets = new int[MAX_WINDOW_GROUP_COUNT][MAX_SWB_COUNT+1];
		ltpData1Present = false;
		ltpData2Present = false;
	}

	/* ========== decoding ========== */
	public void decode(BitStream in, DecoderConfig conf, boolean commonWindow) throws AACException {
		final SampleFrequency sf = conf.getSampleFrequency();
		if(sf.equals(SampleFrequency.SAMPLE_FREQUENCY_NONE)) throw new AACException("invalid sample frequency");

		in.skipBit(); //reserved
		windowSequence = WindowSequence.forInt(in.readBits(2));
		windowShape[PREVIOUS] = windowShape[CURRENT];
		windowShape[CURRENT] = in.readBit();

		int grouping = 0;
		if(windowSequence.equals(WindowSequence.EIGHT_SHORT_SEQUENCE)) {
			maxSFB = in.readBits(4);
			grouping = in.readBits(7);
		}
		else {
			maxSFB = in.readBits(6);
			predictionDataPresent = in.readBool();
			if(predictionDataPresent) readPredictionData(in, conf.getProfile(), sf, commonWindow);
		}

		if(windowSequence.equals(WindowSequence.EIGHT_SHORT_SEQUENCE)) computeWindowGroupingInfoShort(sf, grouping);
		else computeWindowGroupingInfoLong(sf);
	}

	private void readPredictionData(BitStream in, Profile profile, SampleFrequency sf, boolean commonWindow) throws AACException {
		switch(profile) {
			case AAC_MAIN:
				if(icPredict==null) icPredict = new ICPrediction();
				icPredict.decode(in, maxSFB, sf);
				break;
			case AAC_LTP:
				if(ltpData1Present = in.readBool()) {
					if(ltPredict1==null) ltPredict1 = new LTPrediction(frameLength);
					ltPredict1.decode(in, this, profile);
				}
				if(commonWindow) {
					if(ltpData2Present = in.readBool()) {
						if(ltPredict2==null) ltPredict2 = new LTPrediction(frameLength);
						ltPredict2.decode(in, this, profile);
					}
				}
				break;
			case ER_AAC_LTP:
				if(!commonWindow) {
					if(ltpData1Present = in.readBool()) {
						if(ltPredict1==null) ltPredict1 = new LTPrediction(frameLength);
						ltPredict1.decode(in, this, profile);
					}
				}
				break;
			default:
				throw new AACException("unexpected profile for LTP: "+profile);
		}
	}

	private void computeWindowGroupingInfoLong(SampleFrequency sf) throws AACException {
		windowCount = 1;
		windowGroupCount = 1;
		windowGroupLength[0] = 1;
		swbCount = SWB_LONG_WINDOW_COUNT[sf.getIndex()];
		if(swbOffsets==null||swbOffsets.length!=swbCount+1) {
			//only reallocate if needed
			swbOffsets = new int[swbCount+1];
		}

		int offset;
		for(int i = 0; i<swbCount+1; i++) {
			offset = SWB_OFFSET_LONG_WINDOW[sf.getIndex()][i];
			if(offset<0) throw new AACException("invalid swb offset while decoding ICSInfo");
			sectSFBOffsets[0][i] = offset;
			swbOffsets[i] = offset;
		}
		if(sectSFBOffsets[0][swbCount]!=frameLength) throw new AACException("unexpected window length while decoding ICSInfo: "+sectSFBOffsets[0][swbCount]);
	}

	private void computeWindowGroupingInfoShort(SampleFrequency sf, int scaleFactorGrouping) throws AACException {
		windowCount = 8;
		windowGroupCount = 1;
		windowGroupLength[0] = 1;
		swbCount = SWB_SHORT_WINDOW_COUNT[sf.getIndex()];
		if(swbOffsets==null||swbOffsets.length!=swbCount+1) {
			//only reallocate if needed
			swbOffsets = new int[swbCount+1];
		}

		int i;
		for(i = 0; i<swbCount+1; i++) {
			swbOffsets[i] = SWB_OFFSET_SHORT_WINDOW[sf.getIndex()][i];
		}

		int bit = 1<<7;
		for(i = 0; i<windowCount-1; i++) {
			bit >>= 1;
			if((scaleFactorGrouping&bit)==0) {
				windowGroupCount++;
				windowGroupLength[windowGroupCount-1] = 1;
			}
			else windowGroupLength[windowGroupCount-1]++;
		}
		int offset, sectSFB, width;
		for(int g = 0; g<windowGroupCount; g++) {
			sectSFB = 0;
			offset = 0;

			for(i = 0; i<swbCount; i++) {
				if(i+1==swbCount) width = shortFrameLen-SWB_OFFSET_SHORT_WINDOW[sf.getIndex()][i];
				else width = SWB_OFFSET_SHORT_WINDOW[sf.getIndex()][i+1]-SWB_OFFSET_SHORT_WINDOW[sf.getIndex()][i];
				width *= windowGroupLength[g];
				sectSFBOffsets[g][sectSFB++] = offset;
				offset += width;
			}
			sectSFBOffsets[g][sectSFB] = offset;
		}
	}

	/* =========== gets ============ */
	public int getMaxSFB() {
		return maxSFB;
	}

	public int[][] getSectSFBOffsets() {
		return sectSFBOffsets;
	}

	public int getSWBCount() {
		return swbCount;
	}

	public int[] getSWBOffsets() {
		return swbOffsets;
	}

	public int getWindowCount() {
		return windowCount;
	}

	public int getWindowGroupCount() {
		return windowGroupCount;
	}

	public int getWindowGroupLength(int g) {
		return windowGroupLength[g];
	}

	public WindowSequence getWindowSequence() {
		return windowSequence;
	}

	public boolean isEightShortFrame() {
		return windowSequence.equals(WindowSequence.EIGHT_SHORT_SEQUENCE);
	}

	public int getWindowShape(int index) {
		return windowShape[index];
	}

	public boolean isICPredictionPresent() {
		return predictionDataPresent;
	}

	public ICPrediction getICPrediction() {
		return icPredict;
	}

	public boolean isLTPrediction1Present() {
		return ltpData1Present;
	}

	public LTPrediction getLTPrediction1() {
		return ltPredict1;
	}

	public boolean isLTPrediction2Present() {
		return ltpData2Present;
	}

	public LTPrediction getLTPrediction2() {
		return ltPredict2;
	}

	public void unsetPredictionSFB(int sfb) {
		if(predictionDataPresent) icPredict.setPredictionUnused(sfb);
		if(ltpData1Present) ltPredict1.setPredictionUnused(sfb);
		if(ltpData2Present) ltPredict2.setPredictionUnused(sfb);
	}

	public int getSWBOffsetMax() {
		return swbOffsets[swbCount];
	}

	public void setData(ICSInfo info) {
		windowSequence = info.windowSequence;
		windowShape[PREVIOUS] = info.windowShape[PREVIOUS];
		windowShape[CURRENT] = info.windowShape[CURRENT];
		maxSFB = info.maxSFB;
		predictionDataPresent = info.predictionDataPresent;
		if(predictionDataPresent) icPredict = info.icPredict;
		ltpData1Present = info.ltpData1Present;
		if(ltpData1Present) {
			ltPredict1.copy(info.ltPredict1);
			ltPredict2.copy(info.ltPredict2);
		}
		windowCount = info.windowCount;
		windowGroupCount = info.windowGroupCount;
		windowGroupLength = Arrays.copyOf(info.windowGroupLength, info.windowGroupLength.length);
		swbCount = info.swbCount;
		swbOffsets = Arrays.copyOf(info.swbOffsets, info.swbOffsets.length);
		sectSFBOffsets = new int[info.sectSFBOffsets.length][];
		for(int i = 0; i<info.sectSFBOffsets.length; i++) {
			sectSFBOffsets[i] = Arrays.copyOf(info.sectSFBOffsets[i], info.sectSFBOffsets[i].length);
		}
	}
}
