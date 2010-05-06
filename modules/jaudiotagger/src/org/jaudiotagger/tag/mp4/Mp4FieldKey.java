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

package org.jaudiotagger.tag.mp4;

import org.jaudiotagger.tag.mp4.field.Mp4FieldType;
import static org.jaudiotagger.tag.mp4.field.Mp4FieldType.*;
import org.jaudiotagger.tag.mp4.field.Mp4TagReverseDnsField;
import org.jaudiotagger.tag.reference.Tagger;

/**
 * Starting list of known mp4 metadata fields that follow the Parent,Data or ---,issuer,name,data
 * convention. Atoms that contain metadata in other formats are not listed here because they need to be processed
 * specially.
 * <p/>
 * <p>Simple metaitems use the parent atom id as their identifier whereas reverse dns (----) atoms use
 * the reversedns,issuer and name fields as their identifier. When the atom is non-0standard but follws the rules
 * we list it here with an additional Tagger field to indicate where the field was originally designed.
 * <p/>
 * From:
 * http://www.hydrogenaudio.org/forums/index.php?showtopic=29120&st=0&p=251686&#entry251686
 * http://wiki.musicbrainz.org/PicardQt/TagMapping
 * http://atomicparsley.sourceforge.net/mpeg-4files.html
 * <p/>
 * <p/>
 */          
public enum Mp4FieldKey {
    ARTIST("\u00A9ART", TEXT),
    ALBUM("\u00A9alb", TEXT),
    ALBUM_ARTIST("aART", TEXT),
    GENRE_CUSTOM("\u00A9gen", TEXT),
    GENRE("gnre", NUMERIC),
    TITLE("\u00A9nam", TEXT),
    TRACK("trkn", NUMERIC),
    BPM("tmpo", BYTE, 2),
    DAY("\u00A9day", TEXT),
    COMMENT("\u00A9cmt", TEXT),
    COMPOSER("\u00A9wrt", TEXT),
    GROUPING("\u00A9grp", TEXT),
    DISCNUMBER("disk", NUMERIC),
    LYRICS("\u00A9lyr", TEXT),
    RATING("rtng", BYTE),   //AFAIK Cant be set in itunes, but if set to explicit itunes will show as explicit
    ENCODER("\u00A9too", TEXT),
    COMPILATION("cpil", BYTE, 1),
    COPYRIGHT("cprt", TEXT),
    CATEGORY("catg", TEXT),
    KEYWORD("keyw", TEXT),
    DESCRIPTION("desc", TEXT),
    ARTIST_SORT("soar", TEXT),
    ALBUM_ARTIST_SORT("soaa", TEXT),
    ALBUM_SORT("soal", TEXT),
    TITLE_SORT("sonm", TEXT),
    COMPOSER_SORT("soco", TEXT),
    SHOW_SORT("sosn", TEXT),
    SHOW("tvsh", TEXT),      //tv show but also used just as show
    ARTWORK("covr", COVERART_JPEG),
    PURCHASE_DATE("purd", TEXT),
    MUSICBRAINZ_ARTISTID("com.apple.iTunes", "MusicBrainz Artist Id", TEXT, Tagger.PICARD),
    MUSICBRAINZ_ALBUMID("com.apple.iTunes", "MusicBrainz Album Id", TEXT, Tagger.PICARD),
    MUSICBRAINZ_ALBUMARTISTID("com.apple.iTunes", "MusicBrainz Album Artist Id", TEXT, Tagger.PICARD),
    MUSICBRAINZ_TRACKID("com.apple.iTunes", "MusicBrainz Track Id", TEXT, Tagger.PICARD),
    MUSICBRAINZ_DISCID("com.apple.iTunes", "MusicBrainz Disc Id", TEXT, Tagger.PICARD),
    MUSICIP_PUID("com.apple.iTunes", "MusicIP PUID", TEXT, Tagger.PICARD),
    ASIN("com.apple.iTunes", "ASIN", TEXT, Tagger.PICARD),
    MUSICBRAINZ_ALBUM_STATUS("com.apple.iTunes", "MusicBrainz Album Status", TEXT, Tagger.PICARD),
    MUSICBRAINZ_ALBUM_TYPE("com.apple.iTunes", "MusicBrainz Album Type", TEXT, Tagger.PICARD),
    RELEASECOUNTRY("com.apple.iTunes", "MusicBrainz Album Release Country", TEXT, Tagger.PICARD),
    PART_OF_GAPLESS_ALBUM("pgap", BYTE),
    ITUNES_SMPB("com.apple.iTunes", "iTunSMPB", TEXT),
    ITUNES_NORM("com.apple.iTunes", "iTunNORM", TEXT),
    CDDB_1("com.apple.iTunes", "iTunes_CDDB_1", TEXT),
    CDDB_TRACKNUMBER("com.apple.iTunes", "iTunes_CDDB_TrackNumber", TEXT),
    CDDB_IDS("com.apple.iTunes", "iTunes_CDDB_IDs", TEXT),
    LANGUAGE("com.apple.iTunes", "Language", TEXT, Tagger.JAIKOZ),
    KEY("com.apple.iTunes", "key", TEXT, Tagger.JAIKOZ),

