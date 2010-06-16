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

import com.tulskiy.musique.util.Util;

import java.io.File;
import java.util.Formatter;
import java.util.HashMap;

/**
 * Author: Denis Tulskiy
 * Date: Jun 15, 2010
 */
public class Track {
    private HashMap<String, String> meta = new HashMap<String, String>(14, 1.0f);

    private int sampleRate;
    private int channels;
    private int bps;
    private int subsongIndex;
    private long startPosition;
    private long totalSamples;
    private File file;
    private String cueSheet;

    public Track() {
    }

    public String getMeta(String key) {
        return meta.get(key);
    }

    public void setMeta(String key, String value) {
        if (value == null) {
            meta.remove(key);
        }
        meta.put(key, value);
    }

    public void addMeta(String key, String value) {
        String o = meta.get(key);
        meta.put(key, Util.longest(value, o));
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getBps() {
        return bps;
    }

    public void setBps(int bps) {
        this.bps = bps;
    }

    public int getSubsongIndex() {
        return subsongIndex;
    }

    public void setSubsongIndex(int subsongIndex) {
        this.subsongIndex = subsongIndex;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(long totalSamples) {
        this.totalSamples = totalSamples;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Track copy() {
        Track track = new Track();
        track.meta.putAll(meta);

        track.sampleRate = sampleRate;
        track.channels = channels;
        track.bps = bps;
        track.subsongIndex = subsongIndex;
        track.startPosition = startPosition;
        track.totalSamples = totalSamples;
        track.file = file;

        return track;
    }

    public HashMap<String, String> getMeta() {
        return meta;
    }

    public String getArtist() {
        return null;
    }

    public void setArtist(String artist) {
    }

    public String getAlbum() {
        return null;
    }

    public void setAlbum(String album) {
    }

    public String getTitle() {
        return null;
    }

    public void setTitle(String title) {
    }

    public String getAlbumArtist() {
        return null;
    }

    public void setAlbumArtist(String albumArtist) {
    }

    public String getTrackNumber() {
        return null;
    }

    public void setTrackNumber(String trackNumber) {
        if (trackNumber != null) {
            String[] s = trackNumber.split("/");
            if (s.length > 0) {
                String value;
                try {
                    int i = Integer.parseInt(s[0]);
                    value = new Formatter().format("%02d", i).toString();
                } catch (NumberFormatException ignored) {
                    value = s[0];
                }

                setMeta("tracknumber", value);
            }

            if (s.length > 1)
                setMeta("totaltracks", s[1]);
        }
    }

    public String getTotalTracks() {
        return null;
    }

    public void setTotalTracks(String totalTracks) {
    }

    public String getDiscNumber() {
        return null;
    }

    public void setDiscNumber(String discNumber) {
        if (discNumber != null && discNumber.length() > 0) {
            String[] s = discNumber.split("/");
            if (s.length > 0)
                setMeta("discnumber", s[0]);
            if (s.length > 1)
                setMeta("totaldiscs", s[1]);
        }
    }

    public String getTrack() {
        String trackNumber = getMeta("trackNumber");
        String totalTracks = getMeta("totaltracks");
        if (trackNumber != null) {
            if (totalTracks != null && !totalTracks.isEmpty()) {
                return trackNumber + "/" + totalTracks;
            } else {
                return trackNumber;
            }
        }

        return "";
    }

    public String getDisc() {
        String discNumber = getMeta("discnumber");
        String totalDiscs = getMeta("totalDiscs");
        if (discNumber != null) {
            if (totalDiscs != null && !totalDiscs.isEmpty()) {
                return discNumber + "/" + totalDiscs;
            } else {
                return discNumber;
            }
        }

        return "";
    }

    private int queuePosition = -1;

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public String getTotalDiscs() {
        return null;
    }

    public void setTotalDiscs(String totalDiscs) {
    }

    public String getYear() {
        return null;
    }

    public void setYear(String year) {
    }

    public String getGenre() {
        return null;
    }

    public void setGenre(String genre) {
    }

    public String getComment() {
        return null;
    }

    public void setComment(String comment) {
    }

    public void setCueSheet(String cueSheet) {
        this.cueSheet = cueSheet;
    }

    public String getCueSheet() {
        return cueSheet;
    }

    public void setCue(CUESheet sheet) {
    }

    public int getSongID() {
        return 0;
    }

    public String getFilePath() {
        return null;
    }

    public int getCueID() {
        return 0;
    }
}
