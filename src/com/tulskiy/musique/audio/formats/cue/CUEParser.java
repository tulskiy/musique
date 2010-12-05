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

package com.tulskiy.musique.audio.formats.cue;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.TrackIO;
import jwbroek.cuelib.*;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

/**
 * @Author: Denis Tulskiy
 * @Date: 29.06.2009
 */
public class CUEParser {
    public void parse(List<Track> list, Track file, LineNumberReader cueStream, boolean embedded) {
        try {
            CueSheet cueSheet = CueParser.parse(cueStream);
            List<FileData> datas = cueSheet.getFileData();
            String cueLocation = file.getFile().getAbsolutePath();
            if (datas.size() > 0) {
                for (FileData fileData : datas) {
                    if (!embedded) {
                        String parent = file.getFile().getParent();
                        File referencedFile = new File(parent, fileData.getFile());
                        if (!referencedFile.exists())
                            continue;
                        AudioFileReader reader = TrackIO.getAudioFileReader(referencedFile.getName());
                        if (reader == null) break;
                        file = reader.readSingle(referencedFile);
                    }

                    int size = fileData.getTrackData().size();
                    for (int i = 0; i < size; i++) {
                        TrackData trackData = fileData.getTrackData().get(i);
                        Track track = file.copy();
                        track.setCueEmbedded(embedded);
                        if (!embedded)
                            track.setCueLocation(cueLocation);

                        String album = trackData.getMetaData(CueSheet.MetaDataField.ALBUMTITLE);
                        if (album.length() > 0)
                            track.setMeta("album", album);
                        String artist = trackData.getPerformer();
                        track.setMeta("artist", artist != null && artist.length() > 0 ? artist : cueSheet.getPerformer());
                        track.setMeta("albumArtist", cueSheet.getPerformer());
                        track.setMeta("comment", cueSheet.getComment());
                        track.setMeta("title", trackData.getTitle());
                        String year = trackData.getMetaData(CueSheet.MetaDataField.YEAR);
                        if (year.length() > 0)
                            track.setMeta("year", year);
                        track.setTrackNumber(String.valueOf(trackData.getNumber()));
                        String genre = trackData.getMetaData(CueSheet.MetaDataField.GENRE);
                        if (genre.length() > 0)
                            track.setMeta("genre", genre);
                        int sampleRate = track.getSampleRate();
                        long startPosition = indexToSample(trackData.getIndex(1), sampleRate);
//                        System.out.println(song.getFile().getName() + " " + startPosition);
                        long endPosition;
                        if (i >= size - 1) {
                            endPosition = track.getTotalSamples();
                        } else {
                            TrackData nextTrack = fileData.getTrackData().get(i + 1);
                            endPosition = indexToSample(nextTrack.getIndex(1), sampleRate);
                        }
                        track.setTotalSamples(endPosition - startPosition);
                        track.setSubsongIndex(i + 1);
                        track.setStartPosition(startPosition);
                        list.add(track);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long indexToSample(Index index, int sampleRate) {
//        System.out.println(index.getPosition().getTotalFrames() / 75d * sampleRate);
        return (long) (index.getPosition().getTotalFrames() / 75d * sampleRate);
    }
}
