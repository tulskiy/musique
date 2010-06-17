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

import com.tulskiy.musique.playlist.Track;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.Tag;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 9, 2009
 */
public abstract class AudioTagWriter {
    public abstract void write(Track track);

    public abstract boolean isFileSupported(String ext);

    protected void copyCommonFields(Tag abstractTag, Track track) {
        try {
            abstractTag.setAlbum(track.getMeta("album"));
            abstractTag.setArtist(track.getMeta("artist"));
            abstractTag.setComment(track.getMeta("comment"));
            abstractTag.setGenre(track.getMeta("genre"));
            abstractTag.setTitle(track.getMeta("title"));
            abstractTag.setYear(track.getMeta("year"));
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
        }
    }
}
