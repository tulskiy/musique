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

package com.tulskiy.musique.audio.formats.ogg;

import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.playlist.Song;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTagField;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 10, 2009
 */
public class VorbisTagWriter extends AudioTagWriter {
    @Override
    public void write(Song song) {
        try {
            org.jaudiotagger.audio.AudioFile af1 = AudioFileIO.read(song.getFile());
            Tag abstractTag = af1.getTag();
            copyCommonFields(abstractTag, song);

            abstractTag.set(new VorbisCommentTagField("ALBUM ARTIST", song.getAlbumArtist()));
            abstractTag.set(new VorbisCommentTagField("TOTALTRACKS", song.getTotalTracks()));
            abstractTag.set(new VorbisCommentTagField("DISCNUMBER", song.getDiscNumber()));
            abstractTag.set(new VorbisCommentTagField("TOTALDISCS", song.getTotalDiscs()));
            abstractTag.set(new VorbisCommentTagField("TRACKNUMBER", song.getTrackNumber()));

            AudioFileIO.write(af1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("flac") || ext.equalsIgnoreCase("ogg");
    }
}