    //AFAIK These arent actually used by Audio Only files, but there is nothing to prevent them being used
    CONTENT_TYPE("stik", BYTE, 1),
    PODCAST_KEYWORD("keyw", TEXT),
    PODCAST_URL("purl", NUMERIC),   //TODO Actually seems to store text but is marked as numeric!
    EPISODE_GLOBAL_ID("egid", NUMERIC),   //TODO Actually seems to store text but is marked as numeric!
    TV_NETWORK("tvnn", TEXT),
    TV_EPISODE_NUMBER("tven", TEXT),
    TV_SEASON("tvsn", BYTE, 1),
    TV_EPISODE("tves", BYTE, 1),

    //These seem to be used in DRM Files, of type byte , we need to know the byte length to allow them to be written
    //back correctly on saving them, we don't provides options to modify them as may break drm
    AP_ID("apID", TEXT),
    AT_ID("atID", BYTE, 4),
    CN_ID("cnID", BYTE, 4),
    PL_ID("plID", BYTE, 8),
    GE_ID("geID", BYTE, 4),
    SF_ID("sfID", BYTE, 4),
    AK_ID("akID", BYTE, 1),

    //Media Monkey3 beta
    LYRICIST_MM3BETA("lyrc", TEXT, Tagger.MEDIA_MONKEY),
    CONDUCTOR_MM3BETA("cond", TEXT, Tagger.MEDIA_MONKEY),
    ISRC_MMBETA("isrc", TEXT, Tagger.MEDIA_MONKEY),
    MOOD_MM3BETA("mood", TEXT, Tagger.MEDIA_MONKEY),
    SCORE("rate", TEXT, Tagger.MEDIA_MONKEY),    //As in mark out of 100
    ORIGINAL_ARTIST("oart", TEXT, Tagger.MEDIA_MONKEY),
    ORIGINAL_ALBUM_TITLE("otit", TEXT, Tagger.MEDIA_MONKEY),
    ORIGINAL_LYRICIST("olyr", TEXT, Tagger.MEDIA_MONKEY),
    INVOLVED_PEOPLE("peop", TEXT, Tagger.MEDIA_MONKEY),
    TEMPO("empo", TEXT, Tagger.MEDIA_MONKEY),
    OCCASION("occa", TEXT, Tagger.MEDIA_MONKEY),
    QUALITY("qual", TEXT, Tagger.MEDIA_MONKEY),
    CUSTOM_1("cus1", TEXT, Tagger.MEDIA_MONKEY),
    CUSTOM_2("cus2", TEXT, Tagger.MEDIA_MONKEY),
    CUSTOM_3("cus3", TEXT, Tagger.MEDIA_MONKEY),
    CUSTOM_4("cus4", TEXT, Tagger.MEDIA_MONKEY),
    CUSTOM_5("cus5", TEXT, Tagger.MEDIA_MONKEY),

