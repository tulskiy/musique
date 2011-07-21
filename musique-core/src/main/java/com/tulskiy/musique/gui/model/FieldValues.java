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
import java.util.List;

import com.tulskiy.musique.util.Util;

/**
 * @author mliauchuk
 */
public class FieldValues {

	private String singleV;
	private List<String> multiV;
	/** Indicates whether same values should be skipped (true) or added anyway (false). */
	private boolean isStripModeEnabled;
	
	public FieldValues() {
		singleV = null;
		multiV = null;
		isStripModeEnabled = true;
	}
	
	public FieldValues(String value) {
		this();
		set(value);
	}
	
	public FieldValues(List<String> values) {
		this();
		set(values);
	}
	
	public FieldValues(FieldValues values) {
		this();
		set(values);
	}
	
	public void set(String value) {
		clear();
		singleV = value;
	}
	
	public void set(List<String> values) {
		clear();
		if (!Util.isEmpty(values)) {
			if (values.size() == 1) {
				singleV = values.get(0);
			}
			else {
				multiV = new ArrayList<String>(values.size());
				for (String value : values) {
					if (isStripModeEnabled && contains(value)) {
						continue;
					}
					else {
						multiV.add(value);
					}
				}
			}
		}
	}
	
	public void set(FieldValues values) {
		clear();
		if (values != null) {
			for (int i = 0; i < values.size(); i++) {
				add(values.get(i));
			}
		}
	}
	
	public void add(String value) {
		if (isStripModeEnabled && contains(value)) {
			return;
		}

		if (multiV == null) {
			if (singleV == null) {
				set(value);
			}
			else {
				multiV = new ArrayList<String>(2);
				multiV.add(singleV);
				multiV.add(value);
				singleV = null;
			}
		}
		else {
			multiV.add(value);
		}
	}
	
	public void add(FieldValues values) {
		if (!FieldValues.isEmptyEx(values)) {
			for (int i = 0; i < values.size(); i++) {
				add(values.get(i));
			}
		}
	}
	
	public void remove(int index) {
		if (multiV != null) {
			if (index < multiV.size()) {
				multiV.remove(index);
			}
		}
		else if (index == 0) {
			singleV = null;
		}
	}
	
	public void clear() {
		singleV = null;
		if (multiV != null) {
			multiV.clear();
			multiV = null;
		}
	}

	public boolean contains(String value) {
		if (singleV != null) {
			return singleV.equals(value);
		}
		if (multiV != null) {
			for (String s : multiV) {
				if (s != null && s.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	public String get(int index) {
		if (singleV != null) {
			if (index == 0) {
				return singleV;
			}
			else {
				throw new IndexOutOfBoundsException();
			}
		}
		if (multiV != null) {
			return multiV.get(index);
		}
		return null;
	}

	public boolean isEmpty() {
		return singleV == null && Util.isEmpty(multiV);
	}


	public int size() {
		if (singleV != null) {
			return 1;
		}
		if (multiV != null) {
			return multiV.size();
		}
		return 0;
	}

	@Override
	public String toString() {
		if (singleV != null) {
			return singleV;
		}
		if (multiV != null) {
			StringBuilder sb = new StringBuilder("[");
			boolean isFirst = true;
			for (String value : multiV) {
				if (isFirst) {
					isFirst = false;
				}
				else {
					sb.append(", ");
				}
				sb.append(value == null ? "" : value);
			}
			sb.append(']');
			return sb.toString();
		}
		return null;
	}
	
	public static boolean isEmptyEx(FieldValues values) {
		return values == null || values.isEmpty();
	}
	
	public void addFieldValuesToList(List<String> list) {
		if (list != null) {
			for (int i = 0; i < this.size(); i++) {
				String value = this.get(i);
				if (isStripModeEnabled && contains(value)) {
					continue;
				}
				else {
					list.add(value);
				}
			}
		}
	}
	
	public void setFieldValuesToList(List<String> list) {
		if (list != null) {
			list.clear();
			for (int i = 0; i < this.size(); i++) {
				list.add(this.get(i));
			}
		}
	}

}
