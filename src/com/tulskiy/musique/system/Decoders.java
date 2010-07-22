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

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.ape.APEDecoder;
import com.tulskiy.musique.audio.formats.flac.FLACDecoder;
import com.tulskiy.musique.audio.formats.mp3.MP3Decoder;
import com.tulskiy.musique.audio.formats.ogg.VorbisDecoder;
import com.tulskiy.musique.audio.formats.uncompressed.PCMDecoder;
import com.tulskiy.musique.audio.formats.wavpack.WavPackDecoder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.Util;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @Author: Denis Tulskiy
 * @Date: 24.06.2009
 */
public class Decoders {
    private static HashMap<String, Decoder> decoders = new HashMap<String, Decoder>();
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
    }

    public static Decoder getDecoder(Track track) {
        URI location = track.getLocation();
        if ("http".equals(location.getScheme())) {
            try {
                URLConnection con = location.toURL().openConnection();
                String contentType = con.getContentType();

                if ("audio/mpeg".equals(contentType) || "unknown/unknown".equals(contentType)) {
                    // if ContentType is unknown, it is probably
                    // shoutcast, let mp3 decoder decide
                    return decoders.get("mp3");
                }

                if ("application/ogg".equals(contentType)) {
                    return decoders.get("ogg");
                }
                logger.warning("Unsupported ContentType: " + contentType);
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String ext = Util.getFileExt(location.toString()).toLowerCase();
        return decoders.get(ext);
    }
}
