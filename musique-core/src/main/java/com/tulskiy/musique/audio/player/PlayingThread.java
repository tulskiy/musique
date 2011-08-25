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

import com.tulskiy.musique.audio.player.io.AudioOutput;
import com.tulskiy.musique.audio.player.io.Buffer;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.util.AudioMath;

import javax.sound.sampled.AudioFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.tulskiy.musique.audio.player.PlayerEvent.PlayerEventCode;

/**
 * Author: Denis Tulskiy
 * Date: 1/15/11
 */
public class PlayingThread extends Actor implements Runnable {
    public final Logger logger = Logger.getLogger(getClass().getName());
    private static final int BUFFER_SIZE = AudioOutput.BUFFER_SIZE;

    private AudioFormat format;
    private Player player;
    private Buffer buffer;
    private final Object lock = new Object();
    private AudioOutput output = new AudioOutput();
    private Track currentTrack;
    private long currentByte;
    private boolean active = false;
    private double playbackTime;
    private long playbackBytes;

    public PlayingThread(Player player, Buffer buffer) {
        this.player = player;
        this.buffer = buffer;
    }

    @Override
    public void process(Message message) {
        switch (message) {
            case PAUSE:
                setState(!active);
                break;
            case PLAY:
                setState(true);
                break;
            case STOP:
                stop();
                break;
        }
    }

    private void stop() {
        output.flush();
        setState(false);
        output.close();
        updatePlaybackTime();
        player.fireEvent(PlayerEventCode.STOPPED);
    }

    private void setState(boolean newState) {
        if (active != newState) {
            active = newState;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        byte[] buf = new byte[BUFFER_SIZE];
        while (true) {
            synchronized (lock) {
                try {
                    while (!active) {
                        player.fireEvent(PlayerEventCode.PAUSED);
                        output.stop();
                        System.gc();
                        lock.wait();
                    }

                    output.start();
                    player.fireEvent(PlayerEventCode.PLAYING_STARTED);
                    out : while (active) {
                        int len = buffer.read(buf, 0, BUFFER_SIZE);
                        while (len == -1) {
                            if (!openNext()) {
                                stop();
                                break out;
                            }
                            len = buffer.read(buf, 0, BUFFER_SIZE);
                        }
                        currentByte += len;
                        playbackBytes += len;
                        output.write(buf, 0, len);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception while playing. Stopping now", e);
                    currentTrack = null;
                    stop();
                }
            }
        }
    }

    private boolean openNext() {
        try {
            logger.fine("Getting next track");
            Buffer.NextEntry nextEntry = buffer.pollNextTrack();
            if (nextEntry.track == null) {
                return false;
            }
            currentTrack = nextEntry.track;
            if (nextEntry.forced) {
                output.flush();
            }
            format = nextEntry.format;
            output.init(format);
            if (nextEntry.startSample >= 0) {
                currentByte = AudioMath.samplesToBytes(nextEntry.startSample, format.getFrameSize());
                player.fireEvent(PlayerEventCode.SEEK_FINISHED);
            } else {
                currentByte = 0;
                updatePlaybackTime();
                player.fireEvent(PlayerEventCode.FILE_OPENED);
            }
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not open next track", e);
            return false;
        }
    }

    private void updatePlaybackTime() {
        if (format != null) {
            playbackTime = AudioMath.bytesToMillis(
                    playbackBytes, format);
        }
        playbackBytes = 0;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public AudioOutput getOutput() {
        return output;
    }

    public boolean isActive() {
        return active;
    }

    public long getCurrentSample() {
        if (format != null) {
            return AudioMath.bytesToSamples(currentByte, format.getFrameSize());
        } else return 0;
    }

    public double getPlaybackTime() {
        return playbackTime;
    }
}
