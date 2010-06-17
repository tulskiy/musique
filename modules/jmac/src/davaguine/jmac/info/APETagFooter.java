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
import davaguine.jmac.tools.JMACException;

import java.io.EOFException;
import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APETagFooter {
    public String m_cID;                // should equal 'APETAGEX' (char[8])
    public int m_nVersion;            // equals CURRENT_APE_TAG_VERSION (int)
    public int m_nSize;                // the complete size of the tag, including this footer (int)
    public int m_nFields;                // the number of fields in the tag (int)
    public int m_nFlags;                // the tag flags (none currently defined) (int)

    public final static int APE_TAG_FOOTER_BYTES = 32;

    public final static int CURRENT_APE_TAG_VERSION = 2000;

    APETagFooter() {
        this(0, 0);
    }

    APETagFooter(int nFields) {
        this(nFields, 0);
    }

    APETagFooter(int nFields, int nFieldBytes) {
        m_cID = "APETAGEX";
        m_nFields = nFields;
        m_nFlags = APETag.APE_TAG_FLAGS_DEFAULT;
        m_nSize = nFieldBytes + APE_TAG_FOOTER_BYTES;
        m_nVersion = CURRENT_APE_TAG_VERSION;
    }

    public int GetTotalTagBytes() {
        return m_nSize + (GetHasHeader() ? APE_TAG_FOOTER_BYTES : 0);
    }

    public int GetFieldBytes() {
        return m_nSize - APE_TAG_FOOTER_BYTES;
    }

    public int GetFieldsOffset() {
        return GetHasHeader() ? APE_TAG_FOOTER_BYTES : 0;
    }

    public int GetNumberFields() {
        return m_nFields;
    }

    public boolean GetHasHeader() {
        return (m_nFlags & APETag.APE_TAG_FLAG_CONTAINS_HEADER) > 0;
    }

    public boolean GetIsHeader() {
        return (m_nFlags & APETag.APE_TAG_FLAG_IS_HEADER) > 0;
    }

    public int GetVersion() {
        return m_nVersion;
    }

    public boolean GetIsValid(boolean bAllowHeader) {
        boolean bValid = m_cID.equals("APETAGEX") &&
                         (m_nVersion <= CURRENT_APE_TAG_VERSION) &&
                         (m_nFields <= 65536) &&
                         (GetFieldBytes() <= (1024 * 1024 * 16));

        if (bValid && !bAllowHeader && GetIsHeader())
            bValid = false;

        return bValid;
    }


    public static APETagFooter read(final File file, boolean skipId3) throws IOException {
        long pos = file.length() - APE_TAG_FOOTER_BYTES;
        if (skipId3) pos -= ID3Tag.ID3_TAG_BYTES;
        if (pos < 0)
            return null;
        file.seek(pos);
        APETagFooter tag = new APETagFooter();
        try {
            final ByteArrayReader reader = new ByteArrayReader(file, APE_TAG_FOOTER_BYTES);
            tag.m_cID = reader.readString(8, "US-ASCII");
            tag.m_nVersion = reader.readInt();
            tag.m_nSize = reader.readInt();
            tag.m_nFields = reader.readInt();
            tag.m_nFlags = reader.readInt();
            return tag;
        } catch (EOFException e) {
            throw new JMACException("Unsupported Format");
        }
    }

    public void write(final ByteArrayWriter writer) {
        writer.writeString(m_cID, 8);
        writer.writeInt(m_nVersion);
        writer.writeInt(m_nSize);
        writer.writeInt(m_nFields);
        writer.writeInt(m_nFlags);
        writer.writeInt(0);
        writer.writeInt(0);
    }
}
