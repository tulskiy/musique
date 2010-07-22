package org.jaudiotagger.tag.datatype;

import org.jaudiotagger.tag.InvalidDataTypeException;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;

/**
 * Represents a datatype that allow multiple Util but they should be paired, i.e should be 2,4,6.. Util
 * <p/>
 * TODO Pair restriction not currently implemented
 */
public class PairedTextEncodedStringNullTerminated extends MultipleTextEncodedStringNullTerminated {
    public PairedTextEncodedStringNullTerminated(String identifier, AbstractTagFrameBody frameBody) {
        super(identifier, frameBody);
        value = new PairedTextEncodedStringNullTerminated.ValuePairs();
    }

    public PairedTextEncodedStringNullTerminated(TextEncodedStringSizeTerminated object) {
        super(object);
        value = new PairedTextEncodedStringNullTerminated.ValuePairs();
    }

    public PairedTextEncodedStringNullTerminated(PairedTextEncodedStringNullTerminated object) {
        super(object);
    }

    /**
     * Read Null Terminated Util from the array starting at offset, continue until unable to find any null terminated
     * Util or until reached the end of the array. The offset should be set to byte after the last null terminated
     * String found.
     *
     * @param arr    to read the Util from
     * @param offset in the array to start reading from
     * @throws InvalidDataTypeException if unable to find any null terminated Util or if find odd number of Util
     */
    public void readByteArray(byte[] arr, int offset) throws InvalidDataTypeException {
        //logger.finer("Reading PairedTextEncodedStringNullTerminated from array from offset:" + offset);
        super.readByteArray(arr, offset);
        //logger.finer("Read PairedTextEncodedStringNullTerminated from array from offset:");
    }

    /**
     * This holds the values held by this PairedTextEncodedDatatype, always held as pairs of values
     */
    public static class ValuePairs extends MultipleTextEncodedStringNullTerminated.Values {
        public ValuePairs() {
            super();
        }

        /**
         * @return no of values
         */
        public int getNumberOfPairs() {
            if (this.getNumberOfValues() > 0) {
                return this.getNumberOfValues() / 2;
            }
            return 0;
        }
    }

    public ValuePairs getValue() {
        return (ValuePairs) value;
    }

}
