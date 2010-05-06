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
package org.jaudiotagger.tag;

import org.jaudiotagger.tag.datatype.Artwork;

import java.util.Iterator;
import java.util.List;

/**
 * This interface represents the basic data structure for the default
 * audiolibrary functionality.<br>
 * <p/>
 * Some audio file tagging systems allow to specify multiple values for one type
 * of information. The artist for example. Some songs may be a cooperation of
 * two or more artists. Sometimes a tagging user wants to specify them in the
 * tag without making one long text string.<br>
 * For that kind of fields, the <b>commonly</b> used fields have adequate
 * methods for adding values. But it is possible the underlying implementation
 * does not support that kind of storing multiple values.<br>
 * <br>
 * <b>Code Examples:</b><br>
 * <p/>
 * <pre>
 * <code>
 * AudioFile file = AudioFileIO.read(new File(&quot;C:\\test.mp3&quot;));
 * <p/>
 * Tag tag = file.getTag();
 * </code>
 * </pre>
 *
 * @author Raphael Slinckx
 */
public interface Tag {
    /**
     * Adds a tagfield to the structure.<br>
     * <p/>
     * <p>It is not recommended to use this method for normal use of the
     * audiolibrary. The developer will circumvent the underlying
     * implementation. For example, if one adds a field with the field id
     * &quot;TALB&quot; for an mp3 file, and the given {@link TagField}
     * implementation does not return a text field compliant data with
     * {@link TagField#getRawContent()} other software and the audio library
     * won't read the file correctly, if they do read it at all. <br>
     * So for short:<br>
     * <uil>
     * <li>The field is stored withoud validation</li>
     * <li>No conversion of data is perfomed</li>
     * </ul>
     *
     * @param field The field to add.
     */
    public void add(TagField field) throws FieldDataInvalidException;

    /**
     * Adds an album to the tag.<br>
     *
     * @param album Album description
     */
    public void addAlbum(String album) throws FieldDataInvalidException;

    /**
     * Adds an artist to the tag.<br>
     *
     * @param artist Artist's name
     */
    public void addArtist(String artist) throws FieldDataInvalidException;

    /**
     * Adds a comment to the tag.<br>
     *
     * @param comment Comment.
     */
    public void addComment(String comment) throws FieldDataInvalidException;

    /**
     * Adds a genre to the tag.<br>
     *
     * @param genre Genre
     */
    public void addGenre(String genre) throws FieldDataInvalidException;

    /**
     * Adds a title to the tag.<br>
     *
     * @param title Title
     */
    public void addTitle(String title) throws FieldDataInvalidException;

    /**
     * Adds a track to the tag.<br>
     *
     * @param track Track
     */
    public void addTrack(String track) throws FieldDataInvalidException;

    /**
     * Adds a year to the Tag.<br>
     *
     * @param year Year
     */
    public void addYear(String year) throws FieldDataInvalidException;

    /**
     * Returns a {@linkplain List list} of {@link TagField} objects whose &quot;{@linkplain TagField#getId() id}&quot;
     * is the specified one.<br>
     *
     * @param id The field id.
     * @return A list of {@link TagField} objects with the given &quot;id&quot;.
     */
    public List<TagField> get(String id);

    /**
     * @return
     */
    public List<TagField> getAlbum();

    /**
     * @return
     */
    public List<TagField> getArtist();

    /**
     * @return
     */
    public List<TagField> getComment();

    /**
     * @return
     */
    public List<TagField> getGenre();

    /**
     * @return
     */
    public List<TagField> getTitle();

    /**
     * @return
     */
    public List<TagField> getTrack();

    /**
     * @return
     */
    public List<TagField> getYear();

    /**
     * @return
     */
    public String getFirstAlbum();

    /**
     * @return
     */
    public String getFirstArtist();

    /**
     * @return
     */
    public String getFirstComment();

    /**
     * @return
     */
    public String getFirstGenre();

    /**
     * @return
     */
    public String getFirstTitle();

    /**
     * @return
     */
    public String getFirstTrack();

    /**
     * @return
     */
    public String getFirstYear();

