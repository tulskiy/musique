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

package com.tulskiy.musique.playlist;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.gui.playlist.SeparatorTrack;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.AudioMath;
import com.tulskiy.musique.util.Util;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

import org.jaudiotagger.tag.FieldKey;

/**
 * @Author: Denis Tulskiy
 * @Date: Dec 30, 2009
 */
public class Playlist extends ArrayList<Track> {
    private static MessageFormat format = new MessageFormat("\"{0}\" \"{1}\" {2}");

    private static final int VERSION = 2;
    private static final byte[] MAGIC = "BARABASHKA".getBytes();

    private static final Logger logger = Logger.getLogger("musique");
    private ArrayList<PlaylistListener> listeners = new ArrayList<PlaylistListener>();
    private String name;
    private boolean sortAscending = true;
    private String sortBy;
    private String groupBy;
    private Expression groupExpression;
    private boolean libraryView;

    public Playlist(String fmt) {
        try {
            Object[] objects = format.parse(fmt);
            setName((String) objects[0]);
            setGroupBy((String) objects[1]);
            setLibraryView(Boolean.valueOf((String) objects[2]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Playlist() {

    }

    public void cleanUp() {
        Iterator<Track> it = iterator();
        while (it.hasNext()) {
            Track next = it.next();
            if (next instanceof SeparatorTrack)
                it.remove();
        }
    }

    public void save(File file) {
        try {
            //remove the garbage
            cleanUp();
            logger.fine("Saving playlist: " + file.getName());
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)));
            dos.write(MAGIC);
            dos.writeInt(VERSION);
            dos.writeInt(size());
            HashMap<String, String> meta = new HashMap<String, String>();
            TrackData trackData;
            for (Track track : this) {
            	trackData = track.getTrackData();
                dos.writeUTF(trackData.getLocation().toString());
                dos.writeLong(trackData.getStartPosition());
                dos.writeLong(trackData.getTotalSamples());
                dos.writeInt(trackData.getSubsongIndex());
                if (trackData.getSubsongIndex() > 0) {
                    dos.writeBoolean(trackData.isCueEmbedded());
                    if (!trackData.isCueEmbedded())
                        dos.writeUTF(trackData.getCueLocation());
                }
                dos.writeInt(trackData.getBps());
                dos.writeInt(trackData.getChannels());
                dos.writeInt(trackData.getSampleRate());
                dos.writeInt(trackData.getBitrate());
                dos.writeLong(trackData.getDateAdded());
                dos.writeLong(trackData.getLastModified());

                meta.clear();
                // TODO use CODEC const
                meta.put("codec", trackData.getCodec());
                for (FieldKey key : FieldKey.values()) {
                    List<String> values = trackData.getTagFieldValuesSafeAsList(key);
                    for (String value : values) {
                    	meta.put(key.toString(), value);
                    }
                }

                dos.writeInt(meta.size());
                for (Map.Entry<String, String> e : meta.entrySet()) {
                    dos.writeUTF(e.getKey());
                    dos.writeUTF(e.getValue());
                }
            }

            dos.close();
            regroup();
        } catch (IOException e) {
            logger.warning("Failed to save playlist " + file.getName() + ": " + e.getMessage());
        }
    }

    public void load(File file) {
        try {
            TrackDataCache cache = TrackDataCache.getInstance();
            logger.fine("Loading musique playlist: " + file.getName());
            DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(file)));

            byte[] b = new byte[MAGIC.length];
            dis.readFully(b);
            if (!Arrays.equals(b, MAGIC)) {
                logger.warning("Wrong magic word");
                throw new RuntimeException();
            }
            int version = dis.readInt();
            if (version > VERSION) {
                logger.warning("Playlist has newer version, expected: " + VERSION + " got: " + version);
                throw new RuntimeException();
            }
            int size = dis.readInt();
            ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                Track track = new Track();
                TrackData trackData = track.getTrackData();
                trackData.setLocation(dis.readUTF());
                trackData.setStartPosition(dis.readLong());
                trackData.setTotalSamples(dis.readLong());
                trackData.setSubsongIndex(dis.readInt());
                cache.cache(track);
                if (trackData.getSubsongIndex() > 0) {
                    trackData.setCueEmbedded(dis.readBoolean());
                    if (!trackData.isCueEmbedded())
                        trackData.setCueLocation(dis.readUTF());
                }
                trackData.setBps(dis.readInt());
                trackData.setChannels(dis.readInt());
                trackData.setSampleRate(dis.readInt());
                trackData.setBitrate(dis.readInt());
                if (version == 1) {
                    trackData.setDateAdded(System.currentTimeMillis());
                } else {
                    trackData.setDateAdded(dis.readLong());
                    trackData.setLastModified(dis.readLong());
                }
                int metaSize = dis.readInt();

                for (int j = 0; j < metaSize; j++) {
                    String key = dis.readUTF();
                    String value = dis.readUTF();
                    // TODO use CODEC const
                    if (key.equals("codec")) {
                        trackData.setCodec(value);
                    }
                	else {
                        trackData.addTagFieldValues(FieldKey.valueOf(key), value);
                    }
                }

                add(track);
            }

