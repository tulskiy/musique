/*
 * jaudiotagger library
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

import java.util.*;

/**
 * This class is the default implementation for
 * {@link org.jaudiotagger.tag.Tag} and introduces some more useful
 * functionality to be implemented.<br>
 *
 * @author Raphael Slinckx
 */
public abstract class AbstractTag implements Tag {
    /**
     * Stores the amount of {@link TagField} with {@link TagField#isCommon()}
     * <code>true</code>.
     */
    protected int commonNumber = 0;

    /**
     * This map stores the {@linkplain TagField#getId() ids} of the stored
     * fields to the {@linkplain TagField fields} themselves. Because a linked hashMap is used the order
     * that they are added in is preserved, the only exception to this rule is when two fields of the same id
     * exist, both will be returned according to when the first item was added to the file. <br>
     */
    protected Map<String, List<TagField>> fields = new LinkedHashMap<String, List<TagField>>();

    /**
     * Add field
     *
     * @see org.jaudiotagger.tag.Tag#add(org.jaudiotagger.tag.TagField)
     *      <p/>
     *      Changed so add empty fields
     */
    public void add(TagField field) {
        if (field == null) {
            return;
        }

        List<TagField> list = fields.get(field.getId());

        // There was no previous item
        if (list == null) {
            list = new ArrayList<TagField>();
            list.add(field);
            fields.put(field.getId(), list);
            if (field.isCommon()) {
                commonNumber++;
            }
        } else {
            // We append to existing list
            list.add(field);
        }
    }

    /**
     * Add (another) album
     *
     * @see org.jaudiotagger.tag.Tag#addAlbum(java.lang.String)
     */
    public void addAlbum(String s) {
        add(createAlbumField(s));
    }

    /**
     * Add (another) artist
     *
     * @see org.jaudiotagger.tag.Tag#addArtist(java.lang.String)
     */
    public void addArtist(String s) {
        add(createArtistField(s));
    }

    /**
     * Add (another) comment
     *
     * @see org.jaudiotagger.tag.Tag#addComment(java.lang.String)
     */
    public void addComment(String s) {
        add(createCommentField(s));
    }

    /**
     * Add (another) genre
     *
     * @see org.jaudiotagger.tag.Tag#addGenre(java.lang.String)
     */
    public void addGenre(String s) {
        add(createGenreField(s));
    }

    /**
     * Add (another) title
     *
     * @see org.jaudiotagger.tag.Tag#addTitle(java.lang.String)
     */
    public void addTitle(String s) {
        add(createTitleField(s));
    }

    /**
     * Add (another) track
     *
     * @see org.jaudiotagger.tag.Tag#addTrack(java.lang.String)
     */
    public void addTrack(String s) throws FieldDataInvalidException {
        add(createTrackField(s));
    }

    /**
     * (Add (another) year
     *
     * @see org.jaudiotagger.tag.Tag#addYear(java.lang.String)
     */
    public void addYear(String s) {
        add(createYearField(s));
    }

    /**
     * Get list of fields within this tag with the specified id
     *
     * @see org.jaudiotagger.tag.Tag#get(java.lang.String)
     */
    public List<TagField> get(String id) {
        List<TagField> list = fields.get(id);

        if (list == null) {
            return new ArrayList<TagField>();
        }

        return list;
    }

    /**
     * @param id
     * @return
     */

    //Needs to be overridden
    //TODO remove
    public List<TagField> get(TagFieldKey id) throws KeyNotFoundException {
        List<TagField> list = fields.get(id.name());
        if (list == null) {
            return new ArrayList<TagField>();
        }
        return list;
    }

    /**
     * @param id
     * @return matches for this saudio-specific key
     */
    public String getFirst(String id) {
        List<TagField> l = get(id);
        return (l.size() != 0) ? l.get(0).toString() : "";
    }

    public TagField getFirstField(String id) {
        List<TagField> l = get(id);
        return (l.size() != 0) ? l.get(0) : null;
    }

    /**
     * (overridden)
     *
     * @see org.jaudiotagger.tag.Tag#getAlbum()
     */
    public List<TagField> getAlbum() {
        return get(getAlbumId());
    }

    /**
     * Returns the identifier for a field representing the &quot;album&quot;<br>
     *
     * @return identifier for the &quot;album&quot; field.
     * @see TagField#getId()
     */
    protected abstract String getAlbumId();

