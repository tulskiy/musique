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
package net.sourceforge.jaad;

/**
 * All possible sample frequencies AAC data can have.
 * @author in-somnia
 */
public enum SampleFrequency {

	SAMPLE_FREQUENCY_NONE(-1, 0, new int[]{0, 0}, new int[]{0, 0}),
	SAMPLE_FREQUENCY_96000(0, 96000, new int[]{33, 512}, new int[]{31, 9}),
	SAMPLE_FREQUENCY_88200(1, 88200, new int[]{33, 512}, new int[]{31, 9}),
	SAMPLE_FREQUENCY_64000(2, 64000, new int[]{38, 664}, new int[]{34, 10}),
	SAMPLE_FREQUENCY_48000(3, 48000, new int[]{40, 672}, new int[]{40, 14}),
	SAMPLE_FREQUENCY_44100(4, 44100, new int[]{40, 672}, new int[]{42, 14}),
	SAMPLE_FREQUENCY_32000(5, 32000, new int[]{40, 672}, new int[]{51, 14}),
	SAMPLE_FREQUENCY_24000(6, 24000, new int[]{41, 652}, new int[]{46, 14}),
	SAMPLE_FREQUENCY_22050(7, 22050, new int[]{41, 652}, new int[]{46, 14}),
	SAMPLE_FREQUENCY_16000(8, 16000, new int[]{37, 664}, new int[]{42, 14}),
	SAMPLE_FREQUENCY_12000(9, 12000, new int[]{37, 664}, new int[]{42, 14}),
	SAMPLE_FREQUENCY_11025(10, 11025, new int[]{37, 664}, new int[]{42, 14}),
	SAMPLE_FREQUENCY_8000(11, 8000, new int[]{34, 664}, new int[]{39, 14});

	/**
	 * Returns a sample frequency instance for the given index. If the index
	 * is not between 0 and 11 inclusive, SAMPLE_FREQUENCY_NONE is returned.
	 * @return a sample frequency with the given index
	 */
	public static SampleFrequency forInt(int i) throws AACException {
		SampleFrequency freq;
		switch(i) {
			case 0:
				freq = SAMPLE_FREQUENCY_96000;
				break;
			case 1:
				freq = SAMPLE_FREQUENCY_88200;
				break;
			case 2:
				freq = SAMPLE_FREQUENCY_64000;
				break;
			case 3:
				freq = SAMPLE_FREQUENCY_48000;
				break;
			case 4:
				freq = SAMPLE_FREQUENCY_44100;
				break;
			case 5:
				freq = SAMPLE_FREQUENCY_32000;
				break;
			case 6:
				freq = SAMPLE_FREQUENCY_24000;
				break;
			case 7:
				freq = SAMPLE_FREQUENCY_22050;
				break;
			case 8:
				freq = SAMPLE_FREQUENCY_16000;
				break;
			case 9:
				freq = SAMPLE_FREQUENCY_12000;
				break;
			case 10:
				freq = SAMPLE_FREQUENCY_11025;
				break;
			case 11:
				freq = SAMPLE_FREQUENCY_8000;
				break;
			default:
				throw new AACException("invalid sample frequency index: "+i);
		}
		return freq;
	}
	private final int index, frequency;
	private final int[] prediction, maxTNS_SFB;

	private SampleFrequency(int index, int freqency, int[] prediction, int[] maxTNS_SFB) {
		this.index = index;
		this.frequency = freqency;
		this.prediction = prediction;
		this.maxTNS_SFB = maxTNS_SFB;
	}

	/**
	 * Returns this sample frequency's index between 0 (96000) and 11 (8000)
	 * or -1 if this is SAMPLE_FREQUENCY_NONE.
	 * @return the sample frequency's index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the sample frequency as integer value. This may be a value
	 * between 96000 and 8000, or 0 if this is SAMPLE_FREQUENCY_NONE.
	 * @return the sample frequency
	 */
	public int getFrequency() {
		return frequency;
	}

	/**
	 * Returns the highest scale factor band allowed for ICPrediction at this
	 * sample frequency.
	 * This method is mainly used internally.
	 * @return the highest prediction SFB
	 */
	public int getMaximalPredictionSFB() {
		return prediction[0];
	}

	/**
	 * Returns the number of predictors allowed for ICPrediction at this
	 * sample frequency.
	 * This method is mainly used internally.
	 * @return the number of ICPredictors
	 */
	public int getPredictorCount() {
		return prediction[1];
	}

	/**
	 * Returns the highest scale factor band allowed for TNS at this
	 * sample frequency.
	 * This method is mainly used internally.
	 * @return the highest SFB for TNS
	 */
	public int getMaximalTNS_SFB(boolean shortWindow) {
		return maxTNS_SFB[shortWindow ? 1 : 0];
	}

	/**
	 * Returns a string representation of this sample frequency.
	 * The method is identical to <code>getDescription()</code>.
	 * @return the sample frequency's description
	 */
	@Override
	public String toString() {
		return Integer.toString(frequency);
	}
}
