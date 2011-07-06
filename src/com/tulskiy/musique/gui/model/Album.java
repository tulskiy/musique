/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tulskiy.musique.playlist.Track;

/**
 * @author mliauchuk
 */
public class Album {
	
	private static final Integer DEFAULT_DISC = 1; 

	private Map<Integer, List<Track>> discs;
	
	public Album() {
		discs = new LinkedHashMap<Integer, List<Track>>(1, 1);
		discs.put(DEFAULT_DISC, new LinkedList<Track>());
	}
	
	public static String getAlbumId(Track track) {
		return track == null ? null : new StringBuilder("").
			append(track.getTrackData().getAlbumArtist()).append('/').
			append(track.getTrackData().getYear()).append('/').
			append(track.getTrackData().getAlbum()).
			toString();
	}

	public void addTrack(Track track, Integer disc) {
		if (disc == null) {
			disc = DEFAULT_DISC;
		}

		if (discs.get(disc) == null) {
			discs.put(disc, new LinkedList<Track>());
		}

		discs.get(disc).add(track);
	}

	public void addTrack(Track track) {
		addTrack(track, null);
	}
	
	public List<Track> getDiscTracks(Integer disc) {
		return discs.get(disc);
	}
	
	public List<Track> getAlbumTracks() {
		List<Track> tracks = new LinkedList<Track>();

		for (List<Track> discTracks : discs.values()) {
			tracks.addAll(discTracks);
		}

		return tracks;
	}
	
	public Set<Integer> getDiscNumbers() {
		return discs.keySet();
	}
	
	public Integer getTrackTotal(Integer disc) {
		Integer trackTotal = null;
		
		if (discs.get(disc) != null) {
			for (Track track : discs.get(disc)) {
				Integer val = Integer.valueOf(track.getTrackData().getTrack());
				if (trackTotal == null || val > trackTotal) {
					trackTotal = val;
				}
			}
		}
		
		return trackTotal;
	}
	
	public Integer getDiscTotal() {
		Integer discTotal = null;
		
		for (Integer disc : discs.keySet()) {
			if (discTotal == null || disc > discTotal) {
				discTotal = disc;
			}
		}
		
		return discTotal;
	}

}