    /**
     * Get Artist
     *
     * @see org.jaudiotagger.tag.Tag#getArtist()
     */
    public List<TagField> getArtist() {
        return get(getArtistId());
    }

    /**
     * Returns the identifier for a field representing the &quot;artist&quot;<br>
     *
     * @return identifier for the &quot;artist&quot; field.
     * @see TagField#getId()
     */
    protected abstract String getArtistId();

    /**
     * Get Comment
     *
     * @see org.jaudiotagger.tag.Tag#getComment()
     */
    public List<TagField> getComment() {
        return get(getCommentId());
    }

    /**
     * Returns the identifier for a field representing the &quot;comment&quot;<br>
     *
     * @return identifier for the &quot;comment&quot; field.
     * @see TagField#getId()
     */
    protected abstract String getCommentId();

    /**
     * @see org.jaudiotagger.tag.Tag#getFields()
     */
    public Iterator<TagField> getFields() {
        final Iterator<Map.Entry<String, List<TagField>>> it = this.fields.entrySet().iterator();
        return new Iterator<TagField>() {
            private Iterator<TagField> fieldsIt;

            private void changeIt() {
                if (!it.hasNext()) {
                    return;
                }

                Map.Entry<String, List<TagField>> e = it.next();
                List<TagField> l = e.getValue();
                fieldsIt = l.iterator();
            }

            public boolean hasNext() {
                if (fieldsIt == null) {
                    changeIt();
                }
                return it.hasNext() || (fieldsIt != null && fieldsIt.hasNext());
            }

            public TagField next() {
                if (!fieldsIt.hasNext()) {
                    changeIt();
                }

                return fieldsIt.next();
            }

            public void remove() {
                fieldsIt.remove();
            }
        };
    }

    /**
     * Return field count
     * <p/>
     * TODO:There must be a more efficient way to do this.
     *
     * @return field count
     */
    public int getFieldCount() {
        Iterator it = getFields();
        int count = 0;
        while (it.hasNext()) {
            count++;
            it.next();
        }
        return count;
    }

    /**
     * Get the first album or empty string if doesnt exist
     *
     * @see org.jaudiotagger.tag.Tag#getFirstAlbum()
     */
    public String getFirstAlbum() {
        List<TagField> l = getAlbum();
        return (l.size() != 0) ? ((TagTextField) l.get(0)).getContent() : "";
    }

    /**
     * Get the first artist or empty string if doesnt exist
     *
     * @see org.jaudiotagger.tag.Tag#getFirstArtist()
     */
    public String getFirstArtist() {
        List<TagField> l = getArtist();
        return (l.size() != 0) ? ((TagTextField) l.get(0)).getContent() : "";
    }

    /**
     * Get the first comment or empty string if doesnt exist
     *
     * @see org.jaudiotagger.tag.Tag#getFirstComment()
     */
    public String getFirstComment() {
        List<TagField> l = getComment();
        return (l.size() != 0) ? ((TagTextField) l.get(0)).getContent() : "";
    }

    /**
     * Get the first genre or empty string if doesnt exist
     *
     * @see org.jaudiotagger.tag.Tag#getFirstGenre()
     */
    public String getFirstGenre() {
        List<TagField> l = getGenre();
        return (l.size() != 0) ? ((TagTextField) l.get(0)).getContent() : "";
    }

    /**
     * Get the first title or empty string if doesnt exist
     *
     * @see org.jaudiotagger.tag.Tag#getFirstTitle()
     */
    public String getFirstTitle() {
        List<TagField> l = getTitle();
        return (l.size() != 0) ? ((TagTextField) l.get(0)).getContent() : "";
    }

    /**
     * Get the first track or empty string if doesnt exist
     *
     * @see org.jaudiotagger.tag.Tag#getFirstTrack()
     */
    public String getFirstTrack() {
        List<TagField> l = getTrack();
        return (l.size() != 0) ? ((TagTextField) l.get(0)).getContent() : "";
    }

    /**
     * Get the first year or empty string if doesnt exist
     *
     * @see org.jaudiotagger.tag.Tag#getFirstYear()
     */
    public String getFirstYear() {
        List<TagField> l = getYear();
        return (l.size() != 0) ? ((TagTextField) l.get(0)).getContent() : "";
    }