    //Media Monkey 3.0.6 Onwards
    MM_PUBLISHER("com.apple.iTunes", "ORGANIZATION", TEXT, Tagger.MEDIA_MONKEY),
    MM_ORIGINAL_ARTIST("com.apple.iTunes", "ORIGINAL ARTIST", TEXT, Tagger.MEDIA_MONKEY),
    MM_ORIGINAL_ALBUM_TITLE("com.apple.iTunes", "ORIGINAL ALBUM", TEXT, Tagger.MEDIA_MONKEY),
    MM_ORIGINAL_LYRICIST("com.apple.iTunes", "ORIGINAL LYRICIST", TEXT, Tagger.MEDIA_MONKEY),
    MM_INVOLVED_PEOPLE("com.apple.iTunes", "INVOLVED PEOPLE", TEXT, Tagger.MEDIA_MONKEY),
    MM_ORIGINAL_YEAR("com.apple.iTunes", "ORIGINAL YEAR", TEXT, Tagger.MEDIA_MONKEY),
    MM_TEMPO("com.apple.iTunes", "TEMPO", TEXT, Tagger.MEDIA_MONKEY),
    MM_OCCASION("com.apple.iTunes", "OCCASION", TEXT, Tagger.MEDIA_MONKEY),
    MM_QUALITY("com.apple.iTunes", "QUALITY", TEXT, Tagger.MEDIA_MONKEY),
    MM_CUSTOM_1("com.apple.iTunes", "CUSTOM1", TEXT, Tagger.MEDIA_MONKEY),
    MM_CUSTOM_2("com.apple.iTunes", "CUSTOM2", TEXT, Tagger.MEDIA_MONKEY),
    MM_CUSTOM_3("com.apple.iTunes", "CUSTOM3", TEXT, Tagger.MEDIA_MONKEY),
    MM_CUSTOM_4("com.apple.iTunes", "CUSTOM4", TEXT, Tagger.MEDIA_MONKEY),
    MM_CUSTOM_5("com.apple.iTunes", "CUSTOM5", TEXT, Tagger.MEDIA_MONKEY),

    //Picard Qt
    LYRICIST("com.apple.iTunes", "LYRICIST", TEXT, Tagger.PICARD),
    CONDUCTOR("com.apple.iTunes", "CONDUCTOR", TEXT, Tagger.PICARD),
    REMIXER("com.apple.iTunes", "REMIXER", TEXT, Tagger.PICARD),
    ENGINEER("com.apple.iTunes", "ENGINEER", TEXT, Tagger.PICARD),
    PRODUCER("com.apple.iTunes", "PRODUCER", TEXT, Tagger.PICARD),
    DJMIXER("com.apple.iTunes", "DJMIXER", TEXT, Tagger.PICARD),
    MIXER("com.apple.iTunes", "MIXER", TEXT, Tagger.PICARD),
    MOOD("com.apple.iTunes", "MOOD", TEXT, Tagger.PICARD),
    ISRC("com.apple.iTunes", "ISRC", TEXT, Tagger.PICARD),
    MEDIA("com.apple.iTunes", "MEDIA", TEXT, Tagger.PICARD),
    LABEL("com.apple.iTunes", "LABEL", TEXT, Tagger.PICARD),
    CATALOGNO("com.apple.iTunes", "CATALOGNUMBER", TEXT, Tagger.PICARD),
    BARCODE("com.apple.iTunes", "BARCODE", TEXT, Tagger.PICARD),

