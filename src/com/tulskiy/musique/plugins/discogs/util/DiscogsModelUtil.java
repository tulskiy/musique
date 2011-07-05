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

package com.tulskiy.musique.plugins.discogs.util;

import java.util.List;
import java.util.regex.Pattern;

import org.discogs.model.Format;
import org.discogs.model.Release;
import org.discogs.model.ReleaseArtist;

import com.tulskiy.musique.plugins.discogs.model.ReleaseTracklistingModel;
import com.tulskiy.musique.util.Util;


/**
 * @author mliauchuk
 */
public class DiscogsModelUtil {

	public static final String DEFAULT_RELEASE_INFO_TEXT = "N/A";

	public static final Pattern[] trackPositionPatterns = {
			Pattern.compile("(\\d+)\\.(\\d+).+"),
			Pattern.compile("(\\d+)-(\\d+).+"),
			Pattern.compile("CD(\\d+)\\.(\\d+).+"),
			Pattern.compile("CD(\\d+)-(\\d+).+")
	};

	private DiscogsModelUtil() {
		// prevent instantiation for util class
	}

	public static String getReleaseArtistDescription(List<ReleaseArtist> artists,
			boolean useAnv, boolean addAnvChar) {
		StringBuilder result = new StringBuilder("");

		if (artists != null) {
			for (ReleaseArtist artist : artists) {
				if (useAnv && !Util.isEmpty(artist.getANV())) {
					result.append(getArtistNameCleared(artist.getANV()));
					if (addAnvChar) {
						result.append('*');
					}
				}
				else {
					result.append(getArtistNameCleared(artist.getName()));
				}
				if (!Util.isEmpty(artist.getJoin())) {
					result.append(' ').append(artist.getJoin()).append(' ');
				}
			}
		}
		
		return result.toString();
	}
	
	public static String getReleaseLabelDescription(Release release) {
		StringBuilder result = new StringBuilder("");

		if (release != null) {
			for (int i = 0; i < release.getLabelReleases().size(); i++) {
				if (i > 0) {
					result.append(" / ");
				}
				result.append(release.getLabelReleases().get(i).getLabelName());
			}
		}
		
		return result.toString();
	}
	
	public static String getReleaseCatalogNoDescription(Release release) {
		StringBuilder result = new StringBuilder("");

		if (release != null) {
			for (int i = 0; i < release.getLabelReleases().size(); i++) {
				if (i > 0) {
					result.append(" / ");
				}
				result.append(release.getLabelReleases().get(i).getCatalogNumber());
			}
		}
		
		return result.toString();
	}
	
	public static String getReleaseDateDescription(Release release) {
		String result = DEFAULT_RELEASE_INFO_TEXT;

		if (release.getReleaseDateRaw() != null) {
			result = release.getReleaseDateRaw();
		}
//		if (release.getReleaseDate() != null) {
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//			result = formatter.format(release.getReleaseDate());
//		}
		
		return result;
	}
	
	public static String getReleaseFormatDescription(Release release) {
		StringBuilder result = new StringBuilder("");

		for (Format format : release.getFormats()) {
			if (result.length() > 0) {
				result.append(" + ");
			}
			result.append(format.getQuantity()).append(" x ").append(format.getName());
			List<String> descriptions = format.getDescriptions();
			if (!Util.isEmpty(descriptions)) {
				for (String description : descriptions)
				result.append(", ").append(description);
			}
		}
		
		return result.toString();
	}
	
	public static String getArtistNameCleared(String artistName) {
		if (Util.isEmpty(artistName)) {
			return artistName;
		}
		
		String result = artistName.split(" \\(\\d+\\)")[0];
		int theIndex = result.lastIndexOf(", The");
		if (theIndex > -1) {
			result = "The " + result.substring(0, theIndex);
		}
		
		return result;
	}
	
	public static ReleaseTracklistingModel getReleaseTracklistingModel(Release release) {
		return new ReleaseTracklistingModel(release);
	}

}
