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

import com.tulskiy.musique.audio.formats.cue.CUEParser;
import com.tulskiy.musique.playlist.Track;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: Denis Tulskiy
 * @Date: 25.06.2009
 */
public abstract class AudioFileReader {
    private static CUEParser cueParser;
    protected static Charset defaultCharset = Charset.forName("iso8859-1");
    protected final Logger logger = Logger.getLogger("musique");

    public void read(File file, List<Track> list) {
        logger.log(Level.FINE, "Reading file : {0}", file);
        Track track = read(file);
        String cueSheet = track.getCueSheet();
        if (cueSheet != null && cueSheet.length() > 0) {
            if (cueParser == null)
                cueParser = new CUEParser();
            LineNumberReader reader = new LineNumberReader(new StringReader(cueSheet));
            cueParser.parse(list, track, reader, true);
        } else {
            list.add(track);
        }
    }

    protected abstract Track readSingle(Track track);

    public Track reload(Track track) {
        Track res = readSingle(track);
        if (res.isFile())
            res.setLastModified(res.getFile().lastModified());
        return res;
    }

    public Track read(File file) {
        Track track = new Track();
        track.setLocation(file.toURI());
        return reload(track);
    }

    public abstract boolean isFileSupported(String ext);

    protected void copyTagFields(Tag abstractTag, Track track) throws IOException {
        if (abstractTag != null && track != null) {
            track.addMeta("album", abstractTag.getFirstAlbum());
            track.addMeta("artist", abstractTag.getFirstArtist());
            track.addMeta("comment", abstractTag.getFirstComment());
            track.addMeta("title", abstractTag.getFirstTitle());
            track.addMeta("year", abstractTag.getFirstYear());
            track.setCueSheet(abstractTag.getFirst("CUESHEET"));
            track.addMeta("genre", abstractTag.getFirstGenre());
            track.addMeta("albumArtist", abstractTag.getFirst("ALBUM ARTIST"));
            track.setTrackNumber(abstractTag.getFirstTrack());
        }
    }

    protected void copyHeaderFields(GenericAudioHeader header, Track track) {
        if (header != null && track != null) {
            track.setChannels(header.getChannelNumber());
            track.setTotalSamples(header.getTotalSamples());

//            track.setTotalSamples((long) (header.getPreciseLength() * header.getSampleRateAsNumber()));
            track.setSampleRate(header.getSampleRateAsNumber());
            track.setStartPosition(0);
            track.setCodec(header.getFormat());
            track.setBitrate((int) header.getBitRateAsNumber());
        }
    }

    public static void setDefaultCharset(Charset charset) {
        defaultCharset = charset;
    }

    public static Charset getDefaultCharset() {
        return defaultCharset;
    }
}
