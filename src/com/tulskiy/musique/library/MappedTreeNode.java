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

import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * Author: Denis Tulskiy
 * Date: 3/27/11
 */
public class MappedTreeNode implements TreeNode, Comparable<MappedTreeNode> {
    private TreeMap<String, MappedTreeNode> children = new TreeMap<String, MappedTreeNode>();
    private MappedTreeNode parent;
    private String name;

    MappedTreeNode(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(MappedTreeNode o) {
        return toString().compareTo(o.toString());
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        Iterator<MappedTreeNode> it = children.values().iterator();
        for (int i = 0; i < childIndex; i++) {
            it.next();
        }
        return it.next();
    }

    public String getName() {
        return name;
    }

    public MappedTreeNode get(String object) {
        MappedTreeNode node = children.get(object);
        if (node == null) {
            node = new MappedTreeNode(object);
            add(node);
        }

        return node;
    }

    public void add(MappedTreeNode node) {
        children.put(node.getName(), node);
        node.setParent(this);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    public void setParent(MappedTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        int i = 0;
        for (MappedTreeNode child : children.values()) {
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
    public Enumeration<MappedTreeNode> children() {
        return new Enumeration<MappedTreeNode>() {
            Iterator<MappedTreeNode> itr = children.values().iterator();

            @Override
            public boolean hasMoreElements() {
                return itr.hasNext();
            }

            @Override
            public MappedTreeNode nextElement() {
                return itr.next();
            }
        };
    }

    public List<MappedTreeNode> iterate() {
        List<MappedTreeNode> list = new ArrayList<MappedTreeNode>();

        for (MappedTreeNode node : children.values()) {
            if (node.isLeaf()) {
                list.add(node);
            } else {
                list.addAll(node.iterate());
            }
        }

        return list;
    }

    @Override
    public String toString() {
        return name;
    }

    public void removeAllChildren() {
        for (MappedTreeNode child : children.values()) {
            child.removeAllChildren();
        }
        children.clear();
    }
}
