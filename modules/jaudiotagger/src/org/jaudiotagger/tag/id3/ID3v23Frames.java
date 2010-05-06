/*
 * Jaudiotagger Copyright (C)2004,2005
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 * you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jaudiotagger.tag.id3;

import org.jaudiotagger.tag.TagFieldKey;

import java.util.EnumMap;

/**
 * Defines ID3v23 frames and collections that categorise frames within an ID3v23 tag.
 * <p/>
 * You can include frames here that are not officially supported as long as they can be used within an
 * ID3v23Tag
 *
 * @author Paul Taylor
 * @version $Id: ID3v23Frames.java,v 1.13 2008/12/05 11:11:54 paultaylor Exp $
 */
public class ID3v23Frames extends ID3Frames {
    /**
     * Define all frames that are valid within ID3v23
     * Frame IDs begining with T are text frames, & with W are url frames
     */
    public static final String FRAME_ID_V3_ACCOMPANIMENT = "TPE2";
    public static final String FRAME_ID_V3_ALBUM = "TALB";
    public static final String FRAME_ID_V3_ARTIST = "TPE1";
    public static final String FRAME_ID_V3_ATTACHED_PICTURE = "APIC";
    public static final String FRAME_ID_V3_AUDIO_ENCRYPTION = "AENC";
    public static final String FRAME_ID_V3_BPM = "TBPM";
    public static final String FRAME_ID_V3_COMMENT = "COMM";
    public static final String FRAME_ID_V3_COMMERCIAL_FRAME = "COMR";
    public static final String FRAME_ID_V3_COMPOSER = "TCOM";
    public static final String FRAME_ID_V3_CONDUCTOR = "TPE3";
    public static final String FRAME_ID_V3_CONTENT_GROUP_DESC = "TIT1";
    public static final String FRAME_ID_V3_COPYRIGHTINFO = "TCOP";
    public static final String FRAME_ID_V3_ENCODEDBY = "TENC";
    public static final String FRAME_ID_V3_ENCRYPTION = "ENCR";
    public static final String FRAME_ID_V3_EQUALISATION = "EQUA";
    public static final String FRAME_ID_V3_EVENT_TIMING_CODES = "ETCO";
    public static final String FRAME_ID_V3_FILE_OWNER = "TOWN";
    public static final String FRAME_ID_V3_FILE_TYPE = "TFLT";
    public static final String FRAME_ID_V3_GENERAL_ENCAPS_OBJECT = "GEOB";
    public static final String FRAME_ID_V3_GENRE = "TCON";
    public static final String FRAME_ID_V3_GROUP_ID_REG = "GRID";
    public static final String FRAME_ID_V3_HW_SW_SETTINGS = "TSSE";
    public static final String FRAME_ID_V3_INITIAL_KEY = "TKEY";
    public static final String FRAME_ID_V3_IPLS = "IPLS";
    public static final String FRAME_ID_V3_ISRC = "TSRC";
    public static final String FRAME_ID_V3_LANGUAGE = "TLAN";
    public static final String FRAME_ID_V3_LENGTH = "TLEN";
    public static final String FRAME_ID_V3_LINKED_INFO = "LINK";
    public static final String FRAME_ID_V3_LYRICIST = "TEXT";
    public static final String FRAME_ID_V3_MEDIA_TYPE = "TMED";
    public static final String FRAME_ID_V3_MPEG_LOCATION_LOOKUP_TABLE = "MLLT";
    public static final String FRAME_ID_V3_MUSIC_CD_ID = "MCDI";
    public static final String FRAME_ID_V3_ORIGARTIST = "TOPE";
    public static final String FRAME_ID_V3_ORIG_FILENAME = "TOFN";
    public static final String FRAME_ID_V3_ORIG_LYRICIST = "TOLY";
    public static final String FRAME_ID_V3_ORIG_TITLE = "TOAL";
    public static final String FRAME_ID_V3_OWNERSHIP = "OWNE";
    public static final String FRAME_ID_V3_PLAYLIST_DELAY = "TDLY";
    public static final String FRAME_ID_V3_PLAY_COUNTER = "PCNT";
    public static final String FRAME_ID_V3_POPULARIMETER = "POPM";
    public static final String FRAME_ID_V3_POSITION_SYNC = "POSS";
    public static final String FRAME_ID_V3_PRIVATE = "PRIV";
    public static final String FRAME_ID_V3_PUBLISHER = "TPUB";
    public static final String FRAME_ID_V3_RADIO_NAME = "TRSN";
    public static final String FRAME_ID_V3_RADIO_OWNER = "TRSO";
    public static final String FRAME_ID_V3_RECOMMENDED_BUFFER_SIZE = "RBUF";
    public static final String FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT = "RVAD";
    public static final String FRAME_ID_V3_REMIXED = "TPE4";
    public static final String FRAME_ID_V3_REVERB = "RVRB";
    public static final String FRAME_ID_V3_SET = "TPOS";
    public static final String FRAME_ID_V3_SYNC_LYRIC = "SYLT";
    public static final String FRAME_ID_V3_SYNC_TEMPO = "SYTC";
    public static final String FRAME_ID_V3_TDAT = "TDAT";
    public static final String FRAME_ID_V3_TERMS_OF_USE = "USER";
    public static final String FRAME_ID_V3_TIME = "TIME";
    public static final String FRAME_ID_V3_TITLE = "TIT2";
    public static final String FRAME_ID_V3_TITLE_REFINEMENT = "TIT3";
    public static final String FRAME_ID_V3_TORY = "TORY";
    public static final String FRAME_ID_V3_TRACK = "TRCK";
    public static final String FRAME_ID_V3_TRDA = "TRDA";
    public static final String FRAME_ID_V3_TSIZ = "TSIZ";
    public static final String FRAME_ID_V3_TYER = "TYER";
    public static final String FRAME_ID_V3_UNIQUE_FILE_ID = "UFID";
    public static final String FRAME_ID_V3_UNSYNC_LYRICS = "USLT";
    public static final String FRAME_ID_V3_URL_ARTIST_WEB = "WOAR";
    public static final String FRAME_ID_V3_URL_COMMERCIAL = "WCOM";
    public static final String FRAME_ID_V3_URL_COPYRIGHT = "WCOP";
    public static final String FRAME_ID_V3_URL_FILE_WEB = "WOAF";
    public static final String FRAME_ID_V3_URL_OFFICIAL_RADIO = "WORS";
    public static final String FRAME_ID_V3_URL_PAYMENT = "WPAY";
    public static final String FRAME_ID_V3_URL_PUBLISHERS = "WPUB";
    public static final String FRAME_ID_V3_URL_SOURCE_WEB = "WOAS";
    public static final String FRAME_ID_V3_USER_DEFINED_INFO = "TXXX";
    public static final String FRAME_ID_V3_USER_DEFINED_URL = "WXXX";

