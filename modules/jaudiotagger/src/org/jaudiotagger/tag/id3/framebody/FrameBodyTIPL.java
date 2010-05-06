/*
 * Copyright (c) 2008, 2009 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jaudiotagger.tag.id3.framebody;

import org.jaudiotagger.tag.InvalidTagException;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.datatype.PairedTextEncodedStringNullTerminated;
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.nio.ByteBuffer;

/**
 * The 'Involved people list' is intended as a mapping between functions like producer and names. Every odd field is a
 * function and every even is an name or a comma delimited list of names.
 * <p/>
 * TODO currently just reads the first String when directly from file, this will be fixed when we add support for
 * multiple Util for all ID3v24Frames
 * <p/>
 * TODO currently just reads all the values when converted from the corresponding ID3v23 Frame IPLS as a single value
 * (the individual fields from the IPLS frame will be seperated by commas)
 */
public class FrameBodyTIPL extends AbstractFrameBodyTextInfo implements ID3v24FrameBody {
    /**
     * Creates a new FrameBodyTIPL datatype.
     */
    public FrameBodyTIPL() {
    }

    public FrameBodyTIPL(FrameBodyTIPL body) {
        super(body);
    }

    /**
     * Convert from V3 to V4 Frame
     */
    public FrameBodyTIPL(FrameBodyIPLS body) {
        setObjectValue(DataTypes.OBJ_TEXT_ENCODING, body.getTextEncoding());

        PairedTextEncodedStringNullTerminated.ValuePairs value = (PairedTextEncodedStringNullTerminated.ValuePairs) body.getObjectValue(DataTypes.OBJ_TEXT);
        setObjectValue(DataTypes.OBJ_TEXT, value.toString());
    }

    /**
     * Creates a new FrameBodyTIPL datatype.
     *
     * @param textEncoding
     * @param text
     */
    public FrameBodyTIPL(byte textEncoding, String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTIPL datatype.
     *
     * @throws InvalidTagException
     */
    public FrameBodyTIPL(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_INVOLVED_PEOPLE;
    }
}
