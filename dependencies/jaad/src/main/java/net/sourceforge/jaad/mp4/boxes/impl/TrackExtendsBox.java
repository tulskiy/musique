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
package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * This box sets up default values used by the movie fragments. By setting
 * defaults in this way, space and complexity can be saved in each Track
 * Fragment Box.
 *
 * @author in-somnia
 */
public class TrackExtendsBox extends FullBox {

	private long trackID;
	private long defaultSampleDescriptionIndex, defaultSampleDuration, defaultSampleSize;
	private long defaultSampleFlags;

	public TrackExtendsBox() {
		super("Track Extends Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		trackID = in.readBytes(4);
		defaultSampleDescriptionIndex = in.readBytes(4);
		defaultSampleDuration = in.readBytes(4);
		defaultSampleSize = in.readBytes(4);
		/* 6 bits reserved
		 * 2 bits sampleDependsOn
		 * 2 bits sampleIsDependedOn
		 * 2 bits sampleHasRedundancy
		 * 3 bits samplePaddingValue
		 * 1 bit sampleIsDifferenceSample
		 * 16 bits sampleDegradationPriority
		 */
		defaultSampleFlags = in.readBytes(4);
	}

	/**
	 * The track ID identifies the track; this shall be the track ID of a track
	 * in the Movie Box.
	 *
	 * @return the track ID
	 */
	public long getTrackID() {
		return trackID;
	}

	/**
	 * The default sample description index used in the track fragments.
	 *
	 * @return the default sample description index
	 */
	public long getDefaultSampleDescriptionIndex() {
		return defaultSampleDescriptionIndex;
	}

	/**
	 * The default sample duration used in the track fragments.
	 *
	 * @return the default sample duration
	 */
	public long getDefaultSampleDuration() {
		return defaultSampleDuration;
	}

	/**
	 * The default sample size used in the track fragments.
	 *
	 * @return the default sample size
	 */
	public long getDefaultSampleSize() {
		return defaultSampleSize;
	}

	/**
	 * The default 'sample depends on' value as defined in the
	 * SampleDependencyTypeBox.
	 *
	 * @see SampleDependencyTypeBox#getSampleDependsOn()
	 * @return the default 'sample depends on' value
	 */
	public int getSampleDependsOn() {
		return (int) ((defaultSampleFlags>>24)&3);
	}

	/**
	 * The default 'sample is depended on' value as defined in the
	 * SampleDependencyTypeBox.
	 *
	 * @see SampleDependencyTypeBox#getSampleIsDependedOn()
	 * @return the default 'sample is depended on' value
	 */
	public int getSampleIsDependedOn() {
		return (int) ((defaultSampleFlags>>22)&3);
	}

	/**
	 * The default 'sample has redundancy' value as defined in the
	 * SampleDependencyBox.
	 *
	 * @see SampleDependencyTypeBox#getSampleHasRedundancy()
	 * @return the default 'sample has redundancy' value
	 */
	public int getSampleHasRedundancy() {
		return (int) ((defaultSampleFlags>>20)&3);
	}

	/**
	 * The default padding value as defined in the PaddingBitBox.
	 *
	 * @see PaddingBitBox#getPad1()
	 * @return the default padding value
	 */
	public int getSamplePaddingValue() {
		return (int) ((defaultSampleFlags>>17)&7);
	}

	public boolean isSampleDifferenceSample() {
		return ((defaultSampleFlags>>16)&1)==1;
	}

	/**
	 * The default degradation priority for the samples.
	 * @return the default degradation priority
	 */
	public int getSampleDegradationPriority() {
		return (int) (defaultSampleFlags&0xFFFF);
	}
}
