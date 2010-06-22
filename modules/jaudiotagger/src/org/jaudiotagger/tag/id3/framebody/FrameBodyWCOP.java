/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jaudiotagger.tag.id3.framebody;

import org.jaudiotagger.tag.InvalidTagException;
import org.jaudiotagger.tag.id3.ID3v24Frames;

import java.nio.ByteBuffer;

public class FrameBodyWCOP extends AbstractFrameBodyUrlLink implements ID3v24FrameBody, ID3v23FrameBody {
    /**
     * Creates a new FrameBodyWCOP datatype.
     */
    public FrameBodyWCOP() {
    }

    /**
     * Creates a new FrameBodyWCOP datatype.
     *
     * @param urlLink
     */
    public FrameBodyWCOP(String urlLink) {
        super(urlLink);
    }

    public FrameBodyWCOP(FrameBodyWCOP body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyWCOP datatype.
     *
     * @throws java.io.IOException
     * @throws InvalidTagException
     */
    public FrameBodyWCOP(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_URL_COPYRIGHT;
    }
}