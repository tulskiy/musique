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

package com.tulskiy.musique.playlist;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.system.PluginLoader;
import org.jaudiotagger.tag.id3.valuepair.TextEncoding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * @Author: Denis Tulskiy
 * @Date: 23.06.2009
 */
public class TagProcessor {
    private final LinkedList<File> files;
    private final ArrayList<Song> audioFiles = new ArrayList<Song>();
    private Playlist playlist;

    public File getCurrentFile() {
        return currentFile;
    }

    public int getFilesLeft() {
        return files.size();
    }

    private File currentFile;

    public TagProcessor(LinkedList<File> files, Playlist playlist) {
        this.files = files;
        this.playlist = playlist;
    }

    public void start() {
        Worker[] workers = new Worker[10];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker();
            workers[i].start();
        }

        for (Worker w : workers)
            try {
                w.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        audioFiles.remove(null);
        Collections.sort(audioFiles, new Comparator<Song>() {
            public int compare(Song o1, Song o2) {
                return o1.getFile().getAbsolutePath().compareToIgnoreCase(o2.getFile().getAbsolutePath());
            }
        });

        playlist.addAll(audioFiles);
    }

    public void cancel() {
        synchronized (files) {
            files.clear();
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            File file;

            while (true) {
                synchronized (files) {
                    if (files.size() > 0) {
                        file = files.pop();
                    } else break;
                }

                synchronized (audioFiles) {
                    AudioFileReader reader = PluginLoader.getAudioFileReader(file.getName());
                    currentFile = file;
                    if (reader != null)
                        reader.read(file, audioFiles);
                }
            }
        }
    }
}

