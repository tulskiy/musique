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

package com.tulskiy.musique.system;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.AudioTagWriter;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.aac.MP4FileReader;
import com.tulskiy.musique.audio.formats.aac.MP4TagWriter;
import com.tulskiy.musique.audio.formats.ape.APEDecoder;
import com.tulskiy.musique.audio.formats.ape.APEFileReader;
import com.tulskiy.musique.audio.formats.ape.APETagWriter;
import com.tulskiy.musique.audio.formats.cue.CUEFileReader;
import com.tulskiy.musique.audio.formats.flac.FLACDecoder;
import com.tulskiy.musique.audio.formats.flac.FLACFileReader;
import com.tulskiy.musique.audio.formats.mp3.MP3Decoder;
import com.tulskiy.musique.audio.formats.ogg.VorbisDecoder;
import com.tulskiy.musique.audio.formats.ogg.VorbisTagWriter;
import com.tulskiy.musique.audio.formats.mp3.MP3FileReader;
import com.tulskiy.musique.audio.formats.mp3.MP3TagWriter;
import com.tulskiy.musique.audio.formats.ogg.OGGFileReader;
import com.tulskiy.musique.audio.formats.uncompressed.PCMDecoder;
import com.tulskiy.musique.audio.formats.uncompressed.PCMFileReader;
import com.tulskiy.musique.audio.formats.wavpack.WavPackDecoder;
import com.tulskiy.musique.audio.formats.wavpack.WavPackFileReader;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author: Denis Tulskiy
 * @Date: 24.06.2009
 */
public class Decoders {
    private static HashMap<String, Decoder> decoders = new HashMap<String, Decoder>();

    static {
        decoders.put("mp3", new MP3Decoder());
        decoders.put("ogg", new VorbisDecoder());
        PCMDecoder pcmDecoder = new PCMDecoder();
        decoders.put("wav", pcmDecoder);
        decoders.put("au", pcmDecoder);
        decoders.put("aiff", pcmDecoder);
        decoders.put("flac", new FLACDecoder());
        decoders.put("ape", new APEDecoder());
        decoders.put("wv", new WavPackDecoder());
    }

    public static Decoder getDecoder(Track track) {
        String ext = Util.getFileExt(track.getFile()).toLowerCase();
        return decoders.get(ext);
    }
}
