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

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 9, 2009
 */
public abstract class AudioTagWriter {
    public abstract void write(Track track) throws TagWriteException;

    public abstract boolean isFileSupported(String ext);

    protected void copyCommonFields(Tag abstractTag, Track track) throws TagWriteException {
        try {
            abstractTag.setField(FieldKey.ALBUM, track.getMeta("album"));
            abstractTag.setField(FieldKey.ARTIST, track.getMeta("artist"));
            abstractTag.setField(FieldKey.COMMENT, track.getMeta("comment"));
            abstractTag.setField(FieldKey.GENRE, track.getMeta("genre"));
            abstractTag.setField(FieldKey.TITLE, track.getMeta("title"));
            abstractTag.setField(FieldKey.YEAR, track.getMeta("year"));
            abstractTag.setField(FieldKey.ALBUM_ARTIST, track.getMeta("albumArtist"));
            if (!Util.isEmpty(track.getDiscNumber()))
                abstractTag.setField(FieldKey.DISC_NO, track.getDiscNumber());
            if (!Util.isEmpty(track.getTotalDiscs()))
                abstractTag.setField(FieldKey.DISC_TOTAL, track.getTotalDiscs());
            if (!Util.isEmpty(track.getMeta("trackNumber")))
                abstractTag.setField(FieldKey.TRACK, track.getMeta("trackNumber"));
            if (!Util.isEmpty(track.getTotalTracks()))
                abstractTag.setField(FieldKey.TRACK_TOTAL, track.getTotalTracks());
        } catch (FieldDataInvalidException e) {
            throw new TagWriteException(e);
        }
    }
}
