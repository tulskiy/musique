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
import org.jaudiotagger.audio.generic.Utils;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.EmptyFrameException;
import org.jaudiotagger.tag.InvalidFrameException;
import org.jaudiotagger.tag.InvalidFrameIdentifierException;
import org.jaudiotagger.tag.id3.framebody.AbstractID3v2FrameBody;
import org.jaudiotagger.tag.id3.framebody.FrameBodyDeprecated;
import org.jaudiotagger.tag.id3.framebody.FrameBodyUnsupported;
import org.jaudiotagger.tag.id3.framebody.ID3v23FrameBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Represents an ID3v2.3 frame.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: ID3v23Frame.java,v 1.38 2008/11/12 16:41:39 paultaylor Exp $
 */
public class ID3v23Frame extends AbstractID3v2Frame {
    Pattern validFrameIdentifier = Pattern.compile("[A-Z][0-9A-Z]{3}");

    protected static final int FRAME_ID_SIZE = 4;
    protected static final int FRAME_FLAGS_SIZE = 2;
    protected static final int FRAME_SIZE_SIZE = 4;
    protected static final int FRAME_COMPRESSION_UNCOMPRESSED_SIZE = 4;
    protected static final int FRAME_ENCRYPTION_INDICATOR_SIZE = 1;
    protected static final int FRAME_GROUPING_INDICATOR_SIZE = 1;

    protected static final int FRAME_HEADER_SIZE = FRAME_ID_SIZE + FRAME_SIZE_SIZE + FRAME_FLAGS_SIZE;

    /**
     * Creates a new ID3v23 Frame
     */
    public ID3v23Frame() {
    }

    /**
     * Creates a new ID3v23 Frame of type identifier.
     * <p/>
     * <p>An empty body of the correct type will be automatically created.
     * This constructor should be used when wish to create a new
     * frame from scratch using user data.
     */
    public ID3v23Frame(String identifier) {
        super(identifier);
        statusFlags = new StatusFlags();
        encodingFlags = new EncodingFlags();
    }

    /**
     * Copy Constructor
     * <p/>
     * Creates a new v23 frame  based on another v23 frame
     */
    public ID3v23Frame(ID3v23Frame frame) {
        super(frame);
        statusFlags = new StatusFlags(frame.getStatusFlags().getOriginalFlags());
        encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
    }

