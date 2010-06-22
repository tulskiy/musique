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
import org.jaudiotagger.tag.id3.ID3v23Frames;

import java.nio.ByteBuffer;

/**
 * Time Text information frame.
 * <p>The 'Time' frame is a numeric string in the HHMM format containing the time for the recording. This field is always four characters long.
 * <p>Deprecated in v2.4.0
 * <p/>
 * <p>For more details, please refer to the ID3 specifications:
 * <ul>
 * <li><a href="http://www.id3.org/id3v2.3.0.txt">ID3 v2.3.0 Spec</a>
 * </ul>
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodyTIME.java,v 1.13 2008/07/21 10:45:44 paultaylor Exp $
 */
public class FrameBodyTIME extends AbstractFrameBodyTextInfo implements ID3v23FrameBody {
    /**
     * Creates a new FrameBodyTIME datatype.
     */
    public FrameBodyTIME() {
    }

    public FrameBodyTIME(FrameBodyTIME body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyTIME datatype.
     *
     * @param textEncoding
     * @param text
     */
    public FrameBodyTIME(byte textEncoding, String text) {
        super(textEncoding, text);
    }

    /**
     * Creates a new FrameBodyTIME datatype.
     *
     * @throws InvalidTagException
     */
    public FrameBodyTIME(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v23Frames.FRAME_ID_V3_TIME;
    }
}