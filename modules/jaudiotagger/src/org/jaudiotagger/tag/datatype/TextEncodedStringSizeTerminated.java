package org.jaudiotagger.tag.datatype;

import org.jaudiotagger.tag.InvalidDataTypeException;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a String which is not delimited by null character.
 * <p/>
 * This type of String will usually only be used when it is the last field within a frame, when reading the remainder of
 * the byte array will be read, when writing the frame will be accomodate the required size for the String. The String
 * will be encoded based upon the text encoding of the frame that it belongs to.
 * <p/>
 * All TextInformation frames support multiple strings, stored as a null separated list, where null is represented by
 * the termination code for the character encoding. This functionality is only officially support in ID3v24.  Itunes
 * write null terminators characters even though only writes a single value.
 */
public class TextEncodedStringSizeTerminated extends AbstractString {

    /**
     * Creates a new empty TextEncodedStringSizeTerminated datatype.
     *
     * @param identifier identifies the frame type
     */
    public TextEncodedStringSizeTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
    }

    /**
     * Copy constructor
     *
     * @param object
     */
    public TextEncodedStringSizeTerminated(TextEncodedStringSizeTerminated object) {
        super(object);
    }

    public boolean equals(Object obj) {
        if (obj instanceof TextEncodedStringSizeTerminated == false) {
            return false;
        }
        return super.equals(obj);
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
        //logger.finest("Reading from array from offset:" + offset);

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
        value = outBuffer.toString();

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
        byte[] data = null;
        //Try and write to buffer using the CharSet defined by getTextEncodingCharSet()
        try {
            if (TagOptionSingleton.getInstance().isRemoveTrailingTerminatorOnWrite()) {
                String stringValue = (String) value;
                if (stringValue.length() > 0) {
                    if (stringValue.charAt(stringValue.length() - 1) == '\0') {
                        stringValue = (stringValue).substring(0, stringValue.length() - 1);
                        value = stringValue;
                    }
                }
            }

            String charSetName = getTextEncodingCharSet();
            if (charSetName.equals(TextEncoding.CHARSET_UTF_16)) {
                charSetName = TextEncoding.CHARSET_UTF_16_ENCODING_FORMAT;
                CharsetEncoder encoder = Charset.forName(charSetName).newEncoder();
                //Note remember LE BOM is ff fe but tis is handled by encoder Unicode char is fe ff
                ByteBuffer bb = encoder.encode(CharBuffer.wrap('\ufeff' + (String) value));
                data = new byte[bb.limit()];
                bb.get(data, 0, bb.limit());

            } else {
                CharsetEncoder encoder = Charset.forName(charSetName).newEncoder();
                ByteBuffer bb = encoder.encode(CharBuffer.wrap((String) value));
                data = new byte[bb.limit()];
                bb.get(data, 0, bb.limit());
            }
        }
        //Should never happen so if does throw a RuntimeException
        catch (CharacterCodingException ce) {
            //logger.severe(ce.getMessage());
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
        //logger.finest("text encoding:" + textEncoding + " charset:" + charSetName);
        return charSetName;
    }

    /**
     * Split the values seperated by null character
     *
     * @param value the raw value
     * @return list of values, guaranteed to be at least one value
     */
    private static List splitByNullSeperator(String value) {
        String[] valuesarray = value.split("\\u0000");
        List values = Arrays.asList(valuesarray);
        //Read only list so if empty have to create new list
        if (values.size() == 0) {
            values = new ArrayList(1);
            values.add("");
        }
        return values;
    }

    /**
     * Add an additional String to the current String value
     *
     * @param value
     */
    public void addValue(String value) {
        setValue(this.value + "\u0000" + value);
    }

    /**
     * How many values are held, each value is seperated by a null terminator
     *
     * @return number of values held, usually this will be one.
     */
    public int getNumberOfValues() {
        return splitByNullSeperator(((String) value)).size();
    }

    /**
     * Get the nth value
     *
     * @param index
     * @return the nth value
     * @throws IndexOutOfBoundsException if value does not exist
     */
    public String getValueAtIndex(int index) {
        //Split String into seperate components
        List values = splitByNullSeperator((String) value);
        return (String) values.get(index);
    }
}
