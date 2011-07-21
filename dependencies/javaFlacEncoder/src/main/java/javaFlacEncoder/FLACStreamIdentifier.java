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
 * Provides the stream identifier used at beginning of flac streams.
 * @author Preston Lacey
 */
public class FLACStreamIdentifier {
    static final byte streamMarkerByte1 = 0x66;
    static final byte streamMarkerByte2 = 0x4c;
    static final byte streamMarkerByte3 = 0x61;
    static final byte streamMarkerByte4 = 0x43;
    static final byte[] marker = { streamMarkerByte1,
                            streamMarkerByte2,
                            streamMarkerByte3,
                            streamMarkerByte4,
    };

    /**
     * Get an EncodedElement containing the marker(which is itself in a byte
     * array).
     * @return EncodedElement containing the marker. 
     */
    public static EncodedElement getIdentifier() {
        EncodedElement ele = new EncodedElement();
        ele.setData(marker.clone());
        ele.setUsableBits(32);
        return ele;
    }
        
}
