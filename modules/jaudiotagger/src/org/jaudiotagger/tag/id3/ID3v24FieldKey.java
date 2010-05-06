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

package org.jaudiotagger.tag.id3;

import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.id3.framebody.FrameBodyUFID;
import org.jaudiotagger.tag.id3.framebody.FrameBodyWXXX;

/**
 * List of known id3v24 metadata fields
 * <p/>
 * <p>These provide a mapping from the generic key to the underlying ID3v24frames. For example most of the Musicbrainz
 * fields are implemented using a User Defined Text Info Frame, but with a different description key, so this
 * enum provides the link between the two.
 */
public enum ID3v24FieldKey {
    ARTIST(ID3v24Frames.FRAME_ID_ARTIST, Id3FieldType.TEXT),
    ALBUM(ID3v24Frames.FRAME_ID_ALBUM, Id3FieldType.TEXT),
    TITLE(ID3v24Frames.FRAME_ID_TITLE, Id3FieldType.TEXT),
    TRACK(ID3v24Frames.FRAME_ID_TRACK, Id3FieldType.TEXT),
    YEAR(ID3v24Frames.FRAME_ID_YEAR, Id3FieldType.TEXT),
    GENRE(ID3v24Frames.FRAME_ID_GENRE, Id3FieldType.TEXT),
    COMMENT(ID3v24Frames.FRAME_ID_COMMENT, Id3FieldType.TEXT),
    ALBUM_ARTIST(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, "ALBUM ARTIST", Id3FieldType.TEXT),
    DATE(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, "DATE", Id3FieldType.TEXT),
    COMPOSER(ID3v24Frames.FRAME_ID_COMPOSER, Id3FieldType.TEXT),
    GROUPING(ID3v24Frames.FRAME_ID_CONTENT_GROUP_DESC, Id3FieldType.TEXT),
    DISC_NO(ID3v24Frames.FRAME_ID_SET, Id3FieldType.TEXT),
    BPM(ID3v24Frames.FRAME_ID_BPM, Id3FieldType.TEXT),
    ENCODER(ID3v24Frames.FRAME_ID_ENCODEDBY, Id3FieldType.TEXT),
    MUSICBRAINZ_ARTISTID(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_ARTISTID, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASEID(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_ALBUMID, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASEARTISTID(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_ALBUM_ARTISTID, Id3FieldType.TEXT),
    MUSICBRAINZ_TRACK_ID(ID3v24Frames.FRAME_ID_UNIQUE_FILE_ID, FrameBodyUFID.UFID_MUSICBRAINZ, Id3FieldType.TEXT),
    MUSICBRAINZ_DISC_ID(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_DISCID, Id3FieldType.TEXT),
    MUSICIP_ID(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSICIP_ID, Id3FieldType.TEXT),
    AMAZON_ID(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.AMAZON_ASIN, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASE_STATUS(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSICBRAINZ_ALBUM_STATUS, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASE_TYPE(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSICBRAINZ_ALBUM_TYPE, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASE_COUNTRY(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.MUSICBRAINZ_ALBUM_COUNTRY, Id3FieldType.TEXT),
    LYRICS(ID3v24Frames.FRAME_ID_UNSYNC_LYRICS, Id3FieldType.TEXT),
    IS_COMPILATION(ID3v24Frames.FRAME_ID_IS_COMPILATION, Id3FieldType.TEXT),
    ARTIST_SORT(ID3v24Frames.FRAME_ID_ARTIST_SORT_ORDER, Id3FieldType.TEXT),
    ALBUM_ARTIST_SORT(ID3v24Frames.FRAME_ID_ALBUM_ARTIST_SORT_ORDER_ITUNES, Id3FieldType.TEXT),
    ALBUM_SORT(ID3v24Frames.FRAME_ID_ALBUM_SORT_ORDER, Id3FieldType.TEXT),
    TITLE_SORT(ID3v24Frames.FRAME_ID_TITLE_SORT_ORDER, Id3FieldType.TEXT),
    COMPOSER_SORT(ID3v24Frames.FRAME_ID_COMPOSER_SORT_ORDER_ITUNES, Id3FieldType.TEXT),
    COVER_ART(ID3v24Frames.FRAME_ID_ATTACHED_PICTURE, Id3FieldType.BINARY),
    ISRC(ID3v24Frames.FRAME_ID_ISRC, Id3FieldType.TEXT),
    BARCODE(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.BARCODE, Id3FieldType.TEXT),
    CATALO_NO(ID3v24Frames.FRAME_ID_USER_DEFINED_INFO, FrameBodyTXXX.CATALOG_NO, Id3FieldType.TEXT),
    RECORD_LABEL(ID3v24Frames.FRAME_ID_PUBLISHER, Id3FieldType.TEXT),
    LYRICIST(ID3v24Frames.FRAME_ID_LYRICIST, Id3FieldType.TEXT),
    CONDUCTOR(ID3v24Frames.FRAME_ID_CONDUCTOR, Id3FieldType.TEXT),
    REMIXER(ID3v24Frames.FRAME_ID_REMIXED, Id3FieldType.TEXT),
    MOOD(ID3v24Frames.FRAME_ID_MOOD, Id3FieldType.TEXT),
    MEDIA(ID3v24Frames.FRAME_ID_MEDIA_TYPE, Id3FieldType.TEXT),
    URL_OFFICIAL_RELEASE_SITE(ID3v24Frames.FRAME_ID_USER_DEFINED_URL, FrameBodyWXXX.URL_OFFICIAL_RELEASE_SITE, Id3FieldType.TEXT),
    URL_DISCOGS_RELEASE_SITE(ID3v24Frames.FRAME_ID_USER_DEFINED_URL, FrameBodyWXXX.URL_DISCOGS_RELEASE_SITE, Id3FieldType.TEXT),
    URL_WIKIPEDIA_RELEASE_SITE(ID3v24Frames.FRAME_ID_USER_DEFINED_URL, FrameBodyWXXX.URL_WIKIPEDIA_RELEASE_SITE, Id3FieldType.TEXT),
    URL_OFFICIAL_ARTIST_SITE(ID3v24Frames.FRAME_ID_URL_ARTIST_WEB, Id3FieldType.TEXT),
    URL_DISCOGS_ARTIST_SITE(ID3v24Frames.FRAME_ID_USER_DEFINED_URL, FrameBodyWXXX.URL_DISCOGS_ARTIST_SITE, Id3FieldType.TEXT),
    URL_WIKIPEDIA_ARTIST_SITE(ID3v24Frames.FRAME_ID_USER_DEFINED_URL, FrameBodyWXXX.URL_WIKIPEDIA_ARTIST_SITE, Id3FieldType.TEXT),
    LANGUAGE(ID3v24Frames.FRAME_ID_LANGUAGE, Id3FieldType.TEXT),
    KEY(ID3v24Frames.FRAME_ID_INITIAL_KEY, Id3FieldType.TEXT),;
    private String fieldName;

    private String frameId;
    private String subId;
    private Id3FieldType fieldType;

    /**
     * For usual metadata fields that use a data field
     *
     * @param frameId   the frame that will be used
     * @param fieldType of data atom
     */
    ID3v24FieldKey(String frameId, Id3FieldType fieldType) {
        this.frameId = frameId;
        this.fieldType = fieldType;

        this.fieldName = frameId;
    }

    /**
     * @param frameId   the frame that will be used
     * @param subId     the additional key required within the frame to uniquely identify this key
     * @param fieldType
     */
    ID3v24FieldKey(String frameId, String subId, Id3FieldType fieldType) {
        this.frameId = frameId;
        this.subId = subId;
        this.fieldType = fieldType;

        this.fieldName = frameId + ":" + subId;
    }

    /**
     * @return fieldtype
     */
    public Id3FieldType getFieldType() {
        return fieldType;
    }

    /**
     * This is the frame identifier used to write the field
     *
     * @return
     */
    public String getFrameId() {
        return frameId;
    }

    /**
     * This is the subfield used within the frame for this type of field
     *
     * @return subId
     */
    public String getSubId() {
        return subId;
    }

    /**
     * This is the value of the key that can uniquely identifer a key type
     *
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }
}
