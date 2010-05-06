/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package davaguine.jmac.info;

import davaguine.jmac.tools.ByteArrayReader;
import davaguine.jmac.tools.ByteArrayWriter;
import davaguine.jmac.tools.File;

import java.io.EOFException;
import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class ID3Tag {
    public String Header;		    // should equal 'TAG'
    public String Title;			// title
    public String Artist;		    // artist
    public String Album;			// album
    public String Year; 			// year
    public String Comment;  		// comment
    public short Track;	            // track
    public short Genre;	            // genre

    public static String defaultEncoding = "US-ASCII";

    public final static int ID3_TAG_BYTES = 128;

    public static ID3Tag read(final File file) throws IOException {
        file.seek(file.length() - ID3_TAG_BYTES);
        try {
            final ID3Tag tag = new ID3Tag();
            final ByteArrayReader reader = new ByteArrayReader(file, ID3_TAG_BYTES);
            tag.Header = reader.readString(3, defaultEncoding);
            tag.Title = reader.readString(30, defaultEncoding);
            tag.Artist = reader.readString(30, defaultEncoding);
            tag.Album = reader.readString(30, defaultEncoding);
            tag.Year = reader.readString(4, defaultEncoding);
            tag.Comment = reader.readString(29, defaultEncoding);
            tag.Track = reader.readUnsignedByte();
            tag.Genre = reader.readUnsignedByte();
            return tag.Header.equals("TAG") ? tag : null;
        } catch (EOFException e) {
            return null;
        }
    }

    public final void write(final ByteArrayWriter writer) {
        writer.writeString(Header, 3);
        writer.writeString(Title, 30);
        writer.writeString(Artist, 30);
        writer.writeString(Album, 30);
        writer.writeString(Year, 4);
        writer.writeString(Comment, 29);
        writer.writeUnsignedByte(Track);
        writer.writeUnsignedByte(Genre);
    }

    public static void setDefaultEncoding(String encoding) {
        defaultEncoding = encoding;
    }
}
