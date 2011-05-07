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

package com.tulskiy.musique.audio.formats.flac;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.formats.flac.oggflac.OggFlacDecoder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;
import org.kc7bfi.jflac.metadata.Metadata;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.metadata.VorbisComment;

import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * @Author: Denis Tulskiy
 * @Date: 26.06.2009
 */
public class FLACFileReader extends AudioFileReader {
    public Track readSingle(Track track) {
        try {
            /*if (Util.getFileExt(track.getFile()).equalsIgnoreCase("oga")) {
                OggFlacDecoder dec = new OggFlacDecoder();
                dec.open(new RandomAccessFile(track.getFile(), "r"));
                StreamInfo streamInfo = dec.getStreamInfo();
                track.setSampleRate(streamInfo.getSampleRate());
                track.setBps(streamInfo.getBitsPerSample());
                track.setChannels(streamInfo.getChannels());
                track.setTotalSamples(streamInfo.getTotalSamples());

                for (Metadata m : dec.getMetadata()) {
                    if (m instanceof VorbisComment) {
                        VorbisComment comment = (VorbisComment) m;
                        VorbisCommentTag vorbisTag = new VorbisCommentTag();
                        HashMap<String, String> map = comment.getComments();
                        for (String key : map.keySet()) {
                            try {
                                VorbisCommentFieldKey newKey = VorbisCommentFieldKey.valueOf(key);
                                vorbisTag.add(vorbisTag.createTagField(newKey, map.get(key)));
                            } catch (IllegalArgumentException e) {
                                vorbisTag.add(vorbisTag.createTagField(key, map.get(key)));
                            }
                        }
                        copyTagFields(vorbisTag, track);
                    }
                }
            } else*/ {
                FlacFileReader reader = new FlacFileReader();
                AudioFile af1 = reader.read(track.getFile());
                Tag tag = af1.getTag();
                copyTagFields(tag, track);
                GenericAudioHeader audioHeader = (GenericAudioHeader) af1.getAudioHeader();
                copyHeaderFields(audioHeader, track);
            }
        } catch (Exception e) {
            System.out.println("Couldn't read file: " + track.getFile());
        }
        return track;
    }

    public boolean isFileSupported(String ext) {
        //todo fix seeking with oga and uncomment
        return ext.equalsIgnoreCase("flac")/* || ext.equalsIgnoreCase("oga")*/;
    }

}
