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

import davaguine.jmac.tools.ByteArrayWriter;
import davaguine.jmac.tools.JMACException;

import java.io.UnsupportedEncodingException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APETagField {

    public final static int TAG_FIELD_FLAG_READ_ONLY = (1 << 0);

    public final static int TAG_FIELD_FLAG_DATA_TYPE_MASK = (6);
    public final static int TAG_FIELD_FLAG_DATA_TYPE_TEXT_UTF8 = (0 << 1);
    public final static int TAG_FIELD_FLAG_DATA_TYPE_BINARY = (1 << 1);
    public final static int TAG_FIELD_FLAG_DATA_TYPE_EXTERNAL_INFO = (2 << 1);
    public final static int TAG_FIELD_FLAG_DATA_TYPE_RESERVED = (3 << 1);

    // create a tag field (use nFieldBytes = -1 for null-terminated strings)
    public APETagField(String pFieldName, byte[] pFieldValue) {
        this(pFieldName, pFieldValue, 0);
    }

    public APETagField(String pFieldName, byte[] pFieldValue, int nFlags) {
        m_spFieldName = pFieldName;
        m_spFieldValue = pFieldValue;
        m_nFieldFlags = nFlags;

        // data (we'll always allocate two extra bytes and memset to 0 so we're safely NULL terminated)
        m_spFieldValue = new byte[pFieldValue.length];
        System.arraycopy(pFieldValue, 0, m_spFieldValue, 0, pFieldValue.length);

        // flags
        m_nFieldFlags = nFlags;
    }

    public int GetFieldSize() {
        try {
            return m_spFieldName.getBytes("US-ASCII").length + 1 + m_spFieldValue.length + 4 + 4;
        } catch (UnsupportedEncodingException e) {
            throw new JMACException("Unsupported Encoding", e);
        }
    }

    // get the name of the field
    public String GetFieldName() {
        return m_spFieldName;
    }

    // get the value of the field
    public byte[] GetFieldValue() {
        return m_spFieldValue;
    }

    public int GetFieldValueSize() {
        return m_spFieldValue.length;
    }

    // get any special flags
    public int GetFieldFlags() {
        return m_nFieldFlags;
    }

    // output the entire field to a buffer (GetFieldSize() bytes)
    public int SaveField(ByteArrayWriter writer) {
        writer.writeInt(m_spFieldValue.length);
        writer.writeInt(m_nFieldFlags);
        writer.writeZString(m_spFieldName, "US-ASCII");
        writer.writeBytes(m_spFieldValue);

        return GetFieldSize();
    }

    // checks to see if the field is read-only
    public boolean GetIsReadOnly() {
        return (m_nFieldFlags & TAG_FIELD_FLAG_READ_ONLY) > 0;
    }

    public boolean GetIsUTF8Text() {
        return ((m_nFieldFlags & TAG_FIELD_FLAG_DATA_TYPE_MASK) == TAG_FIELD_FLAG_DATA_TYPE_TEXT_UTF8);
    }

    // set helpers (use with EXTREME caution)
    void SetFieldFlags(int nFlags) {
        m_nFieldFlags = nFlags;
    }

    private String m_spFieldName;
    private byte[] m_spFieldValue;
    private int m_nFieldFlags;
}
