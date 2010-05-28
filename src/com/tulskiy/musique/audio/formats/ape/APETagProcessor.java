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

import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.util.Util;
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

    public boolean readAPEv2Tag(Song song) throws IOException {
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(song.getFile(), "r");
            APETag tag = new APETag(ras, true);
            if (tag.GetHasAPETag() || tag.GetHasID3Tag()) {
                if (empty(song.getAlbum())) song.setAlbum(tag.GetFieldString(APETag.APE_TAG_FIELD_ALBUM));
                if (empty(song.getArtist())) song.setArtist(tag.GetFieldString(APETag.APE_TAG_FIELD_ARTIST));
                if (empty(song.getComment())) song.setComment(tag.GetFieldString(APETag.APE_TAG_FIELD_COMMENT));
                if (empty(song.getTitle())) song.setTitle(tag.GetFieldString(APETag.APE_TAG_FIELD_TITLE));
                if (empty(song.getYear())) song.setYear(tag.GetFieldString(APETag.APE_TAG_FIELD_YEAR));
                if (empty(song.getGenre())) song.setGenre(tag.GetFieldString(APETag.APE_TAG_FIELD_GENRE));
                if (empty(song.getAlbumArtist())) song.setAlbumArtist(tag.GetFieldString("ALBUM ARTIST"));
                if (empty(song.getDiscNumber())) song.setDiscNumber(tag.GetFieldString("DISC"));
                if (empty(song.getTrackNumber())) song.setTrackNumber(tag.GetFieldString(APETag.APE_TAG_FIELD_TRACK));

                if (empty(song.getCueSheet())) song.setCueSheet(tag.GetFieldString("CUESHEET"));
//                byte[] artwork = tag.GetFieldBinary(APETag.APE_TAG_FIELD_COVER_ART_FRONT);
//                if (artwork != null) {
//                    ImageIcon icon = new ImageIcon(artwork);
//                    tt.addAlbumart(icon.getImage());
//                }
                if (tag.GetHasAPETag())
//                    song.setCustomHeaderField("hasApeTag", "");
//                if (tag.GetHasID3Tag())
//                    tt.addTagFormat(Tag.ID3V1);
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

    private boolean empty(String field) {
        return field == null || field.isEmpty();
    }

    public boolean writeAPEv2Tag(Song song) throws IOException {
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(song.getFile(), "rw");
            APETag tag = new APETag(ras, true);
            tag.SetFieldString(APETag.APE_TAG_FIELD_ALBUM, song.getAlbum());
            tag.SetFieldString(APETag.APE_TAG_FIELD_ARTIST, song.getArtist());
            tag.SetFieldString(APETag.APE_TAG_FIELD_COMMENT, song.getComment());
            tag.SetFieldString(APETag.APE_TAG_FIELD_GENRE, song.getGenre());
            tag.SetFieldString(APETag.APE_TAG_FIELD_TITLE, song.getTitle());
            tag.SetFieldString(APETag.APE_TAG_FIELD_TRACK, song.getTrack());
            tag.SetFieldString(APETag.APE_TAG_FIELD_YEAR, song.getYear());
            tag.SetFieldString("ALBUM ARTIST", song.getAlbumArtist());
            tag.SetFieldString("DISC", song.getDisc());
            tag.SetFieldString("CUESHEET", song.getCueSheet());

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