    /**
     * Creates a new ID3v23Frame  based on another frame.
     *
     * @param frame
     */
    public ID3v23Frame(AbstractID3v2Frame frame) throws InvalidFrameException {
        //logger.info("Creating frame from a frame of a different version");
        if ((frame instanceof ID3v23Frame) && (frame instanceof ID3v24Frame == false)) {
            throw new UnsupportedOperationException("Copy Constructor not called. Please type cast the argument");
        }
        if (frame instanceof ID3v24Frame) {
            statusFlags = new StatusFlags((ID3v24Frame.StatusFlags) frame.getStatusFlags());
            encodingFlags = new EncodingFlags(frame.getEncodingFlags().getFlags());
        }

        if (frame instanceof ID3v24Frame) {
            if (ID3Tags.isID3v24FrameIdentifier(frame.getIdentifier())) {
                //Version between v4 and v3
                identifier = ID3Tags.convertFrameID24To23(frame.getIdentifier());
                if (identifier != null) {
                    //logger.info("V4:Orig id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                    this.frameBody = (AbstractTagFrameBody) ID3Tags.copyObject(frame.getBody());
                    this.frameBody.setHeader(this);
                    return;
                } else {
                    //Is it a known v4 frame which needs forcing to v3 frame e.g. TDRC - TYER,TDAT
                    identifier = ID3Tags.forceFrameID24To23(frame.getIdentifier());
                    if (identifier != null) {
                        //logger.info("V4:Orig id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                        this.frameBody = this.readBody(identifier, (AbstractID3v2FrameBody) frame.getBody());
                        this.frameBody.setHeader(this);
                        return;
                    }
                    //It is a v24 frame that is not known and cannot be forced in v23 e.g TDRL,in which case
                    //we convert to a framebody unsupported by writing contents as a byte array and feeding
                    //it into FrameBodyUnsupported
                    else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ((AbstractID3v2FrameBody) frame.getBody()).write(baos);

                        identifier = frame.getIdentifier();
                        this.frameBody = new FrameBodyUnsupported(identifier, baos.toByteArray());
                        this.frameBody.setHeader(this);
                        //logger.info("V4:Orig id is:" + frame.getIdentifier() + ":New Id Unsupported is:" + identifier);
                        return;
                    }
                }
            }
            //Unknown Frame e.g NCON
            else if (frame.getBody() instanceof FrameBodyUnsupported) {
                this.frameBody = new FrameBodyUnsupported((FrameBodyUnsupported) frame.getBody());
                this.frameBody.setHeader(this);
                identifier = frame.getIdentifier();
                //logger.info("UNKNOWN:Orig id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                return;
            }
            // Deprecated frame for v24
            else if (frame.getBody() instanceof FrameBodyDeprecated) {
                //Was it valid for this tag version, if so try and reconstruct
                if (ID3Tags.isID3v23FrameIdentifier(frame.getIdentifier())) {
                    this.frameBody = ((FrameBodyDeprecated) frame.getBody()).getOriginalFrameBody();
                    this.frameBody.setHeader(this);
                    identifier = frame.getIdentifier();
                    //logger.info("DEPRECATED:Orig id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                }
                //or was it still deprecated, if so leave as is
                else {
                    this.frameBody = new FrameBodyDeprecated((FrameBodyDeprecated) frame.getBody());
                    this.frameBody.setHeader(this);
                    identifier = frame.getIdentifier();
                    //logger.info("DEPRECATED:Orig id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                    return;
                }
            }
            // Unable to find a suitable framebody, this shold not happen
            else {
                //logger.severe("Orig id is:" + frame.getIdentifier() + "Unable to create Frame Body");
                throw new InvalidFrameException("Orig id is:" + frame.getIdentifier() + "Unable to create Frame Body");
            }
        } else if (frame instanceof ID3v22Frame) {
            if (ID3Tags.isID3v22FrameIdentifier(frame.getIdentifier())) {
                identifier = ID3Tags.convertFrameID22To23(frame.getIdentifier());
                if (identifier != null) {
                    //logger.info("V3:Orig id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                    this.frameBody = (AbstractTagFrameBody) ID3Tags.copyObject(frame.getBody());
                    this.frameBody.setHeader(this);
                    return;
                }
                //Is it a known v2 frame which needs forcing to v23 frame e.g PIC - APIC
                else if (ID3Tags.isID3v22FrameIdentifier(frame.getIdentifier())) {
                    //Force v2 to v3
                    identifier = ID3Tags.forceFrameID22To23(frame.getIdentifier());
                    if (identifier != null) {
                        //logger.info("V22Orig id is:" + frame.getIdentifier() + "New id is:" + identifier);
                        this.frameBody = this.readBody(identifier, (AbstractID3v2FrameBody) frame.getBody());
                        this.frameBody.setHeader(this);
                        return;
                    }
                    //No mechanism exists to convert it to a v23 frame
                    else {
                        this.frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frame.getBody());
                        this.frameBody.setHeader(this);
                        identifier = frame.getIdentifier();
                        //logger.info("Deprecated:V22:orig id id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                        return;
                    }
                }
            }
            // Unknown Frame e.g NCON
            else {
                this.frameBody = new FrameBodyUnsupported((FrameBodyUnsupported) frame.getBody());
                this.frameBody.setHeader(this);
                identifier = frame.getIdentifier();
                //logger.info("UNKNOWN:Orig id is:" + frame.getIdentifier() + ":New id is:" + identifier);
                return;
            }
        }

