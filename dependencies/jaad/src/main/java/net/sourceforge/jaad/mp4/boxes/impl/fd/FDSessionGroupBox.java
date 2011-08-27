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
package net.sourceforge.jaad.mp4.boxes.impl.fd;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The FD session group box is optional, although it is mandatory for files
 * containing more than one FD hint track. It contains a list of sessions as
 * well as all file groups and hint tracks that belong to each session. An FD
 * session sends simultaneously over all FD hint tracks (channels) that are
 * listed in the FD session group box for a particular FD session.
 *
 * Only one session group should be processed at any time. The first listed
 * hint track in a session group specifies the base channel. If the server has
 * no preference between the session groups, the default choice should be the
 * first session group. The group IDs of all file groups containing the files
 * referenced by the hint tracks shall be included in the list of file groups.
 * The file group IDs can in turn be translated into file group names (using the
 * group ID to name box) that can be included by the server in FDTs.
 *
 * @author in-somnia
 */
public class FDSessionGroupBox extends FullBox {

	private long[][] groupIDs, hintTrackIDs;

	public FDSessionGroupBox() {
		super("FD Session Group Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int sessionGroups = (int) in.readBytes(2);
		groupIDs = new long[sessionGroups][];
		hintTrackIDs = new long[sessionGroups][];

		int j, entryCount, channelsInSessionGroup;
		for(int i = 0; i<sessionGroups; i++) {
			entryCount = in.read();
			groupIDs[i] = new long[entryCount];
			for(j = 0; j<entryCount; j++) {
				groupIDs[i][j] = in.readBytes(4);
			}

			channelsInSessionGroup = (int) in.readBytes(2);
			hintTrackIDs[i] = new long[channelsInSessionGroup];
			for(j = 0; j<channelsInSessionGroup; j++) {
				hintTrackIDs[i][j] = in.readBytes(4);
			}
		}
	}

	/**
	 * A group ID indicates a file group that the session group complies with.
	 *
	 * @return all group IDs for all session groups
	 */
	public long[][] getGroupIDs() {
		return groupIDs;
	}

	/**
	 * A hint track ID specifies the track ID of the FD hint track belonging to
	 * a particular session group. Note that one FD hint track corresponds to
	 * one LCT channel.
	 *
	 * @return all hint track IDs for all session groups
	 */
	public long[][] getHintTrackIDs() {
		return hintTrackIDs;
	}
}
