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
package com.tulskiy.musique.plugins.discogs.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.discogs.model.Release;
import org.discogs.model.Track;

import com.tulskiy.musique.util.Util;

/**
 * @author mliauchuk
 */
public class ReleaseTracklistingModel {

	public static final Pattern[] trackPositionPatterns = {
			Pattern.compile("(\\d+)\\.(\\d+)"),
			Pattern.compile("(\\d+)-(\\d+)"),
			Pattern.compile("CD(\\d+)\\.(\\d+)"),
			Pattern.compile("CD(\\d+)-(\\d+)")
	};
	
	private final Map<Track, TracklistingItem> items = new LinkedHashMap<Track, TracklistingItem>();

	public ReleaseTracklistingModel(Release release) {
		List<Track> tracks = new LinkedList<Track>(release.getTracks());
		
		processIndexTracks(tracks);
		processDiscTotal(tracks);
		processDiscAndTrack(tracks);
		processTrackTotal();
	}
	
	public String getTrackTrack(Track track) {
		TracklistingItem item = items.get(track);
		return item == null ? null : item.track.toString();
	}
	
	public String getTrackTrackTotal(Track track) {
		TracklistingItem item = items.get(track);
		return item == null ? null : item.trackTotal.toString();
	}
	
	public String getTrackDisc(Track track) {
		TracklistingItem item = items.get(track);
		return item == null ? null : item.disc.toString();
	}
	
	public String getTrackDiscTotal(Track track) {
		TracklistingItem item = items.get(track);
		return item == null ? null : item.discTotal.toString();
	}
	
	private class TracklistingItem {
		public Integer track;
		public Integer trackTotal;
		public Integer disc;
		public Integer discTotal;
		
		TracklistingItem(Integer track, Integer trackTotal, Integer disc, Integer discTotal) {
			this.track = track;
			this.trackTotal = trackTotal;
			this.disc = disc;
			this.discTotal = discTotal;
		}
	}
	
	private void processIndexTracks(List<Track> tracks) {
		int i = 0;
		while (i < tracks.size()) {
			if (Util.isEmpty(tracks.get(i).getPositionRaw())) {
//				items.put(tracks.get(i), new TracklistingItem(null, null, null, null));
				tracks.remove(i);
			}
			else {
				i++;
			}
		}
	}
	
	private void processDiscTotal(List<Track> tracks) {
		String s;
		int discTotal = 0;

		for (Track track : tracks) {
			s = extractDisc(track.getPositionRaw());
			if (s != null) {
				int disc = Integer.parseInt(s);
				if (disc > discTotal) {
					discTotal = disc;
				}
			}
		}

		if (discTotal == 0) {
			discTotal = 1;
		}

		for (Track track : tracks) {
			items.put(track, new TracklistingItem(null, null, null, discTotal));
		}
	}
	
	private void processDiscAndTrack(List<Track> tracks) {
		Entry<Track, TracklistingItem> entry_prev = null;
		Entry<Track, TracklistingItem> entry_curr = null;
		TracklistingItem item_prev = null;
		TracklistingItem item_curr = null;

		Iterator<Entry<Track, TracklistingItem>> entries = items.entrySet().iterator();
		while (entries.hasNext()) {
			entry_curr = entries.next();
			item_curr = entry_curr.getValue();
			
			if (entry_prev == null) {
				item_curr.disc = 1;
				item_curr.track = 1;
			}
			else {
				String pos = entry_curr.getKey().getPositionRaw();
				String sDisc = extractDisc(pos);
				String sTrack = extractTrack(pos);
				if (sDisc == null && sTrack == null) {
					item_prev = entry_prev.getValue();
					item_curr.disc = item_prev.disc;
					item_curr.track = item_prev.track + 1;
				}
				else {
					item_curr.disc = Integer.parseInt(sDisc);
					item_curr.track = Integer.parseInt(sTrack);
				}
			}

			entry_prev = entry_curr;
		}
	}
	
	private void processTrackTotal() {
		int disc = 0;
		List<TracklistingItem> discItems = new LinkedList<TracklistingItem>();

		Iterator<Entry<Track, TracklistingItem>> entries = items.entrySet().iterator();
		if (entries.hasNext()) {
			TracklistingItem item = entries.next().getValue();
			disc = item.disc;
			discItems.add(item);
		}
		while (entries.hasNext()) {
			TracklistingItem item = entries.next().getValue();
			if (disc != item.disc) {
				disc = item.disc;
				for (TracklistingItem discItem : discItems) {
					discItem.trackTotal = discItems.size();
				}
				discItems.clear();
			}
			discItems.add(item);
		}
		for (TracklistingItem discItem : discItems) {
			discItem.trackTotal = discItems.size();
		}
	}
	
	private String extractDisc(String pos) {
		if (pos != null) {
			for (Pattern tpp : trackPositionPatterns) {
				Matcher matcher = tpp.matcher(pos);
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		}

		return null;
	}
	
	private String extractTrack(String pos) {
		if (pos != null) {
			for (Pattern tpp : trackPositionPatterns) {
				Matcher matcher = tpp.matcher(pos);
				if (matcher.find()) {
					return matcher.group(2);
				}
			}
		}

		return null;
	}

}
