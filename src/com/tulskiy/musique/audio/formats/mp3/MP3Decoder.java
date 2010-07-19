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

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.AudioMath;
import com.tulskiy.musique.util.Util;
import javazoom.jl.decoder.*;

import javax.sound.sampled.AudioFormat;
import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @Author: Denis Tulskiy
 * @Date: 12.06.2009
 */
public class MP3Decoder implements com.tulskiy.musique.audio.Decoder {
    private static final int DECODE_AFTER_SEEK = 9;
    private LinkedHashMap<File, SeekTable> seekTableCache = new LinkedHashMap<File, SeekTable>(10, 0.7f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<File, SeekTable> eldest) {
            return size() > 10;
        }
    };
    private static final Logger logger = Logger.getLogger(MP3Decoder.class.getName());

    private Bitstream bitstream;
    private javazoom.jl.decoder.Decoder decoder;
    private AudioFormat audioFormat;
    private Header readFrame;
    private Track track;

    private long totalSamples;
    private long streamSize;
    private byte[] buffer = new byte[5000];
    private int samplesPerFrame;
    private int sampleOffset = 0;
    private int encDelay;
    private long currentSample;
    private boolean streaming = false;
    private int oldBitrate;

    private Header skipFrame() throws BitstreamException {
        readFrame = bitstream.readFrame();
        if (readFrame == null) {
            return null;
        }
        bitstream.closeFrame();

        return readFrame;
    }

    private int samplesToMinutes(long samples) {
        return (int) (samples / track.getSampleRate() / 60f);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private boolean createBitstream(long targetSample) {
        if (bitstream != null)
            bitstream.close();
        bitstream = null;
        try {
            File file = track.getFile();
            FileInputStream fis = new FileInputStream(file);

            //so we compute target frame first
            targetSample += encDelay;
            int targetFrame = (int) ((double) targetSample / samplesPerFrame);
            sampleOffset = (int) (targetSample - targetFrame * samplesPerFrame) * audioFormat.getFrameSize();

            //then we get the seek table or create it if needed
            SeekTable seekTable = seekTableCache.get(file);
            if (seekTable == null &&
                samplesToMinutes(totalSamples) > 10) {
                seekTable = new SeekTable();
                seekTableCache.put(file, seekTable);
            }

            int currentFrame = 0;
            //if we have a point, use it
            if (seekTable != null) {
                SeekTable.SeekPoint seekPoint = seekTable.get(targetFrame - DECODE_AFTER_SEEK);
                fis.skip(seekPoint.offset);
                currentFrame = seekPoint.frame;
            }

            //then we create the bitstream
            bitstream = new Bitstream(fis);
            decoder = new javazoom.jl.decoder.Decoder();

            readFrame = null;
            for (int i = currentFrame; i < targetFrame - DECODE_AFTER_SEEK; i++) {
                skipFrame();
                //store frame's position
                if (seekTable != null && i % 10000 == 0) {
                    seekTable.add(i, streamSize - bitstream.getPosition());
                }
            }

            //decode some frames to warm up the decoder
            int framesToDecode = targetFrame < DECODE_AFTER_SEEK ? targetFrame : DECODE_AFTER_SEEK;
            for (int i = 0; i < framesToDecode; i++) {
                readFrame = bitstream.readFrame();
                if (readFrame != null)
                    decoder.decodeFrame(readFrame, bitstream);
                bitstream.closeFrame();
            }

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String readLine(InputStream is) {
        try {
            int ch = is.read();

            StringBuilder sb = new StringBuilder();
            while (ch != '\n' && ch != '\r' && ch >= 0) {
                sb.append((char) ch);
                ch = is.read();
            }

            if (ch == '\n' || ch == '\r') {
                //noinspection ResultOfMethodCallIgnored
                is.read();
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean open(final Track track) {
        if (track == null)
            return false;
        this.track = track;
        try {
            URI location = track.getLocation();
            InputStream fis;
            if (track.isFile()) {
                logger.info("Opening file: " + location);
                streaming = false;
                fis = new FileInputStream(this.track.getFile());
                streamSize = this.track.getFile().length();
            } else {
                track.setCodec("MP3 Stream");
                logger.info("Opening stream: " + location);
                streaming = true;
                URLConnection urlConnection = location.toURL().openConnection();
                urlConnection.setRequestProperty("Icy-MetaData", "1");
                fis = urlConnection.getInputStream();

                String metaIntString = null;
                String contentType = urlConnection.getContentType();
                if (!contentType.equals("audio/mpeg")) {
                    if (contentType.equals("unknown/unknown")) {
                        //Java does not parse non-standart headers
                        //used by SHOUTCast
                        logger.fine("Reading SHOUTCast response");
                        String s = readLine(fis);
                        if (!s.equals("ICY 200 OK")) {
                            logger.warning("SHOUTCast invalid response: " + s);
                            return false;
                        }

                        while (true) {
                            s = readLine(fis);

                            if (s.isEmpty()) {
                                break;
                            }

                            String[] ss = s.split(":");
                            if (ss[0].equals("icy-metaint")) {
                                metaIntString = ss[1];
                            } else if (ss[0].equals("icy-genre")) {
                                track.setGenre(ss[1]);
                            } else if (ss[0].equals("icy-name")) {
                                track.setAlbum(ss[1]);
                            }
                        }
                    } else {
                        return false;
                    }
                } else {
                    metaIntString = urlConnection.getHeaderField("icy-metaint");
                    track.setGenre(urlConnection.getHeaderField("icy-genre"));
                    track.setAlbum(urlConnection.getHeaderField("icy-name"));
                }
                if (Util.isEmpty(metaIntString))
                    metaIntString = "0";
                final int metaInt = Integer.valueOf(metaIntString);
                if (metaInt > 0) {
                    fis = new FilterInputStream(fis) {
                        private int count = 0;

                        @Override
                        public int read(byte[] b, int off, int len) throws IOException {
                            int bytesToMeta = metaInt - count;
                            if (bytesToMeta == 0) {
                                int size = read() * 16;
                                if (size > 1) {
                                    byte[] meta = new byte[size];
                                    int i = super.read(meta, 0, size);
                                    String metaString = new String(meta, 0, i, "UTF-8");
                                    String title = "StreamTitle='";
                                    if (metaString.startsWith(title)) {
                                        String[] ss = metaString.substring(title.length(), metaString.indexOf(";") - 1).split(" - ");
                                        if (ss.length > 0) {
                                            if (ss.length > 1) {
                                                track.setArtist(ss[0]);
                                                track.setTitle(ss[1]);
                                            } else {
                                                track.setTitle(ss[0]);
                                            }
                                        }
                                    }
                                }
                                count = 0;
                            }

                            if (bytesToMeta >= 0 && bytesToMeta < len)
                                len = bytesToMeta;

                            int read = super.read(b, off, len);
                            count += read;
                            return read;
                        }
                    };
                }

                decoder = new Decoder();
            }
            bitstream = new Bitstream(fis);
            Header header = bitstream.readFrame();
            encDelay = header.getEncDelay();
            int encPadding = header.getEncPadding();
            int sampleRate = header.frequency();
            int channels = header.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
            track.setSampleRate(sampleRate);
            track.setChannels(channels);
            oldBitrate = track.getBitrate();
            samplesPerFrame = (int) (header.ms_per_frame() * header.frequency() / 1000);
            audioFormat = new AudioFormat(sampleRate, 16, channels, true, false);

            if (!streaming) {
                totalSamples = samplesPerFrame * (header.max_number_of_frames(streamSize) + header.min_number_of_frames(streamSize)) / 2;
                if (encPadding < totalSamples) {
                    totalSamples -= encPadding;
                }
                totalSamples -= encDelay;
                bitstream.close();
                fis.close();
                createBitstream(0);
            }

            currentSample = 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long targetSample) {
        currentSample = targetSample;
        createBitstream(targetSample);
    }

    public int decode(byte[] buf) {
        try {
            readFrame = bitstream.readFrame();

            if (readFrame == null) {
                return -1;
            }

            if (readFrame.bitrate_instant() > 0)
                track.setBitrate(readFrame.bitrate_instant() / 1000);

            if (!streaming && currentSample >= totalSamples)
                return -1;
            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(readFrame, bitstream);
            bitstream.closeFrame();
            int dataLen = output.getBufferLength() * 2;
            int len = dataLen - sampleOffset;
            if (dataLen == 0) {
                return 0;
            }

            toByteArray(output.getBuffer(), 0, output.getBufferLength());
            currentSample += AudioMath.bytesToSamples(len, audioFormat.getFrameSize());

            if (!streaming && currentSample > totalSamples) {
                len -= AudioMath.samplesToBytes(currentSample - totalSamples, audioFormat.getFrameSize());
            }
            System.arraycopy(buffer, sampleOffset, buf, 0, len);
            sampleOffset = 0;
            readFrame = null;
            return len;
        } catch (BitstreamException e) {
            e.printStackTrace();
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        if (bitstream != null)
            bitstream.close();
        track.setBitrate(oldBitrate);
        readFrame = null;
    }

    private void toByteArray(short[] samples, int offs, int len) {
        if (buffer.length < len * 2) {
            buffer = new byte[len * 2 + 1024];
        }
        int idx = 0;
        short s;
        while (len-- > 0) {
            s = samples[offs++];
            buffer[idx++] = (byte) s;
            buffer[idx++] = (byte) (s >>> 8);
        }
    }
}
