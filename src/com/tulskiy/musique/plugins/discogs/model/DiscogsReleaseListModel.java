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

import java.util.LinkedList;
import java.util.List;

import org.discogs.model.ArtistRelease;

import com.tulskiy.musique.util.Util;

/**
 * @author mliauchuk
 */
public class DiscogsReleaseListModel extends DiscogsDefaultListModel {
	
	private List<ArtistRelease> releasesOriginal = new LinkedList<ArtistRelease>();
	private String filter = "";
	
	public void setFilter(String filter) {
		this.filter = Util.isEmpty(filter) ? filter : filter.toLowerCase();
		
		super.clear();
		for (ArtistRelease release : releasesOriginal) {
			filterRelease(release);
		}
	}

	public ArtistRelease getEx(int index) {
		Object item = super.get(index);
		if (item instanceof ArtistRelease) {
			return (ArtistRelease) item;
		}
		
		return null;
	}

	@Override
	public Object get(int index) {
		ArtistRelease release = getEx(index);
		return release == null ? null : getReleaseDescription(release);
	}

	@Override
	public void addElement(Object obj) {
		if (obj != null && obj instanceof ArtistRelease) {
			ArtistRelease release = (ArtistRelease) obj;
			releasesOriginal.add(release);
			filterRelease(release);
		}
	}

	@Override
	public void clear() {
		super.clear();
		releasesOriginal.clear();
	}

	private static String getReleaseDescription(ArtistRelease release) {
		return String.format("%s, %d, %s, %s",
				release.getTitle(),
				release.getYear(),
				Util.formatFieldValues(release.getLabelNames(), ","),
				release.getFormatString());
	}

	private void filterRelease(ArtistRelease release) {
		if (Util.isEmpty(filter) || getReleaseDescription(release).toLowerCase().indexOf(filter) > -1) {
			super.addElement(release);
		}
	}

}
