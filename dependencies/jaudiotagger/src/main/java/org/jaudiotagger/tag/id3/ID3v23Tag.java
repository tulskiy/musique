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
 *  you can getFields a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jaudiotagger.tag.id3;

import org.jaudiotagger.FileConstants;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.logging.ErrorMessage;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.framebody.*;
import org.jaudiotagger.tag.reference.PictureTypes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Represents an ID3v2.3 tag.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: ID3v23Tag.java 932 2010-11-26 13:13:15Z paultaylor $
 */
public class ID3v23Tag extends AbstractID3v2Tag {

    protected static final String TYPE_CRCDATA = "crcdata";
    protected static final String TYPE_EXPERIMENTAL = "experimental";
    protected static final String TYPE_EXTENDED = "extended";
    protected static final String TYPE_PADDINGSIZE = "paddingsize";
    protected static final String TYPE_UNSYNCHRONISATION = "unsyncronisation";


    protected static int TAG_EXT_HEADER_LENGTH = 10;
    protected static int TAG_EXT_HEADER_CRC_LENGTH = 4;
    protected static int FIELD_TAG_EXT_SIZE_LENGTH = 4;
    protected static int TAG_EXT_HEADER_DATA_LENGTH = TAG_EXT_HEADER_LENGTH - FIELD_TAG_EXT_SIZE_LENGTH;

    /**
     * ID3v2.3 Header bit mask
     */
    public static final int MASK_V23_UNSYNCHRONIZATION = FileConstants.BIT7;

    /**
     * ID3v2.3 Header bit mask
     */
    public static final int MASK_V23_EXTENDED_HEADER = FileConstants.BIT6;

    /**
     * ID3v2.3 Header bit mask
     */
    public static final int MASK_V23_EXPERIMENTAL = FileConstants.BIT5;

    /**
     * ID3v2.3 Extended Header bit mask
     */
    public static final int MASK_V23_CRC_DATA_PRESENT = FileConstants.BIT7;

    /**
     * ID3v2.3 RBUF frame bit mask
     */
    public static final int MASK_V23_EMBEDDED_INFO_FLAG = FileConstants.BIT1;

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
     * Crcdata Checksum in extended header
     */
    private int crc32;

    /**
     * Tag padding
     */
    private int paddingSize = 0;

    /**
     * All frames in the tag uses unsynchronisation
     */
    protected boolean unsynchronization = false;

    /**
     * The tag is compressed
     */
    protected boolean compression = false;


    public static final byte RELEASE = 2;
    public static final byte MAJOR_VERSION = 3;
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
     * @return Cyclic Redundancy Check 32 Value
     */
    public int getCrc32() {
        return crc32;
    }

    /**
     * Creates a new empty ID3v2_3 datatype.
     */
    public ID3v23Tag() {
        frameMap = new LinkedHashMap();
        encryptedFrameMap = new LinkedHashMap();
    }

    /**
     * Copy primitives applicable to v2.3
     */
    protected void copyPrimitives(AbstractID3v2Tag copyObj) {
        //logger.info("Copying primitives");
        super.copyPrimitives(copyObj);

        if (copyObj instanceof ID3v23Tag) {
            ID3v23Tag copyObject = (ID3v23Tag) copyObj;
            this.crcDataFlag = copyObject.crcDataFlag;
            this.experimental = copyObject.experimental;
            this.extended = copyObject.extended;
            this.crc32 = copyObject.crc32;
            this.paddingSize = copyObject.paddingSize;
        }
    }


    protected void addFrame(AbstractID3v2Frame frame) {
        try {
            //Special case to handle TDRC frame from V24 that needs breaking up into separate frame in V23
            if ((frame.getIdentifier().equals(ID3v24Frames.FRAME_ID_YEAR)) && (frame.getBody() instanceof FrameBodyTDRC)) {
                translateFrame(frame);
            } else if (frame instanceof ID3v23Frame) {
                copyFrameIntoMap(frame.getIdentifier(), frame);
            } else {
                ID3v23Frame newFrame = new ID3v23Frame(frame);
                copyFrameIntoMap(newFrame.getIdentifier(), newFrame);
            }
        } catch (InvalidFrameException ife) {
            logger.log(Level.SEVERE, "Unable to convert frame:" + frame.getIdentifier());
        }
    }

