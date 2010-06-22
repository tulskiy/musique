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

/**
 * Official artist/performer webpage URL link frames.
 * <p>The 'Official artist/performer webpage' frame is a URL pointing at the artists official webpage. There may be more than one "WOAR" frame in a tag if the audio contains more than one performer, but not with the same content.
 * <p/>
 * <p>For more details, please refer to the ID3 specifications:
 * <ul>
 * <li><a href="http://www.id3.org/id3v2.3.0.txt">ID3 v2.3.0 Spec</a>
 * </ul>
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodyWOAR.java,v 1.13 2008/07/21 10:45:46 paultaylor Exp $
 */
public class FrameBodyWOAR extends AbstractFrameBodyUrlLink implements ID3v24FrameBody, ID3v23FrameBody {
    /**
     * Creates a new FrameBodyWOAR datatype.
     */
    public FrameBodyWOAR() {
    }

    /**
     * Creates a new FrameBodyWOAR datatype.
     *
     * @param urlLink
     */
    public FrameBodyWOAR(String urlLink) {
        super(urlLink);
    }

    public FrameBodyWOAR(FrameBodyWOAR body) {
        super(body);
    }

    /**
     * Creates a new FrameBodyWOAR datatype.
     *
     * @throws InvalidTagException
     */
    public FrameBodyWOAR(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_URL_ARTIST_WEB;
    }
}
