package org.jaudiotagger.tag.id3;

import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.id3.framebody.FrameBodyUFID;
import org.jaudiotagger.tag.id3.framebody.FrameBodyWXXX;

/**
 * List of known id3v22 metadata fields
 * <p/>
 * <p>These provide a mapping from the generic key to the underlying ID3v22frames. For example most of the Musicbrainz
 * fields are implemnted using a User Defined Text Info Frame, but with a different description key, so this
 * enum provides the link between the two.
 */
public enum ID3v22FieldKey {
    ARTIST(ID3v22Frames.FRAME_ID_V2_ARTIST, Id3FieldType.TEXT),
    ALBUM(ID3v22Frames.FRAME_ID_V2_ALBUM, Id3FieldType.TEXT),
    TITLE(ID3v22Frames.FRAME_ID_V2_TITLE, Id3FieldType.TEXT),
    TRACK(ID3v22Frames.FRAME_ID_V2_TRACK, Id3FieldType.TEXT),
    YEAR(ID3v22Frames.FRAME_ID_V2_TYER, Id3FieldType.TEXT),
    GENRE(ID3v22Frames.FRAME_ID_V2_GENRE, Id3FieldType.TEXT),
    COMMENT(ID3v22Frames.FRAME_ID_V2_COMMENT, Id3FieldType.TEXT),
    ALBUM_ARTIST(ID3v22Frames.FRAME_ID_V2_ACCOMPANIMENT, Id3FieldType.TEXT),
    COMPOSER(ID3v22Frames.FRAME_ID_V2_COMPOSER, Id3FieldType.TEXT),
    GROUPING(ID3v22Frames.FRAME_ID_V2_CONTENT_GROUP_DESC, Id3FieldType.TEXT),
    DISC_NO(ID3v22Frames.FRAME_ID_V2_SET, Id3FieldType.TEXT),
    BPM(ID3v22Frames.FRAME_ID_V2_BPM, Id3FieldType.TEXT),
    ENCODER(ID3v22Frames.FRAME_ID_V2_ENCODEDBY, Id3FieldType.TEXT),
    MUSICBRAINZ_ARTISTID(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_ARTISTID, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASEID(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_ALBUMID, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASEARTISTID(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_ALBUM_ARTISTID, Id3FieldType.TEXT),
    MUSICBRAINZ_TRACK_ID(ID3v22Frames.FRAME_ID_V2_UNIQUE_FILE_ID, FrameBodyUFID.UFID_MUSICBRAINZ, Id3FieldType.TEXT),
    MUSICBRAINZ_DISC_ID(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSIC_BRAINZ_DISCID, Id3FieldType.TEXT),
    MUSICIP_ID(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSICIP_ID, Id3FieldType.TEXT),
    AMAZON_ID(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.AMAZON_ASIN, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASE_STATUS(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSICBRAINZ_ALBUM_STATUS, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASE_TYPE(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSICBRAINZ_ALBUM_TYPE, Id3FieldType.TEXT),
    MUSICBRAINZ_RELEASE_COUNTRY(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.MUSICBRAINZ_ALBUM_COUNTRY, Id3FieldType.TEXT),
    LYRICS(ID3v22Frames.FRAME_ID_V2_UNSYNC_LYRICS, Id3FieldType.TEXT),
    IS_COMPILATION(ID3v22Frames.FRAME_ID_V2_IS_COMPILATION, Id3FieldType.TEXT),
    ARTIST_SORT(ID3v22Frames.FRAME_ID_V2_ARTIST_SORT_ORDER_ITUNES, Id3FieldType.TEXT),
    ALBUM_ARTIST_SORT(ID3v22Frames.FRAME_ID_V2_ALBUM_ARTIST_SORT_ORDER_ITUNES, Id3FieldType.TEXT),
    ALBUM_SORT(ID3v22Frames.FRAME_ID_V2_ALBUM_SORT_ORDER_ITUNES, Id3FieldType.TEXT),
    TITLE_SORT(ID3v22Frames.FRAME_ID_V2_TITLE_SORT_ORDER_ITUNES, Id3FieldType.TEXT),
    COMPOSER_SORT(ID3v22Frames.FRAME_ID_V2_COMPOSER_SORT_ORDER_ITUNES, Id3FieldType.TEXT),
    COVER_ART(ID3v22Frames.FRAME_ID_V2_ATTACHED_PICTURE, Id3FieldType.BINARY),
    ISRC(ID3v22Frames.FRAME_ID_V2_ISRC, Id3FieldType.TEXT),
    BARCODE(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.BARCODE, Id3FieldType.TEXT),
    CATALO_NO(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_INFO, FrameBodyTXXX.CATALOG_NO, Id3FieldType.TEXT),
    RECORD_LABEL(ID3v22Frames.FRAME_ID_V2_PUBLISHER, Id3FieldType.TEXT),
    LYRICIST(ID3v22Frames.FRAME_ID_V2_LYRICIST, Id3FieldType.TEXT),
    CONDUCTOR(ID3v22Frames.FRAME_ID_V2_CONDUCTOR, Id3FieldType.TEXT),
    REMIXER(ID3v22Frames.FRAME_ID_V2_REMIXED, Id3FieldType.TEXT),
    MEDIA(ID3v22Frames.FRAME_ID_V2_MEDIA_TYPE, Id3FieldType.TEXT),
    URL_OFFICIAL_RELEASE_SITE(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_URL, FrameBodyWXXX.URL_OFFICIAL_RELEASE_SITE, Id3FieldType.TEXT),
    URL_DISCOGS_RELEASE_SITE(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_URL, FrameBodyWXXX.URL_DISCOGS_RELEASE_SITE, Id3FieldType.TEXT),
    URL_WIKIPEDIA_RELEASE_SITE(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_URL, FrameBodyWXXX.URL_WIKIPEDIA_RELEASE_SITE, Id3FieldType.TEXT),
    URL_OFFICIAL_ARTIST_SITE(ID3v22Frames.FRAME_ID_V2_URL_ARTIST_WEB, Id3FieldType.TEXT),
    URL_DISCOGS_ARTIST_SITE(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_URL, FrameBodyWXXX.URL_DISCOGS_ARTIST_SITE, Id3FieldType.TEXT),
    URL_WIKIPEDIA_ARTIST_SITE(ID3v22Frames.FRAME_ID_V2_USER_DEFINED_URL, FrameBodyWXXX.URL_WIKIPEDIA_ARTIST_SITE, Id3FieldType.TEXT),
    LANGUAGE(ID3v22Frames.FRAME_ID_V2_LANGUAGE, Id3FieldType.TEXT),
    KEY(ID3v22Frames.FRAME_ID_V2_INITIAL_KEY, Id3FieldType.TEXT),;

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
    ID3v22FieldKey(String frameId, Id3FieldType fieldType) {
        this.frameId = frameId;
        this.fieldType = fieldType;

        this.fieldName = frameId;
    }

    /**
     * @param frameId   the frame that will be used
     * @param subId     the additioanl key required within the frame to uniquely identify this key
     * @param fieldType
     */
    ID3v22FieldKey(String frameId, String subId, Id3FieldType fieldType) {
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
