/*
 * Copyright (c) 2008, 2009 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jaudiotagger.tag;

/**
 * This is an enumeration of common tag keys
 * <p/>
 * <p/>
 * <p/>
 * This enumeration is used by subclasses to map from the common key to their implementation key, the keys
 * are grouped within EnumSets within Tag class.
 */
public enum TagFieldKey {
    ARTIST,
    ALBUM,
    TITLE,
    TRACK,
    YEAR,
    GENRE,
    COMMENT,
    ALBUM_ARTIST,
    DATE,
    COMPOSER,
    GROUPING,
    DISC_NO,
    COVER_ART,
    BPM,
    MUSICBRAINZ_ARTISTID,
    MUSICBRAINZ_RELEASEID,
    MUSICBRAINZ_RELEASEARTISTID,
    MUSICBRAINZ_TRACK_ID,
    MUSICBRAINZ_DISC_ID,
    MUSICIP_ID,
    AMAZON_ID,
    MUSICBRAINZ_RELEASE_STATUS,
    MUSICBRAINZ_RELEASE_TYPE,
    MUSICBRAINZ_RELEASE_COUNTRY,
    LYRICS,
    IS_COMPILATION,
    ARTIST_SORT,
    ALBUM_ARTIST_SORT,
    ALBUM_SORT,
    TITLE_SORT,
    COMPOSER_SORT,
    ENCODER,
    ISRC,
    BARCODE,
    CATALOG_NO,
    RECORD_LABEL,
    LYRICIST,
    CONDUCTOR,
    REMIXER,
    MOOD,
    MEDIA,
    URL_OFFICIAL_RELEASE_SITE,
    URL_DISCOGS_RELEASE_SITE,
    URL_WIKIPEDIA_RELEASE_SITE,
    URL_OFFICIAL_ARTIST_SITE,
    URL_DISCOGS_ARTIST_SITE,
    URL_WIKIPEDIA_ARTIST_SITE,
    LANGUAGE,
    KEY;
}
