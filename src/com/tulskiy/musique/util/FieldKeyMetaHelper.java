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

package com.tulskiy.musique.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;

/**
 * @Author: Maksim Liauchuk
 * @Date: 08.05.2011
 */
public class FieldKeyMetaHelper {

	private static final FieldKeyMetaHelper instance = new FieldKeyMetaHelper();
	private static Map<FieldKey, FieldKeyMeta> meta = new LinkedHashMap<FieldKey, FieldKeyMeta>();
	
	static {
		FieldKeyMeta keyMeta;
		int i = 0;

		keyMeta = instance.new FieldKeyMeta(FieldKey.ARTIST, "Artist", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
        keyMeta = instance.new FieldKeyMeta(FieldKey.BAND, "Band", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ALBUM_ARTIST, "Album Artist", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.TITLE, "Title", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ALBUM, "Album", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.YEAR, "Year", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.COMMENT, "Comment", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.GENRE, "Genre", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.TRACK, "Track", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.TRACK_TOTAL, "Total Tracks", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.DISC_NO, "Disc", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.DISC_TOTAL, "Total Discs", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.IS_COMPILATION, "Is Compilation", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.RECORD_LABEL, "Record Label", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.CATALOG_NO, "Catalog No", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.LYRICS, "Lyrics", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.RATING, "Rating", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ARTIST_SORT, "Artist (sort)", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ALBUM_ARTIST_SORT, "Album Artist (sort)", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.TITLE_SORT, "Title (sort)", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ALBUM_SORT, "Album (sort)", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.AMAZON_ID, "Amazon Id", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.BARCODE, "Barcode", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.BPM, "BPM", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.COMPOSER, "Composer", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.COMPOSER_SORT, "Composer (sort)", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.CONDUCTOR, "Conductor", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.COVER_ART, "Cover Art", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.CUSTOM1, "Custom 1", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.CUSTOM2, "Custom 2", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.CUSTOM3, "Custom 3", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.CUSTOM4, "Custom 4", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.CUSTOM5, "Custom 5", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ENCODER, "Encoder", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.FBPM, "FBPM", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.GROUPING, "Grouping", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ISRC, "ISRC", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.KEY, "Key", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.LANGUAGE, "Language", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.LYRICIST, "Lyricist", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MEDIA, "Media", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MOOD, "Mood", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_ARTISTID, "MusicBrainz Artist ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_DISC_ID, "MusicBrainz Disc ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_RELEASEARTISTID, "MusicBrainz Release Artist ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_RELEASEID, "MusicBrainz Release ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_RELEASE_COUNTRY, "MusicBrainz Release Country", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID, "MusicBrainz Release Group ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_RELEASE_STATUS, "MusicBrainz Release Status", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_RELEASE_TYPE, "MusicBrainz Release Type", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_TRACK_ID, "MusicBrainz Track ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICBRAINZ_WORK_ID, "MusicBrainz Work ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MUSICIP_ID, "MusicIP ID", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.OCCASION, "Occasion", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ORIGINAL_ARTIST, "Original Artist", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ORIGINAL_ALBUM, "Original Album", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ORIGINAL_YEAR, "Original Year", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ORIGINAL_LYRICIST, "Original Lyricist", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.QUALITY, "Quality", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.REMIXER, "Remixer", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.SCRIPT, "Script", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.TAGS, "Tags", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.TEMPO, "Tempo", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.URL_DISCOGS_ARTIST_SITE, "URL Discogs Artist Site", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.URL_DISCOGS_RELEASE_SITE, "URL Discogs Release Site", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.URL_LYRICS_SITE, "URL Lyrics Site", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.URL_OFFICIAL_ARTIST_SITE, "URL Official Artist Site", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.URL_OFFICIAL_RELEASE_SITE, "URL Official Release Site", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.URL_WIKIPEDIA_ARTIST_SITE, "URL Wikipedia Artist Site", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.URL_WIKIPEDIA_RELEASE_SITE, "URL Wikipedia Release Site", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ENGINEER, "Engineer", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.PRODUCER, "Producer", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.DJMIXER, "DJ Mixer", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.MIXER, "Mixer", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
		keyMeta = instance.new FieldKeyMeta(FieldKey.ARRANGER, "Arranger", ++i);
		meta.put(keyMeta.getKey(), keyMeta);
	};

	private FieldKeyMetaHelper() {
		// hide constructor
	}
	
	public static FieldKeyMeta getFieldKeyMeta(FieldKey key) {
		FieldKeyMeta keyMeta = meta.get(key);
		if (keyMeta == null) {
			throw new KeyNotFoundException();
		}
		return keyMeta;
	}

	/**
	 * Returns human readable field name.
	 * 
	 * @param key field key const
	 * @return human readable field name
	 */
	public static String getDisplayName(FieldKey key) throws KeyNotFoundException {
		FieldKeyMeta keyMeta = meta.get(key);
		if (keyMeta == null) {
			throw new KeyNotFoundException();
		}
		return keyMeta.getDisplayName();
	}

	/**
	 * Returns priority of displaying field for user.
	 * 
	 * @param key field key const
	 * @return priority of displaying field for user
	 */
	public static int getPriority(FieldKey key) throws KeyNotFoundException {
		FieldKeyMeta keyMeta = meta.get(key);
		if (keyMeta == null) {
			throw new KeyNotFoundException();
		}
		return keyMeta.getPriority();
	}
	
	public class FieldKeyMeta {
		
		private FieldKey key;
		private String displayName;
		private int priority;
		
		public FieldKeyMeta(FieldKey key, String displayName, int priority) {
			this.key = key;
			this.displayName = displayName;
			this.priority = priority;
		}
		
		public FieldKey getKey() {
			return key;
		}
		
		public String getDisplayName() {
			return displayName;
		}
		
		int getPriority() {
			return priority;
		}

		@Override
		public String toString() {
			return displayName;
		}

	}

}