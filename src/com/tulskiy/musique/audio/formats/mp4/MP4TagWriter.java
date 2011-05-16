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

package com.tulskiy.musique.audio.formats.mp4;

import java.util.Iterator;
import java.util.Set;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4DiscNoField;
import org.jaudiotagger.tag.mp4.field.Mp4TrackField;

import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.audio.TagWriteException;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.util.Util;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 28, 2009
 */
public class MP4TagWriter extends AudioTagWriter {
    @Override
    public void write(Track track) throws TagWriteException {
        try {
            org.jaudiotagger.audio.AudioFile af1 = AudioFileIO.read(track.getTrackData().getFile());
            Tag abstractTag = af1.getTag();
            copyTagFields(abstractTag, new Mp4Tag(), track);
			// workaround since track/tracktotal and disc/disctotal share same field
            handleTrackDiscFields(abstractTag, track);
			// workaround since genre and genre custom field types used
            handleGenreFields(abstractTag, track);
            AudioFileIO.write(af1);
        } catch (Exception e) {
            throw new TagWriteException(e);
        }
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("m4a");
    }
    
    private void handleTrackDiscFields(Tag tag, Track track) throws FieldDataInvalidException, KeyNotFoundException {
    	boolean trackFieldUpdated = false;
    	boolean discFieldUpdated = false;
    	
    	TrackData trackData = track.getTrackData();
    	
    	if (!Util.isEmpty(trackData.getTrack())) {
    		tag.deleteField(FieldKey.TRACK);
    		tag.addField(tag.createField(FieldKey.TRACK, trackData.getTrack()));
    		trackFieldUpdated = true;
    	}
    	if (!Util.isEmpty(trackData.getTrackTotal())) {
    		if (!trackFieldUpdated) {
        		tag.deleteField(FieldKey.TRACK);
    			tag.addField(tag.createField(FieldKey.TRACK, "0"));
    		}

    		Mp4TrackField trackField = (Mp4TrackField) tag.getFirstField(FieldKey.TRACK_TOTAL);
    		trackField.setTrackTotal(Integer.valueOf(trackData.getTrackTotal()));

			trackFieldUpdated = true;
    	}
    	if (!trackFieldUpdated) {
    		tag.deleteField(FieldKey.TRACK);
    	}

    	if (!Util.isEmpty(trackData.getDisc())) {
    		tag.deleteField(FieldKey.DISC_NO);
    		tag.addField(tag.createField(FieldKey.DISC_NO, trackData.getDisc()));
    		discFieldUpdated = true;
    	}
    	if (!Util.isEmpty(trackData.getDiscTotal())) {
    		if (!discFieldUpdated) {
        		tag.deleteField(FieldKey.DISC_NO);
    			tag.addField(tag.createField(FieldKey.DISC_NO, "0"));
    		}

    		Mp4DiscNoField discField = (Mp4DiscNoField) tag.getFirstField(FieldKey.DISC_TOTAL);
    		discField.setDiscTotal(Integer.valueOf(trackData.getDiscTotal()));

    		discFieldUpdated = true;
    	}
    	if (!discFieldUpdated) {
    		tag.deleteField(FieldKey.DISC_NO);
    	}
    }

    private void handleGenreFields(Tag tag, Track track) throws FieldDataInvalidException, KeyNotFoundException {
    	Set<String> genres = track.getTrackData().getGenres();
    	if (genres != null && !genres.isEmpty()) {
	    	Mp4Tag mp4Tag = (Mp4Tag) tag;
	    	
	    	mp4Tag.deleteField(Mp4FieldKey.GENRE);
	    	mp4Tag.deleteField(Mp4FieldKey.GENRE_CUSTOM);
	    	
	    	Iterator<String> it = genres.iterator();
	    	while (it.hasNext()) {
	    		String value = it.next();
	    		if (!Util.isEmpty(value)) {
	    			mp4Tag.addField(FieldKey.GENRE, value);
	    		}
	    	}
    	}
    }

}
