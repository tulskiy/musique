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

/**
 * @Author: Denis Tulskiy
 * @Date: 23.07.2009
 */
package com.tulskiy.musique.audio.player;

import com.tulskiy.musique.audio.io.Buffer;
import com.tulskiy.musique.audio.io.PCMOutputStream;

import static com.tulskiy.musique.audio.player.PlayerEvent.PlayerEventCode.*;

import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.util.AudioMath;

import java.util.Vector;
import java.util.logging.Logger;

public class Player2 implements Player {
    private static final int DEFAULT_PCM_BUFFER = (int) (44100 * 4); //1 sec

    private static Vector<PlayerListener> listeners = new Vector<PlayerListener>();
    private static Logger logger = Logger.getLogger(Player2.class.getName());

    private PlayerState state;
    private PlayerThread playerThread;
    private BufferThread bufferThread;

    public Player2() {
        PCMOutputStream buffer = new Buffer(DEFAULT_PCM_BUFFER);
        playerThread = new PlayerThread(this, buffer);
        bufferThread = new BufferThread(playerThread, buffer);
//        addListener(bufferThread);
        playerThread.start();
        bufferThread.start();
    }

    public int getSampleRate() {
        return playerThread.getPlayingFile().getSamplerate();
    }

    public void open(Song audioFile) {
        bufferThread.openFile(audioFile);
    }

    public void play() {
        playerThread.play();
    }

    public void pause() {
        playerThread.pause();
    }

    public void seek(long sample) {
        bufferThread.seek(sample);
    }

    public void stop() {
        try {
            playerThread.pause();
            bufferThread.stopBuffering(true);
            bufferThread.close();
        } catch (Exception ignored) {

        }
    }

    public void next() {
    }

    public void prev() {
    }

    public void setVolume(float volume) {
        playerThread.setVolume(AudioMath.linearToDb(volume));
    }

    public void addListener(PlayerListener listener) {
        listeners.add(listener);
    }

    public long getTotalSamples() {
        return playerThread.getTotalSamples();
    }

    public long getCurrentSample() {
        return playerThread.getCurrentSample();
    }

    public Song getSong() {
        return playerThread.getPlayingFile();
    }

    public PlayerState getState() {
        return state;
    }

    public float getVolume() {
        return 0;
    }

    void setState(PlayerState state) {
        this.state = state;
        logger.fine("Curent state is: " + state);
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


}
