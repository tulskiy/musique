/*
 *  MusicTag Copyright (C)2003,2004
 *
 *  This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser
 *  General Public  License as published by the Free Software Foundation; either version 2.1 of the License,
 *  or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 *  the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along with this library; if not,
 *  you can get a copy from http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jaudiotagger.tag.id3.framebody;

import org.jaudiotagger.tag.InvalidTagException;
import org.jaudiotagger.tag.datatype.ByteArraySizeTerminated;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.ID3v23Frames;

import java.nio.ByteBuffer;

/**
 * Relative volume adjustment frame.
 * <p/>
 * Only partially implemented.
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodyRVAD.java,v 1.17 2008/07/21 10:45:43 paultaylor Exp $
 */
public class FrameBodyRVAD extends AbstractID3v2FrameBody implements ID3v23FrameBody {

    /**
     * Creates a new FrameBodyRVAD datatype.
     */
    public FrameBodyRVAD() {

    }

    public FrameBodyRVAD(FrameBodyRVAD copyObject) {
        super(copyObject);

    }

    /**
     * Convert from V4 to V3 Frame
     */
    public FrameBodyRVAD(FrameBodyRVA2 body) {
        setObjectValue(DataTypes.OBJ_DATA, body.getObjectValue(DataTypes.OBJ_DATA));
    }

    /**
     * Creates a new FrameBodyRVAD datatype.
     *
     * @throws InvalidTagException if unable to create framebody from buffer
     */
    public FrameBodyRVAD(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v23Frames.FRAME_ID_V3_RELATIVE_VOLUME_ADJUSTMENT;
    }

    /**
     * This method is not yet supported.
     *
     * @throws java.lang.UnsupportedOperationException
     *          This method is not yet
     *          supported
     * @todo Implement this java.lang.Object method
     */
    public boolean equals(Object obj) {
        throw new java.lang.UnsupportedOperationException("Method equals() not yet implemented.");
    }

    /**
     * Setup the Object List. A byte Array which will be read upto frame size
     * bytes.
     */
    protected void setupObjectList() {
        objectList.add(new ByteArraySizeTerminated(DataTypes.OBJ_DATA, this));
    }
}