    public static final String FRAME_ID_V3_IS_COMPILATION = "TCMP";
    public static final String FRAME_ID_V3_TITLE_SORT_ORDER_ITUNES = "TSOT";
    public static final String FRAME_ID_V3_ARTIST_SORT_ORDER_ITUNES = "TSOP";
    public static final String FRAME_ID_V3_ALBUM_SORT_ORDER_ITUNES = "TSOA";
    public static final String FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ = "XSOT";
    public static final String FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ = "XSOP";
    public static final String FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ = "XSOA";
    public static final String FRAME_ID_V3_ALBUM_ARTIST_SORT_ORDER_ITUNES = "TSO2";
    public static final String FRAME_ID_V3_COMPOSER_SORT_ORDER_ITUNES = "TSOC";

    private static ID3v23Frames id3v23Frames;

    /**
     * Maps from Generic key to ID3 key
     */
    protected EnumMap<TagFieldKey, ID3v23FieldKey> tagFieldToId3 = new EnumMap<TagFieldKey, ID3v23FieldKey>(TagFieldKey.class);

    public static ID3v23Frames getInstanceOf() {
        if (id3v23Frames == null) {
            id3v23Frames = new ID3v23Frames();
        }
        return id3v23Frames;
    }

    private ID3v23Frames() {
        // The defined v23 frames,
        supportedFrames.add(FRAME_ID_V3_ACCOMPANIMENT);
        supportedFrames.add(FRAME_ID_V3_ALBUM);
        supportedFrames.add(FRAME_ID_V3_ARTIST);
        supportedFrames.add(FRAME_ID_V3_ATTACHED_PICTURE);
        supportedFrames.add(FRAME_ID_V3_AUDIO_ENCRYPTION);
        supportedFrames.add(FRAME_ID_V3_BPM);
        supportedFrames.add(FRAME_ID_V3_COMMENT);
        supportedFrames.add(FRAME_ID_V3_COMMERCIAL_FRAME);
        supportedFrames.add(FRAME_ID_V3_COMPOSER);
        supportedFrames.add(FRAME_ID_V3_CONDUCTOR);
        supportedFrames.add(FRAME_ID_V3_CONTENT_GROUP_DESC);
        supportedFrames.add(FRAME_ID_V3_COPYRIGHTINFO);
        supportedFrames.add(FRAME_ID_V3_ENCODEDBY);
        supportedFrames.add(FRAME_ID_V3_ENCRYPTION);
        supportedFrames.add(FRAME_ID_V3_EQUALISATION);
        supportedFrames.add(FRAME_ID_V3_EVENT_TIMING_CODES);
        supportedFrames.add(FRAME_ID_V3_FILE_OWNER);
        supportedFrames.add(FRAME_ID_V3_FILE_TYPE);
        supportedFrames.add(FRAME_ID_V3_GENERAL_ENCAPS_OBJECT);
        supportedFrames.add(FRAME_ID_V3_GENRE);
        supportedFrames.add(FRAME_ID_V3_GROUP_ID_REG);
        supportedFrames.add(FRAME_ID_V3_HW_SW_SETTINGS);
        supportedFrames.add(FRAME_ID_V3_INITIAL_KEY);
        supportedFrames.add(FRAME_ID_V3_IPLS);
        supportedFrames.add(FRAME_ID_V3_ISRC);
        supportedFrames.add(FRAME_ID_V3_LANGUAGE);
        supportedFrames.add(FRAME_ID_V3_LENGTH);
        supportedFrames.add(FRAME_ID_V3_LINKED_INFO);
        supportedFrames.add(FRAME_ID_V3_LYRICIST);
        supportedFrames.add(FRAME_ID_V3_MEDIA_TYPE);
        supportedFrames.add(FRAME_ID_V3_MPEG_LOCATION_LOOKUP_TABLE);
        supportedFrames.add(FRAME_ID_V3_MUSIC_CD_ID);
        supportedFrames.add(FRAME_ID_V3_ORIGARTIST);
        supportedFrames.add(FRAME_ID_V3_ORIG_FILENAME);
        supportedFrames.add(FRAME_ID_V3_ORIG_LYRICIST);
        supportedFrames.add(FRAME_ID_V3_ORIG_TITLE);
        supportedFrames.add(FRAME_ID_V3_OWNERSHIP);
        supportedFrames.add(FRAME_ID_V3_PLAYLIST_DELAY);
        supportedFrames.add(FRAME_ID_V3_PLAY_COUNTER);
        supportedFrames.add(FRAME_ID_V3_POPULARIMETER);
        supportedFrames.add(FRAME_ID_V3_POSITION_SYNC);
        supportedFrames.add(FRAME_ID_V3_PRIVATE);
        supportedFrames.add(FRAME_ID_V3_PUBLISHER);
        supportedFrames.add(FRAME_ID_V3_RADIO_NAME);
        supportedFrames.add(FRAME_ID_V3_RADIO_OWNER);
        supportedFrames.add(FRAME_ID_V3_RECOMMENDED_BUFFER_SIZE);
        supportedFrames.add(FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT);
        supportedFrames.add(FRAME_ID_V3_REMIXED);
        supportedFrames.add(FRAME_ID_V3_REVERB);
        supportedFrames.add(FRAME_ID_V3_SET);
        supportedFrames.add(FRAME_ID_V3_SYNC_LYRIC);
        supportedFrames.add(FRAME_ID_V3_SYNC_TEMPO);
        supportedFrames.add(FRAME_ID_V3_TDAT);
        supportedFrames.add(FRAME_ID_V3_TERMS_OF_USE);
        supportedFrames.add(FRAME_ID_V3_TIME);
        supportedFrames.add(FRAME_ID_V3_TITLE);
        supportedFrames.add(FRAME_ID_V3_TITLE_REFINEMENT);
        supportedFrames.add(FRAME_ID_V3_TORY);
        supportedFrames.add(FRAME_ID_V3_TRACK);
        supportedFrames.add(FRAME_ID_V3_TRDA);
        supportedFrames.add(FRAME_ID_V3_TSIZ);
        supportedFrames.add(FRAME_ID_V3_TYER);
        supportedFrames.add(FRAME_ID_V3_UNIQUE_FILE_ID);
        supportedFrames.add(FRAME_ID_V3_UNSYNC_LYRICS);
        supportedFrames.add(FRAME_ID_V3_URL_ARTIST_WEB);
        supportedFrames.add(FRAME_ID_V3_URL_COMMERCIAL);
        supportedFrames.add(FRAME_ID_V3_URL_COPYRIGHT);
        supportedFrames.add(FRAME_ID_V3_URL_FILE_WEB);
        supportedFrames.add(FRAME_ID_V3_URL_OFFICIAL_RADIO);
        supportedFrames.add(FRAME_ID_V3_URL_PAYMENT);
        supportedFrames.add(FRAME_ID_V3_URL_PUBLISHERS);
        supportedFrames.add(FRAME_ID_V3_URL_SOURCE_WEB);
        supportedFrames.add(FRAME_ID_V3_USER_DEFINED_INFO);
        supportedFrames.add(FRAME_ID_V3_USER_DEFINED_URL);

        //Extension
        extensionFrames.add(FRAME_ID_V3_IS_COMPILATION);
        extensionFrames.add(FRAME_ID_V3_TITLE_SORT_ORDER_ITUNES);
        extensionFrames.add(FRAME_ID_V3_ARTIST_SORT_ORDER_ITUNES);
        extensionFrames.add(FRAME_ID_V3_ALBUM_SORT_ORDER_ITUNES);
        extensionFrames.add(FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ);
        extensionFrames.add(FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ);
        extensionFrames.add(FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ);
        extensionFrames.add(FRAME_ID_V3_ALBUM_ARTIST_SORT_ORDER_ITUNES);
        extensionFrames.add(FRAME_ID_V3_COMPOSER_SORT_ORDER_ITUNES);

        //Common
        commonFrames.add(FRAME_ID_V3_ARTIST);
        commonFrames.add(FRAME_ID_V3_ALBUM);
        commonFrames.add(FRAME_ID_V3_TITLE);
        commonFrames.add(FRAME_ID_V3_GENRE);
        commonFrames.add(FRAME_ID_V3_TRACK);
        commonFrames.add(FRAME_ID_V3_TYER);
        commonFrames.add(FRAME_ID_V3_COMMENT);

        //Binary
        binaryFrames.add(FRAME_ID_V3_ATTACHED_PICTURE);
        binaryFrames.add(FRAME_ID_V3_AUDIO_ENCRYPTION);
        binaryFrames.add(FRAME_ID_V3_ENCRYPTION);
        binaryFrames.add(FRAME_ID_V3_EQUALISATION);
        binaryFrames.add(FRAME_ID_V3_EVENT_TIMING_CODES);
        binaryFrames.add(FRAME_ID_V3_GENERAL_ENCAPS_OBJECT);
        binaryFrames.add(FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT);
        binaryFrames.add(FRAME_ID_V3_RECOMMENDED_BUFFER_SIZE);
        binaryFrames.add(FRAME_ID_V3_UNIQUE_FILE_ID);

        // Map frameid to a name
        idToValue.put(FRAME_ID_V3_ACCOMPANIMENT, "Text: Band/Orchestra/Accompaniment");
        idToValue.put(FRAME_ID_V3_ALBUM, "Text: Album/Movie/Show title");
        idToValue.put(FRAME_ID_V3_ARTIST, "Text: Lead artist(s)/Lead performer(s)/Soloist(s)/Performing group");
        idToValue.put(FRAME_ID_V3_ATTACHED_PICTURE, "Attached picture");
        idToValue.put(FRAME_ID_V3_AUDIO_ENCRYPTION, "Audio encryption");
        idToValue.put(FRAME_ID_V3_BPM, "Text: BPM (Beats Per Minute)");
        idToValue.put(FRAME_ID_V3_COMMENT, "Comments");
        idToValue.put(FRAME_ID_V3_COMMERCIAL_FRAME, "");
        idToValue.put(FRAME_ID_V3_COMPOSER, "Text: Composer");
        idToValue.put(FRAME_ID_V3_CONDUCTOR, "Text: Conductor/Performer refinement");
        idToValue.put(FRAME_ID_V3_CONTENT_GROUP_DESC, "Text: Content group description");
        idToValue.put(FRAME_ID_V3_COPYRIGHTINFO, "Text: Copyright message");
        idToValue.put(FRAME_ID_V3_ENCODEDBY, "Text: Encoded by");
        idToValue.put(FRAME_ID_V3_ENCRYPTION, "Encryption method registration");
        idToValue.put(FRAME_ID_V3_EQUALISATION, "Equalization");
        idToValue.put(FRAME_ID_V3_EVENT_TIMING_CODES, "Event timing codes");
        idToValue.put(FRAME_ID_V3_FILE_OWNER, "");
        idToValue.put(FRAME_ID_V3_FILE_TYPE, "Text: File type");
        idToValue.put(FRAME_ID_V3_GENERAL_ENCAPS_OBJECT, "General encapsulated datatype");
        idToValue.put(FRAME_ID_V3_GENRE, "Text: Content type");
        idToValue.put(FRAME_ID_V3_GROUP_ID_REG, "");
        idToValue.put(FRAME_ID_V3_HW_SW_SETTINGS, "Text: Software/hardware and settings used for encoding");
        idToValue.put(FRAME_ID_V3_INITIAL_KEY, "Text: Initial key");
        idToValue.put(FRAME_ID_V3_IPLS, "Involved people list");
        idToValue.put(FRAME_ID_V3_ISRC, "Text: ISRC (International Standard Recording Code)");
        idToValue.put(FRAME_ID_V3_LANGUAGE, "Text: Language(s)");
        idToValue.put(FRAME_ID_V3_LENGTH, "Text: Length");
        idToValue.put(FRAME_ID_V3_LINKED_INFO, "Linked information");
        idToValue.put(FRAME_ID_V3_LYRICIST, "Text: Lyricist/text writer");
        idToValue.put(FRAME_ID_V3_MEDIA_TYPE, "Text: Media type");
        idToValue.put(FRAME_ID_V3_MPEG_LOCATION_LOOKUP_TABLE, "MPEG location lookup table");
        idToValue.put(FRAME_ID_V3_MUSIC_CD_ID, "Music CD Identifier");
        idToValue.put(FRAME_ID_V3_ORIGARTIST, "Text: Original artist(s)/performer(s)");
        idToValue.put(FRAME_ID_V3_ORIG_FILENAME, "Text: Original filename");
        idToValue.put(FRAME_ID_V3_ORIG_LYRICIST, "Text: Original Lyricist(s)/text writer(s)");
        idToValue.put(FRAME_ID_V3_ORIG_TITLE, "Text: Original album/Movie/Show title");
        idToValue.put(FRAME_ID_V3_OWNERSHIP, "");
        idToValue.put(FRAME_ID_V3_PLAYLIST_DELAY, "Text: Playlist delay");
        idToValue.put(FRAME_ID_V3_PLAY_COUNTER, "Play counter");
        idToValue.put(FRAME_ID_V3_POPULARIMETER, "Popularimeter");
        idToValue.put(FRAME_ID_V3_POSITION_SYNC, "Position Sync");
        idToValue.put(FRAME_ID_V3_PRIVATE, "Private frame");
        idToValue.put(FRAME_ID_V3_PUBLISHER, "Text: Publisher");
        idToValue.put(FRAME_ID_V3_RADIO_NAME, "");
        idToValue.put(FRAME_ID_V3_RADIO_OWNER, "");
        idToValue.put(FRAME_ID_V3_RECOMMENDED_BUFFER_SIZE, "Recommended buffer size");
        idToValue.put(FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT, "Relative volume adjustment");
        idToValue.put(FRAME_ID_V3_REMIXED, "Text: Interpreted, remixed, or otherwise modified by");
        idToValue.put(FRAME_ID_V3_REVERB, "Reverb");
        idToValue.put(FRAME_ID_V3_SET, "Text: Part of a set");
        idToValue.put(FRAME_ID_V3_SYNC_LYRIC, "Synchronized lyric/text");
        idToValue.put(FRAME_ID_V3_SYNC_TEMPO, "Synced tempo codes");
        idToValue.put(FRAME_ID_V3_TDAT, "Text: Date");
        idToValue.put(FRAME_ID_V3_TERMS_OF_USE, "");
        idToValue.put(FRAME_ID_V3_TIME, "Text: Time");
        idToValue.put(FRAME_ID_V3_TITLE, "Text: Title/Songname/Content description");
        idToValue.put(FRAME_ID_V3_TITLE_REFINEMENT, "Text: Subtitle/Description refinement");
        idToValue.put(FRAME_ID_V3_TORY, "Text: Original release year");
        idToValue.put(FRAME_ID_V3_TRACK, "Text: Track number/Position in set");
        idToValue.put(FRAME_ID_V3_TRDA, "Text: Recording dates");
        idToValue.put(FRAME_ID_V3_TSIZ, "Text: Size");
        idToValue.put(FRAME_ID_V3_TYER, "Text: Year");
        idToValue.put(FRAME_ID_V3_UNIQUE_FILE_ID, "Unique file identifier");
        idToValue.put(FRAME_ID_V3_UNSYNC_LYRICS, "Unsychronized lyric/text transcription");
        idToValue.put(FRAME_ID_V3_URL_ARTIST_WEB, "URL: Official artist/performer webpage");
        idToValue.put(FRAME_ID_V3_URL_COMMERCIAL, "URL: Commercial information");
        idToValue.put(FRAME_ID_V3_URL_COPYRIGHT, "URL: Copyright/Legal information");
        idToValue.put(FRAME_ID_V3_URL_FILE_WEB, "URL: Official audio file webpage");
        idToValue.put(FRAME_ID_V3_URL_OFFICIAL_RADIO, "Official Radio");
        idToValue.put(FRAME_ID_V3_URL_PAYMENT, "URL: Payment");
        idToValue.put(FRAME_ID_V3_URL_PUBLISHERS, "URL: Publishers official webpage");
        idToValue.put(FRAME_ID_V3_URL_SOURCE_WEB, "URL: Official audio source webpage");
        idToValue.put(FRAME_ID_V3_USER_DEFINED_INFO, "User defined text information frame");
        idToValue.put(FRAME_ID_V3_USER_DEFINED_URL, "User defined URL link frame");
        idToValue.put(FRAME_ID_V3_IS_COMPILATION, "Is Compilation");
        idToValue.put(FRAME_ID_V3_TITLE_SORT_ORDER_ITUNES, "Text: title sort order");
        idToValue.put(FRAME_ID_V3_ARTIST_SORT_ORDER_ITUNES, "Text: artist sort order");
        idToValue.put(FRAME_ID_V3_ALBUM_SORT_ORDER_ITUNES, "Text: album sort order");
        idToValue.put(FRAME_ID_V3_TITLE_SORT_ORDER_MUSICBRAINZ, "Text: title sort order");
        idToValue.put(FRAME_ID_V3_ARTIST_SORT_ORDER_MUSICBRAINZ, "Text: artist sort order");
        idToValue.put(FRAME_ID_V3_ALBUM_SORT_ORDER_MUSICBRAINZ, "Text: album sort order");
        idToValue.put(FRAME_ID_V3_ALBUM_ARTIST_SORT_ORDER_ITUNES, "Text:Album Artist Sort Order Frame");
        idToValue.put(FRAME_ID_V3_COMPOSER_SORT_ORDER_ITUNES, "Text:Composer Sort Order Frame");

        createMaps();

        multipleFrames.add(FRAME_ID_V3_USER_DEFINED_INFO);
        multipleFrames.add(FRAME_ID_V3_USER_DEFINED_URL);
        multipleFrames.add(FRAME_ID_V3_ATTACHED_PICTURE);
        multipleFrames.add(FRAME_ID_V3_PRIVATE);
        multipleFrames.add(FRAME_ID_V3_COMMENT);
        multipleFrames.add(FRAME_ID_V3_UNIQUE_FILE_ID);
        multipleFrames.add(FRAME_ID_V3_UNSYNC_LYRICS);
        multipleFrames.add(FRAME_ID_V3_POPULARIMETER);

        discardIfFileAlteredFrames.add(FRAME_ID_V3_EVENT_TIMING_CODES);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_EQUALISATION);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_MPEG_LOCATION_LOOKUP_TABLE);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_POSITION_SYNC);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_SYNC_LYRIC);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_SYNC_TEMPO);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_EVENT_TIMING_CODES);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_ENCODEDBY);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_LENGTH);
        discardIfFileAlteredFrames.add(FRAME_ID_V3_TSIZ);

        //Mapping to generic key
        tagFieldToId3.put(TagFieldKey.ARTIST, ID3v23FieldKey.ARTIST);
        tagFieldToId3.put(TagFieldKey.ALBUM, ID3v23FieldKey.ALBUM);
        tagFieldToId3.put(TagFieldKey.TITLE, ID3v23FieldKey.TITLE);
        tagFieldToId3.put(TagFieldKey.TRACK, ID3v23FieldKey.TRACK);
        tagFieldToId3.put(TagFieldKey.YEAR, ID3v23FieldKey.YEAR);
        tagFieldToId3.put(TagFieldKey.GENRE, ID3v23FieldKey.GENRE);
        tagFieldToId3.put(TagFieldKey.COMMENT, ID3v23FieldKey.COMMENT);
        tagFieldToId3.put(TagFieldKey.ALBUM_ARTIST, ID3v23FieldKey.ALBUM_ARTIST);
        tagFieldToId3.put(TagFieldKey.COMPOSER, ID3v23FieldKey.COMPOSER);
        tagFieldToId3.put(TagFieldKey.GROUPING, ID3v23FieldKey.GROUPING);
        tagFieldToId3.put(TagFieldKey.DISC_NO, ID3v23FieldKey.DISC_NO);
        tagFieldToId3.put(TagFieldKey.BPM, ID3v23FieldKey.BPM);
        tagFieldToId3.put(TagFieldKey.ENCODER, ID3v23FieldKey.ENCODER);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_ARTISTID, ID3v23FieldKey.MUSICBRAINZ_ARTISTID);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_RELEASEID, ID3v23FieldKey.MUSICBRAINZ_RELEASEID);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_RELEASEARTISTID, ID3v23FieldKey.MUSICBRAINZ_RELEASEARTISTID);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_TRACK_ID, ID3v23FieldKey.MUSICBRAINZ_TRACK_ID);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_DISC_ID, ID3v23FieldKey.MUSICBRAINZ_DISC_ID);
        tagFieldToId3.put(TagFieldKey.MUSICIP_ID, ID3v23FieldKey.MUSICIP_ID);
        tagFieldToId3.put(TagFieldKey.AMAZON_ID, ID3v23FieldKey.AMAZON_ID);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_RELEASE_STATUS, ID3v23FieldKey.MUSICBRAINZ_RELEASE_STATUS);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_RELEASE_TYPE, ID3v23FieldKey.MUSICBRAINZ_RELEASE_TYPE);
        tagFieldToId3.put(TagFieldKey.MUSICBRAINZ_RELEASE_COUNTRY, ID3v23FieldKey.MUSICBRAINZ_RELEASE_COUNTRY);
        tagFieldToId3.put(TagFieldKey.LYRICS, ID3v23FieldKey.LYRICS);
        tagFieldToId3.put(TagFieldKey.IS_COMPILATION, ID3v23FieldKey.IS_COMPILATION);
        tagFieldToId3.put(TagFieldKey.ARTIST_SORT, ID3v23FieldKey.ARTIST_SORT);
        tagFieldToId3.put(TagFieldKey.ALBUM_ARTIST_SORT, ID3v23FieldKey.ALBUM_ARTIST_SORT);
        tagFieldToId3.put(TagFieldKey.ALBUM_SORT, ID3v23FieldKey.ALBUM_SORT);
        tagFieldToId3.put(TagFieldKey.TITLE_SORT, ID3v23FieldKey.TITLE_SORT);
        tagFieldToId3.put(TagFieldKey.COMPOSER_SORT, ID3v23FieldKey.COMPOSER_SORT);
        tagFieldToId3.put(TagFieldKey.COVER_ART, ID3v23FieldKey.COVER_ART);
        tagFieldToId3.put(TagFieldKey.URL_DISCOGS_ARTIST_SITE, ID3v23FieldKey.URL_DISCOGS_ARTIST_SITE);
        tagFieldToId3.put(TagFieldKey.URL_DISCOGS_RELEASE_SITE, ID3v23FieldKey.URL_DISCOGS_RELEASE_SITE);
        tagFieldToId3.put(TagFieldKey.URL_WIKIPEDIA_ARTIST_SITE, ID3v23FieldKey.URL_WIKIPEDIA_ARTIST_SITE);
        tagFieldToId3.put(TagFieldKey.URL_WIKIPEDIA_RELEASE_SITE, ID3v23FieldKey.URL_WIKIPEDIA_RELEASE_SITE);
        tagFieldToId3.put(TagFieldKey.URL_OFFICIAL_ARTIST_SITE, ID3v23FieldKey.URL_OFFICIAL_ARTIST_SITE);
        tagFieldToId3.put(TagFieldKey.URL_OFFICIAL_RELEASE_SITE, ID3v23FieldKey.URL_OFFICIAL_RELEASE_SITE);
        tagFieldToId3.put(TagFieldKey.LANGUAGE, ID3v23FieldKey.LANGUAGE);
        tagFieldToId3.put(TagFieldKey.KEY, ID3v23FieldKey.KEY);
    }

    /**
     * @param genericKey
     * @return id3 key for generic key
     */
    public ID3v23FieldKey getId3KeyFromGenericKey(TagFieldKey genericKey) {
        return tagFieldToId3.get(genericKey);
    }
}
