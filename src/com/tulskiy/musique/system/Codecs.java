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

package com.tulskiy.musique.system;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.Encoder;
import com.tulskiy.musique.audio.IcyInputStream;
import com.tulskiy.musique.audio.formats.ape.APEDecoder;
import com.tulskiy.musique.audio.formats.ape.APEEncoder;
import com.tulskiy.musique.audio.formats.flac.FLACDecoder;
import com.tulskiy.musique.audio.formats.flac.FLACEncoder;
import com.tulskiy.musique.audio.formats.mp3.MP3Decoder;
import com.tulskiy.musique.audio.formats.mp4.MP4Demuxer;
import com.tulskiy.musique.audio.formats.ogg.VorbisDecoder;
import com.tulskiy.musique.audio.formats.ogg.VorbisEncoder;
import com.tulskiy.musique.audio.formats.tta.TTADecoder;
import com.tulskiy.musique.audio.formats.uncompressed.PCMDecoder;
import com.tulskiy.musique.audio.formats.uncompressed.PCMEncoder;
import com.tulskiy.musique.audio.formats.wavpack.WavPackDecoder;
import com.tulskiy.musique.audio.formats.wavpack.WavPackEncoder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @Author: Denis Tulskiy
 * @Date: 24.06.2009
 */
public class Codecs {
    private static HashMap<String, Decoder> decoders = new HashMap<String, Decoder>();
    private static HashMap<String, Encoder> encoders = new HashMap<String, Encoder>();
    private static final Logger logger = Logger.getLogger("musique");

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
        MP4Demuxer mp4Demuxer = new MP4Demuxer();
        decoders.put("mp4", mp4Demuxer);
        decoders.put("m4a", mp4Demuxer);
        decoders.put("tta", new TTADecoder());

        encoders.put("wav", new PCMEncoder());
        encoders.put("ape", new APEEncoder());
        encoders.put("ogg", new VorbisEncoder());
        encoders.put("wv", new WavPackEncoder());
        encoders.put("flac", new FLACEncoder());
    }

    public static Decoder getDecoder(Track track) {
        URI location = track.getTrackData().getLocation();
        if (location == null) {
            return null;
        }
        if (track.getTrackData().isStream()) {
            IcyInputStream inputStream = IcyInputStream.create(track);
            String contentType = inputStream.getContentType().trim();
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if ("audio/mpeg".equals(contentType)) {
                return decoders.get("mp3");
            }

            if ("application/ogg".equals(contentType)) {
                return decoders.get("ogg");
            }
            logger.warning("Unsupported ContentType: " + contentType);
            return null;
        }
        String ext = Util.getFileExt(location.toString()).toLowerCase();
        return decoders.get(ext);
    }

    public static Decoder getNewDecoder(Track track) {
        try {
            return getDecoder(track).getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Encoder getEncoder(String format) {
        return encoders.get(format);
    }

    public static Set<String> getFormats() {
        return decoders.keySet();
    }
}
