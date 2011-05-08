/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.gui.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.playlist.Track;

// TODO add support of MultiValue editing
public class TrackInfoItem {

	private FieldKey key;
	private List<Track> tracks;

	private Set<String> allValues;
	private boolean isUpdated;

	public TrackInfoItem(FieldKey key, List<Track> tracks) {
		this.key = key;
		this.tracks = tracks;

		allValues = new LinkedHashSet<String>();
		for (Track track : tracks) {
			allValues.addAll(track.getTrackData().getTagFieldValuesSafeAsSet(key));
		}

		isUpdated = false;
	}

	public FieldKey getKey() {
		return key;
	}

	public List<Track> getTracks() {
		return tracks;
	}

	public void setCommonValue(String value) {
		allValues.clear();
		allValues.add(value);
		isUpdated = true;
	}

	public void updateTrack(Track track) {
		if (isUpdated && tracks.contains(track)) {
			track.getTrackData().setTagFieldValues(key, allValues);
		}
	}

	public void updateTracks() {
		if (isUpdated) {
			for (Track track : tracks) {
				track.getTrackData().setTagFieldValues(key, allValues);
			}
		}
	}

	public boolean isMultiple() {
		return allValues.size() > 1;
	}

	public String toString() {
		String result = null;

		if (allValues.size() > 1) {
			result = "<multiple values> " + allValues.toString();
		} else if (allValues.size() == 1) {
			result = allValues.iterator().next();
		}

		return result;
	}

}
