package org.jaudiotagger.tag.flac;

import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Flac uses Vorbis Comment for most of its metadata and a Flac Picture Block for images
 * <p/>
 * <p/>
 * This class enscapulates the items into a single tag
 */
public class FlacTag implements Tag {
    private VorbisCommentTag tag = null;
    private List<MetadataBlockDataPicture> images = new ArrayList<MetadataBlockDataPicture>();

    public FlacTag(VorbisCommentTag tag, List<MetadataBlockDataPicture> images) {
        this.tag = tag;
        this.images = images;
    }

    /**
     * @return images
     */
    public List<MetadataBlockDataPicture> getImages() {
        return images;
    }

    /**
     * @return the vorbis tag (this is what handles text metadata)
     */
    public VorbisCommentTag getVorbisCommentTag() {
        return tag;
    }

    /**
     * Adds a tagfield to the structure.<br>
     * <p/>
     * <p>It is not recommended to use this method for normal use of the
     * audiolibrary. The developer will circumvent the underlying
     * implementation. For example, if one adds a field with the field id
     * &quot;TALB&quot; for an mp3 file, and the given {@link org.jaudiotagger.tag.TagField}
     * implementation does not return a text field compliant data with
     * {@link org.jaudiotagger.tag.TagField#getRawContent()} other software and the audio library
     * won't read the file correctly, if they do read it at all. <br>
     * So for short:<br>
     * <uil>
     * <li>The field is stored without validation</li>
     * <li>No conversion of data is perfomed</li>
     * </ul>
     *
     * @param field The field to add.
     */
    public void add(TagField field) throws FieldDataInvalidException {
        if (field instanceof MetadataBlockDataPicture) {
            images.add((MetadataBlockDataPicture) field);
        } else {
            tag.add(field);
        }
    }

    /**
     * Adds an album to the tag.<br>
     *
     * @param album Album description
     */
    public void addAlbum(String album) throws FieldDataInvalidException {
        tag.addAlbum(album);
    }

    /**
     * Adds an artist to the tag.<br>
     *
     * @param artist Artist's name
     */
    public void addArtist(String artist) throws FieldDataInvalidException {
        tag.addArtist(artist);
    }

    /**
     * Adds a comment to the tag.<br>
     *
     * @param comment Comment.
     */
    public void addComment(String comment) throws FieldDataInvalidException {
        tag.addComment(comment);
    }

    /**
     * Adds a genre to the tag.<br>
     *
     * @param genre Genre
     */
    public void addGenre(String genre) throws FieldDataInvalidException {
        tag.addGenre(genre);
    }

    /**
     * Adds a title to the tag.<br>
     *
     * @param title Title
     */
    public void addTitle(String title) throws FieldDataInvalidException {
        tag.addTitle(title);
    }

    /**
     * Adds a track to the tag.<br>
     *
     * @param track Track
     */
    public void addTrack(String track) throws FieldDataInvalidException {
        tag.addTrack(track);
    }

    /**
     * Adds a year to the Tag.<br>
     *
     * @param year Year
     */
    public void addYear(String year) throws FieldDataInvalidException {
        tag.addYear(year);
    }

    /**
     * Returns a {@linkplain List list} of {@link TagField} objects whose &quot;{@linkplain TagField#getId() id}&quot;
     * is the specified one.<br>
     *
     * @param id The field id.
     * @return A list of {@link TagField} objects with the given &quot;id&quot;.
     */
    public List<TagField> get(String id) {
        if (id.equals(TagFieldKey.COVER_ART.name())) {
            List<TagField> castImages = new ArrayList<TagField>();
            for (MetadataBlockDataPicture image : images) {
                castImages.add(image);
            }
            return castImages;
        } else {
            return tag.get(id);
        }
    }

    /**
     * @return
     */
    public List<TagField> getAlbum() {
        return tag.getAlbum();
    }

    /**
     * @return
     */
    public List<TagField> getArtist() {
        return tag.getArtist();
    }

    /**
     * @return
     */
    public List<TagField> getComment() {
        return tag.getComment();
    }

    /**
     * @return
     */
    public List<TagField> getGenre() {
        return tag.getGenre();
    }

    /**
     * @return
     */
    public List<TagField> getTitle() {
        return tag.getTitle();
    }

    /**
     * @return
     */
    public List<TagField> getTrack() {
        return tag.getTrack();
    }

    /**
     * @return
     */
    public List<TagField> getYear() {
        return tag.getYear();
    }

    /**
     * @return
     */
    public String getFirstAlbum() {
        return tag.getFirstAlbum();
    }

    /**
     * @return
     */
    public String getFirstArtist() {
        return tag.getFirstArtist();
    }

    /**
     * @return
     */
    public String getFirstComment() {
        return tag.getFirstComment();
    }

    /**
     * @return
     */
    public String getFirstGenre() {
        return tag.getFirstGenre();
    }

    /**
     * @return
     */
    public String getFirstTitle() {
        return tag.getFirstTitle();
    }

