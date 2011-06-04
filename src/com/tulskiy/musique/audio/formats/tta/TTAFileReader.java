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

package com.tulskiy.musique.audio.formats.tta;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.formats.ape.APETagProcessor;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.tta.TTA_Decoder;
import com.tulskiy.tta.TTA_info;

import java.io.FileInputStream;
import java.util.logging.Level;

/**
 * Author: Denis Tulskiy
 * Date: 6/4/11
 */
public class TTAFileReader extends AudioFileReader{
    private static APETagProcessor apeTagProcessor = new APETagProcessor();

    @Override
    protected Track readSingle(Track track) {
        TrackData trackData = track.getTrackData();
        try {
            apeTagProcessor.readAPEv2Tag(track);
            TTA_info info = new TTA_info();
            new TTA_Decoder(new FileInputStream(trackData.getFile())).read_tta_header(info);
            trackData.setCodec("True Audio");
            trackData.setBps(info.bps);
            trackData.setChannels(info.nch);
            trackData.setSampleRate(info.sps);
            trackData.setTotalSamples(info.samples);
            trackData.setBitrate(info.bitrate);
            return track;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error reading file " + trackData.getFile(), e);
        }

        return null;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return "tta".equalsIgnoreCase(ext);
    }
}
