package org.jaudiotagger.tag.datatype;

import org.jaudiotagger.tag.InvalidDataTypeException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;
import org.jaudiotagger.utils.EqualsUtil;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the form 01/10 whereby the second part is optional. This is used by frame such as TRCK and TPOS
 * <p/>
 * Some applications like to prepend the count with a zero to aid sorting, (i.e 02 comes before 10)
 */
@SuppressWarnings({"EmptyCatchBlock"})
public class PartOfSet extends AbstractString {
    /**
     * Creates a new empty  PartOfSet datatype.
     *
     * @param identifier identifies the frame type
     * @param frameBody
     */
    public PartOfSet(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    /**
     * Copy constructor
     *
     * @param object
     */
    public PartOfSet(PartOfSet object) {
        super(object);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof PartOfSet)) {
            return false;
        }

        PartOfSet that = (PartOfSet) obj;

        return EqualsUtil.areEqual(value, that.value);
    }

    /**
     * Read a 'n' bytes from buffer into a String where n is the framesize - offset
     * so thefore cannot use this if there are other objects after it because it has no
     * delimiter.
     * <p/>
     * Must take into account the text encoding defined in the Encoding Object
     * ID3 Text Frames often allow multiple strings seperated by the null char
     * appropriate for the encoding.
     *
     * @param arr    this is the buffer for the frame
     * @param offset this is where to start reading in the buffer for this field
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
//        logger.finest("Reading from array from offset:" + offset);

        //Get the Specified Decoder
        String charSetName = getTextEncodingCharSet();
        CharsetDecoder decoder = Charset.forName(charSetName).newDecoder();

        //Decode sliced inBuffer
        ByteBuffer inBuffer = ByteBuffer.wrap(arr, offset, arr.length - offset).slice();
        CharBuffer outBuffer = CharBuffer.allocate(arr.length - offset);
        decoder.reset();
        CoderResult coderResult = decoder.decode(inBuffer, outBuffer, true);
        if (coderResult.isError()) {
            //logger.warning("Decoding error:" + coderResult.toString());
        }
        decoder.flush(outBuffer);
        outBuffer.flip();

        //Store value
        String stringValue = outBuffer.toString();
        value = new PartOfSetValue(stringValue);

        //SetSize, important this is correct for finding the next datatype
        setSize(arr.length - offset);
        //logger.info("Read SizeTerminatedString:" + value + " size:" + size);
    }

    /**
     * Write String into byte array
     * <p/>
     * It will remove a trailing null terminator if exists if the option
     * RemoveTrailingTerminatorOnWrite has been set.
     *
     * @return the data as a byte array in format to write to file
     */
    public byte[] writeByteArray() {
        String value = getValue().toString();
        byte[] data;
        //Try and write to buffer using the CharSet defined by getTextEncodingCharSet()
        try {
            if (TagOptionSingleton.getInstance().isRemoveTrailingTerminatorOnWrite()) {
                if (value.length() > 0) {
                    if (value.charAt(value.length() - 1) == '\0') {
                        value = value.substring(0, value.length() - 1);
                    }
                }
            }

            String charSetName = getTextEncodingCharSet();
            if (charSetName.equals(TextEncoding.CHARSET_UTF_16)) {
                charSetName = TextEncoding.CHARSET_UTF_16_ENCODING_FORMAT;
                CharsetEncoder encoder = Charset.forName(charSetName).newEncoder();
                //Note remember LE BOM is ff fe but this is handled by encoder Unicode char is fe ff
                ByteBuffer bb = encoder.encode(CharBuffer.wrap('\ufeff' + value));
                data = new byte[bb.limit()];
                bb.get(data, 0, bb.limit());

            } else {
                CharsetEncoder encoder = Charset.forName(charSetName).newEncoder();
                ByteBuffer bb = encoder.encode(CharBuffer.wrap(value));
                data = new byte[bb.limit()];
                bb.get(data, 0, bb.limit());
            }
        }
        //Should never happen so if does throw a RuntimeException
        catch (CharacterCodingException ce) {
            logger.severe(ce.getMessage());
            throw new RuntimeException(ce);
        }
        setSize(data.length);
        return data;
    }

    /**
     * Get the text encoding being used.
     * <p/>
     * The text encoding is defined by the frame body that the text field belongs to.
     *
     * @return the text encoding charset
     */
    protected String getTextEncodingCharSet() {
        byte textEncoding = this.getBody().getTextEncoding();
        String charSetName = TextEncoding.getInstanceOf().getValueForId(textEncoding);
//        logger.finest("text encoding:" + textEncoding + " charset:" + charSetName);
        return charSetName;
    }

    /**
     * Holds data
     */
    public static class PartOfSetValue {
        private static final Pattern trackNoPatternWithTotalCount;
        private static final Pattern trackNoPattern;

        static {
            //Match track/total pattern allowing for extraneous nulls ectera at the end
            trackNoPatternWithTotalCount = Pattern.compile("([0-9]+)/([0-9]+)(.*)", Pattern.CASE_INSENSITIVE);
            trackNoPattern = Pattern.compile("([0-9]+)(.*)", Pattern.CASE_INSENSITIVE);
        }

        private static final String SEPARATOR = "/";
        private Integer count;
        private Integer total;
        private String extra;   //Any extraneous info such as null chars

        public PartOfSetValue() {

        }

        /**
         * When constructing from data
         *
         * @param value
         */
        public PartOfSetValue(String value) {

            Matcher m = trackNoPatternWithTotalCount.matcher(value);
            if (m.matches()) {
                this.count = Integer.parseInt(m.group(1));
                this.total = Integer.parseInt(m.group(2));
                this.extra = m.group(3);
                return;
            }

            m = trackNoPattern.matcher(value);
            if (m.matches()) {
                this.count = Integer.parseInt(m.group(1));
                this.extra = m.group(2);
            }
        }

        /**
         * Newly created
         *
         * @param count
         * @param total
         */
        public PartOfSetValue(Integer count, Integer total) {
            this.count = count;
            this.total = total;
        }


        public Integer getCount() {
            return count;
        }

        public Integer getTotal() {
            return total;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public void setTotal(Integer total) {
            this.total = total;

        }

        public void setCount(String count) {
            try {
                this.count = Integer.parseInt(count);
            } catch (NumberFormatException nfe) {

            }
        }

        public void setTotal(String total) {
            try {
                this.total = Integer.parseInt(total);
            } catch (NumberFormatException nfe) {

            }
        }

        public String toString() {
            //Don't Pad
            StringBuffer sb = new StringBuffer();
            if (!TagOptionSingleton.getInstance().isPadNumbers()) {
                if (count != null) {
                    sb.append(count.intValue());
                } else if (total != null) {
                    sb.append('0');
                }
                if (total != null) {
                    sb.append(SEPARATOR).append(total);
                }
                if (extra != null) {
                    sb.append(extra);
                }
            } else {
                if (count != null) {
                    if (count > 0 && count < 10) {
                        sb.append("0").append(count);
                    } else {
                        sb.append(count.intValue());
                    }
                } else if (total != null) {
                    sb.append('0');
                }
                if (total != null) {
                    if (total > 0 && total < 10) {
                        sb.append(SEPARATOR + "0").append(total);
                    } else {
                        sb.append(SEPARATOR).append(total);
                    }
                }
                if (extra != null) {
                    sb.append(extra);
                }
            }
            return sb.toString();
        }


        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof PartOfSetValue)) {
                return false;
            }

            PartOfSetValue that = (PartOfSetValue) obj;

            return
                    EqualsUtil.areEqual(getCount(), that.getCount()) &&
                            EqualsUtil.areEqual(getTotal(), that.getTotal());
        }

    }


    public PartOfSetValue getValue() {
        return (PartOfSetValue) value;
    }

    public String toString() {
        return value.toString();
    }
}