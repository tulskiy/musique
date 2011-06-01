/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package javaFlacEncoder;

/**
 * The MetadataBlockHeader class is used to creat FLAC compliant Metadata Block
 * Headers. See the FLAC specification for more information.
 *
 * @author Preston Lacey
 */
public class MetadataBlockHeader {
    
    //boolean lastMetadataBlockFlag;//1 bit used
    //byte blockType;//7 bits used
    //int length;//24 bits used

    /**
     * Enum containing the different Metadata block types. See the FLAC spec
     * for more information on the various types.
     */
    public enum MetadataBlockType {
        /** A meta-block containing stream configuration information */
        STREAMINFO,
        /** A meta-block to pad the stream, allowing other meta-data to be
         * written in the future without re-writing the entire stream.
         */
        PADDING,
        /** Application meta-block*/
        APPLICATION,
        /** A meta-block which aids in seeking in the stream */
        SEEKTABLE,
        /** A meta-block for tags/comments */
        VORBIS_COMMENT,
        /** Cuesheet meta-block */
        CUESHEET,
        /** A meta-block to store an image, such as cover-art */
        PICTURE
    };

    /**
     * Constructor. This class defines no instance variables and only static
     * methods.
     */
    public MetadataBlockHeader() {

    }


    /**
     * Create a meta-data block header of the given type, and return the result
     * in a new EncodedElement(so data is ready to be placed directly in FLAC
     * stream)
     *
     * @param lastBlock True if this is the last meta-block in the stream. False
     * otherwise.
     *
     * @param type enum indicating which type of block we're creating.
     * @param length Length of the meta-data block which follows this header.
     * @return EncodedElement containing the header.
     */
    public static EncodedElement getMetadataBlockHeader(boolean lastBlock, 
            MetadataBlockType type, int length) {
        EncodedElement ele = new EncodedElement();
        byte[] data = new byte[4];
        
        int encodedLastBlock = (lastBlock) ? 1:0;
        int index = 0;
        EncodedElement.addInt(encodedLastBlock, 1, index, data);
        index++;
        int encodedType = 0;
        MetadataBlockType[] vals = MetadataBlockType.values();
        for(int i = 0; i < vals.length; i++) {
            if(vals[i] == type) {
                encodedType = i;
                break;
            }
        }
        EncodedElement.addInt(encodedType, 7, index, data);
        index +=7;
        EncodedElement.addInt(length, 24, index, data);
        index +=24;
        ele.setUsableBits(index);
        ele.setData(data);
        return ele;
    }
    
}