    /**
     * This is used when we need to translate a single frame into multiple frames,
     * currently required for v24 TDRC frames.
     *
     * @param frame
     */
    //TODO will overwrite any existing TYER or TIME frame, do we ever want multiples of these
    protected void translateFrame(AbstractID3v2Frame frame) {
        FrameBodyTDRC tmpBody = (FrameBodyTDRC) frame.getBody();
        ID3v23Frame newFrame;
        if (!tmpBody.getYear().equals("")) {
            newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TYER);
            ((FrameBodyTYER) newFrame.getBody()).setText(tmpBody.getYear());
            //logger.info("Adding Frame:" + newFrame.getIdentifier());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
        if (!tmpBody.getDate().equals("")) {
            newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TDAT);
            ((FrameBodyTDAT) newFrame.getBody()).setText(tmpBody.getDate());
            ((FrameBodyTDAT) newFrame.getBody()).setMonthOnly(tmpBody.isMonthOnly());
            //logger.info("Adding Frame:" + newFrame.getIdentifier());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
        if (!tmpBody.getTime().equals("")) {
            newFrame = new ID3v23Frame(ID3v23Frames.FRAME_ID_V3_TIME);
            ((FrameBodyTIME) newFrame.getBody()).setText(tmpBody.getTime());
            ((FrameBodyTIME) newFrame.getBody()).setHoursOnly(tmpBody.isHoursOnly());
            //logger.info("Adding Frame:" + newFrame.getIdentifier());
            frameMap.put(newFrame.getIdentifier(), newFrame);
        }
    }

    /**
     * Copy Constructor, creates a new ID3v2_3 Tag based on another ID3v2_3 Tag
     *
     * @param copyObject
     */
    public ID3v23Tag(ID3v23Tag copyObject) {
        //This doesn't do anything.
        super(copyObject);
        //logger.info("Creating tag from another tag of same type");
        copyPrimitives(copyObject);
        copyFrames(copyObject);

    }

    /**
     * Constructs a new tag based upon another tag of different version/type
     *
     * @param mp3tag
     */
    public ID3v23Tag(AbstractTag mp3tag) {
        //logger.info("Creating tag from a tag of a different version");
        frameMap = new LinkedHashMap();
        encryptedFrameMap = new LinkedHashMap();

        if (mp3tag != null) {
            ID3v24Tag convertedTag;
            //Should use simpler copy constructor
            if (mp3tag instanceof ID3v23Tag) {
                throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
            }
            if (mp3tag instanceof ID3v24Tag) {
                convertedTag = (ID3v24Tag) mp3tag;
            }
            //All tags types can be converted to v2.4 so do this to simplify things
            else {
                convertedTag = new ID3v24Tag(mp3tag);
            }
            this.setLoggingFilename(convertedTag.getLoggingFilename());
            //Copy Primitives
            copyPrimitives(convertedTag);
            //Copy Frames
            copyFrames(convertedTag);
            //logger.info("Created tag from a tag of a different version");
        }
    }

    /**
     * Creates a new ID3v2_3 datatype.
     *
     * @param buffer
     * @param loggingFilename
     * @throws TagException
     */
    public ID3v23Tag(ByteBuffer buffer, String loggingFilename) throws TagException {
        setLoggingFilename(loggingFilename);
        this.read(buffer);
    }


    /**
     * Creates a new ID3v2_3 datatype.
     *
     * @param buffer
     * @throws TagException
     * @deprecated use {@link #ID3v23Tag(ByteBuffer, String)} instead
     */
    public ID3v23Tag(ByteBuffer buffer) throws TagException {
        this(buffer, "");
    }

    /**
     * @return textual tag identifier
     */
    public String getIdentifier() {
        return "ID3v2.30";
    }

