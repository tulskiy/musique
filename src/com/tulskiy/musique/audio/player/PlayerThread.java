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

import com.tulskiy.musique.audio.io.PCMOutputStream;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.PluginLoader;
import com.tulskiy.musique.util.AudioMath;

import javax.sound.sampled.*;
import java.util.LinkedList;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 4, 2009
 */
public class PlayerThread extends Thread {
    private static final int READ_SIZE = (int) (Math.pow(2, 14));
    private static final int DEFAULT_PLAY_BUFFER = (int) (44100 * 4 * .2); //200ms buffer for source line

    private PCMOutputStream buffer;
    private Player2 player;
    private LinkedList<PlayingFile> playingFiles;
    private byte[] readBuf = new byte[READ_SIZE];
    private final Object lock;
    private boolean waitForAction = true;
    private PlayingFile currentPlayingFile;
    private SourceDataLine line;
    private FloatControl masterGain;
//    private DSPBank dspBank;

    public PlayerThread(Player2 player, PCMOutputStream buffer) {
        this.player = player;
        this.buffer = buffer;
        playingFiles = new LinkedList<PlayingFile>();
        lock = new Object();
        pause();
        waitForAction = true;
        currentPlayingFile = new PlayingFile(null);
//        dspBank = new DSPBank();
//        dspBank.addProcessor(new BeatVis());
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                try {
                    while (waitForAction) {
                        player.setState(PlayerState.PAUSED);
                        player.fireEvent(PlayerEvent.PlayerEventCode.PAUSED);
                        lock.wait();
                    }

                    player.fireEvent(PlayerEvent.PlayerEventCode.PLAYING_STARTED);
                    player.setState(PlayerState.PLAYING);
                    while (!waitForAction) {
                        int toRead = READ_SIZE;
                        if (currentPlayingFile.totalBytes != -1) {
                            toRead = (int) Math.min(toRead, currentPlayingFile.totalBytes - currentPlayingFile.currentByte);
                        }

                        int len = buffer.read(readBuf, 0, toRead);

                        currentPlayingFile.currentByte += len;

                        line.write(readBuf, 0, len);
//                        dspBank.process(readBuf, 0, len, 16, false);
                        if (currentPlayingFile.currentByte == currentPlayingFile.totalBytes) {
                            nextFile();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    pause();
                }
            }
        }
    }

    void nextFile() throws LineUnavailableException {
        if (playingFiles.isEmpty()) {
            pause();
            player.fireEvent(PlayerEvent.PlayerEventCode.FINISHED_PLAYING);
        } else {
            currentPlayingFile = playingFiles.removeFirst();
            initLine();
            player.fireEvent(PlayerEvent.PlayerEventCode.FILE_OPENED);
        }
    }

    private void initLine() throws LineUnavailableException {
        AudioFormat fmt = PluginLoader.getDecoder(currentPlayingFile.file).getAudioFormat();
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
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt, DEFAULT_PLAY_BUFFER);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(fmt, DEFAULT_PLAY_BUFFER);
        line.start();
        if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            masterGain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        }
    }


    public void play() {
        waitForAction = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    public void pause() {
        waitForAction = true;
    }

    public Song getPlayingFile() {
        return currentPlayingFile.file;
    }

    public void setVolume(float value) {
        masterGain.setValue(value);
    }

    public long getTotalSamples() {
        return currentPlayingFile.file.getTotalSamples();
    }

    public long getCurrentSample() {
        return AudioMath.bytesToSamples(currentPlayingFile.currentByte,
                currentPlayingFile.frameSize);
    }

    void addFile(Song file) {
        playingFiles.addLast(new PlayingFile(file));
    }

    void setLastTotalBytes(long totalBytes) {
        if (playingFiles.isEmpty())
            currentPlayingFile.totalBytes = totalBytes;
        else
            playingFiles.getLast().totalBytes = totalBytes;
    }

    void reset() {
        pause();

        synchronized (lock) {
            currentPlayingFile = null;
            playingFiles.clear();
        }
    }

    public void setCurrentByte(long offset) {
//        pause();
        currentPlayingFile.currentByte = offset;
//        play();
    }

    class PlayingFile {
        Song file;
        long totalBytes;
        long currentByte;
        int frameSize;

        PlayingFile(Song file) {
            this.file = file;
            currentByte = 0;
            totalBytes = -1;
            if (file != null)
                frameSize = PluginLoader.getDecoder(file).getAudioFormat().getFrameSize();
        }
    }
}
