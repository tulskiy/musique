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

package com.tulskiy.musique.audio.formats.mp3;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.ape.APETagProcessor;
import com.tulskiy.musique.playlist.Song;
import org.jaudiotagger.audio.mp3.LameFrame;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.audio.mp3.XingFrame;
import org.jaudiotagger.tag.TagFieldKey;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 26.06.2009
 */
public class MP3FileReader extends AudioFileReader {
    private static final int GAPLESS_DELAY = 529;

    private static Decoder decoder;
    private APETagProcessor apeTagProcessor = new APETagProcessor();
    private boolean useNativeDecoder = false;

    public void setUseNativeDecoder(boolean useNativeDecoder) {
        this.useNativeDecoder = useNativeDecoder;
        decoder = null;
    }

    public Song readSingle(Song song) {
        MP3File mp3File = null;
        try {
            mp3File = new MP3File(song.getFile(), MP3File.LOAD_IDV2TAG, true);
        } catch (Exception e) {
//            e.printStackTrace();
        }


        if (mp3File != null) {
            try {
                ID3v24Tag v24Tag = mp3File.getID3v2TagAsv24();
                if (v24Tag != null) {
                    copyTagFields(v24Tag, song);
                    if (song.getYear() == null || song.getYear().length() == 0) {
                        song.setYear(v24Tag.getFirst(TagFieldKey.DATE).trim());
                    }
                    song.setAlbumArtist(v24Tag.getFirst(TagFieldKey.ALBUM_ARTIST).trim());
                    song.setDiscNumber(v24Tag.getFirst(TagFieldKey.DISC_NO));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            MP3AudioHeader mp3AudioHeader = mp3File.getMP3AudioHeader();
            copyHeaderFields(mp3AudioHeader, song);

//            int samplesPerFrame = (int) (mp3AudioHeader.getTimePerFrame() * mp3AudioHeader.getSampleRateAsNumber());
//            song.setCustomHeaderNumber("samples_per_frame", samplesPerFrame);

            long totalSamples = song.getTotalSamples();
            int enc_delay = GAPLESS_DELAY;

            XingFrame xingFrame = mp3AudioHeader.getXingFrame();
            if (xingFrame != null) {
                LameFrame lameFrame = xingFrame.getLameFrame();
                if (lameFrame != null) {
                    long length = totalSamples;
                    enc_delay += lameFrame.getEncDelay();
                    int enc_padding = lameFrame.getEncPadding() - GAPLESS_DELAY;
                    if (enc_padding < length)
                        length -= enc_padding;

                    if (totalSamples > length)
                        totalSamples = length;

                    /*song.setCustomHeaderField("tool", lameFrame.getEncoder());
                    String codec_profile = null;
                    switch (lameFrame.getVbrMethod()) {
                        case 1:
                            codec_profile = "CBR";
                            break;
                        case 2:
                            codec_profile = "ABR";
                            break;
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                            codec_profile = "VBR V" + (lameFrame.getVbrMethod() - 2);
                    }
                    if (codec_profile != null) {
                        song.setCustomHeaderField("codec_profile", codec_profile);
                    } */
                } else {
                    totalSamples += GAPLESS_DELAY;
                }
            }

//            song.setCustomHeaderNumber("enc_delay", enc_delay);
            totalSamples -= enc_delay;
//            song.setCustomHeaderNumber("mp3_total_samples", totalSamples);
            song.setTotalSamples(totalSamples);
        } else {
            try {
                apeTagProcessor.readAPEv2Tag(song);
            } catch (IOException e) {

            }
        }


        return song;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp3");
    }

    @Override
    public Decoder getDecoder() {
        if (decoder == null) {
            if (useNativeDecoder) {
                decoder = new MP3NativeDecoder();
            } else {
                decoder = new MP3Decoder();
            }
        }
        return decoder;
    }
}
