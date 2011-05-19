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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

public class TrackInfoItemState {

	private Map<Track, Set<String>> values;
	private boolean isUpdated;

	public TrackInfoItemState(FieldKey key, List<Track> tracks) {
		values = new LinkedHashMap<Track, Set<String>>();
		for (Track track : tracks) {
			values.put(track, new LinkedHashSet<String>(track.getTrackData().getTagFieldValuesSafeAsSet(key)));
		}

		isUpdated = false;
	}

	public TrackInfoItemState(Map<Track, Set<String>> valuesOriginal, boolean isUpdatedOriginal) {
		values = new LinkedHashMap<Track, Set<String>>();
		Set<Entry<Track, Set<String>>> entriesOriginal = valuesOriginal.entrySet();
		if (entriesOriginal != null && !entriesOriginal.isEmpty()) {
			Iterator<Entry<Track, Set<String>>> it = entriesOriginal.iterator();
			while (it.hasNext()) {
				Entry<Track, Set<String>> entry = it.next();
				values.put(entry.getKey(), new LinkedHashSet<String>(entry.getValue()));
			}
		}

		isUpdated = isUpdatedOriginal;
	}
	
	public TrackInfoItemState(TrackInfoItemState state) {
		this(state.values, state.isUpdated);
	}
	
	public void clear() {
		for (Set<String> value : values.values()) {
			value.clear();
		}
		values.clear();
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
	
	public boolean isUpdated() {
		return isUpdated;
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

	public void setValues(Set<String> values) {
		for (Set<String> vs : this.values.values()) {
			vs.clear();
			vs.addAll(values);
		}
		isUpdated = true;
	}

	public void setValues(Set<String> values, Track track) {
		if (track == null) {
			setValues(values);
		}
		else {
			Set<String> vs = this.values.get(track);
			vs.clear();
			vs.addAll(values);
			isUpdated = true;
		}
	}

	public boolean isMultiple() {
		return values.size() > 1;
	}

	public String toString() {
		return Util.formatFieldValues(getValues());
	}

}
