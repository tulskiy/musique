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

package com.tulskiy.musique.audio.formats.flac;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Track;
import org.kc7bfi.jflac.frame.Frame;
import org.kc7bfi.jflac.io.RandomFileInputStream;
import org.kc7bfi.jflac.metadata.Metadata;
import org.kc7bfi.jflac.metadata.SeekTable;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @Author: Denis Tulskiy
 * @Date: 12.06.2009
 */
public class FLACDecoder implements Decoder {
    private RandomAccessFile inputFile;
    private StreamInfo streamInfo;
    private SeekTable seekTable;
    private org.kc7bfi.jflac.FLACDecoder decoder;
    private ByteData byteData;
    private int offset = -1;

    public synchronized boolean open(Track track) {
        try {
            inputFile = new RandomAccessFile(track.getFile(), "r");
//            ogg = iFile.getAudioHeader().getCodec().equals("Ogg FLAC");
//            if (ogg) {
//                oggDecoder = new OggFlacDecoder();
//                oggDecoder.open(inputFile);
//                streamInfo = oggDecoder.getStreamInfo();
//                decoder = oggDecoder.getDecoder();
//            } else {
            decoder = new org.kc7bfi.jflac.FLACDecoder(new RandomFileInputStream(inputFile));
            parseMetadata();
//            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public AudioFormat getAudioFormat() {
        return streamInfo.getAudioFormat();
    }

    private void parseMetadata() {
        streamInfo = null;
        try {
            Metadata[] metadata = decoder.readMetadata();
            for (Metadata m : metadata) {
                if (m instanceof StreamInfo)
                    streamInfo = (StreamInfo) m;
                else if (m instanceof SeekTable)
                    seekTable = (SeekTable) m;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekSample(long sample) {
        decoder.flush();
//        if (ogg) {
//            seekOgg(sample);
//        } else {
        seekFlac(sample);
//        }
        decoder.flush();

    }

    public int decode(byte[] buf) {
        try {
            if (offset != -1) {
                int len = byteData.getLen() - offset;
                System.arraycopy(byteData.getData(), offset, buf, 0, len);
                offset = -1;
                return len;
            }
            Frame readFrame = decoder.readNextFrame();
            if (readFrame == null)
                return -1;
            byteData = decoder.decodeFrame(readFrame, null);
            System.arraycopy(byteData.getData(), 0, buf, 0, byteData.getLen());
            return byteData.getLen();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        try {
            if (inputFile != null)
                inputFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void seekOgg(long target_sample) {
//
//        long left_pos = 0;
//        long right_pos = 0;
//        try {
//            right_pos = inputFile.length();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        long left_sample = 0, right_sample = streamInfo.getTotalSamples();
//        long this_frame_sample = 0;
//        long pos = 0;
//        boolean did_a_seek;
//        int iteration = 0;
//
//        /* In the first iterations, we will calculate the target byte position
//         * by the distance from the target sample to left_sample and
//         * right_sample (let's call it "proportional search").  After that, we
//         * will switch to binary search.
//         */
//        int BINARY_SEARCH_AFTER_ITERATION = 2;
//
//        /* We will switch to a linear search once our current sample is less
//         * than this number of samples ahead of the target sample
//         */
//        long LINEAR_SEARCH_WITHIN_SAMPLES = streamInfo.getMaxBlockSize() * 2;
//
//        /* If the total number of samples is unknown, use a large value, and
//         * force binary search immediately.
//         */
//        if (right_sample == 0) {
//            right_sample = Long.MAX_VALUE;
//            BINARY_SEARCH_AFTER_ITERATION = 0;
//        }
//
//        for (; ; iteration++) {
//            if (iteration == 0 || this_frame_sample > target_sample || target_sample - this_frame_sample > LINEAR_SEARCH_WITHIN_SAMPLES) {
//                if (iteration >= BINARY_SEARCH_AFTER_ITERATION) {
//                    pos = (right_pos + left_pos) / 2;
//                } else {
//                    pos = (long) ((double) (target_sample - left_sample) / (double) (right_sample - left_sample) * (double) (right_pos - left_pos));
//
//                    /* @@@
//                    * before EOF, to make sure we land before the last frame,
//                    * thereby getting a this_frame_sample and so having a better
//                    * estimate.  @@@@@@DELETE:this would also mostly (or totally if we could
//                    * be sure to land before the last frame) avoid the
//                    * end-of-stream case we have to check later.
//                    */
//                }
//
//                /* physical seek */
//                oggDecoder.seekHelper(pos);
//                oggDecoder.flush();
//                oggDecoder.getNextPage(right_pos - pos);
//                did_a_seek = true;
//            } else
//                did_a_seek = false;
//
//            decoder.getBitInputStream().reset();
//            Frame frame;
//            try {
//                frame = decoder.readNextFrame();
//            } catch (IOException e) {
//                e.printStackTrace();
//                return;
//            }
//            if (frame == null) {
//                if (did_a_seek) {
//                    /* this can happen if we seek to a point after the last frame; we drop
//                     * to binary search right away in this case to avoid any wasted
//                     * iterations of proportional search.
//                     */
//                    right_pos = pos;
//                    BINARY_SEARCH_AFTER_ITERATION = 0;
//                } else {
//                    /* this can probably only happen if total_samples is unknown and the
//                     * target_sample is past the end of the stream
//                     */
//                    return;
//                }
//            } else if (frame.header.sampleNumber <= target_sample &&
//                    target_sample <= frame.header.sampleNumber + frame.header.blockSize) {
////                    System.out.println("Done seeking");
//                int offset = (int) (target_sample - frame.header.sampleNumber) * frame.header.channels * frame.header.bitsPerSample / 8;
//                ByteData bd = decoder.decodeFrame(frame, null);
//                outputStream.write(bd.getData(), offset, bd.getLen() - offset);
//                break;
//
//            } else {
//                this_frame_sample = frame.header.sampleNumber;
//
//                if (did_a_seek) {
//                    if (this_frame_sample <= target_sample) {
//                        /* The 'equal' case should not happen, since
//                        * FLAC__stream_decoder_process_single()
//                        * should recognize that it has hit the
//                        * target sample and we would exit through
//                        * the 'break' above.
//                        */
//                        left_sample = this_frame_sample;
//                        /* sanity check to avoid infinite loop */
//                        if (left_pos == pos) {
//                            return;
//                        }
//                        left_pos = pos;
//                    } else if (this_frame_sample > target_sample) {
//                        right_sample = this_frame_sample;
//                        /* sanity check to avoid infinite loop */
//                        if (right_pos == pos) {
//                            return;
//                        }
//                        right_pos = pos;
//                    }
//                }
//            }
//        }
    }

    private void seekFlac(long target_sample) {
        long lower_bound, upper_bound = 0, lower_bound_sample, upper_bound_sample, this_frame_sample;
        long pos;
        int i;
        int approx_bytes_per_frame;
        boolean first_seek = true;
        long total_samples = streamInfo.getTotalSamples();
        int min_blocksize = streamInfo.getMinBlockSize();
        int max_blocksize = streamInfo.getMaxBlockSize();
        int max_framesize = streamInfo.getMaxFrameSize();
        int min_framesize = streamInfo.getMinFrameSize();
        int channels = streamInfo.getChannels();
        int bps = streamInfo.getBitsPerSample();

        /* we are just guessing here */
        if (max_framesize > 0)
            approx_bytes_per_frame = (max_framesize + min_framesize) / 2 + 1;
        else if (min_blocksize == max_blocksize && min_blocksize > 0) {
            approx_bytes_per_frame = min_blocksize * channels * bps / 8 + 64;
        } else
            approx_bytes_per_frame = 4096 * channels * bps / 8 + 64;

        lower_bound = 0;
        lower_bound_sample = 0;
        try {
            upper_bound = inputFile.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        upper_bound_sample = total_samples > 0 ? total_samples : target_sample /*estimate it*/;

        if (seekTable != null) {
            long new_lower_bound = lower_bound;
            long new_upper_bound = upper_bound;
            long new_lower_bound_sample = lower_bound_sample;
            long new_upper_bound_sample = upper_bound_sample;

            /* find the closest seekPosition point <= target_sample, if it exists */
            for (i = seekTable.numberOfPoints() - 1; i >= 0; i--) {
                if (seekTable.getSeekPoint(i).getFrameSamples() > 0 && /* defense against bad seekpoints */
                    (total_samples <= 0 || seekTable.getSeekPoint(i).getSampleNumber() < total_samples) && /* defense against bad seekpoints */
                    seekTable.getSeekPoint(i).getSampleNumber() <= target_sample)
                    break;
            }
            if (i >= 0) { /* i.e. we found a suitable seekPosition point... */
                new_lower_bound = seekTable.getSeekPoint(i).getStreamOffset();
                new_lower_bound_sample = seekTable.getSeekPoint(i).getSampleNumber();
            }

            /* find the closest seekPosition point > target_sample, if it exists */
            for (i = 0; i < seekTable.numberOfPoints(); i++) {
                if (seekTable.getSeekPoint(i).getFrameSamples() > 0 && /* defense against bad seekpoints */
                    (total_samples <= 0 || seekTable.getSeekPoint(i).getSampleNumber() < total_samples) && /* defense against bad seekpoints */
                    seekTable.getSeekPoint(i).getSampleNumber() > target_sample)
                    break;
            }
            if (i < seekTable.numberOfPoints()) { /* i.e. we found a suitable seekPosition point... */
                new_upper_bound = seekTable.getSeekPoint(i).getStreamOffset();
                new_upper_bound_sample = seekTable.getSeekPoint(i).getSampleNumber();
            }
            /* final protection against unsorted seekPosition tables; keep original values if bogus */
            if (new_upper_bound >= new_lower_bound) {
                lower_bound = new_lower_bound;
                upper_bound = new_upper_bound;
                lower_bound_sample = new_lower_bound_sample;
                upper_bound_sample = new_upper_bound_sample;
            }
        }

        if (upper_bound_sample == lower_bound_sample)
            upper_bound_sample++;

        while (true) {
            try {
                /* check if the bounds are still ok */
                if (lower_bound_sample >= upper_bound_sample || lower_bound > upper_bound) {
                    return;
                }

                pos = (long) (lower_bound + ((double) (target_sample - lower_bound_sample) / (double) (upper_bound_sample - lower_bound_sample) * (double) (upper_bound - lower_bound)) - approx_bytes_per_frame);

                if (pos >= upper_bound)
                    pos = upper_bound - 1;
                if (pos < lower_bound)
                    pos = lower_bound;
//                System.out.println("Seek to: " + pos);
                inputFile.seek(pos);
//                decoder.getBitInputStream().skipBitsNoCRC(1);
                decoder.getBitInputStream().reset();

                Frame frame = decoder.readNextFrame();
//                System.out.println("Found: " + frame.header.sampleNumber);
                if (frame.header.sampleNumber <= target_sample &&
                    target_sample <= frame.header.sampleNumber + frame.header.blockSize) {
//                    System.out.println("Done seeking");
                    offset = (int) (target_sample - frame.header.sampleNumber) * frame.header.channels * frame.header.bitsPerSample / 8;
                    byteData = decoder.decodeFrame(frame, null);
                    break;
                }
                /* our write callback will change the state when it gets to the target frame */
                /* actually, we could have got_a_frame if our decoder is at FLAC__STREAM_DECODER_END_OF_STREAM so we need to check for that also */

                this_frame_sample = frame.header.sampleNumber;

                if (decoder.getSamplesDecoded() == 0 || (this_frame_sample + frame.header.blockSize >= upper_bound_sample && !first_seek)) {
                    if (pos == lower_bound) {
                        /* can't move back any more than the first frame, something is fatally wrong */
                        System.err.printf("FLAC Decoder: Seek to %d error. %d samples overrun, sorry\n", target_sample, this_frame_sample - target_sample);
                        return;
                    }
                    /* our last move backwards wasn't big enough, try again */
                    approx_bytes_per_frame = approx_bytes_per_frame != 0 ? approx_bytes_per_frame * 2 : 16;
                    continue;
                }
                /* allow one seekPosition over upper bound, so we can get a correct upper_bound_sample for streams with unknown total_samples */
                first_seek = false;

                /* make sure we are not seeking in corrupted stream */
                if (this_frame_sample < lower_bound_sample) {
                    System.err.println("FLAC Decoder: Seek error. This frame sample is lower than lower bound sample");
                    return;
                }

                /* we need to narrow the search */
                if (target_sample < this_frame_sample) {
                    upper_bound_sample = this_frame_sample + frame.header.blockSize;
                    /*@@@@@@ what will decode position be if at end of stream? */
                    upper_bound = inputFile.getFilePointer() - decoder.getBitInputStream().getInputBytesUnconsumed();
                    approx_bytes_per_frame = (int) (2 * (upper_bound - pos) / 3 + 16);
                } else { /* target_sample >= this_frame_sample + this frame's blocksize */
                    lower_bound_sample = this_frame_sample + frame.header.blockSize;
                    lower_bound = inputFile.getFilePointer() - decoder.getBitInputStream().getInputBytesUnconsumed();
                    approx_bytes_per_frame = (int) (2 * (lower_bound - pos) / 3 + 16);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
