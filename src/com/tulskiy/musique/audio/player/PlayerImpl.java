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
import com.tulskiy.musique.audio.player.dsp.PlaybackOrder;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.PluginLoader;
import com.tulskiy.musique.util.AudioMath;

import javax.sound.sampled.*;
import java.util.ArrayList;

import static com.tulskiy.musique.audio.player.PlayerEvent.PlayerEventCode;
import static com.tulskiy.musique.audio.player.PlayerEvent.PlayerEventCode.PAUSED;
import static com.tulskiy.musique.audio.player.PlayerEvent.PlayerEventCode.PLAYING_STARTED;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 21, 2010
 */
public class PlayerImpl implements Player {
    private PlayerState state = PlayerState.STOPPED;
    private static ArrayList<PlayerListener> listeners = new ArrayList<PlayerListener>();

    private PlayerThread playerThread = new PlayerThread();

    public void open(Song song) {
        playerThread.open(song, true);
    }

    public void play() {
        if (!playerThread.isAlive()) {
            playerThread.start();
        }

        if (state == PlayerState.PLAYING)
            open(playerThread.currentSong);

        playerThread.play();
    }

    public void pause() {
        playerThread.pause();
    }

    public void seek(long sample) {
        playerThread.seek(sample);
    }

    public void stop() {

    }

    public void next() {
        playerThread.next();
    }

    public void prev() {

    }

    public void setVolume(float volume) {
        FloatControl v = playerThread.volume;
        if (v != null) {
            v.setValue(v.getMaximum() * volume);
        }
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    public long getCurrentSample() {
        return playerThread.getCurrentSample();
    }

    public Song getSong() {
        return playerThread.currentSong;
    }

    public PlayerState getState() {
        return state;
    }

    public float getVolume() {
        if (playerThread.volume != null)
            return playerThread.volume.getValue();
        else
            return (float) 80.0;
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
        }

        if (code != null) {
            fireEvent(code);
        }
    }

    void fireEvent(PlayerEvent.PlayerEventCode event) {
        PlayerEvent e = new PlayerEvent(event);
        EventLauncher eventLauncher = new EventLauncher(e);
        eventLauncher.run();
    }

    private class EventLauncher extends Thread {
        private PlayerEvent e;

        private EventLauncher(PlayerEvent e) {
            this.e = e;
        }

        @Override
        public void run() {
            for (PlayerListener l : listeners) {
                l.onEvent(e);
            }
        }
    }

    class PlayerThread extends Thread {
        private final int BUFFER_SIZE = (int) Math.pow(2, 16);

        private final Object lock = new Object();
        private SourceDataLine line;
        private FloatControl volume;
        private long currentByte = 0;
        private boolean paused = true;
        private Song currentSong;
        private Song nextSong;
        private Decoder decoder;
        private PlaybackOrder order = new PlaybackOrder();
        private long cueTotalBytes;

        @SuppressWarnings({"InfiniteLoopStatement"})
        @Override
        public void run() {
            byte[] buf = new byte[65536];
            int len;
            while (true) {
                synchronized (lock) {
                    try {
                        while (paused) {
                            setState(PlayerState.PAUSED);
                            lock.wait();
                        }

                        setState(PlayerState.PLAYING);

                        while (!paused) {
                            if (nextSong != null) {
                                open(nextSong, false);
                                nextSong = null;
                            }

                            len = decoder.decode(buf);

                            if (len == -1) {
                                nextSong = order.next(currentSong);
                                if (nextSong == null)
                                    pause();
                                continue;
                            }

                            if (currentSong.getCueID() != -1) {
                                if (cueTotalBytes <= currentByte + len) {
                                    Song s = order.next(currentSong);

                                    if (s != null) {
                                        len = (int) (cueTotalBytes - currentByte);
                                        nextSong = s;
                                    }
                                }
                            }

                            currentByte += len;

                            line.write(buf, 0, len);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void initLine() throws LineUnavailableException {
            AudioFormat fmt = decoder.getAudioFormat();
            //if it is same format and the line is opened, do nothing
            if (line != null) {
                if (!line.getFormat().matches(fmt)) {
                    line.drain();
                    line.close();
                    line = null;
                } else {
                    return;
                }
            }
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt, BUFFER_SIZE);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt, BUFFER_SIZE);
            line.start();
            if (line.isControlSupported(FloatControl.Type.VOLUME)) {
                volume = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
            }
        }

        public void pause() {
            paused = true;

            synchronized (lock) {
                if (line != null)
                    line.stop();
            }
        }

        public void play() {
            if (paused) {
                if (currentSong == null) {
                    Song s = order.next(currentSong);
                    if (s != null)
                        open(s, true);
                    else
                        return;
                }

                paused = false;

                synchronized (lock) {
                    line.start();
                    lock.notifyAll();
                }
            }
        }

        public void next() {
            Song s = order.next(currentSong);
            if (s != null)
                open(s, true);
            else
                return;
            play();
        }

        public void seek(long sample) {
            PlayerState oldState = state;

            pause();

            if (line != null)
                line.flush();

            if (decoder != null) {
                decoder.seekSample(currentSong.getStartPosition() + sample);
                currentByte = AudioMath.samplesToBytes(sample, decoder.getAudioFormat().getFrameSize());

                if (oldState == PlayerState.PLAYING) {
                    play();
                }
            }
        }

        public synchronized void open(Song song, boolean force) {
            if (force) {
                if (line != null)
                    line.flush();

                pause();

                if (line != null)
                    line.flush();
            }

            try {
                if (decoder != null) {
                    decoder.close();
                }

                if (song != null) {
                    decoder = PluginLoader.getDecoder(song);
                    currentSong = song;
                    currentByte = 0;

                    if (decoder == null || !decoder.open(song))
                        return;

                    decoder.seekSample(song.getStartPosition());
                    if (song.getCueID() > 0) {
                        cueTotalBytes = AudioMath.samplesToBytes(song.getTotalSamples(), decoder.getAudioFormat().getFrameSize());
                    } else {
                        cueTotalBytes = 0;
                    }

                    initLine();

                    fireEvent(PlayerEventCode.FILE_OPENED);
                }

            } catch (LineUnavailableException e) {
                e.printStackTrace();
                pause();
            }
        }

        public synchronized long getCurrentSample() {
            if (decoder != null) {
                return AudioMath.bytesToSamples(currentByte, decoder.getAudioFormat().getFrameSize());
            } else return 0;
        }

        public long getTotalSamples() {
            if (currentSong != null) {
                return currentSong.getTotalSamples();
            } else return 0;
        }
    }
}
