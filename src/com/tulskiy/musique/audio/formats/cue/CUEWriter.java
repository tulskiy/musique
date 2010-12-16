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
import jwbroek.cuelib.CueParser;
import jwbroek.cuelib.CueSheet;
import jwbroek.cuelib.CueSheetSerializer;
import jwbroek.cuelib.TrackData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class should be saving metadata in cue files, but it
 * is too messy with all the special fields foobar2k writes
 * so I won't be doing it now
 * <p/>
 * <p/>
 * Author: Denis Tulskiy
 * Date: Jul 16, 2010
 */
public class CUEWriter {
    public void write(File file, ArrayList<Track> tracks) {
        try {
            boolean cueEmbedded = tracks.get(0).isCueEmbedded();
            LineNumberReader numberReader;

            if (cueEmbedded) {
                Track track = TrackIO.getAudioFileReader(file.getName()).read(file);
                numberReader = new LineNumberReader(new StringReader(track.getCueSheet()));
                System.out.println(track.getCueSheet());
            } else {
                numberReader = new LineNumberReader(new InputStreamReader(
                        new FileInputStream(file), AudioFileReader.getDefaultCharset()));
            }
            CueSheet cueSheet = CueParser.parse(numberReader);
            //update stuff
            List<TrackData> data = cueSheet.getFileData().get(0).getTrackData();
            data.get(2).setPerformer("Minimal Disc");
//            for (Track track : tracks) {
//                int index = track.getSubsongIndex() - 1;
//                TrackData trackData = data.get(index);
//                trackData.setTitle(track.getTitle());
//                trackData.setPerformer(track.getArtist());
//            }
            CueSheetSerializer ser = new CueSheetSerializer();
            String s = ser.serializeCueSheet(cueSheet);
//            System.out.println(s);
            numberReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
