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

package com.tulskiy.musique.audio.player;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.PlaybackOrder;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Codecs;
import com.tulskiy.musique.util.AudioMath;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import java.util.ArrayList;

import static com.tulskiy.musique.audio.player.PlayerEvent.PlayerEventCode.*;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 21, 2010
 */
public class Player {
    private static enum PlayerState {
        PLAYING, PAUSED, STOPPED
    }

    private ArrayList<PlayerListener> listeners = new ArrayList<PlayerListener>();
    private PlayerState state = PlayerState.STOPPED;
    private PlayerThread playerThread = new PlayerThread();
    private PlaybackOrder order;
    private AudioOutput output;
    private boolean stopAfterCurrent = false;

    public Player() {
        this.output = new AudioOutput();
    }

    public void open(Track track) {
        playerThread.open(track, true);
    }

    public void play() {
        if (state != PlayerState.PAUSED)
            open(playerThread.currentTrack);

        playerThread.play();
    }

    public void pause() {
        if (isPlaying())
            playerThread.pause();
        else if (isPaused())
            playerThread.play();
    }

    public void seek(long sample) {
        playerThread.seek(sample);
    }

    public void stop() {
        playerThread.stopPlaying();
    }

    public void next() {
        Track s = order.next(playerThread.currentTrack);
        if (s != null) {
            playerThread.open(s, true);
            playerThread.play();
        } else {
            stop();
        }
    }

    public void prev() {
        Track s = order.prev(playerThread.currentTrack);
        if (s != null) {
            playerThread.open(s, true);
            playerThread.play();
        } else {
            stop();
        }
    }

