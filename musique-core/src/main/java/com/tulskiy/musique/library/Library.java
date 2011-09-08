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

package com.tulskiy.musique.library;

import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Codecs;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.system.configuration.Configuration;
import com.tulskiy.musique.system.configuration.LibraryConfiguration;
import com.tulskiy.musique.util.Util;

import javax.swing.tree.TreeNode;

import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: 10/30/10
 */
public class Library {
    private final Logger logger = Logger.getLogger(getClass().getName());
    private static final String DEFAULT_VIEW = "$if3(%albumArtist%,'?')|$if1(%album%,[[%year% - ]%album%],'?')$if1($greater(%discTotal%,1),[|Disc %disc%],'')|[%trackNumber%. ]%title%";
    private Configuration config = Application.getInstance().getConfiguration();
    private Playlist data;
    private String view;
    private TreeNode rootNode;

    public Library(Playlist data) {
        this.data = data;
        rebuildTree();
    }

    public void rescan(Map<String, Object> progress) {
        List<String> folders = LibraryConfiguration.getFolders();
        if (CollectionUtils.isEmpty(folders)) {
            return;
        }
        progress.put("processing.file", "");

        data.removeDeadItems();

        HashMap<TrackData, Track> trackDatas = new HashMap<TrackData, Track>();
        for (Track track : data) {
            trackDatas.put(track.getTrackData(), track);
        }

        LinkedList<File> queue = new LinkedList<File>();
        for (String path : folders) {
            File f = new File(path);
            if (f.exists())
                queue.add(f);
        }

        HashSet<Track> processed = new HashSet<Track>();
        final Set<String> formats = Codecs.getFormats();
        ArrayList<Track> temp = new ArrayList<Track>();
        while (!queue.isEmpty()) {
            try {
                File file = queue.pop();
                if (progress != null) {
                    if (progress.get("processing.stop") != null) {
                        break;
                    }
                    progress.put("processing.file", file.getAbsolutePath());
                }
                if (file.isDirectory()) {
                    queue.addAll(0, Arrays.asList(file.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            if (file.isHidden() || !file.canRead()) {
                                return false;
                            }

                            if (file.isDirectory())
                                return true;

                            String ext = Util.getFileExt(file).toLowerCase();
                            if (formats.contains(ext)) {
                                String name = Util.removeExt(file.getAbsolutePath()) + ".cue";
                                return !new File(name).exists();
                            }
                            return ext.equals("cue");
                        }
                    })));
                } else {
                    TrackData trackData = new TrackData(file.toURI(), 0);
                    Track track = trackDatas.get(trackData);
                    if (track != null) {
                        if (track.getTrackData().getLastModified() != file.lastModified()) {
                            track.getTrackData().clearTags();
                            TrackIO.getAudioFileReader(file.getName()).reload(track);
                        }
                        processed.add(track);
                    } else {
                        temp.clear();
                        TrackIO.getAudioFileReader(file.getName()).read(file, temp);
                        for (Track newTrack : temp) {
                            trackData = newTrack.getTrackData();
                            if (trackDatas.containsKey(trackData)) {
                                // it must be the cue file, so  merge the track data
                                trackData.merge(newTrack.getTrackData());
                            }
                            processed.add(newTrack);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        data.clear();
        data.addAll(processed);
        processed.clear();
        trackDatas.clear();
        rebuildTree();
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public Playlist getData() {
        return data;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
        rebuildTree();
    }

    private void rebuildTree() {
        logger.fine("Rebuilding tree");
        long time = System.currentTimeMillis();
        if (rootNode == null) {
            rootNode = new MappedTreeNode("All music");
        }

        ((MappedTreeNode) rootNode).removeAllChildren();
        if (data == null) {
            return;
        }

        if (Util.isEmpty(view)) {
            view = DEFAULT_VIEW;
        }

        Expression expr = Parser.parse(view);
        for (Track track : data) {
            Object val = expr.eval(track);
            if (val != null) {
                String[] path = val.toString().split("\\|");

                if (path.length < 2) {
                    continue;
                }

                MappedTreeNode node = (MappedTreeNode) rootNode;
                for (int i = 0; i < path.length - 1; i++) {
                    node = node.get(path[i]);
                }

                //noinspection RedundantStringConstructorCall
                node.add(new TrackNode(track, new String(path[path.length - 1])));
            }
        }
        logger.fine("Finished rebuilding tree: total time: " + (System.currentTimeMillis() - time) + " ms");
    }
}