    /**
     * Get the genres or empty list if none exist
     *
     * @see org.jaudiotagger.tag.Tag#getGenre()
     */
    public List<TagField> getGenre() {
        return get(getGenreId());
    }

    /**
     * Returns the identifier for a field representing the &quot;genre&quot;<br>
     *
     * @return identifier for the &quot;genre&quot; field.
     * @see TagField#getId()
     */
    protected abstract String getGenreId();

    /**
     * Get the titles or empty list if none exist
     *
     * @see org.jaudiotagger.tag.Tag#getTitle()
     */
    public List<TagField> getTitle() {
        return get(getTitleId());
    }

    /**
     * Returns the identifier for a field representing the &quot;title&quot;<br>
     *
     * @return identifier for the &quot;title&quot; field.
     * @see TagField#getId()
     */
    protected abstract String getTitleId();

    /**
     * Get the tracks or empty list if none exist
     *
     * @see org.jaudiotagger.tag.Tag#getTrack()
     */
    public List<TagField> getTrack() {
        return get(getTrackId());
    }

    /**
     * Returns the identifier for a field representing the &quot;track&quot;<br>
     *
     * @return identifier for the &quot;track&quot; field.
     * @see TagField#getId()
     */
    protected abstract String getTrackId();

    /**
     * Get the  years or empty list if none exist
     *
     * @see org.jaudiotagger.tag.Tag#getYear()
     */
    public List<TagField> getYear() {
        return get(getYearId());
    }

    /**
     * Returns the identifier for a field representing the &quot;year&quot;<br>
     *
     * @return identifier for the &quot;year&quot; field.
     * @see TagField#getId()
     */
    protected abstract String getYearId();

    /**
     * Does this tag contain any comon fields
     *
     * @see org.jaudiotagger.tag.Tag#hasCommonFields()
     */
    public boolean hasCommonFields() {
        return commonNumber != 0;
    }

    /**
     * Does this tag contain a field with the specified id
     *
     * @see org.jaudiotagger.tag.Tag#hasField(java.lang.String)
     */
    public boolean hasField(String id) {
        return get(id).size() != 0;
    }

    /**
     * Determines whether the given charset encoding may be used for the
     * represented tagging system.
     *
     * @param enc charset encoding.
     * @return <code>true</code> if the given encoding can be used.
     */
    protected abstract boolean isAllowedEncoding(String enc);

    /**
     * Is this tag empty
     *
     * @see org.jaudiotagger.tag.Tag#isEmpty()
     */
    public boolean isEmpty() {
        return fields.size() == 0;
    }

    /**
     * Set field
     * <p/>
     * Changed:Just because field is empty it doesnt mean it should be deleted. That should be the choice
     * of the developer. (Or does this break things)
     *
     * @see org.jaudiotagger.tag.Tag#set(org.jaudiotagger.tag.TagField)
     */
    public void set(TagField field) {
        if (field == null) {
            return;
        }

        // If there is already an existing field with same id
        // and both are TextFields, we replace the first element
        List<TagField> list = fields.get(field.getId());
        if (list != null) {
            list.set(0, field);
            return;
        }

        // Else we put the new field in the fields.
        list = new ArrayList<TagField>();
        list.add(field);
        fields.put(field.getId(), list);
        if (field.isCommon()) {
            commonNumber++;
        }
    }

    /**
     * Set or add album
     *
     * @see org.jaudiotagger.tag.Tag#setAlbum(java.lang.String)
     */
    public void setAlbum(String s) {
        set(createAlbumField(s));
    }

    /**
     * Set or add artist
     *
     * @see org.jaudiotagger.tag.Tag#setArtist(java.lang.String)
     */
    public void setArtist(String s) {
        set(createArtistField(s));
    }

    /**
     * Set or add comment
     *
     * @see org.jaudiotagger.tag.Tag#setComment(java.lang.String)
     */
    public void setComment(String s) {
        set(createCommentField(s));
    }

    /**
     * Set or add encoding
     *
     * @see org.jaudiotagger.tag.Tag#setEncoding(java.lang.String)
     */
    public boolean setEncoding(String enc) {
        if (!isAllowedEncoding(enc)) {
            return false;
        }

        Iterator it = getFields();
        while (it.hasNext()) {
            TagField field = (TagField) it.next();
            if (field instanceof TagTextField) {
                ((TagTextField) field).setEncoding(enc);
            }
        }

        return true;
    }

