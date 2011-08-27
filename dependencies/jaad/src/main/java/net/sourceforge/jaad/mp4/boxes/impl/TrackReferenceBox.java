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

import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

/**
 * The Track Reference Box provides a reference from the containing track to
 * another track in the presentation. These references are typed. A 'hint'
 * reference links from the containing hint track to the media data that it
 * hints. A content description reference 'cdsc' links a descriptive or
 * metadata track to the content which it describes.
 *
 * Exactly one Track Reference Box can be contained within the Track Box.
 *
 * If this box is not present, the track is not referencing any other track in
 * any way. The reference array is sized to fill the reference type box.
 * @author in-somnia
 */
public class TrackReferenceBox extends BoxImpl {

	private String referenceType;
	private List<Long> trackIDs;

	public TrackReferenceBox() {
		super("Track Reference Box");
		trackIDs = new ArrayList<Long>();
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		referenceType = in.readString(4);

		while(getLeft(in)>3) {
			trackIDs.add(in.readBytes(4));
		}
	}

	/**
	 * The reference type shall be set to one of the following values: 
	 * <ul>
	 * <li>'hint': the referenced track(s) contain the original media for this 
	 * hint track.</li>
	 * <li>'cdsc': this track describes the referenced track.</li>
	 * <li>'hind': this track depends on the referenced hint track, i.e., it 
	 * should only be used if the referenced hint track is used.</li>
	 * @return the reference type
	 */
	public String getReferenceType() {
		return referenceType;
	}

	/**
	 * The track IDs are integers that provide a reference from the containing
	 * track to other tracks in the presentation. Track IDs are never re-used
	 * and cannot be equal to zero.
	 * @return the track IDs this box refers to
	 */
	public List<Long> getTrackIDs() {
		return Collections.unmodifiableList(trackIDs);
	}
}
