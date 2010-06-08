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

import com.tulskiy.musique.db.Column;
import com.tulskiy.musique.db.DBMapper;
import com.tulskiy.musique.db.Entity;
import com.tulskiy.musique.db.Id;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @Author: Denis Tulskiy
 * @Date: Dec 30, 2009
 */
@SuppressWarnings({"serial"})
@Entity("playlists")
public class Playlist extends ArrayList<Song> implements Comparable<Playlist> {
    private DBMapper<Song> songDBMapper = DBMapper.create(Song.class);
    private DBMapper<CUESheet> cueSheetDBMapper = DBMapper.create(CUESheet.class);

    @Id
    private int playlistID = -1;
    @Column
    private String name;
    @Column
    private int position;

    private boolean stopLoading = false;
    private ArrayList<File> tempList;

    public void load() {
//        System.out.println("Loading playlist " + playlistID);
        clear();
        songDBMapper.loadAll("select * from songs where playlistID=" + playlistID + " order by playlistPosition", this);
    }

    public void save() {
        for (int i = 0; i < size(); i++) {
            Song song = get(i);
            song.setPlaylistID(playlistID);
            song.setPlaylistPosition(i);
            songDBMapper.save(song);
        }
    }

    public String getName() {
        return name;
    }

    public int getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(int playlistID) {
        this.playlistID = playlistID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean add(Song song) {
        song.setPlaylistID(playlistID);
//        songDBMapper.save(song);
        return super.add(song);
    }

    @Override
    public boolean addAll(Collection<? extends Song> c) {
        for (Song s : c) {
            add(s);
        }

        return true;
    }

    public void addFiles(File... files) {
        tempList = new ArrayList<File>();
        for (File f : files) {
            if (f.isDirectory()) {
                loadDirectory(f);
            } else if (f.isFile()) {
                tempList.add(f);
            }
        }

        TagProcessor tagProcessor = new TagProcessor(tempList, this);
        tagProcessor.start();
    }

    private void loadDirectory(File dir) {
        if (stopLoading) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();

            for (File file : files) {
                if (stopLoading) {
                    break;
                }
                if (file.isFile()) {
                    tempList.add(file);
                } else {
                    loadDirectory(file);
                }
            }
        }
    }

    @Override
    public void clear() {
        for (Song song : this) {
            songDBMapper.delete(song);
        }
        //hack to delete CUE sheets
        ArrayList<CUESheet> list = new ArrayList<CUESheet>();
        cueSheetDBMapper.loadAll(list);
        for (CUESheet cueSheet : list) {
            cueSheetDBMapper.delete(cueSheet);
        }

        super.clear();
    }

    public boolean removeAll(Collection<?> c) {
        for (Object song : c) {
            songDBMapper.delete((Song) song);
        }
        return super.removeAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Playlist && this == o;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Playlist o) {
        return ((Integer) position).compareTo(o.getPosition());
    }
}
