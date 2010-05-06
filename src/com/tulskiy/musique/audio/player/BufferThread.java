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
import com.tulskiy.musique.audio.io.PCMOutputStream;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.PluginLoader;
import com.tulskiy.musique.util.AudioMath;

import javax.sound.sampled.LineUnavailableException;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 4, 2009
 */
public class BufferThread extends Thread {
    private PCMOutputStream buffer;
    private PlayerThread playerThread;
    private final Object lock;
    private boolean waitForAction;
    private Song bufferingFile;
    private Decoder decoder;
    private long startPosition;
    private long offset;
    private long totalBytes;
    private long currentByte;
    private Playback playback;

    public BufferThread(PlayerThread playerThread, PCMOutputStream buffer) {
        this.playerThread = playerThread;
        this.buffer = buffer;
        lock = new Object();
        playback = new Playback();
        waitForAction = true;
        setPriority(Thread.MIN_PRIORITY);
    }

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        byte[] buf = new byte[65536];
        while (true) {
            synchronized (lock) {
                try {
                    while (waitForAction) {
                        lock.wait();
                    }

                    boolean done;
                    while (!waitForAction) {
                        int ret = decoder.decode(buf);
                        if (ret != -1) {
                            buffer.write(buf, 0, ret);
                            done = false;
                        } else
                            done = true;

                        currentByte = offset + buffer.getTotalBytes();

                        if (done || (totalBytes != -1 && currentByte >= totalBytes)) {
                            if (totalBytes == -1)
                                playerThread.setLastTotalBytes(currentByte);
                            openFile(playback.next(bufferingFile), false, totalBytes != -1);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    waitForAction = true;
                }
            }
        }
    }

    public void seek(long sample) {
        stopBuffering(true);
        synchronized (lock) {
            decoder.seekSample(startPosition + sample);
            offset = AudioMath.samplesToBytes(sample, decoder.getAudioFormat().getFrameSize());
            playerThread.setCurrentByte(offset);
            waitForAction = false;
            lock.notify();
        }
    }

    public void openFile(Song audioFile) {
        openFile(audioFile, true, false);
    }

    private void openFile(Song audioFile, boolean force, boolean noSeek) {
        stopBuffering(force);

        synchronized (lock) {
            close();
            if (audioFile == null) {
                waitForAction = true;
                return;
            }
            bufferingFile = audioFile;

            decoder = PluginLoader.getDecoder(audioFile);

            if (force)
                playerThread.reset();

            decoder.setOutputStream(buffer);
            startPosition = audioFile.getStartPosition();
            if (!noSeek) {
                if (!decoder.open(audioFile))
                    throw new RuntimeException("Decoder could not open the file");
                decoder.seekSample(startPosition);
            }
            offset = 0;
            currentByte = 0;
            buffer.reset();
            if (audioFile.getSubsongIndex() > 0) {
                totalBytes = AudioMath.samplesToBytes(audioFile.getTotalSamples(), decoder.getAudioFormat().getFrameSize());
            } else {
                totalBytes = -1;
            }

            playerThread.addFile(audioFile);
            playerThread.setLastTotalBytes(totalBytes);

            if (force)
                try {
                    playerThread.nextFile();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }

            waitForAction = false;
            lock.notify();
        }
    }

    public void stopBuffering(boolean flush) {
        waitForAction = true;
        if (flush)
            buffer.flush();
    }

    public void close() {
        stopBuffering(false);

        synchronized (lock) {
            if (decoder != null)
                decoder.close();
        }
    }
}
