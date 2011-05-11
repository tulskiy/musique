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

package com.tulskiy.musique.audio.formats.mp3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.ID3v11Tag;
import org.jaudiotagger.tag.id3.ID3v24Frame;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTPOS;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTRCK;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.audio.TagWriteException;
import com.tulskiy.musique.audio.formats.ape.APETagProcessor;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.util.Util;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 10, 2009
 */
public class MP3TagWriter extends AudioTagWriter {
    private APETagProcessor apeTagProcessor = new APETagProcessor();
    
    private final static List<FieldKey> trackDiscFieldKeys = new ArrayList<FieldKey>(
    		Arrays.asList(FieldKey.TRACK, FieldKey.TRACK_TOTAL, FieldKey.DISC_NO, FieldKey.DISC_TOTAL));

    @Override
	public void write(Track track) throws TagWriteException {
    	TrackData trackData = track.getTrackData();
        File file = trackData.getFile();
        TextEncoding.getInstanceOf().setDefaultNonUnicode(AudioFileReader.getDefaultCharset().name());

        if (/*song.getCustomHeaderField("hasApeTag") != null*/false) {
            try {
                apeTagProcessor.writeAPEv2Tag(track);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            MP3File mp3File;
            try {
                mp3File = new MP3File(file, MP3File.LOAD_ALL, false);

                ID3v24Tag id3v2tag = mp3File.getID3v2TagAsv24();
                if (id3v2tag == null) {
                    id3v2tag = new ID3v24Tag();
                }
                copyTagFields(id3v2tag, track);

                ID3v11Tag id3v1Tag = new ID3v11Tag(id3v2tag);
                mp3File.setID3v1Tag(id3v1Tag);
                mp3File.setID3v2Tag(id3v2tag);

                mp3File.commit();
            } catch (Exception e) {
                throw new TagWriteException(e);
            }
        }
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp3");
    }

    // @see AudioTagWriter#copyTagFields(Tag, AbstractTag, Track) as source
    public void copyTagFields(ID3v24Tag tag, Track track) throws KeyNotFoundException, FieldDataInvalidException {
    	String value;
    	boolean firstValue;

    	Iterator<Entry<FieldKey, Set<String>>> entries = track.getTrackData().getAllTagFieldValuesIterator();
		while (entries.hasNext()) {
			Entry<FieldKey, Set<String>> entry = entries.next();
			if (!trackDiscFieldKeys.contains(entry.getKey())) {
				Iterator<String> values = entry.getValue().iterator();
				firstValue = true;
				while (values.hasNext()) {
					value = values.next();
					if (firstValue) {
						tag.deleteField(entry.getKey());
						firstValue = false;
					}
					if (!Util.isEmpty(value)) {
						tag.addField(tag.createField(entry.getKey(), value));
					}
				}
			}
		}
		
		// workaround since track/tracktotal and disc/disctotal share same field
		handleTrackDiscFields(tag, track);
		
		track.getTrackData().removeEmptyTagFields();
    }
    
    private void handleTrackDiscFields(ID3v24Tag tag, Track track) throws FieldDataInvalidException, KeyNotFoundException {
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

    		TagField field = tag.getFirstField(FieldKey.TRACK_TOTAL);
			ID3v24Frame frame = (ID3v24Frame) field;
			FrameBodyTRCK body = (FrameBodyTRCK) frame.getBody();
			body.setTrackTotal(Integer.valueOf(trackData.getTrackTotal()));

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

    		TagField field = tag.getFirstField(FieldKey.DISC_TOTAL);
			ID3v24Frame frame = (ID3v24Frame) field;
			FrameBodyTPOS body = (FrameBodyTPOS) frame.getBody();
			body.setDiscTotal(Integer.valueOf(trackData.getDiscTotal()));

    		discFieldUpdated = true;
    	}
    	if (!discFieldUpdated) {
    		tag.deleteField(FieldKey.DISC_NO);
    	}
    }

}