    public AudioOutput getAudioOutput() {
        return output;
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    public long getCurrentSample() {
        return playerThread.getCurrentSample();
    }

    public Track getTrack() {
        return playerThread.currentTrack;
    }

    public double getPlaybackTime() {
        return playerThread.playbackTime;
    }

    public boolean isPlaying() {
        return state == PlayerState.PLAYING;
    }

    public boolean isPaused() {
        return state == PlayerState.PAUSED;
    }

    public boolean isStopped() {
        return state == PlayerState.STOPPED;
    }

    public void setStopAfterCurrent(boolean stopAfterCurrent) {
        this.stopAfterCurrent = stopAfterCurrent;
    }

    public void setPlaybackOrder(PlaybackOrder order) {
        this.order = order;
    }

    public PlaybackOrder getPlaybackOrder() {
        return order;
    }

    void setState(PlayerState state) {
        this.state = state;
        PlayerEvent.PlayerEventCode code = null;
        switch (state) {
            case PLAYING:
                code = PLAYING_STARTED;
                break;
            case PAUSED:
                code = PAUSED;
                break;
            case STOPPED:
                code = STOPPED;
        }

        if (code != null) {
            fireEvent(code);
        }
    }

    void fireEvent(PlayerEvent.PlayerEventCode event) {
        PlayerEvent e = new PlayerEvent(event);
        for (PlayerListener listener : listeners) {
            listener.onEvent(e);
        }
    }

    class PlayerThread extends Thread {
        private final Object lock = new Object();
        private long currentByte = 0;
        private boolean paused = true;
        private Track currentTrack;
        private Track nextTrack;
        private Decoder decoder;
        private long cueTotalBytes;
        private boolean exit = false;
        private long playbackBytes;
        private double playbackTime;

        @SuppressWarnings({"ConstantConditions"})
        @Override
        public void run() {
            byte[] buf = new byte[65536];
            int len;
            while (!exit) {
                synchronized (lock) {
                    try {
                        while (paused) {
                            if (isPlaying())
                                setState(PlayerState.PAUSED);
                            lock.wait();
                        }
                        if (decoder == null) {
                            stopPlaying();
                            continue;
                        }

                        setState(PlayerState.PLAYING);

                        while (!paused) {
                            if (nextTrack != null) {
                                open(nextTrack, false);
                                nextTrack = null;
                                if (stopAfterCurrent) {
                                    stopPlaying();
                                    continue;
                                }
                            }

                            len = decoder.decode(buf);

                            if (len == -1) {
                                nextTrack = null;
                                if (order != null)
                                    nextTrack = order.next(currentTrack);
                                if (nextTrack == null) {
                                    stopPlaying();
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
                                        stopPlaying();
                                    }
                                }
                            }

                            currentByte += len;
                            playbackBytes += len;

                            output.write(buf, 0, len);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void pause() {
            paused = true;

            synchronized (lock) {
                output.stop();
            }
        }

        public void play() {
            if (!isAlive()) {
                start();
            }

            if (paused) {
                if (currentTrack == null && order != null) {
                    Track s = order.next(currentTrack);
                    if (s != null)
                        open(s, true);
                    else
                        return;
                }

                paused = false;

                synchronized (lock) {
                    output.start();
                    lock.notifyAll();
                }
            }
        }

        public void seek(long sample) {
            PlayerState oldState = state;
            pause();
            output.flush();

            if (decoder != null) {
                decoder.seekSample(currentTrack.getStartPosition() + sample);
                currentByte = AudioMath.samplesToBytes(sample, decoder.getAudioFormat().getFrameSize());

                if (oldState == PlayerState.PLAYING) {
                    play();
                }
            }
        }

        public synchronized void open(Track track, boolean force) {
            if (force) {
                //I shit a lot, need to flush twice :)
                output.flush();
                pause();
                output.flush();
            }

            try {
                if (decoder != null) {
                    playbackTime = AudioMath.bytesToMillis(
                            playbackBytes, decoder.getAudioFormat());

                    decoder.close();
                }

                if (track != null) {
                    if (track.isFile() && !track.getFile().exists()) {
                        decoder = null;
                        return;
                    }
                    decoder = Codecs.getDecoder(track);
                    currentTrack = track;
                    currentByte = 0;
                    playbackBytes = 0;

                    if (decoder == null || !decoder.open(track)) {
                        currentTrack = null;
                        stopPlaying();
                        return;
                    }

                    if (track.getStartPosition() > 0)
                        decoder.seekSample(track.getStartPosition());
                    if (track.getSubsongIndex() > 0) {
                        cueTotalBytes = AudioMath.samplesToBytes(track.getTotalSamples(), decoder.getAudioFormat().getFrameSize());
                    } else {
                        cueTotalBytes = 0;
                    }

                    output.init(decoder.getAudioFormat());
                    fireEvent(FILE_OPENED);
                }

            } catch (LineUnavailableException e) {
                System.err.println("Line is unavailable. Listing all mixers");
                System.err.println("See README for troubleshooting information");
                Mixer.Info[] infos = AudioSystem.getMixerInfo();
                for (Mixer.Info info : infos) {
                    System.err.println(info.getName() + ": " + info.getDescription());
                    Mixer mixer = AudioSystem.getMixer(info);
                    Line.Info[] lineInfo = mixer.getSourceLineInfo();
                    for (Line.Info info1 : lineInfo) {
                        System.err.println("\t" + info1);
                    }
                }
                for (Mixer.Info i : AudioSystem.getMixerInfo()) {
                    for (Line l : AudioSystem.getMixer(i).getSourceLines()) {
                        l.close();
                    }
                }
                pause();
            }
        }

        public void stopPlaying() {
            pause();
            if (decoder != null) {
                playbackTime = AudioMath.bytesToMillis(
                        playbackBytes, decoder.getAudioFormat());
                decoder.close();
            }
            decoder = null;
            setState(PlayerState.STOPPED);
        }

        public synchronized long getCurrentSample() {
            if (decoder != null) {
                return AudioMath.bytesToSamples(currentByte, decoder.getAudioFormat().getFrameSize());
            } else return 0;
        }
    }
}