    /**
     * Returns <code>true</code>, if at least one of the contained
     * {@linkplain TagField fields} is a common field ({@link TagField#isCommon()}).
     *
     * @return <code>true</code> if a {@linkplain TagField#isCommon() common}
     *         field is present.
     */
    public boolean hasCommonFields();

    /**
     * Determines whether the tag has at least one field with the specified
     * &quot;id&quot;.
     *
     * @param id The field id to look for.
     * @return <code>true</code> if tag contains a {@link TagField} with the
     *         given {@linkplain TagField#getId() id}.
     */
    public boolean hasField(String id);

    /**
     * Determines whether the tag has no fields specified.<br>
     *
     * @return <code>true</code> if tag contains no field.
     */
    public boolean isEmpty();

    /**
     * @param field
     * @throws FieldDataInvalidException
     */
    public void set(TagField field) throws FieldDataInvalidException;

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setAlbum(String s) throws FieldDataInvalidException;

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setArtist(String s) throws FieldDataInvalidException;

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setComment(String s) throws FieldDataInvalidException;

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setGenre(String s) throws FieldDataInvalidException;

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setTitle(String s) throws FieldDataInvalidException;

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setTrack(String s) throws FieldDataInvalidException;

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setYear(String s) throws FieldDataInvalidException;

    /**
     * Create a new TagField based on generic key
     * <p/>
     * <p>Only textual data supported at the moment. The genericKey will be mapped
     * to the correct implementation key and return a TagField.
     *
     * @param genericKey is the generic key
     * @param value      to store
     * @return
     */
    public TagField createTagField(TagFieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException;

    /**
     * Retrieve the first value that exists for this key
     *
     * @param id
     * @return
     */
    public String getFirst(String id);

    /**
     * Retrieve String value of first tagfield that exists for this key
     *
     * @param id
     * @return String value or empty string
     */
    public String getFirst(TagFieldKey id) throws KeyNotFoundException;

    /**
     * Retrieve the first tagfield that exists for this key
     * <p/>
     * <p>Can be used to retrieve fields with any identifier, useful if the identifier is not within  the
     * jaudiotagger enum
     *
     * @param id audio specific key
     * @return tag field or null if doesnt exist
     */
    public TagField getFirstField(String id);

    /**
     * @param id
     * @return the first field that matches this generic key
     */
    public TagField getFirstField(TagFieldKey id);

    //TODO, do we need this
    public String toString();

    /**
     * Delete any instance of tag fields with this key
     *
     * @param tagFieldKey
     */
    public void deleteTagField(TagFieldKey tagFieldKey) throws KeyNotFoundException;

    /**
     * Iterator over all the fields within the tag, handle multiple fields with the same id
     *
     * @return iterator over whole list
     */
    public Iterator<TagField> getFields();

    /**
     * Return the number of fields
     * <p/>
     * <p>Fields with the same identifiers are counted seperately
     * i.e two title fields would contribute two to the count
     *
     * @return total number of fields
     */
    public int getFieldCount();

    //TODO is this a special field?
    public boolean setEncoding(String enc) throws FieldDataInvalidException;

    /**
     * Returns a {@linkplain List list} of {@link TagField} objects whose &quot;{@linkplain TagField#getId() id}&quot;
     * is the specified one.<br>
     *
     * @param id The field id.
     * @return A list of {@link TagField} objects with the given &quot;id&quot;.
     */
    public List<TagField> get(TagFieldKey id) throws KeyNotFoundException;

    /**
     * @return a list of all artwork in this file using the format indepedent Artwork class
     */
    public List<Artwork> getArtworkList();

    /**
     * @return first arttwork or null if none exist
     */
    public Artwork getFirstArtwork();

    /**
     * Create artwork field based on the data in artwork
     *
     * @return suitabel tagfield for this format that represents the artwork data
     */
    public TagField createArtworkField(Artwork artwork) throws FieldDataInvalidException;

    /**
     * Create artwork field based on the data in artwork and then set it in the tag itself
     * <p/>
     * <p>Note We provide this extra method to get round problem with VorbisComment requiring two seperate fields to
     * represent an artwork field
     *
     * @return suitabel tagfield for this format that represents the artwork data
     */
    public void createAndSetArtworkField(Artwork artwork) throws FieldDataInvalidException;

}