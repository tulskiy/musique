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
package org.jaudiotagger.audio.generic;

import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.Artwork;

import java.util.Collections;
import java.util.List;

/**
 * This is a complete example implementation of
 * {@link AbstractTag}.<br>
 * The identifiers of commonly used fields is defined by {@link #keys}.<br>
 *
 * @author Raphael Slinckx
 */
public abstract class GenericTag extends AbstractTag {

    /**
     * Implementations of {@link TagTextField} for use with
     * &quot;ISO-8859-1&quot; strings.
     *
     * @author Raphael Slinckx
     */
    private class GenericTagTextField implements TagTextField {

        /**
         * Stores the string.
         */
        private String content;

        /**
         * Stores the identifier.
         */
        private final String id;

        /**
         * Creates an instance.
         *
         * @param fieldId        The identifier.
         * @param initialContent The string.
         */
        public GenericTagTextField(String fieldId, String initialContent) {
            this.id = fieldId;
            this.content = initialContent;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#copyContent(org.jaudiotagger.tag.TagField)
         */
        public void copyContent(TagField field) {
            if (field instanceof TagTextField) {
                this.content = ((TagTextField) field).getContent();
            }
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#getContent()
         */
        public String getContent() {
            return this.content;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#getEncoding()
         */
        public String getEncoding() {
            return "ISO-8859-1";
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#getId()
         */
        public String getId() {
            return id;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#getRawContent()
         */
        public byte[] getRawContent() {
            return this.content == null ? new byte[]{} : Utils.getDefaultBytes(this.content, getEncoding());
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isBinary()
         */
        public boolean isBinary() {
            return false;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isBinary(boolean)
         */
        public void isBinary(boolean b) {
            /* not supported */
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isCommon()
         */
        public boolean isCommon() {
            return true;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagField#isEmpty()
         */
        public boolean isEmpty() {
            return this.content.equals("");
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#setContent(java.lang.String)
         */
        public void setContent(String s) {
            this.content = s;
        }

        /**
         * (overridden)
         *
         * @see org.jaudiotagger.tag.TagTextField#setEncoding(java.lang.String)
         */
        public void setEncoding(String s) {
            /* Not allowed */
        }

        /**
         * (overridden)
         *
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return getId() + " : " + getContent();
        }
    }

    /**
     * Index for the &quot;album&quot;-identifier in {@link #keys}.
     */
    public static final int ALBUM = 1;

    /**
     * Index for the &quot;artist&quot;-identifier in {@link #keys}.
     */
    public static final int ARTIST = 0;

    /**
     * Index for the &quot;comment&quot;-identifier in {@link #keys}.
     */
    public static final int COMMENT = 6;

    /**
     * Index for the &quot;genre&quot;-identifier in {@link #keys}.
     */
    public static final int GENRE = 5;

    /**
     * Stores the generic identifiers of commonly used fields.
     */
    private final static String[] keys = {"ARTIST", "ALBUM", "TITLE", "TRACK", "YEAR", "GENRE", "COMMENT",};

    /**
     * Index for the &quot;title&quot;-identifier in {@link #keys}.
     */
    public static final int TITLE = 2;

    /**
     * Index for the &quot;track&quot;-identifier in {@link #keys}.
     */
    public static final int TRACK = 3;

    /**
     * Index for the &quot;year&quot;-identifier in {@link #keys}.
     */
    public static final int YEAR = 4;

    /**
     * (overridden)
     *
     * @see AbstractTag#createAlbumField(java.lang.String)
     */
    public TagField createAlbumField(String content) {
        return new GenericTagTextField(keys[ALBUM], content);
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#createArtistField(java.lang.String)
     */
    public TagField createArtistField(String content) {
        return new GenericTagTextField(keys[ARTIST], content);
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#createCommentField(java.lang.String)
     */
    public TagField createCommentField(String content) {
        return new GenericTagTextField(keys[COMMENT], content);
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#createGenreField(java.lang.String)
     */
    public TagField createGenreField(String content) {
        return new GenericTagTextField(keys[GENRE], content);
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#createTitleField(java.lang.String)
     */
    public TagField createTitleField(String content) {
        return new GenericTagTextField(keys[TITLE], content);
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#createTrackField(java.lang.String)
     */
    public TagField createTrackField(String content) {
        return new GenericTagTextField(keys[TRACK], content);
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#createYearField(java.lang.String)
     */
    public TagField createYearField(String content) {
        return new GenericTagTextField(keys[YEAR], content);
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#getAlbumId()
     */
    protected String getAlbumId() {
        return keys[ALBUM];
    }

    /**
     * (overridden)
     *
     * @see AbstractTag#getArtistId()
     */
    protected String getArtistId() {
        return keys[ARTIST];
    }

    /**
     * (overridden)
     *
     * @see org.jaudiotagger.audio.generic.AbstractTag#getCommentId()
     */
    protected String getCommentId() {
        return keys[COMMENT];
    }

    /**
     * (overridden)
     *
     * @see org.jaudiotagger.audio.generic.AbstractTag#getGenreId()
     */
    protected String getGenreId() {
        return keys[GENRE];
    }

    /**
     * (overridden)
     *
     * @see org.jaudiotagger.audio.generic.AbstractTag#getTitleId()
     */
    protected String getTitleId() {
        return keys[TITLE];
    }

    /**
     * (overridden)
     *
     * @see org.jaudiotagger.audio.generic.AbstractTag#getTrackId()
     */
    protected String getTrackId() {
        return keys[TRACK];
    }

    /**
     * (overridden)
     *
     * @see org.jaudiotagger.audio.generic.AbstractTag#getYearId()
     */
    protected String getYearId() {
        return keys[YEAR];
    }

    /**
     * (overridden)
     *
     * @see org.jaudiotagger.audio.generic.AbstractTag#isAllowedEncoding(java.lang.String)
     */
    protected boolean isAllowedEncoding(String enc) {
        return true;
    }

    public TagField createTagField(TagFieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        throw new UnsupportedOperationException("Not implemented for this format");
    }

    /**
     * @param genericKey
     * @return
     * @throws KeyNotFoundException
     */
    public String getFirst(TagFieldKey genericKey) throws KeyNotFoundException {
        throw new UnsupportedOperationException("Not implemented for this format");
    }

    /**
     * @param tagFieldKey
     * @throws KeyNotFoundException
     */
    public void deleteTagField(TagFieldKey tagFieldKey) throws KeyNotFoundException {
        throw new UnsupportedOperationException("Not implemented for this format");
    }

    /**
     * @param genericKey
     * @return
     * @throws KeyNotFoundException
     */
    public TagField getFirstField(TagFieldKey genericKey) throws KeyNotFoundException {
        throw new UnsupportedOperationException("Not implemented for this format");
    }

    public List<Artwork> getArtworkList() {
        return Collections.emptyList();
    }

    public TagField createArtworkField(Artwork artwork) throws FieldDataInvalidException {
        throw new UnsupportedOperationException("Not implemented for this format");
    }
}
