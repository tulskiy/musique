package org.jaudiotagger.tag.datatype;

import org.jaudiotagger.tag.InvalidDataTypeException;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a datatype that supports multiple terminated Util (there may only be one)
 */
public class MultipleTextEncodedStringNullTerminated extends AbstractDataType {

    /**
     * Creates a new ObjectStringSizeTerminated datatype.
     *
     * @param identifier identifies the frame type
     */
    public MultipleTextEncodedStringNullTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
        value = new MultipleTextEncodedStringNullTerminated.Values();
    }

    public MultipleTextEncodedStringNullTerminated(TextEncodedStringSizeTerminated object) {
        super(object);
        value = new MultipleTextEncodedStringNullTerminated.Values();
    }

    public MultipleTextEncodedStringNullTerminated(MultipleTextEncodedStringNullTerminated object) {
        super(object);
    }

    public boolean equals(Object obj) {
        if (obj instanceof MultipleTextEncodedStringNullTerminated == false) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns the size in bytes of this datatype when written to file
     *
     * @return size of this datatype
     */
    public int getSize() {
        return size;
    }

    /**
     * Check the value can be encoded with the specified encoding
     */
    public boolean canBeEncoded() {
        for (ListIterator<String> li = ((Values) value).getList().listIterator(); li.hasNext();) {
            TextEncodedStringNullTerminated next = new TextEncodedStringNullTerminated(identifier, frameBody, li.next());
            if (!next.canBeEncoded()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Read Null Terminated Util from the array starting at offset, continue until unable to find any null terminated
     * Util or until reached the end of the array. The offset should be set to byte after the last null terminated
     * String found.
     *
     * @param arr    to read the Util from
     * @param offset in the array to start reading from
     * @throws InvalidDataTypeException if unable to find any null terminated Util
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        //logger.finer("Reading MultipleTextEncodedStringNullTerminated from array from offset:" + offset);
        //Continue until unable to read a null terminated String
        while (true) {
            try {
                //Read String
                TextEncodedStringNullTerminated next = new TextEncodedStringNullTerminated(identifier, frameBody);
                next.readByteArray(arr, offset);

                if (next.getSize() == 0) {
                    break;
                } else {
                    //Add to value
                    ((Values) value).add((String) next.getValue());

                    //Add to size calculation
                    size += next.getSize();

                    //Increment Offset to start of next datatype.
                    offset += next.getSize();
                }
            }
            catch (InvalidDataTypeException idte) {
                break;
            }

            if (size == 0) {
                //logger.warning("No null terminated Util found");
                throw new InvalidDataTypeException("No null terminated Strings found");
            }
        }
        //logger.finer("Read  MultipleTextEncodedStringNullTerminated:" + value + " size:" + size);
    }

    /**
     * For every String write to bytebuffer
     *
     * @return bytebuffer that should be written to file to persist this datatype.
     */
    public byte[] writeByteArray() {
        //logger.finer("Writing MultipleTextEncodedStringNullTerminated");

        int localSize = 0;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            for (ListIterator<String> li = ((Values) value).getList().listIterator(); li.hasNext();) {
                TextEncodedStringNullTerminated next = new TextEncodedStringNullTerminated(identifier, frameBody, li.next());
                buffer.write(next.writeByteArray());
                localSize += next.getSize();
            }
        }
        catch (IOException ioe) {
            //This should never happen because the write is internal with the JVM it is not to a file
            //logger.log(Level.SEVERE, "IOException in MultipleTextEncodedStringNullTerminated when writing byte array", ioe);
            throw new RuntimeException(ioe);
        }

        //Update size member variable
        size = localSize;

        //logger.finer("Written MultipleTextEncodedStringNullTerminated");
        return buffer.toByteArray();
    }

    /**
     * This holds the values held by a MultipleTextEncodedDatatype
     */
    public static class Values {
        private List<String> valueList = new ArrayList<String>();

        public Values() {

        }

        /**
         * Add String Datatype to the value list
         *
         * @param value to add to the list
         */
        public void add(String value) {
            valueList.add(value);
        }

        /**
         * Return the list of values
         *
         * @return the list of values
         */
        public List<String> getList() {
            return valueList;
        }

        /**
         * @return no of values
         */
        public int getNumberOfValues() {
            return valueList.size();
        }

        /**
         * Return the list of values as a single string seperated by a comma
         *
         * @return a string representation of the value
         */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            for (ListIterator<String> li = valueList.listIterator(); li.hasNext();) {
                String next = li.next();
                sb.append(next);
                if (li.hasNext()) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
    }
}
