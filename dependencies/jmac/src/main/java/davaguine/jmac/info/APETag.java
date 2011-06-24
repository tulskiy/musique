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

import davaguine.jmac.tools.*;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class APETag implements Comparator {
    public final static String APE_TAG_FIELD_TITLE = "Title";
    public final static String APE_TAG_FIELD_ARTIST = "Artist";
    public final static String APE_TAG_FIELD_ALBUM = "Album";
    public final static String APE_TAG_FIELD_COMMENT = "Comment";
    public final static String APE_TAG_FIELD_YEAR = "Year";
    public final static String APE_TAG_FIELD_TRACK = "Track";
    public final static String APE_TAG_FIELD_GENRE = "Genre";
    public final static String APE_TAG_FIELD_COVER_ART_FRONT = "Cover Art (front)";
    public final static String APE_TAG_FIELD_NOTES = "Notes";
    public final static String APE_TAG_FIELD_LYRICS = "Lyrics";
    public final static String APE_TAG_FIELD_COPYRIGHT = "Copyright";
    public final static String APE_TAG_FIELD_BUY_URL = "Buy URL";
    public final static String APE_TAG_FIELD_ARTIST_URL = "Artist URL";
    public final static String APE_TAG_FIELD_PUBLISHER_URL = "Publisher URL";
    public final static String APE_TAG_FIELD_FILE_URL = "File URL";
    public final static String APE_TAG_FIELD_COPYRIGHT_URL = "Copyright URL";
    public final static String APE_TAG_FIELD_MJ_METADATA = "Media Jukebox Metadata";
    public final static String APE_TAG_FIELD_TOOL_NAME = "Tool Name";
    public final static String APE_TAG_FIELD_TOOL_VERSION = "Tool Version";
    public final static String APE_TAG_FIELD_PEAK_LEVEL = "Peak Level";
    public final static String APE_TAG_FIELD_REPLAY_GAIN_RADIO = "Replay Gain (radio)";
    public final static String APE_TAG_FIELD_REPLAY_GAIN_ALBUM = "Replay Gain (album)";
    public final static String APE_TAG_FIELD_COMPOSER = "Composer";
    public final static String APE_TAG_FIELD_KEYWORDS = "Keywords";

    /**
     * **************************************************************************************
     * Footer (and header) flags
     * ***************************************************************************************
     */
    public final static int APE_TAG_FLAG_CONTAINS_HEADER = (1 << 31);
    public final static int APE_TAG_FLAG_CONTAINS_FOOTER = (1 << 30);
    public final static int APE_TAG_FLAG_IS_HEADER = (1 << 29);

    public final static int APE_TAG_FLAGS_DEFAULT = APE_TAG_FLAG_CONTAINS_FOOTER;

    public final static String APE_TAG_GENRE_UNDEFINED = "Undefined";

    // create an APE tag
    // bAnalyze determines whether it will analyze immediately or on the first request
    // be careful with multiple threads / file pointer movement if you don't analyze immediately

    public APETag(File pIO) throws IOException {
        this(pIO, true);
    }

    public APETag(File pIO, boolean bAnalyze) throws IOException {
        m_spIO = pIO; // we don't own the IO source

        if (bAnalyze)
            Analyze();
    }

    public APETag(String pFilename) throws IOException {
        this(pFilename, true);
    }

    public APETag(String pFilename, boolean bAnalyze) throws IOException {
        m_spIO = new RandomAccessFile(new java.io.File(pFilename), "r");

        if (bAnalyze)
            Analyze();
    }

    // save the tag to the I/O source (bUseOldID3 forces it to save as an ID3v1.1 tag instead of an APE tag)

    public void Save() throws IOException {
        Save(false);
    }

    public void Save(boolean bUseOldID3) throws IOException {
        Remove(false);

        if (m_aryFields.size() <= 0)
            return;

        if (!bUseOldID3) {
            int z = 0;

            // calculate the size of the whole tag
            int nFieldBytes = 0;
            for (z = 0; z < m_aryFields.size(); z++)
                nFieldBytes += ((APETagField) m_aryFields.get(z)).GetFieldSize();

            // sort the fields
            SortFields();

            // build the footer
            APETagFooter APETagFooter = new APETagFooter(m_aryFields.size(), nFieldBytes);

            // make a buffer for the tag
            int nTotalTagBytes = APETagFooter.GetTotalTagBytes();

            // save the fields
            ByteArrayWriter writer = new ByteArrayWriter(nTotalTagBytes);
            for (z = 0; z < m_aryFields.size(); z++)
                ((APETagField) m_aryFields.get(z)).SaveField(writer);

            // add the footer to the buffer
            APETagFooter.write(writer);

            // dump the tag to the I/O source
            WriteBufferToEndOfIO(writer.getBytes());
        } else {
            // build the ID3 tag
            ID3Tag id3tag = new ID3Tag();
            CreateID3Tag(id3tag);
            ByteArrayWriter writer = new ByteArrayWriter(ID3Tag.ID3_TAG_BYTES);
            id3tag.write(writer);
            WriteBufferToEndOfIO(writer.getBytes());
        }
    }

    // removes any tags from the file (bUpdate determines whether is should re-analyze after removing the tag)

    public void Remove() throws IOException {
        Remove(true);
    }

    public void Remove(boolean bUpdate) throws IOException {
        // variables
        long nOriginalPosition = m_spIO.getFilePointer();

        boolean bID3Removed = true;
        boolean bAPETagRemoved = true;

        while (bID3Removed || bAPETagRemoved) {
            bID3Removed = false;
            bAPETagRemoved = false;

            // ID3 tag
            ID3Tag id3tag = ID3Tag.read(m_spIO);
            if (id3tag != null) {
                m_spIO.setLength(m_spIO.length() - ID3Tag.ID3_TAG_BYTES);
                bID3Removed = true;
            }

            // APE Tag
            APETagFooter footer = APETagFooter.read(m_spIO, false);
            if (footer.GetIsValid(true)) {
                m_spIO.setLength(m_spIO.length() - footer.GetTotalTagBytes());
                bAPETagRemoved = true;
            }

        }

        m_spIO.seek(nOriginalPosition);

        if (bUpdate)
            Analyze();
    }

    public void SetFieldString(String pFieldName, String pFieldValue) throws IOException {
        // remove if empty
//        if ((pFieldValue == null) || (pFieldValue.length() <= 0))
//            RemoveField(pFieldName);
        if (pFieldValue == null) {
            pFieldValue = "";
        }

        byte[] fieldValue = pFieldValue.getBytes("UTF-8");
        byte[] value = new byte[fieldValue.length];
        System.arraycopy(fieldValue, 0, value, 0, fieldValue.length);
        SetFieldBinary(pFieldName, value, APETagField.TAG_FIELD_FLAG_DATA_TYPE_TEXT_UTF8);
    }

    public void SetFieldBinary(String pFieldName, byte[] pFieldValue, int nFieldFlags) throws IOException {
        if (!m_bAnalyzed)
            Analyze();

        if (pFieldName == null)
            return;

        // check to see if we're trying to remove the field (by setting it to NULL or an empty string)
        boolean bRemoving = (pFieldValue == null) || (pFieldValue.length <= 0);

        // get the index
        int nFieldIndex = GetTagFieldIndex(pFieldName);
        if (nFieldIndex >= 0) {
            // existing field

            // fail if we're read-only (and not ignoring the read-only flag)
            if ((!m_bIgnoreReadOnly) && ((APETagField) m_aryFields.get(nFieldIndex)).GetIsReadOnly())
                return;

            // erase the existing field
            if (bRemoving)
                RemoveField(nFieldIndex);

            m_aryFields.set(nFieldIndex, new APETagField(pFieldName, pFieldValue, nFieldFlags));
        } else {
            if (bRemoving)
                return;

            m_aryFields.add(new APETagField(pFieldName, pFieldValue, nFieldFlags));
        }
    }

    // gets the value of a field (returns -1 and an empty buffer if the field doesn't exist)

    public byte[] GetFieldBinary(String pFieldName) throws IOException {
        if (!m_bAnalyzed)
            Analyze();

        APETagField pAPETagField = GetTagField(pFieldName);
        if (pAPETagField == null)
            return null;
        else
            return pAPETagField.GetFieldValue();
    }

    public String GetFieldString(String pFieldName) throws IOException {
        if (!m_bAnalyzed)
            Analyze();

        String ret = null;

        APETagField pAPETagField = GetTagField(pFieldName);
        if (pAPETagField != null) {
            byte[] b = pAPETagField.GetFieldValue();
            int boundary = 0;
            int index = b.length - 1;
            while (index >= 0 && b[index] == 0) {
                index--;
                boundary--;
            }
            if (index < 0)
                ret = "";
            else {
                if (pAPETagField.GetIsUTF8Text() || (m_nAPETagVersion < 2000)) {
//                    if (m_nAPETagVersion >= 2000)
                    ret = new String(b, 0, b.length + boundary, "UTF-8");
//                    else
//                        ret = new String(b, 0, b.length + boundary);
                } else
                    ret = new String(b, 0, b.length + boundary, "UTF-16");
            }
        }
        return ret;
    }

    // remove a specific field

    public void RemoveField(String pFieldName) throws IOException {
        RemoveField(GetTagFieldIndex(pFieldName));
    }

    public void RemoveField(int nIndex) {
        m_aryFields.remove(nIndex);
    }

    // clear all the fields

    public void ClearFields() {
        m_aryFields.clear();
    }

    // get the total tag bytes in the file from the last analyze
    // need to call Save() then Analyze() to update any changes

    public int GetTagBytes() throws IOException {
        if (!m_bAnalyzed)
            Analyze();

        return m_nTagBytes;
    }

    // see whether the file has an ID3 or APE tag

    public boolean GetHasID3Tag() throws IOException {
        if (!m_bAnalyzed)
            Analyze();
        return m_bHasID3Tag;
    }

    public boolean GetHasAPETag() throws IOException {
        if (!m_bAnalyzed)
            Analyze();
        return m_bHasAPETag;
    }

    public int GetAPETagVersion() throws IOException {
        return GetHasAPETag() ? m_nAPETagVersion : -1;
    }

    // gets a desired tag field (returns NULL if not found)
    // again, be careful, because this a pointer to the actual field in this class

    public APETagField GetTagField(String pFieldName) throws IOException {
        int nIndex = GetTagFieldIndex(pFieldName);
        return (nIndex != -1) ? (APETagField) m_aryFields.get(nIndex) : null;
    }

    public APETagField GetTagField(int nIndex) throws IOException {
        if (!m_bAnalyzed)
            Analyze();

        if ((nIndex >= 0) && (nIndex < m_aryFields.size()))
            return (APETagField) m_aryFields.get(nIndex);

        return null;
    }

    public void SetIgnoreReadOnly(boolean bIgnoreReadOnly) {
        m_bIgnoreReadOnly = bIgnoreReadOnly;
    }

    // fills in an ID3_TAG using the current fields (useful for quickly converting the tag)

    public void CreateID3Tag(ID3Tag pID3Tag) throws IOException {
        if (pID3Tag == null)
            return;

        if (!m_bAnalyzed)
            Analyze();

        if (m_aryFields.size() <= 0)
            return;

        pID3Tag.Header = "TAG";
        pID3Tag.Artist = GetFieldID3String(APE_TAG_FIELD_ARTIST);
        pID3Tag.Album = GetFieldID3String(APE_TAG_FIELD_ALBUM);
        pID3Tag.Title = GetFieldID3String(APE_TAG_FIELD_TITLE);
        pID3Tag.Comment = GetFieldID3String(APE_TAG_FIELD_COMMENT);
        pID3Tag.Year = GetFieldID3String(APE_TAG_FIELD_YEAR);
        String track = GetFieldString(APE_TAG_FIELD_TRACK);
        try {
            pID3Tag.Track = Short.parseShort(track);
        } catch (Exception e) {
            pID3Tag.Track = 255;
        }
        pID3Tag.Genre = (short) (new ID3Genre(GetFieldString(APE_TAG_FIELD_GENRE)).getGenre());
    }

    // private functions

    private void Analyze() throws IOException {
        // clean-up
        ClearFields();
        m_nTagBytes = 0;

        m_bAnalyzed = true;

        // store the original location
        long nOriginalPosition = m_spIO.getFilePointer();

        // check for a tag
        m_bHasID3Tag = false;
        m_bHasAPETag = false;
        m_nAPETagVersion = -1;
        final ID3Tag tag = ID3Tag.read(m_spIO);

        if (tag != null) {
            m_bHasID3Tag = true;
            m_nTagBytes += ID3Tag.ID3_TAG_BYTES;
        }

        // set the fields
        if (m_bHasID3Tag) {
            SetFieldID3String(APE_TAG_FIELD_ARTIST, tag.Artist);
            SetFieldID3String(APE_TAG_FIELD_ALBUM, tag.Album);
            SetFieldID3String(APE_TAG_FIELD_TITLE, tag.Title);
            SetFieldID3String(APE_TAG_FIELD_COMMENT, tag.Comment);
            SetFieldID3String(APE_TAG_FIELD_YEAR, tag.Year);
            SetFieldString(APE_TAG_FIELD_TRACK, String.valueOf(tag.Track));

            if ((tag.Genre == ID3Genre.GENRE_UNDEFINED) || (tag.Genre >= ID3Genre.genreCount())) {
//                SetFieldString(APE_TAG_FIELD_GENRE, APE_TAG_GENRE_UNDEFINED);
            } else
                SetFieldString(APE_TAG_FIELD_GENRE, ID3Genre.genreString(tag.Genre));
        }

        m_spIO.seek(nOriginalPosition);
        // try loading the APE tag
        m_footer = APETagFooter.read(m_spIO, m_bHasID3Tag);
        if (m_footer != null && m_footer.GetIsValid(false)) {
            m_bHasAPETag = true;
            m_nAPETagVersion = m_footer.GetVersion();

            int nRawFieldBytes = m_footer.GetFieldBytes();
            m_nTagBytes += m_footer.GetTotalTagBytes();

            long pos = m_spIO.length() - m_footer.GetTotalTagBytes() - m_footer.GetFieldsOffset();
            if (m_bHasID3Tag)
                pos -= ID3Tag.ID3_TAG_BYTES;
            m_spIO.seek(pos);

            try {
                final ByteArrayReader reader = new ByteArrayReader(m_spIO, nRawFieldBytes);

                // parse out the raw fields
                for (int z = 0; z < m_footer.GetNumberFields(); z++)
                    LoadField(reader);
            } catch (EOFException e) {
                throw new JMACException("Can't Read APE Tag Fields");
            }
        }

        // restore the file pointer
        m_spIO.seek(nOriginalPosition);
    }

    private int GetTagFieldIndex(String pFieldName) throws IOException {
        if (!m_bAnalyzed)
            Analyze();
        if (pFieldName == null) return -1;

        for (int z = 0; z < m_aryFields.size(); z++) {
            if (pFieldName.toLowerCase().equals(((APETagField) m_aryFields.get(z)).GetFieldName().toLowerCase()))
                return z;
        }

        return -1;
    }

    private void WriteBufferToEndOfIO(byte[] pBuffer) throws IOException {
        long nOriginalPosition = m_spIO.getFilePointer();
        m_spIO.seek(m_spIO.length());
        m_spIO.write(pBuffer);
        m_spIO.seek(nOriginalPosition);
    }

    private void LoadField(ByteArrayReader reader) throws IOException {
        // size and flags
        int nFieldValueSize = reader.readInt();
        int nFieldFlags = reader.readInt();

        String fieldName = reader.readString("UTF-8");

        // value
        byte[] fieldValue = new byte[nFieldValueSize];
        reader.readFully(fieldValue);

        // set
        SetFieldBinary(fieldName, fieldValue, nFieldFlags);
    }

    private void SortFields() {
        // sort the tag fields by size (so that the smallest fields are at the front of the tag)
        Arrays.sort(m_aryFields.toArray(), this);
    }

    public int compare(Object pA, Object pB) {
        APETagField pFieldA = (APETagField) pA;
        APETagField pFieldB = (APETagField) pB;

        return pFieldA.GetFieldSize() - pFieldB.GetFieldSize();
    }

    // helper set / get field functions

    private String GetFieldID3String(String pFieldName) throws IOException {
        return GetFieldString(pFieldName);
    }

    private void SetFieldID3String(String pFieldName, String pFieldValue) throws IOException {
        SetFieldString(pFieldName, pFieldValue.trim());
    }

    public APETagFooter getFooter() {
        return m_footer;
    }

    // private data
    private File m_spIO;
    private boolean m_bAnalyzed = false;
    private int m_nTagBytes = 0;
    private List m_aryFields = new ArrayList();
    private boolean m_bHasAPETag;
    private int m_nAPETagVersion;
    private boolean m_bHasID3Tag;
    private boolean m_bIgnoreReadOnly = false;
    private APETagFooter m_footer = null;
}
