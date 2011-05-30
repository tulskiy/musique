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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

public class TrackInfoItemState {

	private Map<Track, FieldValues> values;
	private boolean isUpdated;

	public TrackInfoItemState(FieldKey key, List<Track> tracks) {
		values = new LinkedHashMap<Track, FieldValues>();
		for (Track track : tracks) {
			values.put(track, new FieldValues(track.getTrackData().getTagFieldValuesSafe(key)));
		}

		isUpdated = false;
	}

	public TrackInfoItemState(Map<Track, FieldValues> valuesOriginal, boolean isUpdatedOriginal) {
		values = new LinkedHashMap<Track, FieldValues>();
		Set<Entry<Track, FieldValues>> entriesOriginal = valuesOriginal.entrySet();
		if (entriesOriginal != null && !entriesOriginal.isEmpty()) {
			Iterator<Entry<Track, FieldValues>> it = entriesOriginal.iterator();
			while (it.hasNext()) {
				Entry<Track, FieldValues> entry = it.next();
				values.put(entry.getKey(), new FieldValues(entry.getValue()));
			}
		}

		isUpdated = isUpdatedOriginal;
	}
	
	public TrackInfoItemState(TrackInfoItemState state) {
		this(state.values, state.isUpdated);
	}
	
	public void clear() {
		for (FieldValues value : values.values()) {
			value.clear();
		}
		values.clear();
	}
	
	public FieldValues getValues() {
		FieldValues result = new FieldValues();

		for (FieldValues vs : values.values()) {
			result.add(vs);
		}

		return result;
	}
	
	public FieldValues getValues(Track track) {
		return track == null ? getValues() : values.get(track);
	}
	
	public boolean isUpdated() {
		return isUpdated;
	}
	
	public void addValue(String value) {
		for (FieldValues vs : values.values()) {
			vs.add(value);
		}
		isUpdated = true;
	}
	
	public void addValue(String value, Track track) {
		if (track == null) {
			addValue(value);
		}
		else {
			FieldValues vs = values.get(track);
			vs.add(value);
			isUpdated = true;
		}
	}

	public void setValue(String value) {
		for (FieldValues vs : values.values()) {
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
			FieldValues vs = values.get(track);
			vs.clear();
			vs.add(value);
			isUpdated = true;
		}
	}

	public void setValues(FieldValues values) {
		for (FieldValues vs : this.values.values()) {
			vs.clear();
			vs.add(values);
		}
		isUpdated = true;
	}

	public void setValues(FieldValues values, Track track) {
		if (track == null) {
			setValues(values);
		}
		else {
			FieldValues vs = this.values.get(track);
			vs.clear();
			vs.add(values);
			isUpdated = true;
		}
	}

	public boolean isMultiple() {
		return getValues().size() > 1;
	}

	public String toString() {
		return Util.formatFieldValues(getValues());
	}

}
