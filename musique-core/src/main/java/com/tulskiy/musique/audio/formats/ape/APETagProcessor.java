/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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
import com.tulskiy.musique.util.Util;

import davaguine.jmac.info.APETag;
import davaguine.jmac.info.ID3Tag;
import davaguine.jmac.tools.RandomAccessFile;

import java.io.IOException;

import org.jaudiotagger.tag.FieldKey;

/**
 * Author: Denis Tulskiy
 * Date: 26.06.2009
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
            	setMusiqueTagFieldValue(tag, trackData, FieldKey.ARTIST, APETag.APE_TAG_FIELD_ARTIST);
            	setMusiqueTagFieldValue(tag, trackData, FieldKey.ALBUM, APETag.APE_TAG_FIELD_ALBUM);
            	setMusiqueTagFieldValue(tag, trackData, FieldKey.TITLE, APETag.APE_TAG_FIELD_TITLE);
            	setMusiqueTagFieldValue(tag, trackData, FieldKey.YEAR, APETag.APE_TAG_FIELD_YEAR);
            	setMusiqueTagFieldValue(tag, trackData, FieldKey.GENRE, APETag.APE_TAG_FIELD_GENRE);
            	setMusiqueTagFieldValue(tag, trackData, FieldKey.COMMENT, APETag.APE_TAG_FIELD_COMMENT);
            	setMusiqueTagFieldValue(tag, trackData, FieldKey.ALBUM_ARTIST, "album artist");
            	handleTrackDiscFields(tag, trackData);

            	setCustomMusiqueTagFieldValue(tag, trackData, FieldKey.RECORD_LABEL);
            	setCustomMusiqueTagFieldValue(tag, trackData, FieldKey.CATALOG_NO);
            	setCustomMusiqueTagFieldValue(tag, trackData, FieldKey.RATING);

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

    public void writeAPEv2Tag(Track track) throws IOException {
    	TrackData trackData = track.getTrackData();
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(trackData.getFile(), "rw");
            APETag tag = new APETag(ras, true);

            setApeTagFieldValue(tag, trackData, FieldKey.ARTIST, APETag.APE_TAG_FIELD_ARTIST);
            setApeTagFieldValue(tag, trackData, FieldKey.ALBUM, APETag.APE_TAG_FIELD_ALBUM);
            setApeTagFieldValue(tag, trackData, FieldKey.TITLE, APETag.APE_TAG_FIELD_TITLE);
            setApeTagFieldValue(tag, trackData, FieldKey.YEAR, APETag.APE_TAG_FIELD_YEAR);
            setApeTagFieldValue(tag, trackData, FieldKey.GENRE, APETag.APE_TAG_FIELD_GENRE);
            setApeTagFieldValue(tag, trackData, FieldKey.COMMENT, APETag.APE_TAG_FIELD_COMMENT);
            setApeTagFieldValue(tag, trackData, FieldKey.TRACK, APETag.APE_TAG_FIELD_TRACK);
            setApeTagFieldValue(tag, trackData, FieldKey.ALBUM_ARTIST, "Album Artist");

            setCustomApeTagFieldValue(tag, trackData, FieldKey.DISC_NO);
            setCustomApeTagFieldValue(tag, trackData, FieldKey.TRACK_TOTAL);
            setCustomApeTagFieldValue(tag, trackData, FieldKey.DISC_TOTAL);
            setCustomApeTagFieldValue(tag, trackData, FieldKey.RECORD_LABEL);
            setCustomApeTagFieldValue(tag, trackData, FieldKey.CATALOG_NO);
            setCustomApeTagFieldValue(tag, trackData, FieldKey.RATING);

            // TODO review this hardcoded const
            tag.SetFieldString("CUESHEET", trackData.getCueSheet());

            tag.Save();
    		
    		track.getTrackData().removeEmptyTagFields();
        } finally {
            if (ras != null)
                ras.close();
        }
    }

    private void setMusiqueTagFieldValue(APETag tag, TrackData trackData, FieldKey musiqueKey, String apeKey) throws IOException {
    	String value = tag.GetFieldString(apeKey);
    	if (value != null) {
    		trackData.setTagFieldValues(musiqueKey, value);
    	}
    }

    private void setApeTagFieldValue(APETag tag, TrackData trackData, FieldKey musiqueKey, String apeKey) throws IOException {
    	String value = trackData.getFirstTagFieldValue(musiqueKey);
    	if (!Util.isEmpty(value)) {
    		tag.SetFieldString(apeKey, value);
    	}
    	else if (tag.GetFieldString(apeKey) != null) {
    		tag.RemoveField(apeKey);
    	}
    }

    private void setCustomMusiqueTagFieldValue(APETag tag, TrackData trackData, FieldKey musiqueKey) throws IOException {
    	setMusiqueTagFieldValue(tag, trackData, musiqueKey, musiqueKey.toString());
    }

    private void setCustomApeTagFieldValue(APETag tag, TrackData trackData, FieldKey musiqueKey) throws IOException {
    	setApeTagFieldValue(tag, trackData, musiqueKey, musiqueKey.toString());
    }
    
    private void handleTrackDiscFields(APETag tag, TrackData trackData) throws IOException {
    	String value = tag.GetFieldString(APETag.APE_TAG_FIELD_TRACK);
    	if (!Util.isEmpty(value)) {
	    	if (!value.contains("/")) {
	    		setMusiqueTagFieldValue(tag, trackData, FieldKey.TRACK, APETag.APE_TAG_FIELD_TRACK);
	        	setCustomMusiqueTagFieldValue(tag, trackData, FieldKey.TRACK_TOTAL);
	    	}
	    	else {
	    		String[] parts = value.split("/");
	    		trackData.setTagFieldValues(FieldKey.TRACK, parts[0]);
	    		if (parts.length > 1) {
	    			trackData.setTagFieldValues(FieldKey.TRACK_TOTAL, parts[1]);
	    		}
	    	}
    	}

    	setCustomMusiqueTagFieldValue(tag, trackData, FieldKey.DISC_NO);
    	setCustomMusiqueTagFieldValue(tag, trackData, FieldKey.DISC_TOTAL);
    }

}
