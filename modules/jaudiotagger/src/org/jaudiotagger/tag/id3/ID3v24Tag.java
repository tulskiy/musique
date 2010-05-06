/*
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jaudiotagger.tag.id3;

import org.jaudiotagger.FileConstants;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.framebody.*;
import org.jaudiotagger.tag.lyrics3.AbstractLyrics3;
import org.jaudiotagger.tag.lyrics3.Lyrics3v2;
import org.jaudiotagger.tag.lyrics3.Lyrics3v2Field;
import org.jaudiotagger.tag.reference.GenreTypes;
import org.jaudiotagger.tag.reference.PictureTypes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

/**
 * Represents an ID3v2.4 tag.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: ID3v24Tag.java,v 1.51 2008/12/10 13:14:27 paultaylor Exp $
 */
public class ID3v24Tag extends AbstractID3v2Tag {
    protected static final String TYPE_FOOTER = "footer";
    protected static final String TYPE_IMAGEENCODINGRESTRICTION = "imageEncodingRestriction";
    protected static final String TYPE_IMAGESIZERESTRICTION = "imageSizeRestriction";
    protected static final String TYPE_TAGRESTRICTION = "tagRestriction";
    protected static final String TYPE_TAGSIZERESTRICTION = "tagSizeRestriction";
    protected static final String TYPE_TEXTENCODINGRESTRICTION = "textEncodingRestriction";
    protected static final String TYPE_TEXTFIELDSIZERESTRICTION = "textFieldSizeRestriction";
    protected static final String TYPE_UPDATETAG = "updateTag";
    protected static final String TYPE_CRCDATA = "crcdata";
    protected static final String TYPE_EXPERIMENTAL = "experimental";
    protected static final String TYPE_EXTENDED = "extended";
    protected static final String TYPE_PADDINGSIZE = "paddingsize";
    protected static final String TYPE_UNSYNCHRONISATION = "unsyncronisation";

    protected static int TAG_EXT_HEADER_LENGTH = 6;
    protected static int TAG_EXT_HEADER_UPDATE_LENGTH = 1;
    protected static int TAG_EXT_HEADER_CRC_LENGTH = 6;
    protected static int TAG_EXT_HEADER_RESTRICTION_LENGTH = 2;
    protected static int TAG_EXT_HEADER_CRC_DATA_LENGTH = 5;
    protected static int TAG_EXT_HEADER_RESTRICTION_DATA_LENGTH = 1;
    protected static int TAG_EXT_NUMBER_BYTES_DATA_LENGTH = 1;

    /**
     * ID3v2.4 Header bit mask
     */
    public static final int MASK_V24_UNSYNCHRONIZATION = FileConstants.BIT7;

    /**
     * ID3v2.4 Header bit mask
     */
    public static final int MASK_V24_EXTENDED_HEADER = FileConstants.BIT6;

    /**
     * ID3v2.4 Header bit mask
     */
    public static final int MASK_V24_EXPERIMENTAL = FileConstants.BIT5;

    /**
     * ID3v2.4 Header bit mask
     */
    public static final int MASK_V24_FOOTER_PRESENT = FileConstants.BIT4;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_TAG_UPDATE = FileConstants.BIT6;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_CRC_DATA_PRESENT = FileConstants.BIT5;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_TAG_RESTRICTIONS = FileConstants.BIT4;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_TAG_SIZE_RESTRICTIONS = (byte) FileConstants.BIT7 | FileConstants.BIT6;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_TEXT_ENCODING_RESTRICTIONS = FileConstants.BIT5;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_TEXT_FIELD_SIZE_RESTRICTIONS = FileConstants.BIT4 | FileConstants.BIT3;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_IMAGE_ENCODING = FileConstants.BIT2;

    /**
     * ID3v2.4 Extended header bit mask
     */
    public static final int MASK_V24_IMAGE_SIZE_RESTRICTIONS = FileConstants.BIT2 | FileConstants.BIT1;

    /**
     * ID3v2.4 Header Footer are the same as the header flags. WHY?!?! move the
     * flags from thier position in 2.3??????????
     */
    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_TAG_ALTER_PRESERVATION = FileConstants.BIT6;

    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_FILE_ALTER_PRESERVATION = FileConstants.BIT5;

    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_READ_ONLY = FileConstants.BIT4;

    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_GROUPING_IDENTITY = FileConstants.BIT6;

    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_COMPRESSION = FileConstants.BIT4;

    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_ENCRYPTION = FileConstants.BIT3;

    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_FRAME_UNSYNCHRONIZATION = FileConstants.BIT2;

