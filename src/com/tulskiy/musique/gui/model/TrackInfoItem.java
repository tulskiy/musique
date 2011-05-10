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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.playlist.Track;

public class TrackInfoItem {

	private FieldKey key;
	private List<Track> tracks;

	private Map<Track, Set<String>> values;
	private boolean isUpdated;

	public TrackInfoItem(FieldKey key, List<Track> tracks) {
		this.key = key;
		this.tracks = tracks;
		initValues();
		isUpdated = false;
	}

	public FieldKey getKey() {
		return key;
	}

	public List<Track> getTracks() {
		return tracks;
	}
	
	public void initValues() {
		values = new LinkedHashMap<Track, Set<String>>();
		for (Track track : tracks) {
			values.put(track, new HashSet<String>(track.getTrackData().getTagFieldValuesSafeAsSet(key)));
		}
	}
	
	public Set<String> getValues() {
		Set<String> result = new LinkedHashSet<String>();

		for (Set<String> vs : values.values()) {
			result.addAll(vs);
		}

		return result;
	}
	
	public Set<String> getValues(Track track) {
		return track == null ? getValues() : values.get(track);
	}

	public int getValuesAmount() {
		return getValues().size();
	}
	
	public void addValue(String value) {
		for (Set<String> vs : values.values()) {
			vs.add(value);
		}
		isUpdated = true;
	}
	
	public void addValue(String value, Track track) {
		if (track == null) {
			addValue(value);
		}
		else {
			Set<String> vs = values.get(track);
			vs.add(value);
			isUpdated = true;
		}
	}

	public void setValue(String value) {
		for (Set<String> vs : values.values()) {
			vs.clear();
			vs.add(value);
		}
		isUpdated = true;
	}

	public void setValue(String value, Track track) {
		if (track == null) {
			setValue(value);
		}
		else {
			Set<String> vs = values.get(track);
			vs.clear();
			vs.add(value);
			isUpdated = true;
		}
	}

	public void update() {
		if (isUpdated) {
			for (Track track : tracks) {
				track.getTrackData().setTagFieldValues(key, values.get(track));
			}
		}
	}

	public void update(Track track) {
		if (track == null) {
			update();
		}
		else if (isUpdated && tracks.contains(track)) {
			track.getTrackData().setTagFieldValues(key, values.get(track));
		}
	}

	public boolean isMultiple() {
		return values.size() > 1;
	}

	public String toString() {
		String result = null;
		Set<String> vs = getValues();

		if (vs.size() > 1) {
			result = "<multiple values> " + vs.toString();
		}
		else if (vs.size() == 1) {
			result = vs.iterator().next();
		}

		return result;
	}

}
