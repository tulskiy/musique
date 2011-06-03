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

import java.util.Comparator;

import org.jaudiotagger.tag.KeyNotFoundException;

import com.tulskiy.musique.util.FieldKeyMetaHelper;

public class TrackInfoItemComparator implements Comparator<TrackInfoItem> {

	@Override
	public int compare(TrackInfoItem o1, TrackInfoItem o2) {
		int priority1;
		int priority2;

		try {
			priority1 = FieldKeyMetaHelper.getPriority(o1.getKey());
		} catch (KeyNotFoundException knfe) {
			priority1 = Integer.MAX_VALUE;
		}
		try {
			priority2 = FieldKeyMetaHelper.getPriority(o2.getKey());
		} catch (KeyNotFoundException knfe) {
			priority2 = Integer.MAX_VALUE;
		}

		return priority1 - priority2;
	}

}