    /**
     * ID3v2.4 Header Footer bit mask
     */
    public static final int MASK_V24_DATA_LENGTH_INDICATOR = FileConstants.BIT1;

    /**
     * CRC Checksum calculated
     */
    protected boolean crcDataFlag = false;

    /**
     * Experiemntal tag
     */
    protected boolean experimental = false;

    /**
     * Contains extended header
     */
    protected boolean extended = false;

    /**
     * All frames in the tag uses unsynchronisation
     */
    protected boolean unsynchronization = false;

    /**
     * CRC Checksum
     */
    protected int crcData = 0;

    /**
     * Contains a footer
     */
    protected boolean footer = false;

    /**
     * Tag is an update
     */
    protected boolean updateTag = false;

    /**
     * Tag has restrictions
     */
    protected boolean tagRestriction = false;

    /**
     *
     */
    protected byte imageEncodingRestriction = 0;

    /**
     *
     */
    protected byte imageSizeRestriction = 0;

    /**
     *
     */
    protected byte tagSizeRestriction = 0;

    /**
     *
     */
    protected byte textEncodingRestriction = 0;

    /**
     * Tag padding
     */
    protected int paddingSize = 0;

    /**
     *
     */
    protected byte textFieldSizeRestriction = 0;

    public static final byte RELEASE = 2;
    public static final byte MAJOR_VERSION = 4;
    public static final byte REVISION = 0;

    /**
     * Retrieve the Release
     */
    public byte getRelease() {
        return RELEASE;
    }

    /**
     * Retrieve the Major Version
     */
    public byte getMajorVersion() {
        return MAJOR_VERSION;
    }

    /**
     * Retrieve the Revision
     */
    public byte getRevision() {
        return REVISION;
    }

    /**
     * Creates a new empty ID3v2_4 datatype.
     */
    public ID3v24Tag() {
        frameMap = new LinkedHashMap();
    }

    /**
     * Copy primitives applicable to v2.4, this is used when cloning a v2.4 datatype
     * and other objects such as v2.3 so need to check instanceof
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObj) {
        //logger.info("Copying primitives");
        super.copyPrimitives(copyObj);

        if (copyObj instanceof ID3v24Tag) {
            ID3v24Tag copyObject = (ID3v24Tag) copyObj;
            this.footer = copyObject.footer;
            this.tagRestriction = copyObject.tagRestriction;
            this.updateTag = copyObject.updateTag;
            this.imageEncodingRestriction = copyObject.imageEncodingRestriction;
            this.imageSizeRestriction = copyObject.imageSizeRestriction;
            this.tagSizeRestriction = copyObject.tagSizeRestriction;
            this.textEncodingRestriction = copyObject.textEncodingRestriction;
            this.textFieldSizeRestriction = copyObject.textFieldSizeRestriction;
        }
    }

    /**
     * Copy frames from one tag into a v2.4 tag
     */
    protected void copyFrames(AbstractID3v2Tag copyObject) {
        //logger.info("Copying Frames,there are:" + copyObject.frameMap.keySet().size() + " different types");
        frameMap = new LinkedHashMap();
        //Copy Frames that are a valid 2.4 type
        Iterator iterator = copyObject.frameMap.keySet().iterator();
        AbstractID3v2Frame frame;
        ID3v24Frame newFrame = null;
        while (iterator.hasNext()) {
            String id = (String) iterator.next();
            Object o = copyObject.frameMap.get(id);
            //SingleFrames
            if (o instanceof AbstractID3v2Frame) {
                frame = (AbstractID3v2Frame) o;
                try {
                    newFrame = new ID3v24Frame(frame);
                    //logger.info("Adding Frame:" + newFrame.getIdentifier());
                    copyFrameIntoMap(newFrame.getIdentifier(), newFrame);
                }
                catch (InvalidFrameException ife) {
                    //logger.log(Level.SEVERE, "Unable to convert frame:" + frame.getIdentifier(), ife);
                }
            }
            //MultiFrames
            else if (o instanceof ArrayList) {
                ArrayList<AbstractID3v2Frame> multiFrame = new ArrayList<AbstractID3v2Frame>();
                for (ListIterator<AbstractID3v2Frame> li = ((ArrayList<AbstractID3v2Frame>) o).listIterator(); li.hasNext();) {
                    frame = li.next();
                    try {
                        newFrame = new ID3v24Frame(frame);
                        multiFrame.add(newFrame);
                    }
                    catch (InvalidFrameException ife) {
                        //logger.log(Level.SEVERE, "Unable to convert frame:" + frame.getIdentifier());
                    }
                }
                //Ensure that the list actually contains at lest one value before adding
                if (multiFrame.size() > 0) {
                    if (newFrame != null) {
                        //logger.finest("Adding multi frame list to map:" + newFrame.getIdentifier());
                        frameMap.put(newFrame.getIdentifier(), multiFrame);
                    }
                }
            }
        }
    }

