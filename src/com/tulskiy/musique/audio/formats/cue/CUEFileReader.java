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
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @Author: Denis Tulskiy
 * @Date: 29.06.2009
 */
public class CUEFileReader extends AudioFileReader {
    private static CUEParser cueParser;
    private Charset charset;

    public CUEFileReader() {
        Configuration conf = Application.getInstance().getConfiguration();
        String enc = conf.getString("cue.externalEncoding", "windows-1251");
        charset = Charset.forName(enc);
    }

    public void read(File f, List<Track> list) {
        Track track = new Track();
        track.setFile(f);
        if (cueParser == null)
            cueParser = new CUEParser();
        try {
            LineNumberReader numberReader = new LineNumberReader(
                    new InputStreamReader(
                            new FileInputStream(f), charset));
            cueParser.parse(list, track, numberReader, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Track readSingle(Track track) {
        //do nothing here
        return null;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("cue");
    }

    @Override
    public Decoder getDecoder() {
        return null;
    }
}
