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

import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

public class SingleTagFieldModel extends AbstractTableModel implements TagFieldModel {

	private TrackInfoItem trackInfoItem;
	private Track selectedTrack;
	private List<String> oldValues = new LinkedList<String>();
	private List<String> newValues = new LinkedList<String>();

	public SingleTagFieldModel(TrackInfoItem item) {
		this(item, null);
	}

	public SingleTagFieldModel(TrackInfoItem item, Track track) {
		trackInfoItem = item;
		selectedTrack = track;
		trackInfoItem.addState();
		initValues();
		if (getRowCount() == 0) {
			addValue();
		}
	}

	public TrackInfoItem getTrackInfoItem() {
		return trackInfoItem;
	}
	
	public boolean isMultiTrackEditMode() {
		return selectedTrack == null;
	}
	
	private void initValues() {
		oldValues.clear();
		newValues.clear();
		if (isMultiTrackEditMode()) {
			for (Track track : trackInfoItem.getTracks()) {
				oldValues.add(track.getTrackData().getFileName());
				newValues.add(Util.formatFieldValues(trackInfoItem.getState().getValues(track)));
			}
		}
		else {
			oldValues.addAll(trackInfoItem.getState().getValues(selectedTrack));
			newValues.addAll(oldValues);
		}
	}
	
	public void addValue() {
		oldValues.add("<new value>");
		newValues.add("");
	}
	
	public void removeValue(int index) {
		oldValues.remove(index);
		newValues.remove(index);
	}
	
	private void updateState() {
		if (!isMultiTrackEditMode()) {
			if (newValues.isEmpty()) {
				trackInfoItem.getState().setValue("", selectedTrack);
			}
			else {
				trackInfoItem.getState().setValue(newValues.get(0), selectedTrack);
				for (int i = 1; i < newValues.size(); i++) {
					trackInfoItem.getState().addValue(newValues.get(i), selectedTrack);
				}
			}
		}
	}
	
	public void approveModel() {
		updateState();
		trackInfoItem.approveState(false);
		initValues();
	}
	
	public void refreshModel() {
		initValues();
	}
	
	public void rejectModel() {
		trackInfoItem.rejectState();
		initValues();
	}

	@Override
	public int getRowCount() {
		return oldValues.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		String result = null;

		if (rowIndex > -1 & rowIndex < getRowCount()) {
			result = columnIndex == 0 ? oldValues.get(rowIndex) : newValues.get(rowIndex);
		}
		
		return result;
	}

	@Override
	public String getColumnName(int column) {
		if (isMultiTrackEditMode()) {
			return column == 0 ? "File" : "Value(s)";
		}
		else {
			return column == 0 ? "Original" : "New";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (!isMultiTrackEditMode()) {
			newValues.set(rowIndex, (String) aValue);
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1 && !isMultiTrackEditMode();
	}
}