    //Jaikoz
    URL_OFFICIAL_RELEASE_SITE("com.apple.iTunes", "URL_OFFICIAL_RELEASE_SITE", TEXT, Tagger.JAIKOZ),
    URL_DISCOGS_RELEASE_SITE("com.apple.iTunes", "URL_DISCOGS_RELEASE_SITE", TEXT, Tagger.JAIKOZ),
    URL_WIKIPEDIA_RELEASE_SITE("com.apple.iTunes", "URL_WIKIPEDIA_RELEASE_SITE", TEXT, Tagger.JAIKOZ),
    URL_OFFICIAL_ARTIST_SITE("com.apple.iTunes", "URL_OFFICIAL_ARTIST_SITE", TEXT, Tagger.JAIKOZ),
    URL_DISCOGS_ARTIST_SITE("com.apple.iTunes", "URL_DISCOGS_ARTIST_SITE", TEXT, Tagger.JAIKOZ),
    URL_WIKIPEDIA_ARTIST_SITE("com.apple.iTunes", "URL_WIKIPEDIA_ARTIST_SITE", TEXT, Tagger.JAIKOZ),

    //Winamp
    WINAMP_PUBLISHER("com.nullsoft.winamp", "publisher", TEXT, Tagger.WINAMP),

    //Unknown
    KEYS("keys", TEXT),;
    private Tagger tagger;
    private String fieldName;
    private String issuer;
    private String identifier;
    private Mp4FieldType fieldType;
    private int fieldLength;

    /**
     * For usual metadata fields that use a data field
     *
     * @param fieldName
     * @param fieldType of data atom
     */
    Mp4FieldKey(String fieldName, Mp4FieldType fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    /**
     * For usual metadata fields that use a data field, but not recognised as standard field
     *
     * @param fieldName
     * @param fieldType of data atom
     */
    Mp4FieldKey(String fieldName, Mp4FieldType fieldType, Tagger tagger) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.tagger = tagger;
    }

    /**
     * For usual metadata fields that use a data field where the field length is fixed
     * such as Byte fields
     *
     * @param fieldName
     * @param fieldType
     * @param fieldLength
     */
    Mp4FieldKey(String fieldName, Mp4FieldType fieldType, int fieldLength) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldLength = fieldLength;
    }

    /**
     * For reverse dns fields that use an internal fieldname of '----' and have  additional issuer
     * and identifier fields, we use all three seperated by a ':' ) to give us a unique key
     *
     * @param identifier
     * @param fieldType  of data atom
     */
    Mp4FieldKey(String issuer, String identifier, Mp4FieldType fieldType) {

        this.issuer = issuer;
        this.identifier = identifier;
        this.fieldName = Mp4TagReverseDnsField.IDENTIFIER + ":" + issuer + ":" + identifier;
        this.fieldType = fieldType;
    }

    /**
     * For reverse dns fields that use an internal fieldname of '----' and have  additional issuer
     * and identifier fields, we use all three seperated by a ':' ) to give us a unique key
     * For non-standard fields
     *
     * @param identifier
     * @param fieldType  of data atom
     */
    Mp4FieldKey(String issuer, String identifier, Mp4FieldType fieldType, Tagger tagger) {

        this.issuer = issuer;
        this.identifier = identifier;
        this.fieldName = Mp4TagReverseDnsField.IDENTIFIER + ":" + issuer + ":" + identifier;
        this.fieldType = fieldType;
        this.tagger = tagger;
    }

    /**
     * This is the value of the fieldname that is actually used to write mp4
     *
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return fieldtype
     */
    public Mp4FieldType getFieldType() {
        return fieldType;
    }

    /**
     * @return true if this is a reverse dns key
     */
    public boolean isReverseDnsType() {
        return identifier.startsWith(Mp4TagReverseDnsField.IDENTIFIER);
    }

    /**
     * @return issuer (Reverse Dns Fields Only)
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @return identifier (Reverse Dns Fields Only)
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return field length (currently only used by byte fields)
     */
    public int getFieldLength() {
        return fieldLength;
    }

    public Tagger getTagger() {
        if (tagger != null) {
            return tagger;
        }
        return Tagger.ITUNES;
    }
}