            dis.close();
        } catch (Exception e) {
            logger.warning("Failed to load playlist " + file.getName() + ": " + e.getMessage());
        }
    }

    public boolean isLibraryView() {
        return libraryView;
    }

    public void setLibraryView(boolean libraryView) {
        this.libraryView = libraryView;
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

    public List<String> loadM3U(String location) {
        Scanner fi;
        ArrayList<String> items = new ArrayList<String>();
        logger.fine("Loading M3U from: " + location);
        try {
            File parent = null;
            if (location.toLowerCase().startsWith("http://")) {
                fi = new Scanner(new URL(location).openStream());
            } else {
                File source = new File(location);
                fi = new Scanner(source);
                parent = source.getParentFile().getAbsoluteFile();
            }

            while (fi.hasNextLine()) {
                String line = fi.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                // skip utf8 BOM
                if (((int) line.charAt(0)) == 0xFEFF) {
                    line = line.substring(1);
                }

                if (line.toLowerCase().startsWith("http://")) {
                    items.add(line);
                } else {
                    //it's a file, resolve it
                    File file = new File(line);
                    if (!file.isAbsolute())
                        file = new File(parent, line);
                    items.add(file.getAbsolutePath());
                }
            }
            fi.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    public void saveM3U(File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.println("#EXTM3U");
            Expression expression = Parser.parse("[%artist% - ]%title%");
            for (Track track : this) {
                if (track.getTrackData().isStream()) {
                    pw.println(track.getTrackData().getLocation());
                } else if (track.getTrackData().isFile()) {
                    int seconds = (int) AudioMath.samplesToMillis(
                            track.getTrackData().getTotalSamples(),
                            track.getTrackData().getSampleRate()) / 1000;
                    String title = String.valueOf(expression.eval(track));
                    pw.printf("#EXTINF:%d,%s\n%s\n",
                            seconds, title,
                            track.getTrackData().getFile().getAbsolutePath());
                }
            }
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<String> loadPLS(String location) {
        Scanner fi;
        ArrayList<String> items = new ArrayList<String>();
        logger.fine("Loading PLS from: " + location);

        try {
            if (location.toLowerCase().startsWith("http://")) {
                fi = new Scanner(new URL(location).openStream());
            } else {
                fi = new Scanner(new File(location));
            }

            if (!fi.nextLine().equalsIgnoreCase("[playlist]")) {
                logger.warning("PLS has to start with [playlist]: " + location);
                return items;
            }

            fi.useDelimiter("[=\\p{javaWhitespace}+]");
            while (fi.hasNext()) {
                String line = fi.next().trim();
                if (line.toLowerCase().startsWith("file")) {
                    items.add(fi.next());
                }
            }
            fi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public void savePLS(File file) {
        try {
            PrintWriter pw = new PrintWriter(file);
            Expression expression = Parser.parse("[%artist% - ]%title%");
            pw.println("[playlist]");
            pw.println("NumberOfEntries=" + size());
            for (int i = 0; i < size(); i++) {
                Track track = get(i);
                int index = i + 1;
                if (track.getTrackData().isFile()) {
                    pw.printf("File%d=%s\n", index, track.getTrackData().getFile().getAbsolutePath());
                    pw.printf("Title%d=%s\n", index, expression.eval(track));
                    pw.printf("Length%d=%s\n", index, (int) AudioMath.samplesToMillis(
                            track.getTrackData().getTotalSamples(),
                            track.getTrackData().getSampleRate()) / 1000);
                } else if (track.getTrackData().isStream()) {
                    pw.printf("File%d=%s\n", index, track.getTrackData().getLocation().normalize());
                }
                pw.println();
            }
            pw.println("Version=2");
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int insertItem(String address, int location, boolean recurse, Map<String, Object> progress) {
        ArrayList<Track> temp = new ArrayList<Track>();
        LinkedList<Object> queue = new LinkedList<Object>();

        if (location == -1)
            location = size();
        String ext = Util.getFileExt(address);
        if (ext.equals("m3u") || ext.equals("m3u8")) {
            queue.addAll(loadM3U(address));
        } else if (ext.equals("pls")) {
            queue.addAll(loadPLS(address));
        } else if (ext.equals("mus")) {
            Playlist newPl = new Playlist();
            newPl.load(new File(address));
            addAll(location, newPl);
        } else {
            queue.push(address);
        }

        while (!queue.isEmpty()) {
            try {
                Object top = queue.pop();
                String topStr = top.toString();
                if (progress != null) {
                    if (progress.get("processing.stop") != null) {
                        break;
                    }
                    progress.put("processing.file", topStr);
                }

                if (top instanceof String && topStr.startsWith("http://")) {
                    Track track = new Track();

                    URI uri = new URI(topStr);
                    String title = uri.getPath();
                    if (Util.isEmpty(title))
                        title = uri.getHost();
                    track.getTrackData().addTitle(title);
                    track.getTrackData().setLocation(uri.toString());
                    track.getTrackData().setTotalSamples(-1);
                    temp.add(track);
                } else {
                    File file = null;
                    if (top instanceof String)
                        file = new File(topStr);
                    else if (top instanceof File)
                        file = (File) top;

                    if (recurse && file.isDirectory()) {
                        queue.addAll(0, Arrays.asList(file.listFiles()));
                    } else if (file.isFile()) {
                        AudioFileReader reader = TrackIO.getAudioFileReader(file.getName());
                        if (reader != null) {
                            if (!Util.getFileExt(file).equals("cue")) {
                                String name = Util.removeExt(file.getAbsolutePath()) + ".cue";
                                if (new File(name).exists()) {
                                    continue;
                                }
                            }
                            reader.read(file, temp);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        Collections.sort(temp, new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                return o1.getTrackData().getLocation().compareTo(o2.getTrackData().getLocation());
            }
        });
        TrackDataCache cache = TrackDataCache.getInstance();
        for (Track track : temp) {
            cache.cache(track);
        }
        addAll(location, temp);
        firePlaylistChanged();
        int size = temp.size();
        queue.clear();
        temp.clear();
        return size;
    }

    public void sort(String expression, boolean toggle) {
        cleanUp();
        logger.fine("Sorting playlist with expression: " + expression);
        if (toggle && expression.equals(sortBy)) {
            sortAscending = !sortAscending;
        } else {
            sortAscending = true;
            sortBy = expression;
        }

        final Expression e = Parser.parse(expression);
        Collections.sort(this, new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                try {
                    Object v1 = e.eval(o1);
                    Object v2 = e.eval(o2);
                    if (v1 != null && v2 != null) {
                        int i = v1.toString().compareToIgnoreCase(v2.toString());
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

    public void setGroupBy(String expression) {
        groupBy = expression;
        logger.fine("Grouping playlist with expression: " + expression);
        groupExpression = Util.isEmpty(expression) ? null : Parser.parse(expression);

        firePlaylistChanged();
    }

    public void firePlaylistChanged() {
        regroup();
        for (PlaylistListener listener : listeners) {
            listener.playlistUpdated(this);
        }
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
        return format.format(new Object[]{name, groupBy, libraryView});
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Playlist && this == o;
    }

    public void removeDeadItems() {
        for (Iterator it = this.iterator(); it.hasNext();) {
            Track track = (Track) it.next();
            if (track.getTrackData().getLocation() == null)
                continue;
            if (track.getTrackData().isFile() && !track.getTrackData().getFile().exists()) {
                it.remove();
            }
        }
        firePlaylistChanged();
    }

    public void removeDuplicates() {
        ArrayList<Track> dup = new ArrayList<Track>();
        for (int i = 0; i < size() - 1; i++) {
            Track t1 = get(i);
            URI l1 = t1.getTrackData().getLocation();
            if (l1 == null)
                continue;
            for (int j = i + 1; j < size(); j++) {
                Track t2 = get(j);

                if (l1.equals(t2.getTrackData().getLocation()) &&
                        t1.getTrackData().getSubsongIndex() == t2.getTrackData().getSubsongIndex()) {
                    dup.add(t2);
                }
            }
        }

        removeAll(dup);
        firePlaylistChanged();
    }

    public void addChangeListener(PlaylistListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(PlaylistListener listener) {
        listeners.remove(listener);
    }
}

