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

package com.tulskiy.musique.plugins.discogs;

import java.util.LinkedList;
import java.util.List;

import org.discogs.model.Artist;
import org.discogs.ws.Discogs;
import org.discogs.ws.search.ArtistSearchResult;
import org.discogs.ws.search.SearchResult;

/**
 * @author mliauchuk
 */
public class DiscogsCaller implements Runnable {
	
	private static Discogs DISCOGS = null;

	private CallMode mode;
	private String query;
	private DiscogsListener callback;
	
	public static enum CallMode {
		ARTIST,
		RELEASE,
		SEARCH_ARTISTS;
	}

	public DiscogsCaller(CallMode mode, String query, DiscogsListener callback) {
		super();

		if (DISCOGS == null) {
			DISCOGS = new Discogs(DiscogsPlugin.API_KEY);
		}

		this.mode = mode;
		this.query = query;
		this.callback = callback;
	}

	@Override
	public void run() {
		callback.onRetrieveStart(mode);

		Object result = null;

		try {
			switch (mode) {
				case ARTIST:
					result = DISCOGS.getArtist(query);
					break;
				case RELEASE:
					result = DISCOGS.getRelease(query);
					break;
				case SEARCH_ARTISTS:
					List<Artist> artists = new LinkedList<Artist>();
					List<SearchResult> srs = DISCOGS.search("artists", query).getSearchResults();
					for (SearchResult sr : srs) {
						if (sr instanceof ArtistSearchResult) {
							try {
								artists.add(((ArtistSearchResult) sr).getArtist());
							}
							catch (Exception e) {
								// failed to retrieve, don't bother
							}
						}
					}
					result = artists.isEmpty() ? null : artists;
					break;
				default:
					break;
			}
		}
		catch (Exception e) {
			// failed to retrieve, don't bother
		}

		// TODO investigate how to avoid this ugly hack (Cancel button pressed while querying Discogs)
		if (Thread.interrupted()) {
			return;
		}

		callback.onRetrieveFinish(mode, result);
	}

}