    /**
     * @return
     */
    public String getFirstTrack() {
        return tag.getFirstTrack();
    }

    /**
     * @return
     */
    public String getFirstYear() {
        return tag.getFirstYear();
    }

    /**
     * Returns <code>true</code>, if at least one of the contained
     * {@linkplain TagField fields} is a common field ({@link TagField#isCommon()}).
     *
     * @return <code>true</code> if a {@linkplain TagField#isCommon() common}
     *         field is present.
     */
    public boolean hasCommonFields() {
        return tag.hasCommonFields();
    }

    /**
     * Determines whether the tag has at least one field with the specified
     * &quot;id&quot;.
     *
     * @param id The field id to look for.
     * @return <code>true</code> if tag contains a {@link TagField} with the
     *         given {@linkplain TagField#getId() id}.
     */
    public boolean hasField(String id) {
        if (id.equals(TagFieldKey.COVER_ART.name())) {
            return images.size() > 0;
        } else {
            return tag.hasField(id);
        }
    }

    /**
     * Determines whether the tag has no fields specified.<br>
     * <p/>
     * <p>If there are no images we return empty if either there is no VorbisTag or if there is a
     * VorbisTag but it is empty
     *
     * @return <code>true</code> if tag contains no field.
     */
    public boolean isEmpty() {
        return (tag == null || tag.isEmpty()) && images.size() == 0;
    }

    /**
     * @param field
     * @throws FieldDataInvalidException
     */
    public void set(TagField field) throws FieldDataInvalidException {
        if (field instanceof MetadataBlockDataPicture) {
            if (images.size() == 0) {
                images.add(0, (MetadataBlockDataPicture) field);
            } else {
                images.set(0, (MetadataBlockDataPicture) field);
            }
        } else {
            tag.set(field);
        }
    }

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setAlbum(String s) throws FieldDataInvalidException {
        tag.setAlbum(s);
    }

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setArtist(String s) throws FieldDataInvalidException {
        tag.setArtist(s);
    }

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setComment(String s) throws FieldDataInvalidException {
        tag.setComment(s);
    }

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setGenre(String s) throws FieldDataInvalidException {
        tag.setGenre(s);
    }

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setTitle(String s) throws FieldDataInvalidException {
        tag.setTitle(s);
    }

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setTrack(String s) throws FieldDataInvalidException {
        tag.setTrack(s);
    }

    /**
     * @param s
     * @throws FieldDataInvalidException
     */
    public void setYear(String s) throws FieldDataInvalidException {
        tag.setYear(s);
    }

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
    public TagField createTagField(TagFieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (genericKey.equals(TagFieldKey.COVER_ART)) {
            throw new UnsupportedOperationException("Please use the createArtworkField methods to create coverart ");
        } else {
            return tag.createTagField(genericKey, value);
        }
    }

    /**
     * Retrieve the first value that exists for this key
     *
     * @param id
     * @return
     */
    public String getFirst(String id) {
        if (id.equals(TagFieldKey.COVER_ART.name())) {
            throw new UnsupportedOperationException("Please use the createArtworkField methods to create coverart ");
        } else {
            return tag.getFirst(id);
        }
    }

    /**
     * Retrieve String value of first tagfield that exists for this key
     *
     * @param id
     * @return String value or empty string
     */
    public String getFirst(TagFieldKey id) throws KeyNotFoundException {
        if (id.equals(TagFieldKey.COVER_ART)) {
            throw new UnsupportedOperationException("Please use the createArtworkField methods to create coverart ");
        } else {
            return tag.getFirst(id);
        }

    }

    /**
     * Retrieve the first tagfield that exists for this key
     * <p/>
     * <p>Can be used to retrieve fields with any identifier, useful if the identifier is not within  the
     * jaudiotagger enum
     *
     * @param id audio specific key
     * @return tag field or null if doesnt exist
     */
    public TagField getFirstField(String id) {
        if (id.equals(TagFieldKey.COVER_ART)) {
            if (images.size() > 0) {
                return images.get(0);
            } else {
                return null;
            }
        } else {
            return tag.getFirstField(id);
        }
    }

    public TagField getFirstField(TagFieldKey genericKey) throws KeyNotFoundException {
        if (genericKey == null) {
            throw new KeyNotFoundException();
        }

        if (genericKey == TagFieldKey.COVER_ART) {
            return getFirstField(TagFieldKey.COVER_ART.name());
        } else {
            return tag.getFirstField(genericKey);
        }
    }

    /**
     * Delete any instance of tag fields with this key
     *
     * @param tagFieldKey
     */
    public void deleteTagField(TagFieldKey tagFieldKey) throws KeyNotFoundException {
        if (tagFieldKey.equals(TagFieldKey.COVER_ART)) {
            images.clear();
        } else {
            tag.deleteTagField(tagFieldKey);
        }
    }

    /**
     * Iterator over all the fields within the tag, handle multiple fields with the same id
     *
     * @return iterator over whole list
     */
    //TODO add images to iterator
    public Iterator<TagField> getFields() {
        return tag.getFields();
    }