    /**
     * Copy Constructor, creates a new ID3v2_4 Tag based on another ID3v2_4 Tag
     */
    public ID3v24Tag(ID3v24Tag copyObject) {
        //logger.info("Creating tag from another tag of same type");
        copyPrimitives(copyObject);
        copyFrames(copyObject);
    }

    /**
     * Creates a new ID3v2_4 datatype based on another (non 2.4) tag
     *
     * @param mp3tag
     */
    public ID3v24Tag(AbstractTag mp3tag) {
        //logger.info("Creating tag from a tag of a different version");
        frameMap = new LinkedHashMap();
        if (mp3tag != null) {
            //Should use simpler copy constructor
            if ((mp3tag instanceof ID3v24Tag)) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            }
            /* If we get a tag, we want to convert to id3v2_4
             * both id3v1 and lyrics3 convert to this type
             * id3v1 needs to convert to id3v2_4 before converting to lyrics3
             */
            else if (mp3tag instanceof AbstractID3v2Tag) {
                copyPrimitives((AbstractID3v2Tag) mp3tag);
                copyFrames((AbstractID3v2Tag) mp3tag);
            }
            //IDv1
            else if (mp3tag instanceof ID3v1Tag) {
                // convert id3v1 tags.
                ID3v1Tag id3tag = (ID3v1Tag) mp3tag;
                ID3v24Frame newFrame;
                AbstractID3v2FrameBody newBody;
                if (id3tag.title.length() > 0) {
                    newBody = new FrameBodyTIT2((byte) 0, id3tag.title);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_TITLE);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.artist.length() > 0) {
                    newBody = new FrameBodyTPE1((byte) 0, id3tag.artist);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_ARTIST);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.album.length() > 0) {
                    newBody = new FrameBodyTALB((byte) 0, id3tag.album);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_ALBUM);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.year.length() > 0) {
                    newBody = new FrameBodyTDRC((byte) 0, id3tag.year);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_YEAR);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (id3tag.comment.length() > 0) {
                    newBody = new FrameBodyCOMM((byte) 0, "ENG", "", id3tag.comment);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_COMMENT);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (((id3tag.genre & ID3v1Tag.BYTE_TO_UNSIGNED) >= 0) && ((id3tag.genre & ID3v1Tag.BYTE_TO_UNSIGNED) != ID3v1Tag.BYTE_TO_UNSIGNED)) {
                    Integer genreId = id3tag.genre & ID3v1Tag.BYTE_TO_UNSIGNED;
                    String genre = "(" + genreId + ") " + GenreTypes.getInstanceOf().getValueForId(genreId);

                    newBody = new FrameBodyTCON((byte) 0, genre);
                    newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_GENRE);
                    newFrame.setBody(newBody);
                    frameMap.put(newFrame.getIdentifier(), newFrame);
                }
                if (mp3tag instanceof ID3v11Tag) {
                    ID3v11Tag id3tag2 = (ID3v11Tag) mp3tag;
                    if (id3tag2.track > 0) {
                        newBody = new FrameBodyTRCK((byte) 0, Byte.toString(id3tag2.track));
                        newFrame = new ID3v24Frame(ID3v24Frames.FRAME_ID_TRACK);
                        newFrame.setBody(newBody);
                        frameMap.put(newFrame.getIdentifier(), newFrame);
                    }
                }
            }
            //Lyrics 3
            else if (mp3tag instanceof AbstractLyrics3) {
                //Put the conversion stuff in the individual frame code.
                Lyrics3v2 lyric;
                if (mp3tag instanceof Lyrics3v2) {
                    lyric = new Lyrics3v2((Lyrics3v2) mp3tag);
                } else {
                    lyric = new Lyrics3v2(mp3tag);
                }
                Iterator<Lyrics3v2Field> iterator = lyric.iterator();
                Lyrics3v2Field field;
                ID3v24Frame newFrame;
                while (iterator.hasNext()) {
                    try {
                        field = iterator.next();
                        newFrame = new ID3v24Frame(field);
                        frameMap.put(newFrame.getIdentifier(), newFrame);
                    }
                    catch (InvalidTagException ex) {
                        //logger.warning("Unable to convert Lyrics3 to v24 Frame:Frame Identifier");
                    }
                }
            }
        }
    }

    /**
     * Creates a new ID3v2_4 datatype.
     *
     * @param buffer
     * @param loggingFilename
     * @throws TagException
     */
    public ID3v24Tag(ByteBuffer buffer, String loggingFilename) throws TagException {
        frameMap = new LinkedHashMap();
        setLoggingFilename(loggingFilename);
        this.read(buffer);
    }

    /**
     * Creates a new ID3v2_4 datatype.
     *
     * @param buffer
     * @throws TagException
     * @deprecated use {@link #ID3v24Tag(ByteBuffer,String)} instead
     */
    public ID3v24Tag(ByteBuffer buffer) throws TagException {
        this(buffer, "");
    }

    /**
     * @return identifier
     */
    public String getIdentifier() {
        return "ID3v2.40";
    }

    /**
     * Return tag size based upon the sizes of the frames rather than the physical
     * no of bytes between start of ID3Tag and start of Audio Data.
     *
     * @return size
     */
    public int getSize() {
        int size = TAG_HEADER_LENGTH;
        if (extended) {
            size += this.TAG_EXT_HEADER_LENGTH;
            if (updateTag) {
                size += this.TAG_EXT_HEADER_UPDATE_LENGTH;
            }
            if (crcDataFlag) {
                size += this.TAG_EXT_HEADER_CRC_LENGTH;
            }
            if (tagRestriction) {
                size += this.TAG_EXT_HEADER_RESTRICTION_LENGTH;
            }
        }
        size += super.getSize();
        //logger.finer("Tag Size is" + size);
        return size;
    }

    /**
     * @param obj
     * @return equality
     */
    public boolean equals(Object obj) {
        if ((obj instanceof ID3v24Tag) == false) {
            return false;
        }
        ID3v24Tag object = (ID3v24Tag) obj;
        if (this.footer != object.footer) {
            return false;
        }
        if (this.imageEncodingRestriction != object.imageEncodingRestriction) {
            return false;
        }
        if (this.imageSizeRestriction != object.imageSizeRestriction) {
            return false;
        }
        if (this.tagRestriction != object.tagRestriction) {
            return false;
        }
        if (this.tagSizeRestriction != object.tagSizeRestriction) {
            return false;
        }
        if (this.textEncodingRestriction != object.textEncodingRestriction) {
            return false;
        }
        if (this.textFieldSizeRestriction != object.textFieldSizeRestriction) {
            return false;
        }
        if (this.updateTag != object.updateTag) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Read the size of a tag, based on  the value written in the tag header
     *
     * @param buffer
     * @return
     * @throws TagException
     */
    public int readSize(ByteBuffer buffer) {

        //Skip over flags
        byte flags = buffer.get();

        // Read the size, this is size of tag not including  the tag header
        int size = ID3SyncSafeInteger.bufferToValue(buffer);

        //Return the exact size of tag as set in the tag header
        return size + TAG_HEADER_LENGTH;
    }

    /**
     * Read Tag from Specified file.
     * Read tag header, delegate reading of frames to readFrames()
     *
     * @param byteBuffer to read the tag from
     * @throws TagException
     * @throws TagNotFoundException
     * @throws InvalidTagException
     */
    public void read(ByteBuffer byteBuffer) throws TagException {
        int size;
        byte[] buffer;
        if (seek(byteBuffer) == false) {
            throw new TagNotFoundException(getLoggingFilename() + ":" + getIdentifier() + " tag not found");
        }
        //Flags
        byte flags = byteBuffer.get();
        unsynchronization = (flags & MASK_V24_UNSYNCHRONIZATION) != 0;
        extended = (flags & MASK_V24_EXTENDED_HEADER) != 0;
        experimental = (flags & MASK_V24_EXPERIMENTAL) != 0;
        footer = (flags & MASK_V24_FOOTER_PRESENT) != 0;

        if (isUnsynchronization()) {
            //logger.info(getLoggingFilename() + ":" + "ID3v24 Tag is unsynchronized");
        }

        if (extended) {
            //logger.warning(getLoggingFilename() + ":" + "ID3v24 Tag is extended");
        }

        if (experimental) {
            //logger.warning(getLoggingFilename() + ":" + "ID3v24 Tag is experimental");
        }

        if (footer) {
            //logger.warning(getLoggingFilename() + ":" + "ID3v24 Tag has footer");
        }

        // Read the size, this is size of tag apart from tag header
        size = ID3SyncSafeInteger.bufferToValue(byteBuffer);
        //logger.info(getLoggingFilename() + ":" + "Reading tag from file size set in header is" + size);
        if (extended) {
            // int is 4 bytes.
            int extendedHeaderSize = byteBuffer.getInt();
            // the extended header must be atleast 6 bytes
            if (extendedHeaderSize <= TAG_EXT_HEADER_LENGTH) {
                throw new InvalidTagException(getLoggingFilename() + ":" + "Invalid Extended Header Size.");
            }
            //Number of bytes
            byteBuffer.get();
            // Read the extended flag bytes
            byte extFlag = byteBuffer.get();
            updateTag = (extFlag & MASK_V24_TAG_UPDATE) != 0;
            crcDataFlag = (extFlag & MASK_V24_CRC_DATA_PRESENT) != 0;
            tagRestriction = (extFlag & MASK_V24_TAG_RESTRICTIONS) != 0;
            // read the length byte if the flag is set
            // this tag should always be zero but just in case
            // read this information.
            if (updateTag) {
                byteBuffer.get();
            }
            if (crcDataFlag) {
                // the CRC has a variable length
                byteBuffer.get();
                buffer = new byte[TAG_EXT_HEADER_CRC_DATA_LENGTH];
                byteBuffer.get(buffer, 0, TAG_EXT_HEADER_CRC_DATA_LENGTH);
                crcData = 0;
                for (int i = 0; i < TAG_EXT_HEADER_CRC_DATA_LENGTH; i++) {
                    crcData <<= 8;
                    crcData += buffer[i];
                }
            }
            if (tagRestriction) {
                byteBuffer.get();
                buffer = new byte[1];
                byteBuffer.get(buffer, 0, 1);
                tagSizeRestriction = (byte) ((buffer[0] & MASK_V24_TAG_SIZE_RESTRICTIONS) >> 6);
                textEncodingRestriction = (byte) ((buffer[0] & MASK_V24_TEXT_ENCODING_RESTRICTIONS) >> 5);
                textFieldSizeRestriction = (byte) ((buffer[0] & MASK_V24_TEXT_FIELD_SIZE_RESTRICTIONS) >> 3);
                imageEncodingRestriction = (byte) ((buffer[0] & MASK_V24_IMAGE_ENCODING) >> 2);
                imageSizeRestriction = (byte) (buffer[0] & MASK_V24_IMAGE_SIZE_RESTRICTIONS);
            }
        }
        //Note if there was an extended header the size value has padding taken
        //off so we dont search it.
        readFrames(byteBuffer, size);

    }

    /**
     * Read frames from tag
     */
    protected void readFrames(ByteBuffer byteBuffer, int size) {
        //logger.finest(getLoggingFilename() + ":" + "Start of frame body at" + byteBuffer.position());
        //Now start looking for frames
        ID3v24Frame next;
        frameMap = new LinkedHashMap();
        //Read the size from the Tag Header
        this.fileReadSize = size;
        // Read the frames until got to upto the size as specified in header
        //logger.finest(getLoggingFilename() + ":" + "Start of frame body at:" + byteBuffer.position() + ",frames data size is:" + size);
        while (byteBuffer.position() <= size) {
            String id;
            try {
                //Read Frame
                //logger.finest(getLoggingFilename() + ":" + "looking for next frame at:" + byteBuffer.position());
                next = new ID3v24Frame(byteBuffer, getLoggingFilename());
                id = next.getIdentifier();
                loadFrameIntoMap(id, next);
            }
            //Found Empty Frame
            catch (EmptyFrameException ex) {
                //logger.warning(getLoggingFilename() + ":" + "Empty Frame:" + ex.getMessage());
                this.emptyFrameBytes += TAG_HEADER_LENGTH;
            }
            catch (InvalidFrameIdentifierException ifie) {
                //logger.info(getLoggingFilename() + ":" + "Invalid Frame Identifier:" + ifie.getMessage());
                this.invalidFrameBytes++;
                //Dont try and find any more frames
                break;
            }
            //Problem trying to find frame
            catch (InvalidFrameException ife) {
                //logger.warning(getLoggingFilename() + ":" + "Invalid Frame:" + ife.getMessage());
                this.invalidFrameBytes++;
                //Dont try and find any more frames
                break;
            }
        }
    }

    /**
     * Write the ID3 header to the ByteBuffer.
     * <p/>
     * TODO Calculate the CYC Data Check
     * TODO Reintroduce Extended Header
     *
     * @param padding is the size of the padding
     * @param size    is the size of the body data
     * @return ByteBuffer
     * @throws IOException
     */
    private ByteBuffer writeHeaderToBuffer(int padding, int size) throws IOException {
        //This would only be set if every frame in tag has been unsynchronized, I only unsychronize frames
        //that need it, in any case I have been advised not to set it even then.
        unsynchronization = false;

        // Flags,currently we never calculate the CRC
        // and if we dont calculate them cant keep orig values. Tags are not
        // experimental and we never create extended header to keep things simple.
        extended = false;
        experimental = false;
        footer = false;

        // Create Header Buffer,allocate maximum possible size for the header
        ByteBuffer headerBuffer = ByteBuffer.allocate(TAG_HEADER_LENGTH);
        //TAGID
        headerBuffer.put(TAG_ID);

        //Major Version
        headerBuffer.put(getMajorVersion());

        //Minor Version
        headerBuffer.put(getRevision());

        //Flags
        byte flagsByte = 0;
        if (isUnsynchronization()) {
            flagsByte |= MASK_V24_UNSYNCHRONIZATION;
        }
        if (extended) {
            flagsByte |= MASK_V24_EXTENDED_HEADER;
        }
        if (experimental) {
            flagsByte |= MASK_V24_EXPERIMENTAL;
        }
        if (footer) {
            flagsByte |= MASK_V24_FOOTER_PRESENT;
        }
        headerBuffer.put(flagsByte);

        //Size As Recorded in Header, don't include the main header length
        //Additional Header Size,(for completeness we never actually write the extended header, or footer)
        int additionalHeaderSize = 0;
        if (extended) {
            additionalHeaderSize += this.TAG_EXT_HEADER_LENGTH;
            if (updateTag) {
                additionalHeaderSize += this.TAG_EXT_HEADER_UPDATE_LENGTH;
            }
            if (crcDataFlag) {
                additionalHeaderSize += this.TAG_EXT_HEADER_CRC_LENGTH;
            }
            if (tagRestriction) {
                additionalHeaderSize += this.TAG_EXT_HEADER_RESTRICTION_LENGTH;
            }
        }

        //Size As Recorded in Header, don't include the main header length
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(padding + size + additionalHeaderSize));

        //Write Extended Header
        ByteBuffer extHeaderBuffer = null;
        if (extended) {
            //Write Extended Header Size
            int extendedSize = TAG_EXT_HEADER_LENGTH;
            if (updateTag) {
                extendedSize += TAG_EXT_HEADER_UPDATE_LENGTH;
            }
            if (crcDataFlag) {
                extendedSize += TAG_EXT_HEADER_CRC_LENGTH;
            }
            if (tagRestriction) {
                extendedSize += TAG_EXT_HEADER_RESTRICTION_LENGTH;
            }
            extHeaderBuffer = ByteBuffer.allocate(extendedSize);
            extHeaderBuffer.putInt(extendedSize);
            //Write Number of flags Byte
            extHeaderBuffer.put((byte) TAG_EXT_NUMBER_BYTES_DATA_LENGTH);
            //Write Extended Flags
            byte extFlag = 0;
            if (updateTag) {
                extFlag |= MASK_V24_TAG_UPDATE;
            }
            if (crcDataFlag) {
                extFlag |= MASK_V24_CRC_DATA_PRESENT;
            }
            if (tagRestriction) {
                extFlag |= MASK_V24_TAG_RESTRICTIONS;
            }
            extHeaderBuffer.put(extFlag);
            //Write Update Data
            if (updateTag) {
                extHeaderBuffer.put((byte) 0);
            }
            //Write CRC Data
            if (crcDataFlag) {
                extHeaderBuffer.put((byte) TAG_EXT_HEADER_CRC_DATA_LENGTH);
                extHeaderBuffer.put((byte) 0);
                extHeaderBuffer.putInt(crcData);
            }
            //Write Tag Restriction
            if (tagRestriction) {
                extHeaderBuffer.put((byte) TAG_EXT_HEADER_RESTRICTION_DATA_LENGTH);
                //todo not currently setting restrictions
                extHeaderBuffer.put((byte) 0);
            }
        }

        if (extHeaderBuffer != null) {
            extHeaderBuffer.flip();
            headerBuffer.put(extHeaderBuffer);
        }

        headerBuffer.flip();
        return headerBuffer;
    }

    /**
     * Write this tag to file.
     *
     * @param file
     * @throws IOException
     */
    public void write(File file, long audioStartLocation) throws IOException {
        //logger.info("Writing tag to file");

        //Write Body Buffer
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();

        //Calculate Tag Size including Padding
        int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, (int) audioStartLocation);

        //Calculate padding bytes required
        int padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);

        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);
        writeBufferToFile(file, headerBuffer, bodyByteBuffer, padding, sizeIncPadding, audioStartLocation);
    }

    /**
     * Write tag to channel
     *
     * @param channel
     * @throws IOException
     */
    public void write(WritableByteChannel channel) throws IOException {
        //logger.info("Writing tag to channel");

        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        ByteBuffer headerBuffer = writeHeaderToBuffer(0, bodyByteBuffer.length);

        channel.write(headerBuffer);
        channel.write(ByteBuffer.wrap(bodyByteBuffer));
    }

    /**
     * Display the tag in an XMLFormat
     */
    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());

        super.createStructureHeader();

        //Header
        MP3File.getStructureFormatter().openHeadingElement(TYPE_HEADER, "");
        MP3File.getStructureFormatter().addElement(TYPE_UNSYNCHRONISATION, this.isUnsynchronization());
        MP3File.getStructureFormatter().addElement(TYPE_CRCDATA, this.crcData);
        MP3File.getStructureFormatter().addElement(TYPE_EXPERIMENTAL, this.experimental);
        MP3File.getStructureFormatter().addElement(TYPE_EXTENDED, this.extended);
        MP3File.getStructureFormatter().addElement(TYPE_PADDINGSIZE, this.paddingSize);
        MP3File.getStructureFormatter().addElement(TYPE_FOOTER, this.footer);
        MP3File.getStructureFormatter().addElement(TYPE_IMAGEENCODINGRESTRICTION, this.paddingSize);
        MP3File.getStructureFormatter().addElement(TYPE_IMAGESIZERESTRICTION, this.imageSizeRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TAGRESTRICTION, this.tagRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TAGSIZERESTRICTION, this.tagSizeRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TEXTFIELDSIZERESTRICTION, this.textFieldSizeRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_TEXTENCODINGRESTRICTION, this.textEncodingRestriction);
        MP3File.getStructureFormatter().addElement(TYPE_UPDATETAG, this.updateTag);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_HEADER);

        //Body
        super.createStructureBody();

        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);
    }

    /**
     * Are all frame swithin this tag unsynchronized
     * <p/>
     * <p>Because synchronization occurs at the frame level it is not normally desirable to unsynchronize all frames
     * and hence this flag is not normally set.
     *
     * @return are all frames within the tag unsynchronized
     */
    public boolean isUnsynchronization() {
        return unsynchronization;
    }

    protected String getArtistId() {
        return ID3v24Frames.FRAME_ID_ARTIST;
    }

    protected String getAlbumId() {
        return ID3v24Frames.FRAME_ID_ALBUM;
    }

    protected String getTitleId() {
        return ID3v24Frames.FRAME_ID_TITLE;
    }

    protected String getTrackId() {
        return ID3v24Frames.FRAME_ID_TRACK;
    }

    protected String getYearId() {
        return ID3v24Frames.FRAME_ID_YEAR;
    }

    protected String getCommentId() {
        return ID3v24Frames.FRAME_ID_COMMENT;
    }

    protected String getGenreId() {
        return ID3v24Frames.FRAME_ID_GENRE;
    }

    /**
     * Create a new frame with the specified frameid
     *
     * @param id
     * @return
     */
    public ID3v24Frame createFrame(String id) {
        return new ID3v24Frame(id);
    }

    /**
     * Create Frame for Id3 Key
     * <p/>
     * Only textual data supported at the moment, should only be used with frames that
     * support a simple string argument.
     *
     * @param id3Key
     * @param value
     * @return
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    public TagField createTagField(ID3v24FieldKey id3Key, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (id3Key == null) {
            throw new KeyNotFoundException();
        }
        return super.doCreateTagField(new FrameAndSubId(id3Key.getFrameId(), id3Key.getSubId()), value);
    }

    /**
     * Retrieve the first value that exists for this id3v24key
     *
     * @param id3v24FieldKey
     * @return
     */
    public String getFirst(ID3v24FieldKey id3v24FieldKey) throws KeyNotFoundException {
        if (id3v24FieldKey == null) {
            throw new KeyNotFoundException();
        }
        return super.doGetFirst(new FrameAndSubId(id3v24FieldKey.getFrameId(), id3v24FieldKey.getSubId()));
    }

    /**
     * Delete fields with this id3v24FieldKey
     *
     * @param id3v24FieldKey
     */
    public void deleteTagField(ID3v24FieldKey id3v24FieldKey) throws KeyNotFoundException {
        if (id3v24FieldKey == null) {
            throw new KeyNotFoundException();
        }
        super.doDeleteTagField(new FrameAndSubId(id3v24FieldKey.getFrameId(), id3v24FieldKey.getSubId()));
    }

    protected FrameAndSubId getFrameAndSubIdFromGenericKey(TagFieldKey genericKey) {
        ID3v24FieldKey id3v24FieldKey = ID3v24Frames.getInstanceOf().getId3KeyFromGenericKey(genericKey);
        if (id3v24FieldKey == null) {
            throw new KeyNotFoundException();
        }
        return new FrameAndSubId(id3v24FieldKey.getFrameId(), id3v24FieldKey.getSubId());
    }

    protected ID3Frames getID3Frames() {
        return ID3v24Frames.getInstanceOf();
    }

    /**
     * @return comparator used to order frames in preferred order for writing to file
     *         so that most important frames are written first.
     */
    public Comparator getPreferredFrameOrderComparator() {
        return ID3v24PreferredFrameOrderComparator.getInstanceof();
    }

    public List<Artwork> getArtworkList() {
        List<TagField> coverartList = get(TagFieldKey.COVER_ART);
        List<Artwork> artworkList = new ArrayList<Artwork>(coverartList.size());

        for (TagField next : coverartList) {
            FrameBodyAPIC coverArt = (FrameBodyAPIC) ((AbstractID3v2Frame) next).getBody();
            Artwork artwork = new Artwork();
            artwork.setMimeType(coverArt.getMimeType());
            artwork.setPictureType(coverArt.getPictureType());
            if (coverArt.isImageUrl()) {
                artwork.setLinked(true);
                artwork.setImageUrl(coverArt.getImageUrl());
            } else {
                artwork.setBinaryData(coverArt.getImageData());
            }
            artworkList.add(artwork);
        }
        return artworkList;
    }

    public TagField createArtworkField(Artwork artwork) throws FieldDataInvalidException {
        AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(TagFieldKey.COVER_ART).getFrameId());
        FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
        body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, artwork.getBinaryData());
        body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, artwork.getPictureType());
        body.setObjectValue(DataTypes.OBJ_MIME_TYPE, artwork.getMimeType());
        body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
        return frame;
    }

    /**
     * Create Artwork
     *
     * @param data
     * @param mimeType of the image
     * @see PictureTypes
     */
    public TagField createArtworkField(byte[] data, String mimeType) {
        AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(TagFieldKey.COVER_ART).getFrameId());
        FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
        body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, data);
        body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, PictureTypes.DEFAULT_ID);
        body.setObjectValue(DataTypes.OBJ_MIME_TYPE, mimeType);
        body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
        return frame;
    }
}
