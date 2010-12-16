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
    private Configuration config = Application.getInstance().getConfiguration();
    private Playlist data;
    private String view;
    private DefaultMutableTreeNode rootNode;

    public Library(Playlist data) {
        this.data = data;
        buildDataTree();
    }

    public void rescan() {
        ArrayList<String> folders = config.getList("library.folders", null);
        if (folders == null || folders.isEmpty()) {
            return;
        }
    }

    public DefaultMutableTreeNode getRootNode() {
        return rootNode;
    }

    public void setData(Playlist data) {
        this.data = data;
        buildDataTree();
    }

    public Playlist getData() {
        return data;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
        buildDataTree();
    }

    private void buildDataTree() {
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
