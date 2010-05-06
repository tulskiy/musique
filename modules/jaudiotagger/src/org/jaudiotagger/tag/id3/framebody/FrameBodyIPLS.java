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
import org.jaudiotagger.tag.datatype.NumberHashMap;
import org.jaudiotagger.tag.datatype.PairedTextEncodedStringNullTerminated;
import org.jaudiotagger.tag.id3.ID3v23Frames;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

/**
 * Involved People List ID3v22/v23 Only
 * <p/>
 * Since there might be a lot of people contributing to an audio file in various ways, such as musicians and technicians,
 * the 'Text information frames' are often insufficient to list everyone involved in a project.
 * The 'Involved people list' is a frame containing the names of those involved, and how they were involved.
 * The body simply contains a terminated string with the involvement directly followed by a terminated string with
 * the involvee followed by a new involvement and so on. There may only be one "IPLS" frame in each tag.
 * <p/>
 * <Header for 'Involved people list', ID: "IPLS">
 * Text encoding	$xx
 * People list strings	<text strings according to encoding>
 * <p/>
 * <p>For more details, please refer to the ID3 specifications:
 * <ul>
 * <li><a href="http://www.id3.org/id3v2.3.0.txt">ID3 v2.3.0 Spec</a>
 * </ul>
 *
 * @author : Paul Taylor
 * @author : Eric Farng
 * @version $Id: FrameBodyIPLS.java,v 1.16 2008/11/12 16:41:38 paultaylor Exp $
 * @TODO currently just allows any number of values, should only really support pairs of values
 */
public class FrameBodyIPLS extends AbstractID3v2FrameBody implements ID3v23FrameBody {
    /**
     * Creates a new FrameBodyIPLS datatype.
     */
    public FrameBodyIPLS() {
        super();
        setObjectValue(DataTypes.OBJ_TEXT_ENCODING, TextEncoding.ISO_8859_1);
    }

    public FrameBodyIPLS(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v23 frame identifier
     *
     * @return the ID3v23 frame identifier for this frame type
     */
    public String getIdentifier() {
        return ID3v23Frames.FRAME_ID_V3_IPLS;
    }

    public FrameBodyIPLS(FrameBodyIPLS body) {
        super(body);
    }

    /**
     * Convert from V4 to V3 Frame
     */
    public FrameBodyIPLS(FrameBodyTIPL body) {
        setObjectValue(DataTypes.OBJ_TEXT_ENCODING, body.getTextEncoding());

        String valueAsCommaSeperatedString = (String) body.getObjectValue(DataTypes.OBJ_TEXT);

        PairedTextEncodedStringNullTerminated.ValuePairs value = new PairedTextEncodedStringNullTerminated.ValuePairs();
        StringTokenizer stz = new StringTokenizer(valueAsCommaSeperatedString, ",");
        while (stz.hasMoreTokens()) {
            value.add(stz.nextToken());
        }
        setObjectValue(DataTypes.OBJ_TEXT, value);
    }

    /**
     * Because have a text encoding we need to check the data values do not contain characters that cannot be encoded in
     * current encoding before we write data. If they do change the encoding.
     */
    public void write(ByteArrayOutputStream tagBuffer) {
        if (((PairedTextEncodedStringNullTerminated) getObject(DataTypes.OBJ_TEXT)).canBeEncoded() == false) {
            this.setTextEncoding(TextEncoding.UTF_16);
        }
        super.write(tagBuffer);
    }

    /**
     * Consists of a text encoding , and then a series of null terminated Util, there should be an even number
     * of Util as they are paired as involvement/involvee
     */
    protected void setupObjectList() {
        objectList.add(new NumberHashMap(DataTypes.OBJ_TEXT_ENCODING, this, TextEncoding.TEXT_ENCODING_FIELD_SIZE));
        objectList.add(new PairedTextEncodedStringNullTerminated(DataTypes.OBJ_TEXT, this));
    }

    /**
     * Get value at index
     *
     * @param index
     * @return value at index
     */
    public String getValueAtIndex(int index) {
        PairedTextEncodedStringNullTerminated text = (PairedTextEncodedStringNullTerminated) getObject(DataTypes.OBJ_TEXT);
        return text.getValue().getList().get(index);
    }

    /**
     * @return number of text values, shopuld be an even number because should make up pairs of values
     */
    public int getNumberOfValues() {
        PairedTextEncodedStringNullTerminated text = (PairedTextEncodedStringNullTerminated) getObject(DataTypes.OBJ_TEXT);
        return text.getValue().getNumberOfValues();
    }

    /**
     * @return number of text pairs
     */
    public int getNumberOfPairs() {
        PairedTextEncodedStringNullTerminated text = (PairedTextEncodedStringNullTerminated) getObject(DataTypes.OBJ_TEXT);
        return text.getValue().getNumberOfPairs();
    }
}
