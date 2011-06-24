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

package com.tulskiy.musique.audio.player;

import java.util.logging.Logger;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.player.io.Buffer;
import com.tulskiy.musique.playlist.PlaybackOrder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.system.Codecs;
import com.tulskiy.musique.util.AudioMath;

/**
 * Author: Denis Tulskiy
 * Date: 1/15/11
 */
public class BufferingThread extends Actor implements Runnable {
    private static final Logger logger = Logger.getLogger("musique");
    private PlaybackOrder order;

    private final Object lock = new Object();
    private long currentByte = 0;
    private Track currentTrack;
    private Track nextTrack;
    private Decoder decoder;
    private long cueTotalBytes;
    private boolean active;

    private Buffer buffer;
    private boolean stopAfterCurrent = false;

    public BufferingThread(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void process(Message message) {
        Object[] params = message.getParams();
        switch (message) {
            case OPEN:
                if (params.length > 0 && params[0] instanceof Track) {
                    Track track = (Track) params[0];
                    pause(true);
                    open(track, true);
                }
                break;
            case SEEK:
                if (params.length > 0 && params[0] instanceof Long) {
                    Long sample = (Long) params[0];
                    seek(sample);
                }
                break;
            case STOP:
                stop(true);
                break;
        }
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        byte[] buf = new byte[65536];
        int len;
        while (true) {
            synchronized (lock) {
                try {
                    while (!active) {
                        lock.wait();
                    }
                    if (decoder == null) {
                        stop(false);
                        continue;
                    }

                    while (active) {
                        if (nextTrack != null) {
                            if (stopAfterCurrent) {
                                stop(false);
                                stopAfterCurrent = false;
                                continue;
                            }
                            open(nextTrack, false);
                            nextTrack = null;
                        }

                        len = decoder.decode(buf);

                        if (len == -1) {
                            nextTrack = null;
                            if (order != null)
                                nextTrack = order.next(currentTrack);
                            if (nextTrack == null) {
                                stop(false);
                            }
                            continue;
                        }

                        if (currentTrack.getTrackData().isCue()) {
                            if (cueTotalBytes <= currentByte + len) {
                                Track s = null;
                                if (order != null)
                                    s = order.next(currentTrack);

                                len = (int) (cueTotalBytes - currentByte);
                                if (s != null) {
                                    nextTrack = s;
                                } else {
                                    stop(false);
                                }
                            }
                        }

                        currentByte += len;

                        buffer.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop(boolean flush) {
        logger.fine("Stop buffering");
        nextTrack = null;
        pause(flush);
        buffer.addNextTrack(null, null, -1, false);
        if (decoder != null) {
            decoder.close();
        }
        decoder = null;
    }

    private void pause(boolean flush) {
        active = false;
        if (flush)
            buffer.flush();
        synchronized (lock) {
        }
        if (flush)
            buffer.flush();
    }

    private void start() {
        active = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public synchronized void open(Track track, boolean forced) {
        if (decoder != null) {
            decoder.close();
        }

        if (track != null) {
            TrackData trackData = track.getTrackData();
            logger.fine("Opening track " + trackData.getLocation());

            if (trackData.isFile() && !trackData.getFile().exists()) {
                //try to get the next one
                track = order.next(track);
                if (track == null || (
                		trackData.isFile() && !trackData.getFile().exists())) {
                    decoder = null;
                    return;
                }
            }
            decoder = Codecs.getDecoder(track);
            currentTrack = track;
            currentByte = 0;

            if (decoder == null || !decoder.open(track)) {
                currentTrack = null;
                stop(false);
                return;
            }

            buffer.addNextTrack(currentTrack, decoder.getAudioFormat(), -1, forced);

            if (trackData.getStartPosition() > 0)
                decoder.seekSample(trackData.getStartPosition());
            if (trackData.getSubsongIndex() > 0) {
                cueTotalBytes = AudioMath.samplesToBytes(trackData.getTotalSamples(), decoder.getAudioFormat().getFrameSize());
            } else {
                cueTotalBytes = 0;
            }

            start();
            logger.fine("Finished opening track");
        }
    }

    public void seek(long sample) {
        boolean oldState = active;
        pause(true);

        if (decoder != null) {
            decoder.seekSample(currentTrack.getTrackData().getStartPosition() + sample);
            currentByte = AudioMath.samplesToBytes(sample, decoder.getAudioFormat().getFrameSize());
            buffer.addNextTrack(currentTrack, decoder.getAudioFormat(), sample, true);
            if (oldState) {
                start();
            }
        }
    }

    public PlaybackOrder getOrder() {
        return order;
    }

    public void setOrder(PlaybackOrder order) {
        this.order = order;
    }

    public boolean isActive() {
        return active;
    }

    public void setStopAfterCurrent(boolean stopAfterCurrent) {
        this.stopAfterCurrent = stopAfterCurrent;
    }
}