    /**
     * Return frame size based upon the sizes of the tags rather than the physical
     * no of bytes between start of ID3Tag and start of Audio Data.
     * <p/>
     * TODO this is incorrect, because of subclasses
     *
     * @return size of tag
     */
    public int getSize() {
        int size = TAG_HEADER_LENGTH;
        if (extended) {
            size += TAG_EXT_HEADER_LENGTH;
            if (crcDataFlag) {
                size += TAG_EXT_HEADER_CRC_LENGTH;
            }
        }
        size += super.getSize();
        return size;
    }

    /**
     * Is Tag Equivalent to another tag
     *
     * @param obj
     * @return true if tag is equivalent to another
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof ID3v23Tag)) {
            return false;
        }
        ID3v23Tag object = (ID3v23Tag) obj;
        if (this.crc32 != object.crc32) {
            return false;
        }
        if (this.crcDataFlag != object.crcDataFlag) {
            return false;
        }
        if (this.experimental != object.experimental) {
            return false;
        }
        if (this.extended != object.extended) {
            return false;
        }
        return this.paddingSize == object.paddingSize && super.equals(obj);
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

        //Return the exact size of tag as setField in the tag header
        return size + TAG_HEADER_LENGTH;
    }

    /**
     * Read header flags
     * <p/>
     * <p>Log info messages for flags that have been set and log warnings when bits have been set for unknown flags</p>
     *
     * @param buffer
     * @throws TagException
     */
    private void readHeaderFlags(ByteBuffer buffer) throws TagException {
        //Allowable Flags
        byte flags = buffer.get();
        unsynchronization = (flags & MASK_V23_UNSYNCHRONIZATION) != 0;
        extended = (flags & MASK_V23_EXTENDED_HEADER) != 0;
        experimental = (flags & MASK_V23_EXPERIMENTAL) != 0;

        //Not allowable/Unknown Flags
        if ((flags & FileConstants.BIT4) != 0) {
            //logger.warning(ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET.getMsg(getLoggingFilename(), FileConstants.BIT4));
        }

        if ((flags & FileConstants.BIT3) != 0) {
            //logger.warning(ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET.getMsg(getLoggingFilename(), FileConstants.BIT3));
        }

        if ((flags & FileConstants.BIT2) != 0) {
            //logger.warning(ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET.getMsg(getLoggingFilename(), FileConstants.BIT2));
        }

        if ((flags & FileConstants.BIT1) != 0) {
            //logger.warning(ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET.getMsg(getLoggingFilename(), FileConstants.BIT1));
        }

        if ((flags & FileConstants.BIT0) != 0) {
            //logger.warning(ErrorMessage.ID3_INVALID_OR_UNKNOWN_FLAG_SET.getMsg(getLoggingFilename(), FileConstants.BIT0));
        }

        if (isUnsynchronization()) {
            //logger.info(ErrorMessage.ID3_TAG_UNSYNCHRONIZED.getMsg(getLoggingFilename()));
        }

        if (extended) {
            //logger.info(ErrorMessage.ID3_TAG_EXTENDED.getMsg(getLoggingFilename()));
        }

        if (experimental) {
            //logger.info(ErrorMessage.ID3_TAG_EXPERIMENTAL.getMsg(getLoggingFilename()));
        }
    }

