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

package com.tulskiy.musique.audio.formats.aac;

import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.playlist.Track;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.mp4.field.Mp4DiscNoField;
import org.jaudiotagger.tag.mp4.field.Mp4TagTextField;
import org.jaudiotagger.tag.mp4.field.Mp4TrackField;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 28, 2009
 */
public class MP4TagWriter extends AudioTagWriter {
    @Override
    public void write(Track track) {
        try {
            org.jaudiotagger.audio.AudioFile af1 = AudioFileIO.read(track.getFile());
            Tag abstractTag = af1.getTag();

            copyCommonFields(abstractTag, track);

            abstractTag.set(new Mp4TagTextField("aART", track.getMeta("albumArtist")));
            abstractTag.set(new Mp4DiscNoField(track.getDisc()));
            abstractTag.set(new Mp4TrackField(track.getTrack()));

            AudioFileIO.write(af1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("m4a");
    }
}
