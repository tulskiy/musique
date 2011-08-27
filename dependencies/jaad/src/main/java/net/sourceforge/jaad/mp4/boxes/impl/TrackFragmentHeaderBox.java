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
 * Each movie fragment can add zero or more fragments to each track; and a track
 * fragment can add zero or more contiguous runs of samples. The track fragment
 * header sets up information and defaults used for those runs of samples.
 * 
 * @author in-somnia
 */
public class TrackFragmentHeaderBox extends FullBox {

	private long trackID;
	private boolean baseDataOffsetPresent, sampleDescriptionIndexPresent,
			defaultSampleDurationPresent, defaultSampleSizePresent,
			defaultSampleFlagsPresent;
	private boolean durationIsEmpty;
	private long baseDataOffset, sampleDescriptionIndex, defaultSampleDuration,
			defaultSampleSize, defaultSampleFlags;

	public TrackFragmentHeaderBox() {
		super("Track Fragment Header Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		trackID = in.readBytes(4);

		//optional fields
		baseDataOffsetPresent = ((flags&1)==1);
		baseDataOffset = baseDataOffsetPresent ? in.readBytes(8) : 0;

		sampleDescriptionIndexPresent = ((flags&2)==2);
		sampleDescriptionIndex = sampleDescriptionIndexPresent ? in.readBytes(4) : 0;

		defaultSampleDurationPresent = ((flags&8)==8);
		defaultSampleDuration = defaultSampleDurationPresent ? in.readBytes(4) : 0;

		defaultSampleSizePresent = ((flags&16)==16);
		defaultSampleSize = defaultSampleSizePresent ? in.readBytes(4) : 0;

		defaultSampleFlagsPresent = ((flags&32)==32);
		defaultSampleFlags = defaultSampleFlagsPresent ? in.readBytes(4) : 0;

		durationIsEmpty = ((flags&0x10000)==0x10000);
	}

	public long getTrackID() {
		return trackID;
	}

	public boolean isBaseDataOffsetPresent() {
		return baseDataOffsetPresent;
	}

	public long getBaseDataOffset() {
		return baseDataOffset;
	}

	public boolean isSampleDescriptionIndexPresent() {
		return sampleDescriptionIndexPresent;
	}

	public long getSampleDescriptionIndex() {
		return sampleDescriptionIndex;
	}

	public boolean isDefaultSampleDurationPresent() {
		return defaultSampleDurationPresent;
	}

	public long getDefaultSampleDuration() {
		return defaultSampleDuration;
	}

	public boolean isDefaultSampleSizePresent() {
		return defaultSampleSizePresent;
	}

	public long getDefaultSampleSize() {
		return defaultSampleSize;
	}

	public boolean isDefaultSampleFlagsPresent() {
		return defaultSampleFlagsPresent;
	}

	public long getDefaultSampleFlags() {
		return defaultSampleFlags;
	}

	public boolean isDurationIsEmpty() {
		return durationIsEmpty;
	}
}
