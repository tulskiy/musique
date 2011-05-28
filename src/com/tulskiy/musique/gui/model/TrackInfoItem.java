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

import java.util.List;
import java.util.Stack;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.playlist.Track;

public class TrackInfoItem {

	private FieldKey key;
	private List<Track> tracks;

	private Stack<TrackInfoItemState> states;

	public TrackInfoItem(FieldKey key, List<Track> tracks) {
		this.key = key;
		this.tracks = tracks;

		states = new Stack<TrackInfoItemState>();
		addState(key, tracks);
	}

	public TrackInfoItem(TrackInfoItem item) {
		key = item.getKey();
		tracks = item.getTracks();

		states = new Stack<TrackInfoItemState>();
		addState(item.getState());
	}

	public FieldKey getKey() {
		return key;
	}

	public List<Track> getTracks() {
		return tracks;
	}
	
	public TrackInfoItemState getState() {
		return states.peek();
	}

	/**
	 * Adds new state cloned from current.
	 */
	public void addState() {
		addState(getState());
	}

	/**
	 * Adds new state cloned from given one.
	 */
	private void addState(TrackInfoItemState state) {
		states.push(new TrackInfoItemState(state));
	}

	/**
	 * Adds new state based on field key and tracks.
	 * 
	 * @param key field key for this track info item
	 * @param tracks list of tracks associated with this track info item
	 */
	private void addState(FieldKey key, List<Track> tracks) {
		states.push(new TrackInfoItemState(key, tracks));
	}

	/**
	 * Approves current state (replaces previous state with current one).
	 * 
	 * @param updateTracks indicates whether tracks are to be updated with approved state data
	 */
	public void approveState(boolean updateTracks) {
		// update states
		if (states.size() > 1) {
			TrackInfoItemState currentState = states.pop();
			states.pop();
			states.push(currentState);
		}
		// update tracks
		if (updateTracks && getState().isUpdated()) {
			for (Track track : tracks) {
				track.getTrackData().setTagFieldValues(key, getState().getValues(track));
				// empty fields will be removed when file is actually written to disk
//				track.getTrackData().removeEmptyTagField(key);
			}
		}
	}

	/**
	 * Rejects current state.
	 * Nothing happens in case there is one state only.
	 */
	public void rejectState() {
		if (states.size() > 1) {
			states.pop().clear();
		}
	}

	/**
	 * Resets track info item state to initial one.
	 */
	public void resetStates() {
		while (states.size() > 1) {
			rejectState();
		}
	}

	public boolean isMultiple() {
		return getState().isMultiple();
	}

	public String toString() {
		return getState().toString();
	}

}
