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

import org.discogs.ws.Discogs;

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
		RELEASE;
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
