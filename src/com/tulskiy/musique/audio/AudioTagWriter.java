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

package com.tulskiy.musique.audio;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.jaudiotagger.audio.generic.AbstractTag;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 9, 2009
 */
public abstract class AudioTagWriter {

	public abstract void write(Track track) throws TagWriteException;

    public abstract boolean isFileSupported(String ext);

    /**
     * Copies Musique track tag field values to destination format specific container.
     * 
     * @param tag destination format specific container
     * @param abstractTag destination format specific container implementation (just to create specific TagFields)
     * @param track Musique track
     * @throws KeyNotFoundException
     * @throws FieldDataInvalidException
     */
    // in case of logic change, review MP3TagWriter and APETagProcessor
    // TODO take a look if refactoring to AbstractTag only fits (in format specific writers)
    public void copyTagFields(Tag tag, AbstractTag abstractTag, Track track) throws TagWriteException {
    	String value;
    	boolean firstValue;

    	Iterator<Entry<FieldKey, Set<String>>> entries = track.getTrackData().getAllTagFieldValuesIterator();
    	try {
			while (entries.hasNext()) {
				Entry<FieldKey, Set<String>> entry = entries.next();
				Iterator<String> values = entry.getValue().iterator();
				firstValue = true;
				while (values.hasNext()) {
					value = values.next();
					if (firstValue) {
						tag.deleteField(entry.getKey());
						firstValue = false;
					}
					if (!Util.isEmpty(value)) {
						tag.addField(abstractTag.createField(entry.getKey(), value));
					}
				}
			}
    	}
    	catch (KeyNotFoundException knfe) {
    		throw new TagWriteException(knfe);
    	}
    	catch (FieldDataInvalidException fdie) {
    		throw new TagWriteException(fdie);
    	}
		
		track.getTrackData().removeEmptyTagFields();
    }

}
