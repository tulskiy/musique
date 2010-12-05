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

package com.tulskiy.musique.audio.formats.ogg;

import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.Info;
import com.jcraft.jorbis.VorbisFile;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

/**
 * @Author: Denis Tulskiy
 * @Date: 17.07.2009
 */
public class VorbisDecoder implements Decoder {
    private VorbisFile vorbisFile;
    private AudioFormat audioFormat;
    private boolean streaming = false;
    private Track track;
    private int oldBitrate;

    public boolean open(Track track) {
        try {
            this.track = track;
            if (track.isFile()) {
                logger.fine("Opening file: " + track.getFile());
                vorbisFile = new VorbisFile(track.getFile().getAbsolutePath());
                streaming = false;
                oldBitrate = track.getBitrate();
            } else if (track.isStream()) {
                URL url = track.getLocation().toURL();
                logger.fine("Opening stream: " + URLDecoder.decode(url.toString(), "utf8"));
                URLConnection urlConnection = url.openConnection();
                String contentType = urlConnection.getContentType();
                if (!contentType.equals("application/ogg")) {
                    logger.warning("Wrong content type: " + contentType);
                    return false;
                }
                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                vorbisFile = new VorbisFile(bis, null, 0);
                streaming = true;
                reloadComments(track);
                track.setCodec("OGG Vorbis Stream");
            }
            Info info = vorbisFile.getInfo()[0];
            track.setSampleRate(info.rate);
            track.setChannels(info.channels);
            audioFormat = new AudioFormat(info.rate, 16, info.channels, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void reloadComments(Track track) {
        try {
            Comment[] comments = vorbisFile.getComment();
            for (Comment c : comments) {
                for (int i = 0; i < c.comments; i++) {
                    byte[] data = c.user_comments[i];
                    String comment = new String(data, 0, data.length - 1, "UTF-8");
                    String[] strings = comment.split("=");
                    String key = strings[0].toLowerCase();
                    String value = strings[1];

                    if (key.equals("tracknumber"))
                        track.setTrackNumber(value);
                    else if (key.equals("albumartist"))
                        track.setMeta("albumArtist", value);
                    else if (key.equals("date"))
                        track.setMeta("year", value);
                    else
                        track.setMeta(key, value);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long sample) {
        vorbisFile.pcm_seek(sample);
    }

    public int decode(byte[] buf) {
        int ret = vorbisFile.read(buf, buf.length);
        track.setBitrate(vorbisFile.bitrate_instant() / 1000);
        if (ret <= 0) {
            //it's a stream, open it again
            if (streaming) {
                if (!open(track))
                    return -1;
                else
                    return 0;
            }
            return -1;
        }
        return ret;
    }

    public void close() {
        try {
            if (vorbisFile != null) {
                vorbisFile.close();
                track.setBitrate(oldBitrate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
