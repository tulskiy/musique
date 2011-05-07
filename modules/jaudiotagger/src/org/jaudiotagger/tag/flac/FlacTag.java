package org.jaudiotagger.tag.flac;

import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.logging.ErrorMessage;

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

    public void addField(TagField field) throws FieldDataInvalidException {
        if (field instanceof MetadataBlockDataPicture) {
            images.add((MetadataBlockDataPicture) field);
        } else {
            tag.addField(field);
        }
    }

    public List<TagField> getFields(String id) {
        if (id.equals(FieldKey.COVER_ART.name())) {
            List<TagField> castImages = new ArrayList<TagField>();
            for (MetadataBlockDataPicture image : images) {
                castImages.add(image);
            }
            return castImages;
        } else {
            return tag.getFields(id);
        }
    }


    public boolean hasCommonFields() {
        return tag.hasCommonFields();
    }

    public boolean hasField(String id) {
        if (id.equals(FieldKey.COVER_ART.name())) {
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

    public void setField(FieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        TagField tagfield = createField(genericKey, value);
        setField(tagfield);
    }

    public void addField(FieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        TagField tagfield = createField(genericKey, value);
        addField(tagfield);
    }

    /**
     * Create and set field with name of vorbisCommentkey
     *
     * @param vorbisCommentKey
     * @param value
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public void setField(String vorbisCommentKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        TagField tagfield = createField(vorbisCommentKey, value);
        setField(tagfield);
    }

    /**
     * Create and add field with name of vorbisCommentkey
     *
     * @param vorbisCommentKey
     * @param value
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public void addField(String vorbisCommentKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        TagField tagfield = createField(vorbisCommentKey, value);
        addField(tagfield);
    }

    /**
     * @param field
     * @throws FieldDataInvalidException
     */
    public void setField(TagField field) throws FieldDataInvalidException {
        if (field instanceof MetadataBlockDataPicture) {
            if (images.size() == 0) {
                images.add(0, (MetadataBlockDataPicture) field);
            } else {
                images.set(0, (MetadataBlockDataPicture) field);
            }
        } else {
            tag.setField(field);
        }
    }

    public TagField createField(FieldKey genericKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (genericKey.equals(FieldKey.COVER_ART)) {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        } else {
            return tag.createField(genericKey, value);
        }
    }

    /**
     * Create Tag Field using ogg key
     *
     * @param vorbisCommentFieldKey
     * @param value
     * @return
     * @throws org.jaudiotagger.tag.KeyNotFoundException
     *
     * @throws org.jaudiotagger.tag.FieldDataInvalidException
     *
     */
    public TagField createField(VorbisCommentFieldKey vorbisCommentFieldKey, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (vorbisCommentFieldKey.equals(VorbisCommentFieldKey.COVERART)) {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        return tag.createField(vorbisCommentFieldKey, value);
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
    public TagField createField(String vorbisCommentFieldKey, String value) {
        if (vorbisCommentFieldKey.equals(VorbisCommentFieldKey.COVERART.getFieldName())) {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        }
        return tag.createField(vorbisCommentFieldKey, value);
    }

    public String getFirst(String id) {
        if (id.equals(FieldKey.COVER_ART.name())) {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_CREATED_WITH_THIS_METHOD.getMsg());
        } else {
            return tag.getFirst(id);
        }
    }

    /**
     * The m parameter is effectively ignored
     *
     * @param id
     * @param n
     * @param m
     * @return
     */
    public String getSubValue(FieldKey id, int n, int m) {
        return getValue(id, n);
    }

    public String getValue(FieldKey id, int index) throws KeyNotFoundException {
        if (id.equals(FieldKey.COVER_ART)) {
            throw new UnsupportedOperationException(ErrorMessage.ARTWORK_CANNOT_BE_RETRIEVED_WITH_THIS_METHOD.getMsg());
        } else {
            return tag.getValue(id, index);
        }
    }

    public String getFirst(FieldKey id) throws KeyNotFoundException {
        return getValue(id, 0);
    }

    public TagField getFirstField(String id) {
        if (id.equals(FieldKey.COVER_ART.name())) {
            if (images.size() > 0) {
                return images.get(0);
            } else {
                return null;
            }
        } else {
            return tag.getFirstField(id);
        }
    }

    public TagField getFirstField(FieldKey genericKey) throws KeyNotFoundException {
        if (genericKey == null) {
            throw new KeyNotFoundException();
        }

        if (genericKey == FieldKey.COVER_ART) {
            return getFirstField(FieldKey.COVER_ART.name());
        } else {
            return tag.getFirstField(genericKey);
        }
    }

    /**
     * Delete any instance of tag fields with this key
     *
     * @param fieldKey
     */
    public void deleteField(FieldKey fieldKey) throws KeyNotFoundException {
        if (fieldKey.equals(FieldKey.COVER_ART)) {
            images.clear();
        } else {
            tag.deleteField(fieldKey);
        }
    }

    public void deleteField(String id) throws KeyNotFoundException {
        if (id.equals(FieldKey.COVER_ART.name())) {
            images.clear();
        } else {
            tag.deleteField(id);
        }
    }


    //TODO addField images to iterator
    public Iterator<TagField> getFields() {
        return tag.getFields();
    }

    public int getFieldCount() {
        return tag.getFieldCount() + images.size();
    }

    public int getFieldCountIncludingSubValues() {
        return getFieldCount();
    }

    public boolean setEncoding(String enc) throws FieldDataInvalidException {
        return tag.setEncoding(enc);
    }

    public List<TagField> getFields(FieldKey id) throws KeyNotFoundException {
        if (id.equals(FieldKey.COVER_ART)) {
            List<TagField> castImages = new ArrayList<TagField>();
            for (MetadataBlockDataPicture image : images) {
                castImages.add(image);
            }
            return castImages;
        } else {
            return tag.getFields(id);
        }
    }

    public TagField createArtworkField(byte[] imageData, int pictureType, String mimeType, String description, int width, int height, int colourDepth, int indexedColouredCount) throws FieldDataInvalidException {
        return new MetadataBlockDataPicture(imageData, pictureType, mimeType, description, width, height, colourDepth, indexedColouredCount);
    }

    /**
     * Create Artwork when  have the bufferedimage
     *
     * @param bi
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
        } catch (IOException ioe) {
            throw new FieldDataInvalidException("Unable to convert image to bytearray, check mimetype parameter");
        }
    }

    /**
     * Create Link to Image File, not recommended because if either flac or image file is moved link
     * will be broken.
     *
     * @param url
     * @return
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
    public TagField createField(Artwork artwork) throws FieldDataInvalidException {
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
            } catch (IOException ioe) {
                throw new FieldDataInvalidException("Unable to createField bufferd image from the image");
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

    public void setField(Artwork artwork) throws FieldDataInvalidException {
        this.setField(createField(artwork));
    }

    public void addField(Artwork artwork) throws FieldDataInvalidException {
        this.addField(createField(artwork));
    }

    public List<Artwork> getArtworkList() {
        List<Artwork> artworkList = new ArrayList<Artwork>(images.size());

        for (MetadataBlockDataPicture coverArt : images) {
            Artwork artwork = Artwork.createArtworkFromMetadataBlockDataPicture(coverArt);
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

    /**
     * Delete all instance of artwork Field
     *
     * @throws KeyNotFoundException
     */
    public void deleteArtworkField() throws KeyNotFoundException {
        this.deleteField(FieldKey.COVER_ART);
    }
}