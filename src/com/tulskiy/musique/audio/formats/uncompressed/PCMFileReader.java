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

package com.tulskiy.musique.audio.formats.uncompressed;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.jaudiotagger.tag.TagFieldKey;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.util.Util;

/**
 * @Author: Denis Tulskiy
 * @Date: 30.06.2009
 */
public class PCMFileReader extends AudioFileReader {
    private static Decoder decoder = new PCMDecoder();

    public Track readSingle(Track track) {
    	TrackData trackData = track.getTrackData();
        File file = trackData.getFile();

        String title = Util.removeExt(file.getName());
        trackData.setTagFieldValues(TagFieldKey.TITLE, title);
        try {
            AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
            trackData.setStartPosition(0);
            AudioFormat audioFormat = format.getFormat();
            trackData.setSampleRate((int) audioFormat.getSampleRate());
            trackData.setTotalSamples(format.getFrameLength());
            trackData.setChannels(audioFormat.getChannels());
            trackData.setCodec(Util.getFileExt(file).toUpperCase());
            if (format.getFrameLength() > 0)
            	trackData.setBitrate((int) (format.getByteLength() / format.getFrameLength() * audioFormat.getSampleRate() / 100));
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + trackData.getFile());
        }
        return track;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("wav") || ext.equalsIgnoreCase("au")
               || ext.equalsIgnoreCase("aiff");
    }

}