    /**
     * Read the optional extended header
     *
     * @param buffer
     * @param size
     */
    private void readExtendedHeader(ByteBuffer buffer, int size) {
        // Int is 4 bytes.
        int extendedHeaderSize = buffer.getInt();
        // Extended header without CRC Data
        if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH) {
            //Flag should not be setField , if is log a warning
            byte extFlag = buffer.get();
            crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
            if (crcDataFlag) {
                //logger.warning(ErrorMessage.ID3_TAG_CRC_FLAG_SET_INCORRECTLY.getMsg(getLoggingFilename()));
            }
            //2nd Flag Byte (not used)
            buffer.get();

            //Take padding and ext header size off the size to be read
            paddingSize = buffer.getInt();
            if (paddingSize > 0) {
                //logger.info(ErrorMessage.ID3_TAG_PADDING_SIZE.getMsg(getLoggingFilename(), paddingSize));
            }
            size = size - (paddingSize + TAG_EXT_HEADER_LENGTH);
        } else if (extendedHeaderSize == TAG_EXT_HEADER_DATA_LENGTH + TAG_EXT_HEADER_CRC_LENGTH) {
            //logger.info(ErrorMessage.ID3_TAG_CRC.getMsg(getLoggingFilename()));

            //Flag should be setField, if nor just act as if it is
            byte extFlag = buffer.get();
            crcDataFlag = (extFlag & MASK_V23_CRC_DATA_PRESENT) != 0;
            if (!crcDataFlag) {
                //logger.warning(ErrorMessage.ID3_TAG_CRC_FLAG_SET_INCORRECTLY.getMsg(getLoggingFilename()));
            }
            //2nd Flag Byte (not used)
            buffer.get();
            //Take padding size of size to be read
            paddingSize = buffer.getInt();
            if (paddingSize > 0) {
                //logger.info(ErrorMessage.ID3_TAG_PADDING_SIZE.getMsg(getLoggingFilename(), paddingSize));
            }
            size = size - (paddingSize + TAG_EXT_HEADER_LENGTH + TAG_EXT_HEADER_CRC_LENGTH);
            //CRC Data
            crc32 = buffer.getInt();
            //logger.info(ErrorMessage.ID3_TAG_CRC_SIZE.getMsg(getLoggingFilename(), crc32));
        }
        //Extended header size is only allowed to be six or ten bytes so this is invalid but instead
        //of giving up lets guess its six bytes and carry on and see if we can read file ok
        else {
            //logger.warning(ErrorMessage.ID3_EXTENDED_HEADER_SIZE_INVALID.getMsg(getLoggingFilename(), extendedHeaderSize));
            buffer.position(buffer.position() - FIELD_TAG_EXT_SIZE_LENGTH);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(ByteBuffer buffer) throws TagException {
        int size;
        if (!seek(buffer)) {
            throw new TagNotFoundException(getIdentifier() + " tag not found");
        }
        //logger.info(getLoggingFilename() + ":" + "Reading ID3v23 tag");

        readHeaderFlags(buffer);

        // Read the size, this is size of tag not including the tag header
        size = ID3SyncSafeInteger.bufferToValue(buffer);
        //logger.info(ErrorMessage.ID_TAG_SIZE.getMsg(getLoggingFilename(), size));

        //Extended Header
        if (extended) {
            readExtendedHeader(buffer, size);
        }

        //Slice Buffer, so position markers tally with size (i.e do not include tagHeader)
        ByteBuffer bufferWithoutHeader = buffer.slice();
        //We need to synchronize the buffer
        if (isUnsynchronization()) {
            bufferWithoutHeader = ID3Unsynchronization.synchronize(bufferWithoutHeader);
        }

        readFrames(bufferWithoutHeader, size);
        //logger.info(getLoggingFilename() + ":Loaded Frames,there are:" + frameMap.keySet().size());

    }


    /**
     * Read the frames
     * <p/>
     * Read from byteBuffer upto size
     *
     * @param byteBuffer
     * @param size
     */
    protected void readFrames(ByteBuffer byteBuffer, int size) {
        //Now start looking for frames
        ID3v23Frame next;
        frameMap = new LinkedHashMap();
        encryptedFrameMap = new LinkedHashMap();


        //Read the size from the Tag Header
        this.fileReadSize = size;
        //logger.finest(getLoggingFilename() + ":Start of frame body at:" + byteBuffer.position() + ",frames data size is:" + size);

        // Read the frames until got to up to the size as specified in header or until
        // we hit an invalid frame identifier or padding
        while (byteBuffer.position() < size) {
            String id;
            try {
                //Read Frame
                //logger.finest(getLoggingFilename() + ":Looking for next frame at:" + byteBuffer.position());
                next = new ID3v23Frame(byteBuffer, getLoggingFilename());
                id = next.getIdentifier();
                loadFrameIntoMap(id, next);
            }
            //Found Padding, no more frames
            catch (PaddingException ex) {
                logger.config(getLoggingFilename() + ":Found padding starting at:" + byteBuffer.position());
                break;
            }
            //Found Empty Frame, log it - empty frames should not exist
            catch (EmptyFrameException ex) {
                //logger.warning(getLoggingFilename() + ":Empty Frame:" + ex.getMessage());
                this.emptyFrameBytes += ID3v23Frame.FRAME_HEADER_SIZE;
            } catch (InvalidFrameIdentifierException ifie) {
                //logger.warning(getLoggingFilename() + ":Invalid Frame Identifier:" + ifie.getMessage());
                this.invalidFrames++;
                //Don't try and find any more frames
                break;
            }
            //Problem trying to find frame, often just occurs because frameHeader includes padding
            //and we have reached padding
            catch (InvalidFrameException ife) {
                //logger.warning(getLoggingFilename() + ":Invalid Frame:" + ife.getMessage());
                this.invalidFrames++;
                //Don't try and find any more frames
                break;
            }
            //Failed reading frame but may just have invalid data but correct length so lets carry on
            //in case we can read the next frame
            catch (InvalidDataTypeException idete) {
                //logger.warning(getLoggingFilename() + ":Corrupt Frame:" + idete.getMessage());
                this.invalidFrames++;
                continue;
            }
        }
    }

    /**
     * Write the ID3 header to the ByteBuffer.
     * <p/>
     * TODO Calculate the CYC Data Check
     * TODO Reintroduce Extended Header
     *
     * @param padding is the size of the padding portion of the tag
     * @param size    is the size of the body data
     * @return ByteBuffer
     * @throws IOException
     */
    private ByteBuffer writeHeaderToBuffer(int padding, int size) throws IOException {
        // Flags,currently we never calculate the CRC
        // and if we dont calculate them cant keep orig values. Tags are not
        // experimental and we never createField extended header to keep things simple.
        extended = false;
        experimental = false;
        crcDataFlag = false;

        // Create Header Buffer,allocate maximum possible size for the header
        ByteBuffer headerBuffer = ByteBuffer.
                allocate(TAG_HEADER_LENGTH + TAG_EXT_HEADER_LENGTH + TAG_EXT_HEADER_CRC_LENGTH);

        //TAGID
        headerBuffer.put(TAG_ID);

        //Major Version
        headerBuffer.put(getMajorVersion());

        //Minor Version
        headerBuffer.put(getRevision());

        //Flags
        byte flagsByte = 0;
        if (isUnsynchronization()) {
            flagsByte |= MASK_V23_UNSYNCHRONIZATION;
        }
        if (extended) {
            flagsByte |= MASK_V23_EXTENDED_HEADER;
        }
        if (experimental) {
            flagsByte |= MASK_V23_EXPERIMENTAL;
        }
        headerBuffer.put(flagsByte);

        //Additional Header Size,(for completeness we never actually write the extended header)
        int additionalHeaderSize = 0;
        if (extended) {
            additionalHeaderSize += TAG_EXT_HEADER_LENGTH;
            if (crcDataFlag) {
                additionalHeaderSize += TAG_EXT_HEADER_CRC_LENGTH;
            }
        }

        //Size As Recorded in Header, don't include the main header length
        headerBuffer.put(ID3SyncSafeInteger.valueToBuffer(padding + size + additionalHeaderSize));

        //Write Extended Header
        if (extended) {
            byte extFlagsByte1 = 0;
            byte extFlagsByte2 = 0;

            //Contains CRCData
            if (crcDataFlag) {
                headerBuffer.putInt(TAG_EXT_HEADER_DATA_LENGTH + TAG_EXT_HEADER_CRC_LENGTH);
                extFlagsByte1 |= MASK_V23_CRC_DATA_PRESENT;
                headerBuffer.put(extFlagsByte1);
                headerBuffer.put(extFlagsByte2);
                headerBuffer.putInt(paddingSize);
                headerBuffer.putInt(crc32);
            }
            //Just extended Header
            else {
                headerBuffer.putInt(TAG_EXT_HEADER_DATA_LENGTH);
                headerBuffer.put(extFlagsByte1);
                headerBuffer.put(extFlagsByte2);
                //Newly Calculated Padding As Recorded in Extended Header
                headerBuffer.putInt(padding);
            }
        }

        headerBuffer.flip();
        return headerBuffer;
    }


    /**
     * Write tag to file
     * <p/>
     * TODO:we currently never write the Extended header , but if we did the size calculation in this
     * method would be slightly incorrect
     *
     * @param file The file to write to
     * @throws IOException
     */
    public void write(File file, long audioStartLocation) throws IOException {
        setLoggingFilename(file.getName());
        //logger.info("Writing tag to file:" + getLoggingFilename());

        //Write Body Buffer
        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        //logger.info(getLoggingFilename() + ":bodybytebuffer:sizebeforeunsynchronisation:" + bodyByteBuffer.length);

        // Unsynchronize if option enabled and unsync required
        unsynchronization = TagOptionSingleton.getInstance().isUnsyncTags() && ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        if (isUnsynchronization()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            //logger.info(getLoggingFilename() + ":bodybytebuffer:sizeafterunsynchronisation:" + bodyByteBuffer.length);
        }

        int sizeIncPadding = calculateTagSize(bodyByteBuffer.length + TAG_HEADER_LENGTH, (int) audioStartLocation);
        int padding = sizeIncPadding - (bodyByteBuffer.length + TAG_HEADER_LENGTH);
        //logger.info(getLoggingFilename() + ":Current audiostart:" + audioStartLocation);
        //logger.info(getLoggingFilename() + ":Size including padding:" + sizeIncPadding);
        //logger.info(getLoggingFilename() + ":Padding:" + padding);

        ByteBuffer headerBuffer = writeHeaderToBuffer(padding, bodyByteBuffer.length);
        writeBufferToFile(file, headerBuffer, bodyByteBuffer, padding, sizeIncPadding, audioStartLocation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(WritableByteChannel channel) throws IOException {
        //logger.info(getLoggingFilename() + ":Writing tag to channel");

        byte[] bodyByteBuffer = writeFramesToBuffer().toByteArray();
        //logger.info(getLoggingFilename() + ":bodybytebuffer:sizebeforeunsynchronisation:" + bodyByteBuffer.length);

        // Unsynchronize if option enabled and unsync required
        unsynchronization = TagOptionSingleton.getInstance().isUnsyncTags() && ID3Unsynchronization.requiresUnsynchronization(bodyByteBuffer);
        if (isUnsynchronization()) {
            bodyByteBuffer = ID3Unsynchronization.unsynchronize(bodyByteBuffer);
            //logger.info(getLoggingFilename() + ":bodybytebuffer:sizeafterunsynchronisation:" + bodyByteBuffer.length);
        }
        ByteBuffer headerBuffer = writeHeaderToBuffer(0, bodyByteBuffer.length);

        channel.write(headerBuffer);
        channel.write(ByteBuffer.wrap(bodyByteBuffer));
    }

    /**
     * For representing the MP3File in an XML Format
     */
    public void createStructure() {

        MP3File.getStructureFormatter().openHeadingElement(TYPE_TAG, getIdentifier());

        super.createStructureHeader();

        //Header
        MP3File.getStructureFormatter().openHeadingElement(TYPE_HEADER, "");
        MP3File.getStructureFormatter().addElement(TYPE_UNSYNCHRONISATION, this.isUnsynchronization());
        MP3File.getStructureFormatter().addElement(TYPE_EXTENDED, this.extended);
        MP3File.getStructureFormatter().addElement(TYPE_EXPERIMENTAL, this.experimental);
        MP3File.getStructureFormatter().addElement(TYPE_CRCDATA, this.crc32);
        MP3File.getStructureFormatter().addElement(TYPE_PADDINGSIZE, this.paddingSize);
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_HEADER);
        //Body
        super.createStructureBody();
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_TAG);
    }

    /**
     * @return is tag unsynchronized
     */
    public boolean isUnsynchronization() {
        return unsynchronization;
    }

    public ID3v23Frame createFrame(String id) {
        return new ID3v23Frame(id);
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
    public TagField createField(ID3v23FieldKey id3Key, String value) throws KeyNotFoundException, FieldDataInvalidException {
        if (id3Key == null) {
            throw new KeyNotFoundException();
        }
        return super.doCreateTagField(new FrameAndSubId(id3Key.getFrameId(), id3Key.getSubId()), value);
    }

    /**
     * Retrieve the first value that exists for this id3v23key
     *
     * @param id3v23FieldKey
     * @return
     * @throws org.jaudiotagger.tag.KeyNotFoundException
     *
     */
    public String getFirst(ID3v23FieldKey id3v23FieldKey) throws KeyNotFoundException {
        if (id3v23FieldKey == null) {
            throw new KeyNotFoundException();
        }

        FrameAndSubId frameAndSubId = new FrameAndSubId(id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId());
        if (id3v23FieldKey == ID3v23FieldKey.TRACK) {
            AbstractID3v2Frame frame = getFirstField(frameAndSubId.getFrameId());
            return String.valueOf(((FrameBodyTRCK) frame.getBody()).getTrackNo());
        } else if (id3v23FieldKey == ID3v23FieldKey.TRACK_TOTAL) {
            AbstractID3v2Frame frame = getFirstField(frameAndSubId.getFrameId());
            return String.valueOf(((FrameBodyTRCK) frame.getBody()).getTrackTotal());
        } else if (id3v23FieldKey == ID3v23FieldKey.DISC_NO) {
            AbstractID3v2Frame frame = getFirstField(frameAndSubId.getFrameId());
            return String.valueOf(((FrameBodyTPOS) frame.getBody()).getDiscNo());
        } else if (id3v23FieldKey == ID3v23FieldKey.DISC_TOTAL) {
            AbstractID3v2Frame frame = getFirstField(frameAndSubId.getFrameId());
            return String.valueOf(((FrameBodyTPOS) frame.getBody()).getDiscTotal());
        } else {
            return super.doGetValueAtIndex(frameAndSubId, 0);
        }
    }

    /**
     * Delete fields with this id3v23FieldKey
     *
     * @param id3v23FieldKey
     * @throws org.jaudiotagger.tag.KeyNotFoundException
     *
     */
    public void deleteField(ID3v23FieldKey id3v23FieldKey) throws KeyNotFoundException {
        if (id3v23FieldKey == null) {
            throw new KeyNotFoundException();
        }
        super.doDeleteTagField(new FrameAndSubId(id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId()));
    }

    /**
     * Delete fields with this (frame) id
     *
     * @param id
     */
    public void deleteField(String id) {
        super.doDeleteTagField(new FrameAndSubId(id, null));
    }

    protected FrameAndSubId getFrameAndSubIdFromGenericKey(FieldKey genericKey) {
        ID3v23FieldKey id3v23FieldKey = ID3v23Frames.getInstanceOf().getId3KeyFromGenericKey(genericKey);
        if (id3v23FieldKey == null) {
            throw new KeyNotFoundException();
        }
        return new FrameAndSubId(id3v23FieldKey.getFrameId(), id3v23FieldKey.getSubId());
    }

    protected ID3Frames getID3Frames() {
        return ID3v23Frames.getInstanceOf();
    }

    /**
     * @return comparator used to order frames in preferred order for writing to file
     *         so that most important frames are written first.
     */
    public Comparator getPreferredFrameOrderComparator() {
        return ID3v23PreferredFrameOrderComparator.getInstanceof();
    }

    /**
     * {@inheritDoc}
     */
    public List<Artwork> getArtworkList() {
        List<TagField> coverartList = getFields(FieldKey.COVER_ART);
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

    /**
     * {@inheritDoc}
     */
    public TagField createField(Artwork artwork) throws FieldDataInvalidException {
        AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(FieldKey.COVER_ART).getFrameId());
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
     * @return
     * @see PictureTypes
     */
    public TagField createArtworkField(byte[] data, String mimeType) {
        AbstractID3v2Frame frame = createFrame(getFrameAndSubIdFromGenericKey(FieldKey.COVER_ART).getFrameId());
        FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
        body.setObjectValue(DataTypes.OBJ_PICTURE_DATA, data);
        body.setObjectValue(DataTypes.OBJ_PICTURE_TYPE, PictureTypes.DEFAULT_ID);
        body.setObjectValue(DataTypes.OBJ_MIME_TYPE, mimeType);
        body.setObjectValue(DataTypes.OBJ_DESCRIPTION, "");
        return frame;
    }

    public int getPaddingSize() {
        return paddingSize;
    }
}
