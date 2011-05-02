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

package com.tulskiy.musique.audio.formats.ape;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;

import davaguine.jmac.info.APETag;
import davaguine.jmac.info.ID3Tag;
import davaguine.jmac.tools.RandomAccessFile;

import java.io.IOException;

import org.jaudiotagger.tag.TagFieldKey;

/**
 * @Author: Denis Tulskiy
 * @Date: 26.06.2009
 */
public class APETagProcessor {

    public APETagProcessor() {
        ID3Tag.setDefaultEncoding("windows-1251");
    }

    public boolean readAPEv2Tag(Track track) throws IOException {
    	TrackData trackData = track.getTrackData();
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(trackData.getFile(), "r");
            APETag tag = new APETag(ras, true);
            if (tag.GetHasAPETag() || tag.GetHasID3Tag()) {
            	trackData.setTagFieldValues(TagFieldKey.ARTIST, tag.GetFieldString(APETag.APE_TAG_FIELD_ARTIST));
            	trackData.setTagFieldValues(TagFieldKey.ALBUM, tag.GetFieldString(APETag.APE_TAG_FIELD_ALBUM));
            	trackData.setTagFieldValues(TagFieldKey.TITLE, tag.GetFieldString(APETag.APE_TAG_FIELD_TITLE));
            	trackData.setTagFieldValues(TagFieldKey.YEAR, tag.GetFieldString(APETag.APE_TAG_FIELD_YEAR));
            	trackData.setTagFieldValues(TagFieldKey.GENRE, tag.GetFieldString(APETag.APE_TAG_FIELD_GENRE));
            	trackData.setTagFieldValues(TagFieldKey.COMMENT, tag.GetFieldString(APETag.APE_TAG_FIELD_COMMENT));
            	trackData.setTagFieldValues(TagFieldKey.TRACK, tag.GetFieldString(APETag.APE_TAG_FIELD_TRACK));

            	setCustomMusiqueTagFieldValue(tag, trackData, TagFieldKey.DISC_NO);
            	// TODO add TRACK/DISC TOTAL fields
            	setCustomMusiqueTagFieldValue(tag, trackData, TagFieldKey.ALBUM_ARTIST);
            	setCustomMusiqueTagFieldValue(tag, trackData, TagFieldKey.RECORD_LABEL);
            	setCustomMusiqueTagFieldValue(tag, trackData, TagFieldKey.CATALOG_NO);

            	trackData.setCueSheet(tag.GetFieldString("CUESHEET"));
                if (tag.GetHasAPETag())
                    return tag.GetHasAPETag();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ras != null)
                ras.close();
        }
        return false;
    }

    public boolean writeAPEv2Tag(Track track) throws IOException {
    	TrackData trackData = track.getTrackData();
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(trackData.getFile(), "rw");
            APETag tag = new APETag(ras, true);
            tag.SetFieldString(APETag.APE_TAG_FIELD_ARTIST, trackData.getFirstTagFieldValue(TagFieldKey.ARTIST));
            tag.SetFieldString(APETag.APE_TAG_FIELD_ALBUM, trackData.getFirstTagFieldValue(TagFieldKey.ALBUM));
            tag.SetFieldString(APETag.APE_TAG_FIELD_TITLE, trackData.getFirstTagFieldValue(TagFieldKey.TITLE));
            tag.SetFieldString(APETag.APE_TAG_FIELD_YEAR, trackData.getFirstTagFieldValue(TagFieldKey.YEAR));
            tag.SetFieldString(APETag.APE_TAG_FIELD_GENRE, trackData.getFirstTagFieldValue(TagFieldKey.GENRE));
            tag.SetFieldString(APETag.APE_TAG_FIELD_COMMENT, trackData.getFirstTagFieldValue(TagFieldKey.COMMENT));
            tag.SetFieldString(APETag.APE_TAG_FIELD_TRACK, trackData.getFirstTagFieldValue(TagFieldKey.TRACK));

            setCustomApeTagFieldValue(tag, trackData, TagFieldKey.DISC_NO);
        	// TODO add TRACK/DISC TOTAL fields
            setCustomApeTagFieldValue(tag, trackData, TagFieldKey.ALBUM_ARTIST);
            setCustomApeTagFieldValue(tag, trackData, TagFieldKey.RECORD_LABEL);
            setCustomApeTagFieldValue(tag, trackData, TagFieldKey.CATALOG_NO);

            tag.SetFieldString("CUESHEET", trackData.getCueSheet());

            tag.Save();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ras != null)
                ras.close();
        }
        return false;
    }

    private void setCustomMusiqueTagFieldValue(APETag tag, TrackData trackData, TagFieldKey key) throws IOException {
    	trackData.setTagFieldValues(key, tag.GetFieldString(key.toString()));
    }

    private void setCustomApeTagFieldValue(APETag tag, TrackData trackData, TagFieldKey key) throws IOException {
    	tag.SetFieldString(key.toString(), trackData.getFirstTagFieldValue(key));
    }

}
