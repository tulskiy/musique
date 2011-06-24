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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.util.Util;

/**
 * @author mliauchuk
 */
public class FileInfoModel extends MultiTagFieldModel {

	class Entry {
		String key;
		Object value;

		Entry(String key, Object value) {
			this.key = key;
			this.value = value;
		}
	}

	private ArrayList<Entry> list;

	public FileInfoModel(List<Track> tracks) {
		super(tracks);
	}

	@Override
	protected void loadTracks(List<Track> tracks) {
		list = new ArrayList<Entry>();
		if (tracks.size() == 1) {
			fillSingleTrack(tracks.get(0));
		} else {
			fillMultipleTracks(tracks);
		}
	}

	private void fillMultipleTracks(List<Track> tracks) {
		list.add(new Entry("Tracks selected", tracks.size()));
		long fileSize = 0;
		double length = 0;
		HashMap<String, Integer> formats = new HashMap<String, Integer>();
		HashMap<String, Integer> channels = new HashMap<String, Integer>();
		HashMap<String, Integer> sampleRate = new HashMap<String, Integer>();
		HashSet<String> files = new HashSet<String>();
		for (Track track : tracks) {
			TrackData trackData = track.getTrackData();
			if (trackData.isFile()) {
				fileSize += trackData.getFile().length();
				length += trackData.getTotalSamples() / (double) trackData.getSampleRate();
				files.add(trackData.getFile().getAbsolutePath());
				increment(formats, trackData.getCodec());
				increment(channels, trackData.getChannelsAsString());
				increment(sampleRate, trackData.getSampleRate() + " Hz");
			}
		}

		list.add(new Entry("Files", files.toString()));
		list.add(new Entry("Total size", fileSize + " bytes"));
		list.add(new Entry("Total Length", Util.formatSeconds(length, 3)));
		list.add(new Entry("Format", calcPercentage(formats)));
		list.add(new Entry("Channels", calcPercentage(channels)));
		list.add(new Entry("Sample Rate", calcPercentage(sampleRate)));
	}

	private Object calcPercentage(Map<String, Integer> map) {
		double total = 0;
		for (Integer val : map.values()) {
			total += val;
		}
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		boolean single = map.size() == 1;
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> entry : list) {
			sb.append(entry.getKey());
			if (!single) {
				sb.append(" (").append(String.format("%.2f", entry.getValue() / total * 100)).append("%), ");
			}
		}

		return sb.toString().replaceAll(", $", "");
	}

	private void increment(Map<String, Integer> map, String key) {
		Integer val = map.get(key);
		if (val == null) {
			map.put(key, 1);
		} else {
			map.put(key, val + 1);
		}
	}

	private void fillSingleTrack(Track track) {
		TrackData trackData = track.getTrackData();
		list.add(new Entry("Location", trackData.getLocation().toString().replaceAll("%\\d\\d", " ")));
		if (trackData.isFile())
			list.add(new Entry("File Size (bytes)", trackData.getFile().length()));
		if (trackData.getTotalSamples() >= 0)
			list.add(new Entry("Length", Util.samplesToTime(trackData.getTotalSamples(), trackData.getSampleRate(), 3)
					+ " (" + trackData.getTotalSamples() + " samples)"));
		list.add(new Entry("Subsong Index", trackData.getSubsongIndex()));
		if (trackData.isCue()) {
			list.add(new Entry("Cue Embedded", trackData.isCueEmbedded()));
			if (!trackData.isCueEmbedded()) {
				list.add(new Entry("Cue Path", trackData.getCueLocation()));
			}
		}
		list.add(new Entry("Format", trackData.getCodec()));
		if (!Util.isEmpty(trackData.getEncoder())) {
			list.add(new Entry("Encoder", trackData.getEncoder()));
		}
		list.add(new Entry("Channels", trackData.getChannels()));
		if (trackData.getSampleRate() > 0)
			list.add(new Entry("Sample Rate", trackData.getSampleRate() + " Hz"));
	}

	@Override
	public int getRowCount() {
		return list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Entry entry = list.get(rowIndex);
		if (columnIndex == 0)
			return entry.key;
		else
			return String.valueOf(entry.value);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
