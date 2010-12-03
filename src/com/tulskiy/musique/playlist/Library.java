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

import com.sun.java.swing.plaf.gtk.GTKLookAndFeel;
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Denis Tulskiy
 * Date: 10/30/10
 */
public class Library {
    private static final String DEFAULT_VIEW = "$if3(%albumArtist%,'Unknown Artist')|[%year% - ]$if3(%album%,'Unknown Album')|[%trackNumber%. ]%title%";
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
        rootNode = new DefaultMutableTreeNode("All Music");
        if (data == null)
            return;
        if (Util.isEmpty(view)) {
            view = DEFAULT_VIEW;
        }
        data.sort(view, false);
        Expression expr = Parser.parse(view);
        DefaultMutableTreeNode currentNode = null;
        for (Track track : data) {
            Object val = expr.eval(track);
            if (val != null) {
                String[] parts = val.toString().split("\\|");

                if (parts.length < 2) {
                    continue;
                }

                String parentName = parts[0];
                TrackNode trackNode = new TrackNode(track, parts[parts.length - 1]);
                if (currentNode == null || !compare(currentNode, parentName)) {
                    currentNode = new DefaultMutableTreeNode(parentName);
                    rootNode.add(currentNode);
                }

                DefaultMutableTreeNode subNode = null;
                if (currentNode.getChildCount() > 0)
                    subNode = (DefaultMutableTreeNode) currentNode.getLastChild();
                DefaultMutableTreeNode parent = currentNode;
                for (int i = 1; i < parts.length - 1; i++) {
                    String part = parts[i];
                    if (subNode == null || !subNode.getUserObject().equals(part)) {
                        if (subNode != null)
                            parent = (DefaultMutableTreeNode) subNode.getParent();
                        subNode = new DefaultMutableTreeNode(part);
                        parent.add(subNode);
                    }
                }
                if (subNode == null) {
                    subNode = new DefaultMutableTreeNode("?");
                    parent.add(subNode);
                }

                subNode.add(trackNode);
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
