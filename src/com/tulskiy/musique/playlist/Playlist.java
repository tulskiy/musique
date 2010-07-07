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
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Main;
import com.tulskiy.musique.util.Util;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @Author: Denis Tulskiy
 * @Date: Dec 30, 2009
 */
public class Playlist extends ArrayList<Track> {
    private static Parser parser = new Parser();

    private static MessageFormat format = new MessageFormat("\"{0}\" \"{1}\"");

    private static final int VERSION = 1;
    private static String[] metaMap = {
            "artist", "album", "albumArtist", "title",
            "trackNumber", "totalTracks", "discNumber", "totalDiscs",
            "year", "genre", "comment"
    };

    private static final Logger logger = Logger.getLogger(Playlist.class.getName());
    private String name;
    private boolean sortAscending = true;
    private String sortBy;
    private String groupBy;
    private Expression groupExpression;

    public Playlist(String fmt) {
        try {
            Object[] objects = format.parse(fmt);
            setName((String) objects[0]);
            groupBy((String) objects[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Playlist() {

    }

    public void cleanUp() {
        removeAll(Collections.singleton(new SeparatorTrack(null, 0)));
    }

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
                dos.writeUTF(track.getLocation().toString());
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
                track.setLocation(new URI(dis.readUTF()));
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
        } catch (Exception e) {
            logger.warning("Failed to load playlist " + file.getName());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void addLocation(String location) {
        try {
            URI loc = new URI(location);

            String scheme = loc.getScheme();
            if ("http".equals(scheme)) {
                Track track = new Track();

                String title = loc.getPath();
                if (Util.isEmpty(title))
                    title = loc.getHost();
                track.setTitle(title);
                track.setLocation(loc);
                track.setTotalSamples(-1);
                add(track);
            } else if (scheme == null || "file".equals(scheme)) {

            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void sort(String expression) {
        cleanUp();

        if (expression.equals(sortBy)) {
            sortAscending = !sortAscending;
        } else {
            sortAscending = true;
            sortBy = expression;
        }

        final Expression e = parser.parse(expression);
        Collections.sort(this, new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                try {
                    Object v1 = e.eval(o1);
                    Object v2 = e.eval(o2);
                    if (v1 != null && v2 != null) {
                        int i = v1.toString().compareTo(v2.toString());
                        if (!sortAscending)
                            i = -i;
                        return i;
                    }
                } catch (Exception ignored) {
                }
                return 0;
            }
        });

        firePlaylistChanged();
    }

    public void groupBy(String expression) {
        groupBy = expression;
        groupExpression = Util.isEmpty(expression) ? null : parser.parse(expression);

        regroup();
    }

    public void firePlaylistChanged() {
        regroup();
    }

    public void regroup() {
        cleanUp();

        if (groupExpression == null)
            return;

        int start = 0;
        int size = 0;
        final String unknown = "?";
        String groupName = null;
        for (int i = 0; i < size(); i++) {
            Track track = get(i);
            Object o = groupExpression.eval(track);
            String value = null;
            if (o != null)
                value = o.toString();

            if (Util.isEmpty(value))
                value = unknown;

            if (groupName == null) {
                groupName = value;
                start = i;
                size = 1;
                continue;
            }

            //noinspection ConstantConditions
            boolean sameGroup = value.equalsIgnoreCase(groupName);
            if (sameGroup)
                size++;

            if (!sameGroup) {
                if (size > 0) {
                    addGroup(groupName, start, size);
                } else {
                    i--;
                }

                groupName = null;
            }
        }

        if (groupName != null)
            addGroup(groupName, start, size);
    }

    private void addGroup(String groupName, int start, int size) {
        SeparatorTrack group = new SeparatorTrack(groupName, size);
        add(start, group);
    }

    @Override
    public Track get(int index) {
        return index >= 0 && index < size() ? super.get(index) : null;
    }

    @Override
    public String toString() {
        if (groupBy == null)
            groupBy = "";
        return format.format(new Object[]{name, groupBy});
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Playlist && this == o;
    }
}

