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

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.player.io.Buffer;
import com.tulskiy.musique.playlist.PlaybackOrder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Codecs;
import com.tulskiy.musique.util.AudioMath;

import static com.tulskiy.musique.audio.player.PlayerEvent.PlayerEventCode;

/**
 * Author: Denis Tulskiy
 * Date: 1/15/11
 */
public class BufferingThread extends Actor implements Runnable {
    private PlaybackOrder order;

    private final Object lock = new Object();
    private long currentByte = 0;
    private Track currentTrack;
    private Track nextTrack;
    private Decoder decoder;
    private long cueTotalBytes;
    private long totalBytes;
    private double playbackTime;
    private boolean active;

    private Player player;
    private Buffer buffer;

    public BufferingThread(Player player, Buffer buffer) {
        this.player = player;
        this.buffer = buffer;
    }

    @Override
    public void process(Message message) {
        Object[] params = message.getParams();
        switch (message) {
            case OPEN:
                if (params.length > 0 && params[0] instanceof Track) {
                    Track track = (Track) params[0];
                    pause();
                    open(track);
                }
                break;
            case SEEK:
                if (params.length > 0 && params[0] instanceof Long) {
                    Long sample = (Long) params[0];
                    seek(sample);
                }
                break;
            case STOP:
                stop();
                player.fireEvent(PlayerEventCode.STOPPED);
                break;
        }
    }

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
                        stop();
                        continue;
                    }

                    while (active) {
                        if (nextTrack != null) {
                            open(nextTrack);
                            nextTrack = null;
//                            if (stopAfterCurrent) {
//                                stopPlaying();
//                                continue;
//                            }
                        }

                        len = decoder.decode(buf);

                        if (len == -1) {
                            nextTrack = null;
                            if (order != null)
                                nextTrack = order.next(currentTrack);
                            if (nextTrack == null) {
                                stop();
                            }
                            continue;
                        }

                        if (currentTrack.isCue()) {
                            if (cueTotalBytes <= currentByte + len) {
                                Track s = null;
                                if (order != null)
                                    s = order.next(currentTrack);

                                len = (int) (cueTotalBytes - currentByte);
                                if (s != null) {
                                    nextTrack = s;
                                } else {
                                    stop();
                                }
                            }
                        }

                        currentByte += len;
                        totalBytes += len;

                        buffer.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        System.out.println("Stop buffering");
        pause();
        if (decoder != null) {
            playbackTime = AudioMath.bytesToMillis(
                    totalBytes, decoder.getAudioFormat());
            decoder.close();
        }
        decoder = null;
    }

    private void pause() {
        active = false;
        buffer.flush();
        synchronized (lock) {

        }
        byte[] b = new byte[(int) Math.pow(2, 10)];
        buffer.write(b, 0, b.length);
    }

    private void start() {
        active = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public synchronized void open(Track track) {
        System.out.println("In open");

        if (decoder != null) {
            playbackTime = AudioMath.bytesToMillis(
                    totalBytes, decoder.getAudioFormat());

            decoder.close();
        }

        if (track != null) {
            if (track.isFile() && !track.getFile().exists()) {
                //try to get the next one
                track = order.next(track);
                if (track == null || (
                        track.isFile() && !track.getFile().exists())) {
                    decoder = null;
                    return;
                }
            }
            decoder = Codecs.getDecoder(track);
            currentTrack = track;
            currentByte = 0;
            totalBytes = 0;

            if (decoder == null || !decoder.open(track)) {
                currentTrack = null;
                stop();
                return;
            }

            buffer.addNextTrack(currentTrack, decoder.getAudioFormat(), 0);
//            byte[] empty = new byte[44100];
//            buffer.write(empty, 0, empty.length);

            if (track.getStartPosition() > 0)
                decoder.seekSample(track.getStartPosition());
            if (track.getSubsongIndex() > 0) {
                cueTotalBytes = AudioMath.samplesToBytes(track.getTotalSamples(), decoder.getAudioFormat().getFrameSize());
            } else {
                cueTotalBytes = 0;
            }

            start();
            System.out.println("finished open");
        }
    }

    public void seek(long sample) {
        boolean oldState = active;
        pause();

        if (decoder != null) {
            decoder.seekSample(currentTrack.getStartPosition() + sample);
            currentByte = AudioMath.samplesToBytes(sample, decoder.getAudioFormat().getFrameSize());
            buffer.addNextTrack(currentTrack, decoder.getAudioFormat(), sample);
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
}
