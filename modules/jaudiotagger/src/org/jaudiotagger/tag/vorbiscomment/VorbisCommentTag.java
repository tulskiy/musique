/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphael Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jaudiotagger.tag.vorbiscomment;

import org.jaudiotagger.audio.generic.AbstractTag;
import org.jaudiotagger.audio.ogg.util.VorbisHeader;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagFieldKey;
import org.jaudiotagger.tag.datatype.Artwork;
import static org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey.*;
import org.jaudiotagger.tag.vorbiscomment.util.Base64Coder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * This is the logical representation of  Vorbis Comment Data
 */
public class VorbisCommentTag extends AbstractTag {
    private static EnumMap<TagFieldKey, VorbisCommentFieldKey> tagFieldToOggField = new EnumMap<TagFieldKey, VorbisCommentFieldKey>(TagFieldKey.class);

    static {
        tagFieldToOggField.put(TagFieldKey.ARTIST, VorbisCommentFieldKey.ARTIST);
        tagFieldToOggField.put(TagFieldKey.ALBUM, VorbisCommentFieldKey.ALBUM);
        tagFieldToOggField.put(TagFieldKey.TITLE, VorbisCommentFieldKey.TITLE);
        tagFieldToOggField.put(TagFieldKey.TRACK, VorbisCommentFieldKey.TRACKNUMBER);
        tagFieldToOggField.put(TagFieldKey.YEAR, VorbisCommentFieldKey.DATE);
        tagFieldToOggField.put(TagFieldKey.GENRE, VorbisCommentFieldKey.GENRE);
        tagFieldToOggField.put(TagFieldKey.COMMENT, VorbisCommentFieldKey.COMMENT);
        tagFieldToOggField.put(TagFieldKey.ALBUM_ARTIST, VorbisCommentFieldKey.ALBUMARTIST);
        tagFieldToOggField.put(TagFieldKey.COMPOSER, VorbisCommentFieldKey.COMPOSER);
        tagFieldToOggField.put(TagFieldKey.GROUPING, VorbisCommentFieldKey.GROUPING);
        tagFieldToOggField.put(TagFieldKey.DISC_NO, VorbisCommentFieldKey.DISCNUMBER);
        tagFieldToOggField.put(TagFieldKey.BPM, VorbisCommentFieldKey.BPM);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_ARTISTID, VorbisCommentFieldKey.MUSICBRAINZ_ARTISTID);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_RELEASEID, VorbisCommentFieldKey.MUSICBRAINZ_ALBUMID);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_RELEASEARTISTID, VorbisCommentFieldKey.MUSICBRAINZ_ALBUMARTISTID);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_TRACK_ID, VorbisCommentFieldKey.MUSICBRAINZ_TRACKID);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_DISC_ID, VorbisCommentFieldKey.MUSICBRAINZ_DISCID);
        tagFieldToOggField.put(TagFieldKey.MUSICIP_ID, VorbisCommentFieldKey.MUSICIP_PUID);
        tagFieldToOggField.put(TagFieldKey.AMAZON_ID, VorbisCommentFieldKey.ASIN);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_RELEASE_STATUS, VorbisCommentFieldKey.MUSICBRAINZ_ALBUMSTATUS);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_RELEASE_TYPE, VorbisCommentFieldKey.MUSICBRAINZ_ALBUMTYPE);
        tagFieldToOggField.put(TagFieldKey.MUSICBRAINZ_RELEASE_COUNTRY, VorbisCommentFieldKey.RELEASECOUNTRY);
        tagFieldToOggField.put(TagFieldKey.LYRICS, VorbisCommentFieldKey.LYRICS);
        tagFieldToOggField.put(TagFieldKey.IS_COMPILATION, VorbisCommentFieldKey.COMPILATION);
        tagFieldToOggField.put(TagFieldKey.ARTIST_SORT, VorbisCommentFieldKey.ARTISTSORT);
        tagFieldToOggField.put(TagFieldKey.ALBUM_ARTIST_SORT, VorbisCommentFieldKey.ALBUMARTISTSORT);
        tagFieldToOggField.put(TagFieldKey.ALBUM_SORT, VorbisCommentFieldKey.ALBUMSORT);
        tagFieldToOggField.put(TagFieldKey.TITLE_SORT, VorbisCommentFieldKey.TITLESORT);
        tagFieldToOggField.put(TagFieldKey.COMPOSER_SORT, VorbisCommentFieldKey.COMPOSERSORT);
        tagFieldToOggField.put(TagFieldKey.ENCODER, VorbisCommentFieldKey.VENDOR);     //Known as vendor in VorbisComment
        tagFieldToOggField.put(TagFieldKey.ISRC, VorbisCommentFieldKey.ISRC);
        tagFieldToOggField.put(TagFieldKey.BARCODE, VorbisCommentFieldKey.BARCODE);
        tagFieldToOggField.put(TagFieldKey.CATALOG_NO, VorbisCommentFieldKey.CATALOGNUMBER);
        tagFieldToOggField.put(TagFieldKey.RECORD_LABEL, VorbisCommentFieldKey.LABEL);
        tagFieldToOggField.put(TagFieldKey.LYRICIST, VorbisCommentFieldKey.LYRICIST);
        tagFieldToOggField.put(TagFieldKey.CONDUCTOR, VorbisCommentFieldKey.CONDUCTOR);
        tagFieldToOggField.put(TagFieldKey.REMIXER, VorbisCommentFieldKey.REMIXER);
        tagFieldToOggField.put(TagFieldKey.MOOD, VorbisCommentFieldKey.MOOD);
        tagFieldToOggField.put(TagFieldKey.MEDIA, VorbisCommentFieldKey.MEDIA);
        tagFieldToOggField.put(TagFieldKey.URL_DISCOGS_ARTIST_SITE, VorbisCommentFieldKey.URL_DISCOGS_ARTIST_SITE);
        tagFieldToOggField.put(TagFieldKey.URL_DISCOGS_RELEASE_SITE, VorbisCommentFieldKey.URL_DISCOGS_RELEASE_SITE);
        tagFieldToOggField.put(TagFieldKey.URL_OFFICIAL_ARTIST_SITE, VorbisCommentFieldKey.URL_OFFICIAL_ARTIST_SITE);
        tagFieldToOggField.put(TagFieldKey.URL_OFFICIAL_RELEASE_SITE, VorbisCommentFieldKey.URL_OFFICIAL_RELEASE_SITE);
        tagFieldToOggField.put(TagFieldKey.URL_WIKIPEDIA_ARTIST_SITE, VorbisCommentFieldKey.URL_WIKIPEDIA_ARTIST_SITE);
        tagFieldToOggField.put(TagFieldKey.URL_WIKIPEDIA_RELEASE_SITE, VorbisCommentFieldKey.URL_WIKIPEDIA_RELEASE_SITE);
        tagFieldToOggField.put(TagFieldKey.LANGUAGE, VorbisCommentFieldKey.LANGUAGE);
        tagFieldToOggField.put(TagFieldKey.KEY, VorbisCommentFieldKey.KEY);
    }

    //This is the vendor string that will be written if no other is supplied. Should be the name of the software
    //that actually encoded the file in the first place.
    public static final String DEFAULT_VENDOR = "jaudiotagger";

    /**
     * Only used within Package, hidden because it doesnt set Vendor
     * which should be done when created by end user
     */
    public VorbisCommentTag() {

    }

    /**
     * Use to construct a new tag properly initialized
     *
     * @return
     */
    public static VorbisCommentTag createNewTag() {
        VorbisCommentTag tag = new VorbisCommentTag();
        tag.setVendor(DEFAULT_VENDOR);
        return tag;
    }

    public TagField createAlbumField(String content) {
        if (content == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(getAlbumId(), content);
    }

    public TagField createArtistField(String content) {
        if (content == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(getArtistId(), content);
    }

    public TagField createCommentField(String content) {
        if (content == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(getCommentId(), content);
    }

    public TagField createGenreField(String content) {
        if (content == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(getGenreId(), content);
    }

    public TagField createTitleField(String content) {
        if (content == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(getTitleId(), content);
    }

    public TagField createTrackField(String content) {
        if (content == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(getTrackId(), content);
    }

    public TagField createYearField(String content) {
        if (content == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(getYearId(), content);
    }

    protected String getAlbumId() {
        return ALBUM.name();
    }

    protected String getArtistId() {
        return ARTIST.name();
    }

    protected String getCommentId() {
        return COMMENT.name();
    }

    protected String getGenreId() {
        return GENRE.name();
    }

    protected String getTitleId() {
        return TITLE.name();
    }

    protected String getTrackId() {
        return TRACKNUMBER.name();
    }

    /**
     * @return the vendor, generically known as the encoder
     */
    public String getVendor() {
        return getFirst(VENDOR.name());
    }

    protected String getYearId() {
        return DATE.toString();
    }

    /**
     * Set the vendor, known as the encoder  generally
     * <p/>
     * We dont want this to be blank, when written to file this field is written to a different location
     * to all other fields but user of library can just reat it as another field
     *
     * @param vendor
     */
    public void setVendor(String vendor) {
        if (vendor == null) {
            vendor = DEFAULT_VENDOR;
        }
        super.set(new VorbisCommentTagField(VENDOR.name(), vendor));
    }

    protected boolean isAllowedEncoding(String enc) {
        return enc.equals(VorbisHeader.CHARSET_UTF_8);
    }

    public String toString() {
        return "OGG " + super.toString();
    }

    /**
     * Create Tag Field using generic key
     */
    @Override
    public TagField createTagField(TagFieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (genericKey == null) {
            throw new KeyNotFoundException();
        }
        return createTagField(tagFieldToOggField.get(genericKey), value);
    }

    /**
     * Create Tag Field using ogg key
     *
     * @param vorbisCommentFieldKey
     * @param value
     * @return
     */
    public TagField createTagField(VorbisCommentFieldKey vorbisCommentFieldKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (value == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        if (vorbisCommentFieldKey == null) {
            throw new KeyNotFoundException();
        }

        return new VorbisCommentTagField(vorbisCommentFieldKey.name(), value);
    }

    /**
     * Create Tag Field using ogg key
     * <p/>
     * This method is provided to allow you to create key of any value because VorbisComment allows
     * arbitary keys.
     *
     * @param vorbisCommentFieldKey
     * @param value
     * @return
     */
    public TagField createTagField(String vorbisCommentFieldKey, String value) {
        if (value == null) {
            throw new IllegalArgumentException(ErrorMessage.GENERAL_INVALID_NULL_ARGUMENT.getMsg());
        }
        return new VorbisCommentTagField(vorbisCommentFieldKey, value);
    }

    /**
     * Maps the generic key to the ogg key and return the list of values for this field
     *
     * @param genericKey
     */
    @Override
    public List<TagField> get(TagFieldKey genericKey) throws KeyNotFoundException {
        VorbisCommentFieldKey vorbisCommentFieldKey = tagFieldToOggField.get(genericKey);
        if (vorbisCommentFieldKey == null) {
            throw new KeyNotFoundException();
        }
        return super.get(vorbisCommentFieldKey.name());
    }

    /**
     * Retrieve the first value that exists for this vorbis comment key
     *
     * @param vorbisCommentKey
     * @return
     */
    public List<TagField> get(VorbisCommentFieldKey vorbisCommentKey) throws KeyNotFoundException {
        if (vorbisCommentKey == null) {
            throw new KeyNotFoundException();
        }
        return super.get(vorbisCommentKey.name());
    }

    /**
     * Retrieve the first value that exists for this generic key
     *
     * @param genericKey
     * @return
     */
    public String getFirst(TagFieldKey genericKey) throws KeyNotFoundException {
        VorbisCommentFieldKey vorbisCommentFieldKey = tagFieldToOggField.get(genericKey);
        if (vorbisCommentFieldKey == null) {
            throw new KeyNotFoundException();
        }
        return super.getFirst(vorbisCommentFieldKey.name());
    }

    /**
     * Retrieve the first value that exists for this vorbis comment key
     *
     * @param vorbisCommentKey
     * @return
     */
    public String getFirst(VorbisCommentFieldKey vorbisCommentKey) throws KeyNotFoundException {
        if (vorbisCommentKey == null) {
            throw new KeyNotFoundException();
        }
        return super.getFirst(vorbisCommentKey.name());
    }

    /**
     * Delete fields with this generic key
     *
     * @param genericKey
     */
    public void deleteTagField(TagFieldKey genericKey) throws KeyNotFoundException {
        if (genericKey == null) {
            throw new KeyNotFoundException();
        }
        VorbisCommentFieldKey vorbisCommentFieldKey = tagFieldToOggField.get(genericKey);
        deleteTagField(vorbisCommentFieldKey);
    }

    /**
     * Delete fields with this vorbisCommentFieldKey
     *
     * @param vorbisCommentFieldKey
     */
    public void deleteTagField(VorbisCommentFieldKey vorbisCommentFieldKey) throws KeyNotFoundException {
        if (vorbisCommentFieldKey == null) {
            throw new KeyNotFoundException();
        }
        super.deleteField(vorbisCommentFieldKey.name());
    }

    /**
     * Create artwork field
     * <p/>
     * Actually create two fields , the data field and the mimetype
     *
     * @param data     raw image data
     * @param mimeType mimeType of data
     *                 <p/>
     *                 TODO could possibly work out mimetype from data, but unlike mp4 there is nothing to restrict to only png
     *                 or jpeg images
     * @return
     */
    public void setArtworkField(byte[] data, String mimeType) {
        char[] testdata = Base64Coder.encode(data);
        String base64image = new String(testdata);
        VorbisCommentTagField dataField = new VorbisCommentTagField(VorbisCommentFieldKey.COVERART.name(), base64image);
        VorbisCommentTagField mimeField = new VorbisCommentTagField(VorbisCommentFieldKey.COVERARTMIME.name(), mimeType);

        set(dataField);
        set(mimeField);

    }

    /**
     * Retrieve artwork raw data
     *
     * @return
     */
    public byte[] getArtworkBinaryData() {
        String base64data = this.getFirst(VorbisCommentFieldKey.COVERART);
        byte[] rawdata = Base64Coder.decode(base64data.toCharArray());
        return rawdata;
    }

    /**
     * @return mimetype
     */
    public String getArtworkMimeType() {
        return this.getFirst(VorbisCommentFieldKey.COVERARTMIME);
    }

    /**
     * Is this tag empty
     * <p/>
     * <p>Overridden because check for size of one because there is always a vendor tag unless just
     * created an empty vorbis tag as part of flac tag in which case size could be zero
     *
     * @see org.jaudiotagger.tag.Tag#isEmpty()
     */
    public boolean isEmpty() {
        return fields.size() <= 1;
    }

    /**
     * Add Field
     * <p/>
     * <p>Overidden because there can only be one vendor set
     *
     * @param field
     */
    public void add(TagField field) {
        if (field.getId().equals(VorbisCommentFieldKey.VENDOR.name())) {
            super.set(field);
        } else {
            super.add(field);
        }
    }

    public TagField getFirstField(TagFieldKey genericKey) throws KeyNotFoundException {
        if (genericKey == null) {
            throw new KeyNotFoundException();
        }
        return getFirstField(tagFieldToOggField.get(genericKey).name());
    }

    public List<Artwork> getArtworkList() {
        List<Artwork> artworkList = new ArrayList<Artwork>(1);

        if (getArtworkBinaryData() != null & getArtworkBinaryData().length > 0) {
            Artwork artwork = new Artwork();
            artwork.setMimeType(getArtworkMimeType());
            artwork.setBinaryData(getArtworkBinaryData());
            artworkList.add(artwork);
        }
        return artworkList;
    }

    /**
     * Create artwork field
     * <p/>
     * Not supported because reuire two fields to be created use
     *
     * @return
     */
    public TagField createArtworkField(Artwork artwork) throws FieldDataInvalidException {
        throw new UnsupportedOperationException("Please use createAndSetArtworkField instead");
    }

    /**
     * Create artwork field
     * <p/>
     * Actually sets two fields
     *
     * @return
     */
    @Override
    public void createAndSetArtworkField(Artwork artwork) throws FieldDataInvalidException {
        char[] testdata = Base64Coder.encode(artwork.getBinaryData());
        String base64image = new String(testdata);
        TagField imageTagField = createTagField(VorbisCommentFieldKey.COVERART, base64image);
        TagField imageTypeField = createTagField(VorbisCommentFieldKey.COVERARTMIME, artwork.getMimeType());

        this.set(imageTagField);
        this.set(imageTypeField);
    }
}