    /**
     * Set or add genre
     *
     * @see org.jaudiotagger.tag.Tag#setGenre(java.lang.String)
     */
    public void setGenre(String s) {
        set(createGenreField(s));
    }

    /**
     * Set or add title
     *
     * @see org.jaudiotagger.tag.Tag#setTitle(java.lang.String)
     */
    public void setTitle(String s) {
        set(createTitleField(s));
    }

    /**
     * Set or add track
     *
     * @see org.jaudiotagger.tag.Tag#setTrack(java.lang.String)
     */
    public void setTrack(String s) throws FieldDataInvalidException {
        set(createTrackField(s));
    }

    /**
     * Set or add year
     *
     * @see org.jaudiotagger.tag.Tag#setYear(java.lang.String)
     */
    public void setYear(String s) {
        set(createYearField(s));
    }

    /**
     * (overridden)
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer out = new StringBuffer();
        out.append("Tag content:\n");
        Iterator it = getFields();
        while (it.hasNext()) {
            TagField field = (TagField) it.next();
            out.append("\t");
            out.append(field.getId());
            out.append(":");
            out.append(field.toString());
            out.append("\n");
        }
        return out.toString().substring(0, out.length() - 1);
    }

    /**
     * @param genericKey
     * @param value
     * @return
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public abstract TagField createTagField(TagFieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException;

    /**
     * @param genericKey
     * @return
     * @throws KeyNotFoundException
     */
    public abstract String getFirst(TagFieldKey genericKey) throws KeyNotFoundException;

    /**
     * @param genericKey
     * @return
     * @throws KeyNotFoundException
     */
    public abstract TagField getFirstField(TagFieldKey genericKey) throws KeyNotFoundException;

    /**
     * @param tagFieldKey
     * @throws KeyNotFoundException
     */
    public abstract void deleteTagField(TagFieldKey tagFieldKey) throws KeyNotFoundException;

    /**
     * Delete all ocurrences of field.
     *
     * @param key
     */
    protected void deleteField(String key) {
        Object removed = fields.remove(key);
        //if (removed != null && field.isCommon())
        //    commonNumber--;
        return;
    }

    /**
     * Creates a field which represents the &quot;album&quot;.<br>
     * The field will already contain the given content.
     *
     * @param content The content of the created field.
     * @return tagfield representing the &quot;album&quot;
     */
    public abstract TagField createAlbumField(String content);

    /**
     * Creates a field which represents the &quot;artist&quot;.<br>
     * The field will already contain the given content.
     *
     * @param content The content of the created field.
     * @return tagfield representing the &quot;artist&quot;
     */
    public abstract TagField createArtistField(String content);

    /**
     * Creates a field which represents the &quot;comment&quot;.<br>
     * The field will already contain the given content.
     *
     * @param content The content of the created field.
     * @return tagfield representing the &quot;comment&quot;
     */
    public abstract TagField createCommentField(String content);

    /**
     * Creates a field which represents the &quot;genre&quot;.<br>
     * The field will already contain the given content.
     *
     * @param content The content of the created field.
     * @return tagfield representing the &quot;genre&quot;
     */
    public abstract TagField createGenreField(String content);

    /**
     * Creates a field which represents the &quot;title&quot;.<br>
     * The field will already contain the given content.
     *
     * @param content The content of the created field.
     * @return tagfield representing the &quot;title&quot;
     */
    public abstract TagField createTitleField(String content);

    /**
     * Creates a field which represents the &quot;track&quot;.<br>
     * The field will already contain the given content.
     *
     * @param content The content of the created field.
     * @return tagfield representing the &quot;track&quot;
     */
    public abstract TagField createTrackField(String content) throws FieldDataInvalidException;

    /**
     * Creates a field which represents the &quot;year&quot;.<br>
     * The field will already contain the given content.
     *
     * @param content The content of the created field.
     * @return tagfield representing the &quot;year&quot;
     */
    public abstract TagField createYearField(String content);

    public Artwork getFirstArtwork() {
        List<Artwork> artwork = getArtworkList();
        if (artwork.size() > 0) {
            return artwork.get(0);
        }
        return null;
    }

    /**
     * Create field and then set within tag itself
     *
     * @param artwork
     * @throws FieldDataInvalidException
     */
    public void createAndSetArtworkField(Artwork artwork) throws FieldDataInvalidException {
        this.set(createArtworkField(artwork));
    }

}
