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

package com.tulskiy.musique.audio;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.jaudiotagger.audio.generic.AbstractTag;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagFieldKey;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 9, 2009
 */
public abstract class AudioTagWriter {

	public abstract void write(Track track);

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
    public void copyTagFields(Tag tag, AbstractTag abstractTag, Track track)
    		throws KeyNotFoundException, FieldDataInvalidException {
    	TagField field;
    	String value;
    	boolean firstValue;

    	Iterator<Entry<TagFieldKey, Set<String>>> entries = track.getTrackData().getAllTagFieldValuesIterator();
		while (entries.hasNext()) {
			Entry<TagFieldKey, Set<String>> entry = entries.next();
			Iterator<String> values = entry.getValue().iterator();
			firstValue = true;
			while (values.hasNext()) {
				value = values.next();
				if (Util.isEmpty(value)) {
					tag.deleteTagField(entry.getKey());
				}
				else {
					field = abstractTag.createTagField(entry.getKey(), value);
					if (firstValue) {
						tag.set(field);
						firstValue = false;
					}
					else {
						tag.add(field);
					}
				}
			}
		}
    }

}