    /**
     * Return the number of fields
     * <p/>
     * <p>Fields with the same identifiers are counted seperately
     * i.e two title fields would contribute two to the count
     *
     * @return total number of fields
     */
    public int getFieldCount() {
        return tag.getFieldCount() + images.size();
    }

    public boolean setEncoding(String enc) throws FieldDataInvalidException {
        return tag.setEncoding(enc);
    }

    /**
     * Returns a {@linkplain List list} of {@link TagField} objects whose &quot;{@linkplain TagField#getId() id}&quot;
     * is the specified one.<br>
     *
     * @param id The field id.
     * @return A list of {@link TagField} objects with the given &quot;id&quot;.
     */
    public List<TagField> get(TagFieldKey id) throws KeyNotFoundException {
        if (id.equals(TagFieldKey.COVER_ART)) {
            List<TagField> castImages = new ArrayList<TagField>();
            for (MetadataBlockDataPicture image : images) {
                castImages.add(image);
            }
            return castImages;
        } else {
            return tag.get(id);
        }

    }

    /**
     * Create Artwork when have the raw image data
     *
     * @param imageData
     * @param pictureType
     * @param mimeType
     * @param description
     * @param width
     * @param height
     * @param colourDepth
     * @param indexedColouredCount
     * @return
     * @throws FieldDataInvalidException
     */
    public TagField createArtworkField(byte[] imageData, int pictureType, String mimeType, String description, int width, int height, int colourDepth, int indexedColouredCount) throws FieldDataInvalidException {
        return new MetadataBlockDataPicture(imageData, pictureType, mimeType, description, width, height, colourDepth, indexedColouredCount);
    }

    /**
     * Create Artwork when  have the bufferedimage
     *
     * @param pictureType
     * @param mimeType
     * @param description
     * @param colourDepth
     * @param indexedColouredCount
     * @return
     * @throws FieldDataInvalidException
     */
    public TagField createArtworkField(BufferedImage bi, int pictureType, String mimeType, String description, int colourDepth, int indexedColouredCount) throws FieldDataInvalidException {
        //Convert to byte array
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(bi, ImageFormats.getFormatForMimeType(mimeType), new DataOutputStream(output));

            //Add to image list
            return new MetadataBlockDataPicture(output.toByteArray(), pictureType, mimeType, description, bi.getWidth(), bi.getHeight(), colourDepth, indexedColouredCount);
        }
        catch (IOException ioe) {
            throw new FieldDataInvalidException("Unable to convert image to bytearray, check mimetype parameter");
        }
    }

    /**
     * Create Link to Image File, not recommended because if either flac or image file is moved link
     * will be broken.
     */
    public TagField createLinkedArtworkField(String url) {
        //Add to image list
        return new MetadataBlockDataPicture(Utils.getDefaultBytes(url, TextEncoding.CHARSET_ISO_8859_1), PictureTypes.DEFAULT_ID, MetadataBlockDataPicture.IMAGE_IS_URL, "", 0, 0, 0, 0);
    }

    /**
     * Create artwork field
     *
     * @return
     */
    public TagField createArtworkField(Artwork artwork) throws FieldDataInvalidException {
        if (artwork.isLinked()) {
            return new MetadataBlockDataPicture(
                    Utils.getDefaultBytes(artwork.getImageUrl(), TextEncoding.CHARSET_ISO_8859_1),
                    artwork.getPictureType(),
                    MetadataBlockDataPicture.IMAGE_IS_URL,
                    "",
                    0,
                    0,
                    0,
                    0);
        } else {
            BufferedImage image;
            try {
                image = artwork.getImage();
            }
            catch (IOException ioe) {
                throw new FieldDataInvalidException("Unable to create bufferd image from the image");
            }

            return new MetadataBlockDataPicture(artwork.getBinaryData(),
                                                artwork.getPictureType(),
                                                artwork.getMimeType(),
                                                artwork.getDescription(),
                                                image.getWidth(),
                                                image.getHeight(),
                                                0,
                                                0);
        }
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

    public List<Artwork> getArtworkList() {
        List<Artwork> artworkList = new ArrayList<Artwork>(images.size());

        for (MetadataBlockDataPicture coverArt : images) {
            Artwork artwork = new Artwork();
            artwork.setMimeType(coverArt.getMimeType());
            artwork.setDescription(coverArt.getDescription());
            artwork.setPictureType(coverArt.getPictureType());
            if (coverArt.isImageUrl()) {
                artwork.setLinked(coverArt.isImageUrl());
                artwork.setImageUrl(coverArt.getImageUrl());
            } else {
                artwork.setBinaryData(coverArt.getImageData());
            }
            artworkList.add(artwork);
        }
        return artworkList;
    }

    public Artwork getFirstArtwork() {
        List<Artwork> artwork = getArtworkList();
        if (artwork.size() > 0) {
            return artwork.get(0);
        }
        return null;
    }
}