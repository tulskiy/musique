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
 * The shadow sync table provides an optional set of sync samples that can be
 * used when seeking or for similar purposes. In normal forward play they are
 * ignored.
 *
 * Each entry in the ShadowSyncTable consists of a pair of sample numbers. The
 * first entry (shadowed-sample-number) indicates the number of the sample that
 * a shadow sync will be defined for. This should always be a non-sync sample
 * (e.g. a frame difference). The second sample number (sync-sample-number)
 * indicates the sample number of the sync sample (i.e. key frame) that can be
 * used when there is a random access at, or before, the shadowed-sample-number.
 *
 * The entries in the ShadowSyncBox shall be sorted based on the
 * shadowed-sample-number field. The shadow sync samples are normally placed in
 * an area of the track that is not presented during normal play (edited out by
 * means of an edit list), though this is not a requirement. The shadow sync
 * table can be ignored and the track will play (and seek) correctly if it is
 * ignored (though perhaps not optimally).
 *
 * The ShadowSyncSample replaces, not augments, the sample that it shadows (i.e.
 * the next sample sent is shadowed-sample-number+1). The shadow sync sample is
 * treated as if it occurred at the time of the sample it shadows, having the
 * duration of the sample it shadows.
 *
 * Hinting and transmission might become more complex if a shadow sample is used
 * also as part of normal playback, or is used more than once as a shadow. In
 * this case the hint track might need separate shadow syncs, all of which can
 * get their media data from the one shadow sync in the media track, to allow
 * for the different time-stamps etc. needed in their headers. 
 *
 * @author in-somnia
 */
public class ShadowSyncSampleBox extends FullBox {

	private long[][] sampleNumbers;

	public ShadowSyncSampleBox() {
		super("Shadow Sync Sample Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		
		final int entryCount = (int) in.readBytes(4);
		sampleNumbers = new long[entryCount][2];

		for(int i = 0; i<entryCount; i++) {
			sampleNumbers[i][0] = in.readBytes(4); //shadowedSampleNumber;
			sampleNumbers[i][1] = in.readBytes(4); //syncSampleNumber;
		}
	}

	/**
	 * A map of sample number pairs:
	 * 0 (shadowed-sample-number): gives the number of a sample for which there
	 * is an alternative sync sample.
	 * 1 (sync-sample-number): gives the number of the alternative sync sample.
	 *
	 * @return the sample number pairs
	 */
	public long[][] getSampleNumbers() {
		return sampleNumbers;
	}
}
