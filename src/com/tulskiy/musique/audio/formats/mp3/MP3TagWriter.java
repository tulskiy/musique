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

package com.tulskiy.musique.audio.formats.mp3;

import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.audio.formats.ape.APETagProcessor;
import com.tulskiy.musique.playlist.Song;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagFieldKey;

import java.io.File;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 10, 2009
 */
public class MP3TagWriter extends AudioTagWriter {
    private APETagProcessor apeTagProcessor = new APETagProcessor();

    @Override
    public void write(Song song) {
        File file = song.getFile();

        if (/*song.getCustomHeaderField("hasApeTag") != null*/false) {
            try {
                apeTagProcessor.writeAPEv2Tag(song);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            MP3File mp3File;
            try {
                mp3File = new MP3File(file, MP3File.LOAD_IDV2TAG, false);

                org.jaudiotagger.tag.Tag id3v2tag = mp3File.getTagOrCreateAndSetDefault();
                copyCommonFields(id3v2tag, song);

                id3v2tag.setTrack(song.getTrack());
                id3v2tag.set(id3v2tag.createTagField(TagFieldKey.DISC_NO, song.getDisc()));
                id3v2tag.set(id3v2tag.createTagField(TagFieldKey.ALBUM_ARTIST, song.getAlbumArtist()));
                mp3File.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp3");
    }
}
