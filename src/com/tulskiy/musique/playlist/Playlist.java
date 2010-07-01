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

import com.tulskiy.musique.gui.playlist.SeparatorTrack;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @Author: Denis Tulskiy
 * @Date: Dec 30, 2009
 */
public class Playlist extends ArrayList<Track> {
    private static final int VERSION = 1;
    private static String[] metaMap = {
            "artist", "album", "albumArtist", "title",
            "trackNumber", "totalTracks", "discNumber", "totalDiscs",
            "year", "genre", "comment"
    };

    private Logger logger = Logger.getLogger(Playlist.class.getName());
    private String name;

    public void save(File file) {
        try {
            //remove the garbage
            cleanUp();

            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)));
            dos.writeInt(VERSION);
            dos.writeInt(size());
            HashMap<String, String> meta = new HashMap<String, String>();
            for (Track track : this) {
                dos.writeUTF(track.getFile().getAbsolutePath());
                dos.writeLong(track.getStartPosition());
                dos.writeLong(track.getTotalSamples());
                dos.writeInt(track.getBps());
                dos.writeInt(track.getChannels());
                dos.writeInt(track.getSampleRate());

                meta.clear();
                for (String key : metaMap) {
                    String value = track.getMeta(key);
                    if (value != null && !value.isEmpty()) {
                        meta.put(key, value);
                    }
                }

                dos.writeInt(meta.size());
                for (Map.Entry<String, String> e : meta.entrySet()) {
                    dos.writeUTF(e.getKey());
                    dos.writeUTF(e.getValue());
                }
            }

            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cleanUp() {
        removeAll(Collections.singleton(new SeparatorTrack(null, 0)));
    }

    public void load(File file) {
        try {
            logger.info("Loading playlist: " + file.getName());
            DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(file)));

            int version = dis.readInt();
            if (version != VERSION) {
                logger.severe("Wrong playlist version: " + version);
                return;
            }
            int size = dis.readInt();
            for (int i = 0; i < size; i++) {
                Track track = new Track();
                track.setFile(new File(dis.readUTF()));
                track.setStartPosition(dis.readLong());
                track.setTotalSamples(dis.readLong());
                track.setBps(dis.readInt());
                track.setChannels(dis.readInt());
                track.setSampleRate(dis.readInt());

                int metaSize = dis.readInt();

                for (int j = 0; j < metaSize; j++) {
                    String key = dis.readUTF();
                    String value = dis.readUTF();
                    track.setMeta(key, value);
                }

                add(track);
            }

            dis.close();
        } catch (IOException e) {
            logger.warning("Failed to load playlist " + file.getName());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Playlist && this == o;
    }

    @Override
    public String toString() {
        return name;
    }
}

