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

import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Codecs;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.Util;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * Author: Denis Tulskiy
 * Date: 10/30/10
 */
public class Library {
    private static final String DEFAULT_VIEW = "$if3(%albumArtist%,'Unknown Artist')|[%year% - ]$if3(%album%,'Unknown Album')$if1($greater(%totalDiscs%,1),[|Disc %discNumber%],'')|[%trackNumber%. ]%title%";
    //    private static final String DEFAULT_VIEW = "$if3(%albumArtist%,'?')$if1(%album%,[|[%year% - ]%album%],'')$if1($greater(%totalDiscs%,1),[|Disc %discNumber%],'')|[%trackNumber%. ]%title%";
    private Configuration config = Application.getInstance().getConfiguration();
    private Playlist data;
    private String view;
    private DefaultMutableTreeNode rootNode;

    public Library(Playlist data) {
        this.data = data;
        rebuildTree();
    }

    public void rescan(Map<String, Object> progress) {
        ArrayList<String> folders = config.getList("library.folders", null);
        if (folders == null || folders.isEmpty()) {
            return;
        }

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
                        if (track.getLastModified() != file.lastModified()) {
                            track.clearTags();
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

    public DefaultMutableTreeNode getRootNode() {
        return rootNode;
    }

    public void setData(Playlist data) {
        this.data = data;
        rebuildTree();
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
        if (rootNode == null)
            rootNode = new DefaultMutableTreeNode("All Music");
        rootNode.removeAllChildren();
        if (data == null)
            return;
        if (Util.isEmpty(view)) {
            view = DEFAULT_VIEW;
        }
        data.sort(view, false);
        Expression expr = Parser.parse(view);
        DefaultMutableTreeNode currentNode = rootNode;
        for (Track track : data) {
            Object val = expr.eval(track);
            if (val != null) {
                String[] path = val.toString().split("\\|");

                if (path.length < 2) {
                    continue;
                }

                TreeNode[] currentNodePath = currentNode.getPath();
                int start = -1;
                for (int i = 1; i < path.length; i++) {
                    if (currentNodePath.length <= i) {
                        currentNode = rootNode;
                        start = 0;
                        break;
                    }
                    currentNode = (DefaultMutableTreeNode) currentNodePath[i];
                    if (!currentNode.getUserObject().equals(path[i - 1])) {
                        currentNode = (DefaultMutableTreeNode) currentNode.getParent();
                        start = i - 1;
                        break;
                    }
                }

                if (start != -1) {
                    for (int i = start; i < path.length - 1; i++) {
                        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(path[i]);
                        currentNode.add(newChild);
                        currentNode = newChild;
                    }
                }

                currentNode.add(new TrackNode(track, path[path.length - 1]));
            }
        }
    }

    private boolean compare(DefaultMutableTreeNode node, Object value) {
        return value != null &&
                node.getUserObject().toString().
                        compareToIgnoreCase(value.toString()) == 0;
    }

    class TrackNode extends DefaultMutableTreeNode {
        public Track track;
        public String text;

        TrackNode(Track track, String text) {
            super(track);
            this.track = track;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
