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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.util.FieldKeyMetaHelper;

/**
 * @author mliauchuk
 */
public class MultiTagFieldModel extends AbstractTableModel implements TagFieldModel {

	private List<TrackInfoItem> trackInfoItems = new LinkedList<TrackInfoItem>();
	private List<TrackInfoItem> trackInfoItemsRemoved = new LinkedList<TrackInfoItem>();

	public MultiTagFieldModel(List<Track> tracks) {
		loadTracks(tracks);
	}

	protected void loadTracks(List<Track> tracks) {
		Set<FieldKey> usedKeys = new LinkedHashSet<FieldKey>();
		for (int i = 0; i < tracks.size(); i++) {
			TrackData trackData = tracks.get(i).getTrackData();
			trackData.populateWithEmptyCommonTagFields();

			Iterator<Entry<FieldKey, FieldValues>> entries = trackData
					.getAllTagFieldValuesIterator();
			while (entries.hasNext()) {
				Entry<FieldKey, FieldValues> entry = entries.next();
				if (!usedKeys.contains(entry.getKey())) {
					usedKeys.add(entry.getKey());
					trackInfoItems.add(new TrackInfoItem(entry.getKey(), tracks));
				}
			}
		}
		sort();
	}

	public List<TrackInfoItem> getTrackInfoItems() {
		return trackInfoItems;
	}

	public void addTrackInfoItem(TrackInfoItem item) {
		int i = 0;
		while (i < trackInfoItemsRemoved.size()) {
			TrackInfoItem tii = trackInfoItemsRemoved.get(i);
			if (tii.getKey().equals(item.getKey())) {
				trackInfoItemsRemoved.remove(tii);
			}
			else {
				i++;
			}
		}
		trackInfoItems.add(item);
	}

	// TODO optimize
	public void mergeTrackInfoItems(List<TrackInfoItem> items) {
		boolean isFound;

		for (TrackInfoItem item : items) {
			isFound = false;

			// update existing fields
			for (TrackInfoItem tii : trackInfoItems) {
				if (tii.getKey().equals(item.getKey())) {
					for (int i = 0; i < item.getTracks().size() && i < tii.getTracks().size(); i++) {
						Track itemTrack = item.getTracks().get(i);
						Track tiiTrack = tii.getTracks().get(i);
						tii.getState().setValues(item.getState().getValues(itemTrack), tiiTrack);
					}
					isFound = true;
					break;
				}
			}

			// update removed fields
			if (!isFound) {	
				for (TrackInfoItem tiir : trackInfoItemsRemoved) {
					if (tiir.getKey().equals(item.getKey())) {
						for (int i = 0; i < item.getTracks().size() && i < tiir.getTracks().size(); i++) {
							Track itemTrack = item.getTracks().get(i);
							Track tiiTrack = tiir.getTracks().get(i);
							tiir.getState().setValues(item.getState().getValues(itemTrack), tiiTrack);
						}
						trackInfoItems.add(tiir);
						trackInfoItemsRemoved.remove(tiir);
						isFound = true;
						break;
					}
				}
			}

			// add new fields
			if (!isFound) {
				List<Track> tracks = trackInfoItems.isEmpty() ? trackInfoItemsRemoved.get(0).getTracks() : trackInfoItems.get(0).getTracks();
				TrackInfoItem itemNew = new TrackInfoItem(item.getKey(), tracks);
				for (int i = 0; i < item.getTracks().size() && i < tracks.size(); i++) {
					Track itemTrack = item.getTracks().get(i);
					Track itemNewTrack = tracks.get(i);
					itemNew.getState().setValues(item.getState().getValues(itemTrack), itemNewTrack);
				}
				addTrackInfoItem(itemNew);
			}
		}
	}

	public void removeTrackInfoItems(List<TrackInfoItem> items) {
		for (TrackInfoItem item : items) {
			item.getState().setValue("");
		}
		trackInfoItemsRemoved.addAll(items);
		trackInfoItems.removeAll(items);
	}

	/**
	 * Syncs removed and present track info items. Finally, empty "removed" tags
	 * will be physically removed by AudioFileWriter. Should be used by
	 * "apply changes" event handler only.
	 */
	private void sync() {
		trackInfoItems.addAll(trackInfoItemsRemoved);
		trackInfoItemsRemoved.clear();
	}
	
	public void approveModel() {
		sync();
    	for (TrackInfoItem item : trackInfoItems) {
    		item.approveState(true);
    	}
    	sort();
	}

	public void refreshModel() {
		sort();
	}
	
	public void rejectModel() {
		sync();
    	for (TrackInfoItem item : trackInfoItems) {
    		item.rejectState();
    	}
	}
	
	public void sort() {
		Collections.sort(trackInfoItems, new TrackInfoItemComparator());
	}
	
	public List<FieldKey> getAllUsedFieldKeys() {
		List<FieldKey> result = new LinkedList<FieldKey>();
		
		for (TrackInfoItem item : trackInfoItems) {
			result.add(item.getKey());
		}
		for (TrackInfoItem item : trackInfoItemsRemoved) {
			result.add(item.getKey());
		}
		
		return result;
	}

	@Override
	public int getRowCount() {
		return trackInfoItems.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex > trackInfoItems.size())
			return null;

		if (columnIndex == 0)
			return FieldKeyMetaHelper.getDisplayName(trackInfoItems.get(rowIndex).getKey());
		else
			return trackInfoItems.get(rowIndex).toString();
	}

	@Override
	public String getColumnName(int column) {
		return column == 0 ? "Key" : "Value";
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		trackInfoItems.get(rowIndex).getState().setValue((String) aValue);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

}
