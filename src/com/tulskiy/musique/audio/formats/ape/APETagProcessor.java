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
import davaguine.jmac.info.APETag;
import davaguine.jmac.info.ID3Tag;
import davaguine.jmac.tools.RandomAccessFile;

import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 26.06.2009
 */
public class APETagProcessor {

    public APETagProcessor() {
        ID3Tag.setDefaultEncoding("windows-1251");
    }

    public boolean readAPEv2Tag(Track track) throws IOException {
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(track.getFile(), "r");
            APETag tag = new APETag(ras, true);
            if (tag.GetHasAPETag() || tag.GetHasID3Tag()) {
                track.addMeta("album", tag.GetFieldString(APETag.APE_TAG_FIELD_ALBUM));
                track.addMeta("artist", tag.GetFieldString(APETag.APE_TAG_FIELD_ARTIST));
                track.addMeta("comment", tag.GetFieldString(APETag.APE_TAG_FIELD_COMMENT));
                track.addMeta("title", tag.GetFieldString(APETag.APE_TAG_FIELD_TITLE));
                track.addMeta("year", tag.GetFieldString(APETag.APE_TAG_FIELD_YEAR));
                track.addMeta("genre", tag.GetFieldString(APETag.APE_TAG_FIELD_GENRE));
                track.addMeta("albumArtist", tag.GetFieldString("ALBUM ARTIST"));
                track.setDiscNumber(tag.GetFieldString("DISC"));
                track.setTrackNumber(tag.GetFieldString(APETag.APE_TAG_FIELD_TRACK));

                track.setCueSheet(tag.GetFieldString("CUESHEET"));
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
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(track.getFile(), "rw");
            APETag tag = new APETag(ras, true);
            tag.SetFieldString(APETag.APE_TAG_FIELD_ALBUM, track.getMeta("album"));
            tag.SetFieldString(APETag.APE_TAG_FIELD_ARTIST, track.getMeta("artist"));
            tag.SetFieldString(APETag.APE_TAG_FIELD_COMMENT, track.getMeta("comment"));
            tag.SetFieldString(APETag.APE_TAG_FIELD_GENRE, track.getMeta("genre"));
            tag.SetFieldString(APETag.APE_TAG_FIELD_TITLE, track.getMeta("title"));
            tag.SetFieldString(APETag.APE_TAG_FIELD_TRACK, track.getTrack());
            tag.SetFieldString(APETag.APE_TAG_FIELD_YEAR, track.getMeta("year"));
            tag.SetFieldString("ALBUM ARTIST", track.getMeta("albumArtist"));
            tag.SetFieldString("DISC", track.getDisc());
            tag.SetFieldString("CUESHEET", track.getCueSheet());

            tag.Save();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ras != null)
                ras.close();
        }
        return false;
    }
}