        //logger.warning("Frame is unknown version");
    }

    /**
     * Creates a new ID3v23Frame datatype by reading from byteBuffer.
     *
     * @param byteBuffer to read from
     */
    public ID3v23Frame(ByteBuffer byteBuffer, String loggingFilename) throws InvalidFrameException {
        setLoggingFilename(loggingFilename);
        read(byteBuffer);
    }

    /**
     * Creates a new ID3v23Frame datatype by reading from byteBuffer.
     *
     * @param byteBuffer to read from
     * @deprecated use {@link #ID3v23Frame(ByteBuffer,String)} instead
     */
    public ID3v23Frame(ByteBuffer byteBuffer) throws InvalidFrameException {
        this(byteBuffer, "");
    }

    /**
     * Return size of frame
     *
     * @return int frame size
     */
    public int getSize() {
        return frameBody.getSize() + ID3v23Frame.FRAME_HEADER_SIZE;
    }

    /**
     * Compare for equality
     * To be deemed equal obj must be a IDv23Frame with the same identifier
     * and the same flags.
     * containing the same body,datatype list ectera.
     * equals() method is made up from all the various components
     *
     * @param obj
     * @return if true if this object is equivalent to obj
     */
    public boolean equals(Object obj) {
        if ((obj instanceof ID3v23Frame) == false) {
            return false;
        }
        ID3v23Frame object = (ID3v23Frame) obj;
        if (this.statusFlags.getOriginalFlags() != object.statusFlags.getOriginalFlags()) {
            return false;
        }
        if (this.encodingFlags.getFlags() != object.encodingFlags.getFlags()) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Read the frame from a bytebuffer
     *
     * @param byteBuffer buffer to read from
     */
    public void read(ByteBuffer byteBuffer) throws InvalidFrameException {
        //logger.info(getLoggingFilename() + ":Read Frame from byteBuffer");

        if (byteBuffer.position() + FRAME_HEADER_SIZE >= byteBuffer.limit()) {
            //logger.warning(getLoggingFilename() + ":No space to find another frame:");
            throw new InvalidFrameException(getLoggingFilename() + ":No space to find another frame");
        }

        byte[] buffer = new byte[FRAME_ID_SIZE];

        // Read the Frame Identifier
        byteBuffer.get(buffer, 0, FRAME_ID_SIZE);
        identifier = new String(buffer);

        // Is this a valid identifier?
        if (isValidID3v2FrameIdentifier(identifier) == false) {
            //logger.info(getLoggingFilename() + ":Invalid identifier:" + identifier);
            byteBuffer.position(byteBuffer.position() - (FRAME_ID_SIZE - 1));
            throw new InvalidFrameIdentifierException(getLoggingFilename() + ":" + identifier + "is not a valid ID3v2.30 frame");
        }
        //Read the size field (as Big Endian Int - byte buffers always initlised to Big endian order)
        frameSize = byteBuffer.getInt();
        if (frameSize < 0) {
            //logger.warning(getLoggingFilename() + ":Invalid Frame Size:" + identifier);
            throw new InvalidFrameException(identifier + " is invalid frame");
        } else if (frameSize == 0) {
            //logger.warning(getLoggingFilename() + ":Empty Frame Size:" + identifier);
            //We dont process this frame or add to framemap becuase contains no useful information
            //Skip the two flag bytes so in correct position for subsequent frames
            byteBuffer.get();
            byteBuffer.get();
            throw new EmptyFrameException(identifier + " is empty frame");
        } else if (frameSize > byteBuffer.remaining()) {
            //logger.warning(getLoggingFilename() + ":Invalid Frame size larger than size before mp3 audio:" + identifier);
            throw new InvalidFrameException(identifier + " is invalid frame");
        }

        //Read the flag bytes
        statusFlags = new StatusFlags(byteBuffer.get());
        encodingFlags = new EncodingFlags(byteBuffer.get());
        String id;

        //If this identifier is a valid v24 identifier or easily converted to v24
        id = ID3Tags.convertFrameID23To24(identifier);

        // Cant easily be converted to v23 but is it a valid v24 identifier
        if (id == null) {
            // It is a valid v23 identifier so should be able to find a
            //  frame body for it.
            if (ID3Tags.isID3v23FrameIdentifier(identifier) == true) {
                id = identifier;
            }
            // Unknown so will be created as FrameBodyUnsupported
            else {
                id = UNSUPPORTED_ID;
            }
        }
        //logger.fine(getLoggingFilename() + ":Identifier was:" + identifier + " reading using:" + id + "with frame size:" + frameSize);

        //Read extra bits appended to frame header for various encodings
        //These are not included in header size but are included in frame size but wont be read when we actually
        //try to read the frame body data
        int extraHeaderBytesCount = 0;
        int decompressedFrameSize = -1;
        if (((EncodingFlags) encodingFlags).isCompression()) {
            //Read the Decompressed Size
            decompressedFrameSize = byteBuffer.getInt();
            extraHeaderBytesCount = FRAME_COMPRESSION_UNCOMPRESSED_SIZE;
            //logger.fine(getLoggingFilename() + ":Decompressed frame size is:" + decompressedFrameSize);
        }

        if (((EncodingFlags) encodingFlags).isEncryption()) {
            //Read the Encryption byte, but do nothing with it
            extraHeaderBytesCount += FRAME_ENCRYPTION_INDICATOR_SIZE;
            byteBuffer.get();
        }

        if (((EncodingFlags) encodingFlags).isGrouping()) {
            //Read the Grouping byte, but do nothing with it
            extraHeaderBytesCount += FRAME_GROUPING_INDICATOR_SIZE;
            byteBuffer.get();
        }

        //Work out the real size of the framebody data
        int realFrameSize = frameSize - extraHeaderBytesCount;

        ByteBuffer frameBodyBuffer = null;

        //Uncompress the frame
        if (((EncodingFlags) encodingFlags).isCompression()) {
            // Decompress the bytes into this buffer, size initialized from header field
            byte[] result = new byte[decompressedFrameSize];
            byte[] input = new byte[realFrameSize];

            //Store position ( just after frame header and any extra bits)
            //Read frame data into array, and then put buffer back to where it was
            int position = byteBuffer.position();
            byteBuffer.get(input, 0, realFrameSize);
            byteBuffer.position(position);

            Inflater decompresser = new Inflater();
            decompresser.setInput(input);
            try {
                decompresser.inflate(result);
            }
            catch (DataFormatException dfe) {
                dfe.printStackTrace();
                //Update position of main buffer, so no attempt is made to reread these bytes
                byteBuffer.position(byteBuffer.position() + realFrameSize);
                throw new InvalidFrameException(getLoggingFilename() + "Unable to decompress this frame:" + identifier + ":" + dfe.getMessage());
            }
            decompresser.end();
            frameBodyBuffer = ByteBuffer.wrap(result);
        }

        //Read the body data
        try {
            if (((EncodingFlags) encodingFlags).isCompression()) {
                frameBody = readBody(id, frameBodyBuffer, decompressedFrameSize);
            } else {
                //Create Buffer that only contains the body of this frame rather than the remainder of tag
                frameBodyBuffer = byteBuffer.slice();
                frameBodyBuffer.limit(realFrameSize);
                frameBody = readBody(id, frameBodyBuffer, realFrameSize);
            }
            //TODO code seems to assume that if the frame created is not a v23FrameBody
            //it should be deprecated, but what about if somehow a V24Frame has been put into a V23 Tag, shouldnt
            //it then be created as FrameBodyUnsupported
            if (!(frameBody instanceof ID3v23FrameBody)) {
                //logger.info(getLoggingFilename() + ":Converted frame body with:" + identifier + " to deprecated framebody");
                frameBody = new FrameBodyDeprecated((AbstractID3v2FrameBody) frameBody);
            }
        }
        finally {
            //Update position of main buffer, so no attempt is made to reread these bytes
            byteBuffer.position(byteBuffer.position() + realFrameSize);
        }
    }

    /**
     * Write the frame to bufferOutputStream
     *
     * @throws IOException
     */
    public void write(ByteArrayOutputStream tagBuffer) {
        //logger.info("Writing frame to buffer:" + getIdentifier());
        //This is where we will write header, move position to where we can
        //write body
        ByteBuffer headerBuffer = ByteBuffer.allocate(FRAME_HEADER_SIZE);

        //Write Frame Body Data
        ByteArrayOutputStream bodyOutputStream = new ByteArrayOutputStream();
        ((AbstractID3v2FrameBody) frameBody).write(bodyOutputStream);
        //Write Frame Header write Frame ID
        if (getIdentifier().length() == 3) {
            identifier = identifier + ' ';
        }
        headerBuffer.put(Utils.getDefaultBytes(getIdentifier(), "ISO-8859-1"), 0, FRAME_ID_SIZE);
        //Write Frame Size
        int size = frameBody.getSize();
        //logger.fine("Frame Size Is:" + size);
        headerBuffer.putInt(frameBody.getSize());

        //Write the Flags
        //Status Flags:leave as they were when we read
        headerBuffer.put(statusFlags.getWriteFlags());

        //Enclosing Flags, first reset
        encodingFlags.resetFlags();
        //Encoding we dont support any of flags so don't set any
        headerBuffer.put(encodingFlags.getFlags());

        try {
            //Add header to the Byte Array Output Stream
            tagBuffer.write(headerBuffer.array());

            //Add body to the Byte Array Output Stream
            tagBuffer.write(bodyOutputStream.toByteArray());
        }
        catch (IOException ioe) {
            //This could never happen coz not writing to file, so convert to RuntimeException
            throw new RuntimeException(ioe);
        }

    }

    protected AbstractID3v2Frame.StatusFlags getStatusFlags() {
        return statusFlags;
    }

    protected AbstractID3v2Frame.EncodingFlags getEncodingFlags() {
        return encodingFlags;
    }

    /**
     * This represents a frame headers Status Flags
     * Make adjustments if necessary based on frame type and specification.
     */
    class StatusFlags extends AbstractID3v2Frame.StatusFlags {
        public static final String TYPE_TAGALTERPRESERVATION = "typeTagAlterPreservation";
        public static final String TYPE_FILEALTERPRESERVATION = "typeFileAlterPreservation";
        public static final String TYPE_READONLY = "typeReadOnly";

        /**
         * Discard frame if tag altered
         */
        public static final int MASK_TAG_ALTER_PRESERVATION = FileConstants.BIT7;

        /**
         * Discard frame if audio file part  altered
         */
        public static final int MASK_FILE_ALTER_PRESERVATION = FileConstants.BIT6;

        /**
         * Frame tagged as read only
         */
        public static final int MASK_READ_ONLY = FileConstants.BIT5;

        public StatusFlags() {
            originalFlags = (byte) 0;
            writeFlags = (byte) 0;
        }

        StatusFlags(byte flags) {
            originalFlags = flags;
            writeFlags = flags;
            modifyFlags();
        }

        /**
         * Use this constructor when convert a v24 frame
         */
        StatusFlags(ID3v24Frame.StatusFlags statusFlags) {
            originalFlags = convertV4ToV3Flags(statusFlags.getOriginalFlags());
            writeFlags = originalFlags;
            modifyFlags();
        }

        private byte convertV4ToV3Flags(byte v4Flag) {
            byte v3Flag = (byte) 0;
            if ((v4Flag & ID3v24Frame.StatusFlags.MASK_FILE_ALTER_PRESERVATION) != 0) {
                v3Flag |= (byte) MASK_FILE_ALTER_PRESERVATION;
            }
            if ((v4Flag & ID3v24Frame.StatusFlags.MASK_TAG_ALTER_PRESERVATION) != 0) {
                v3Flag |= (byte) MASK_TAG_ALTER_PRESERVATION;
            }
            return v3Flag;
        }

        protected void modifyFlags() {
            String str = getIdentifier();
            if (ID3v23Frames.getInstanceOf().isDiscardIfFileAltered(str) == true) {
                writeFlags |= (byte) MASK_FILE_ALTER_PRESERVATION;
                writeFlags &= (byte) ~MASK_TAG_ALTER_PRESERVATION;
            } else {
                writeFlags &= (byte) ~MASK_FILE_ALTER_PRESERVATION;
                writeFlags &= (byte) ~MASK_TAG_ALTER_PRESERVATION;
            }
        }

        public void createStructure() {
            MP3File.getStructureFormatter().openHeadingElement(TYPE_FLAGS, "");
            MP3File.getStructureFormatter().addElement(TYPE_TAGALTERPRESERVATION, originalFlags & MASK_TAG_ALTER_PRESERVATION);
            MP3File.getStructureFormatter().addElement(TYPE_FILEALTERPRESERVATION, originalFlags & MASK_FILE_ALTER_PRESERVATION);
            MP3File.getStructureFormatter().addElement(TYPE_READONLY, originalFlags & MASK_READ_ONLY);
            MP3File.getStructureFormatter().closeHeadingElement(TYPE_FLAGS);
        }
    }

    /**
     * This represents a frame headers Encoding Flags
     */
    class EncodingFlags extends AbstractID3v2Frame.EncodingFlags {
        public static final String TYPE_COMPRESSION = "compression";
        public static final String TYPE_ENCRYPTION = "encryption";
        public static final String TYPE_GROUPIDENTITY = "groupidentity";

        /**
         * Frame is compressed
         */
        public static final int MASK_COMPRESSION = FileConstants.BIT7;

        /**
         * Frame is encrypted
         */
        public static final int MASK_ENCRYPTION = FileConstants.BIT6;

        /**
         * Frame is part of a group
         */
        public static final int MASK_GROUPING_IDENTITY = FileConstants.BIT5;

        public EncodingFlags() {
            super();
        }

        public EncodingFlags(byte flags) {
            super(flags);
            logEnabledFlags();
        }

        public void logEnabledFlags() {
            if (isCompression()) {
                //logger.warning(getLoggingFilename() + ":" + identifier + " is compressed");
            }

            if (isEncryption()) {
                //logger.warning(getLoggingFilename() + ":" + identifier + " is encrypted");
            }

            if (isGrouping()) {
                //logger.warning(getLoggingFilename() + ":" + identifier + " is grouped");
            }
        }

        public boolean isCompression() {
            return (flags & MASK_COMPRESSION) > 0;
        }

        public boolean isEncryption() {
            return (flags & MASK_ENCRYPTION) > 0;
        }

        public boolean isGrouping() {
            return (flags & MASK_GROUPING_IDENTITY) > 0;
        }

        public void createStructure() {
            MP3File.getStructureFormatter().openHeadingElement(TYPE_FLAGS, "");
            MP3File.getStructureFormatter().addElement(TYPE_COMPRESSION, flags & MASK_COMPRESSION);
            MP3File.getStructureFormatter().addElement(TYPE_ENCRYPTION, flags & MASK_ENCRYPTION);
            MP3File.getStructureFormatter().addElement(TYPE_GROUPIDENTITY, flags & MASK_GROUPING_IDENTITY);
            MP3File.getStructureFormatter().closeHeadingElement(TYPE_FLAGS);
        }
    }

    /**
     * Does the frame identifier meet the syntax for a idv3v2 frame identifier.
     * must start with a capital letter and only contain capital letters and numbers
     *
     * @param identifier to be checked
     * @return whether the identifier is valid
     */
    public boolean isValidID3v2FrameIdentifier(String identifier) {
        Matcher m = validFrameIdentifier.matcher(identifier);
        return m.matches();
    }

    /**
     * Return String Representation of body
     */
    public void createStructure() {
        MP3File.getStructureFormatter().openHeadingElement(TYPE_FRAME, getIdentifier());
        MP3File.getStructureFormatter().addElement(TYPE_FRAME_SIZE, frameSize);
        statusFlags.createStructure();
        encodingFlags.createStructure();
        frameBody.createStructure();
        MP3File.getStructureFormatter().closeHeadingElement(TYPE_FRAME);
    }

    /**
     * @return true if considered a common frame
     */
    public boolean isCommon() {
        return ID3v23Frames.getInstanceOf().isCommon(getId());
    }

    /**
     * @return true if considered a common frame
     */
    public boolean isBinary() {
        return ID3v23Frames.getInstanceOf().isBinary(getId());
    }
}
