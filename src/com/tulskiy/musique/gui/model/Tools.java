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

package com.tulskiy.musique.gui.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

/**
 * @author mliauchuk
 */
public class Tools {

	private Tools() {
		// hides constructor
	}

	// TODO refactor
	public static Collection<Album> groupTracksByAlbum(TrackInfoItem item) {
		if (Util.isEmpty(item.getTracks())) {
			return null;
		}

		Map<String, Album> albums = new LinkedHashMap<String, Album>(1, 1);
		for (Track track : item.getTracks()) {
			String albumId = Album.getAlbumId(track);
			String discRaw = item.getState().getValues(track).get(0);
			Integer disc = Util.isEmpty(discRaw) ? null : Integer.valueOf(discRaw);

			if (albums.containsKey(albumId)) {
				albums.get(albumId).addTrack(track, disc);
			}
			else {
				Album album = new Album();
				album.addTrack(track, disc);
				albums.put(albumId, album);
			}
		}

		return albums.values();
	}
	
	public static void autoTrackNumber(MultiTagFieldModel model) {
		TrackInfoItem track = getOrCreateTrackInfoItem(FieldKey.TRACK, model);
		TrackInfoItem trackTotal = getOrCreateTrackInfoItem(FieldKey.TRACK_TOTAL, model);
		TrackInfoItem disc = getOrCreateTrackInfoItem(FieldKey.DISC_NO, model);
		TrackInfoItem discTotal = getOrCreateTrackInfoItem(FieldKey.DISC_TOTAL, model);
		
		Collection<Album> albums = Tools.groupTracksByAlbum(disc);
		
		for (Album album : albums) {
			Integer dt = album.getDiscTotal();
			for (Integer dn : album.getDiscNumbers()) {
				Integer tn = 0;
				for (Track t : album.getDiscTracks(dn)) {
					tn++;
					track.getState().setValue(tn.toString(), t);
					disc.getState().setValue(dn.toString(), t);
					discTotal.getState().setValue(dt.toString(), t);
				}
				for (Track t : album.getDiscTracks(dn)) {
					trackTotal.getState().setValue(tn.toString(), t);
				}
			}
		}
	}
    
    private static TrackInfoItem getTrackInfoItem(FieldKey key, MultiTagFieldModel model) {
    	for (TrackInfoItem item : model.getTrackInfoItems()) {
    		if (item.getKey().equals(key)) {
    			return item;
    		}
    	}

    	return null;
    }
    
    private static TrackInfoItem getOrCreateTrackInfoItem(FieldKey key, MultiTagFieldModel model) {
		TrackInfoItem item = getTrackInfoItem(key, model);

		if (item == null) {
	    	item = new TrackInfoItem(key, model.getTrackInfoItems().get(0).getTracks());
	    	// clear new state values since they could be taken from tracks (if items were remove but not approved yet)
	    	item.getState().setValue("");
			model.addTrackInfoItem(item);
		}

		return item;
    }
}
