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
