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
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.ID3v11Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
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
    	TagField field;
    	String value;
    	boolean firstValue;

    	Iterator<Entry<FieldKey, Set<String>>> entries = track.getTrackData().getAllTagFieldValuesIterator();
		while (entries.hasNext()) {
			Entry<FieldKey, Set<String>> entry = entries.next();
			Iterator<String> values = entry.getValue().iterator();
			firstValue = true;
			while (values.hasNext()) {
				value = values.next();
				if (Util.isEmpty(value)) {
					tag.deleteField(entry.getKey());
				}
				else {
					field = tag.createField(entry.getKey(), value);
					if (firstValue) {
						tag.setField(field);
						firstValue = false;
					}
					else {
						tag.addField(field);
					}
				}
			}
		}
    }

}
