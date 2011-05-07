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

package com.tulskiy.musique.audio.formats.mp3;

import java.io.IOException;
import java.util.List;

import org.jaudiotagger.audio.mp3.LameFrame;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.audio.mp3.XingFrame;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.ID3v24Frame;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.AbstractFrameBodyTextInfo;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTPOS;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTRCK;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.formats.ape.APETagProcessor;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;

import davaguine.jmac.info.ID3Tag;

/**
 * @Author: Denis Tulskiy
 * @Date: 26.06.2009
 */
public class MP3FileReader extends AudioFileReader {
    private static final int GAPLESS_DELAY = 529;

    private APETagProcessor apeTagProcessor = new APETagProcessor();

    public Track readSingle(Track track) {
    	TrackData trackData = track.getTrackData();
        TextEncoding.getInstanceOf().setDefaultNonUnicode(defaultCharset.name());
        ID3Tag.setDefaultEncoding(defaultCharset.name());
        MP3File mp3File = null;
        try {
            mp3File = new MP3File(trackData.getFile(), MP3File.LOAD_IDV2TAG, true);
        } catch (Exception ignored) {
            System.out.println("Couldn't read file: " + trackData.getFile());
        }

        ID3v24Tag v24Tag = null;
        if (mp3File != null) {
            try {
                v24Tag = mp3File.getID3v2TagAsv24();
                if (v24Tag != null) {
                    copyCommonTagFields(v24Tag, track);
                    copySpecificTagFields(v24Tag, track);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            MP3AudioHeader mp3AudioHeader = mp3File.getMP3AudioHeader();
            copyHeaderFields(mp3AudioHeader, track);

            long totalSamples = trackData.getTotalSamples();
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
                } else {
                    totalSamples += GAPLESS_DELAY;
                }
            }

            totalSamples -= enc_delay;
            trackData.setTotalSamples(totalSamples);
        }

        // TODO review correctness of reading APETag only in case ID3 is missed
        // for example, maybe useful to read and set those fields
        // that are missed in ID3 but presented in APE
        if (v24Tag == null) {
	        try {
	            apeTagProcessor.readAPEv2Tag(track);
	        }
	        catch (Exception ignored) {
	        }
        }

        return track;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("mp3");
    }

	@Override
	protected void copyCommonTagFields(Tag tag, Track track) throws IOException {
		ID3v24Tag v24Tag = (ID3v24Tag) tag;
		for (FieldKey key : FieldKey.values()) {
			setMusiqueTagFieldValues(track, key, v24Tag);
		}
	}

//	@Override
//	protected void copySpecificTagFields(Tag tag, Track track) {
//		ID3v24Tag v24Tag = (ID3v24Tag) tag;
//	}

	// TODO review (T?? [but not TXXX] are only supported at the moment)
	private void setMusiqueTagFieldValues(Track track, FieldKey key, ID3v24Tag tag) {
		List<TagField> fields;

		try {
			fields = tag.getFields(key);
		}
		catch (KeyNotFoundException ignored) {
			return;
		}

		for (TagField field : fields) {
			ID3v24Frame frame = (ID3v24Frame) field;
			if (frame.getBody() instanceof FrameBodyTRCK) {
				FrameBodyTRCK body = (FrameBodyTRCK) frame.getBody();
				track.getTrackData().addTrack(body.getTrackNo());
				track.getTrackData().addTrackTotal(body.getTrackTotal());
			}
			else if (frame.getBody() instanceof FrameBodyTPOS) {
				FrameBodyTPOS body = (FrameBodyTPOS) frame.getBody();
				track.getTrackData().addDisc(body.getDiscNo());
				track.getTrackData().addDiscTotal(body.getDiscTotal());
			}
			else if (frame.getBody() instanceof AbstractFrameBodyTextInfo) {
				AbstractFrameBodyTextInfo body = (AbstractFrameBodyTextInfo) frame.getBody();
				// TODO not sure about body.getFirstTextValue()
				track.getTrackData().addTagFieldValues(key, body.getFirstTextValue());
			}
		}
	}

}
