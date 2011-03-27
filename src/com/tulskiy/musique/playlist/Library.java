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

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: 10/30/10
 */
public class Library {
    private final Logger logger = Logger.getLogger("musique");
    //    private static final String DEFAULT_VIEW = "$if3(%albumArtist%,'Unknown Artist')|[%year% - ]$if3(%album%,'Unknown Album')$if1($greater(%totalDiscs%,1),[|Disc %discNumber%],'')|[%trackNumber%. ]%title%";
    private static final String DEFAULT_VIEW = "$if3(%albumArtist%,'?')|$if1(%album%,[[%year% - ]%album%],'?')$if1($greater(%totalDiscs%,1),[|Disc %discNumber%],'')|[%trackNumber%. ]%title%";
    private Configuration config = Application.getInstance().getConfiguration();
    private Playlist data;
    private String view;
    private TreeNode rootNode;

    public Library(Playlist data) {
        this.data = data;
        rebuildTree();
    }

    public void rescan(Map<String, Object> progress) {
        ArrayList<String> folders = config.getList("library.folders", null);
        if (folders == null || folders.isEmpty()) {
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
            rootNode = new SortedTreeNode("All music");
        }

        ((SortedTreeNode) rootNode).removeAllChildren();
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

                SortedTreeNode node = (SortedTreeNode) rootNode;
                for (int i = 0; i < path.length - 1; i++) {
                    node = node.get(path[i]);
                }

                node.add(new TrackNode(track, path[path.length - 1]));
            }
        }
        logger.fine("Finished rebuilding tree: total time: " + (System.currentTimeMillis() - time) + " ms");
    }

    class SortedTreeNode implements TreeNode, Comparable<SortedTreeNode> {
        private TreeMap<String, SortedTreeNode> children = new TreeMap<String, SortedTreeNode>();
        private SortedTreeNode parent;
        private String name;

        SortedTreeNode(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(SortedTreeNode o) {
            return toString().compareTo(o.toString());
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            Iterator<SortedTreeNode> it = children.values().iterator();
            for (int i = 0; i < childIndex; i++) {
                it.next();
            }
            return it.next();
        }

        public String getName() {
            return name;
        }

        public SortedTreeNode get(String object) {
            SortedTreeNode node = children.get(object);
            if (node == null) {
                node = new SortedTreeNode(object);
                add(node);
            }

            return node;
        }

        public void add(SortedTreeNode node) {
            children.put(node.getName(), node);
            node.setParent(this);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        public void setParent(SortedTreeNode parent) {
            this.parent = parent;
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            int i = 0;
            for (SortedTreeNode child : children.values()) {
                if (child.equals(node)) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return children.isEmpty();
        }

        @Override
        public Enumeration<SortedTreeNode> children() {
            return new Enumeration<SortedTreeNode>() {
                Iterator<SortedTreeNode> itr = children.values().iterator();

                @Override
                public boolean hasMoreElements() {
                    return itr.hasNext();
                }

                @Override
                public SortedTreeNode nextElement() {
                    return itr.next();
                }
            };
        }

        @Override
        public String toString() {
            return name;
        }

        public void removeAllChildren() {
            for (SortedTreeNode child : children.values()) {
                child.removeAllChildren();
            }
            children.clear();
        }
    }

    public class TrackNode extends SortedTreeNode {
        public Track track;
        public String text;

        TrackNode(Track track, String text) {
            super(text);
            this.track = track;
            this.text = text;
        }

        public Track getTrack() {
            return track;
        }
    }
}
