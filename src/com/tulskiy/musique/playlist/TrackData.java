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

import com.tulskiy.musique.util.Util;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;
import java.util.HashSet;

/**
 * Author: Denis Tulskiy
 * Date: 11/14/10
 */
public class TrackData implements Cloneable {
    //meta fields
    private String artist;
    private String album;
    private String title;
    private String albumArtist;
    private String trackNumber;
    private String totalTracks;
    private String discNumber;
    private String totalDiscs;
    private String year;
    private String genre;
    private String comment;

    //song info
    private int sampleRate;
    private int channels;
    private int bps;
    private int bitrate;
    private int subsongIndex;
    private long startPosition;
    private long totalSamples;
    private String locationString;
    private boolean cueEmbedded;
    private String cueLocation;
    private String codec;

    //runtime stuff
    private String cueSheet;
    private String track;
    private String length;
    private String fileName;
    private String directory;
    private long dateAdded;
    private long lastModified;

    public TrackData() {
    }

    public TrackData(URI location, int subsongIndex) {
        locationString = location.toString();
        setSubsongIndex(subsongIndex);
    }

    public TrackData copy() {
        try {
            return (TrackData) this.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }

    /**
     * Merges all fields from newData to current instance
     *
     * @param newData TrackData to merge from
     * @return current instance
     */
    public TrackData merge(TrackData newData) {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                Object value = field.get(newData);
                if (value != null)
                    field.set(this, value);
            } catch (IllegalAccessException ignored) {
            }
        }
        return this;
    }

    public void clearTags() {
        String[] tags = {"artist", "album", "albumArtist", "title",
                "year", "genre", "comment"};
        for (String tag : tags) {
            setMeta(tag, "");
        }
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

    public String getChannelsAsString() {
        switch (getChannels()) {
            case 1:
                return "Mono";
            case 2:
                return "Stereo";
            default:
                return getChannels() + " ch";
        }
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

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
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
        length = null;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public URI getLocation() {
        try {
            return new URI(locationString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLocation(String location) {
        locationString = location;
    }

    public File getFile() {
        return new File(getLocation());
    }

    public boolean isFile() {
        return getLocation() != null && !isStream();
    }

    public boolean isStream() {
        return getLocation() != null && "http".equals(getLocation().getScheme());
    }

    public String getArtist() {
        return Util.firstNotEmpty(artist, albumArtist);
    }

    public String getAlbum() {
        return album;
    }

    public String getTitle() {
        return Util.firstNotEmpty(title, getFileName());
    }

    public String getAlbumArtist() {
        return Util.firstNotEmpty(albumArtist, artist);
    }

    /**
     * @return track number formatted to two digits
     */
    public String getTrackNumber() {
        return track;
    }

    public void setTrackNumber(String trackNumber) {
        if (trackNumber != null) {
            String[] s = trackNumber.split("/");
            if (s.length > 0) {
                try {
                    int i = Integer.parseInt(s[0]);
                    if (i != 0)
                        track = new Formatter().format("%02d", i).toString();
                    else
                        track = null;
                } catch (NumberFormatException ignored) {
                    track = s[0];
                }

                this.trackNumber = s[0];

            }

            if (s.length > 1)
                this.totalTracks = s[1];
        }
    }

    public String getTotalTracks() {
        return totalTracks;
    }

    public void setTotalTracks(String totalTracks) {
        this.totalTracks = totalTracks.intern();
    }

    public void setTotalDiscs(String totalDiscs) {
        this.totalDiscs = totalDiscs.intern();
    }

    public String getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(String discNumber) {
        if (discNumber != null && discNumber.length() > 0) {
            String[] s = discNumber.split("/");
            if (s.length > 0)
                this.discNumber = s[0];
            if (s.length > 1)
                this.totalDiscs = s[1];
        }
    }

    public String getTotalDiscs() {
        return totalDiscs;
    }

    public String getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public String getComment() {
        return comment;
    }

    public String getCueSheet() {
        return cueSheet;
    }

    public void setCueSheet(String cueSheet) {
        this.cueSheet = cueSheet;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec.intern();
    }

    public String getDirectory() {
        if (directory == null) {
            directory = getFile().getParentFile().getName();
        }
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getTrack() {
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
        if (discNumber != null) {
            if (totalDiscs != null && !totalDiscs.isEmpty()) {
                return discNumber + "/" + totalDiscs;
            } else {
                return discNumber;
            }
        }

        return "";
    }

    public String getLength() {
        if (length == null)
            length = Util.samplesToTime(totalSamples, sampleRate, 0);
        return length;
    }

    public boolean isCueEmbedded() {
        return cueEmbedded;
    }

    public void setCueEmbedded(boolean cueEmbedded) {
        this.cueEmbedded = cueEmbedded;
    }

    public String getCueLocation() {
        return cueLocation;
    }

    public void setCueLocation(String cueLocation) {
        this.cueLocation = cueLocation;
    }

    public boolean isCue() {
        return subsongIndex > 0;
    }

    public String getFileName() {
        if (fileName == null) {
            fileName = Util.removeExt(getFile().getName());
        }
        return fileName;
    }

    //OK, this is a hack, but I don't want to redo all this stuff

    public void addMeta(String key, String value) {
        setMeta(key, Util.longest(getMeta(key), value));
    }

    public String getMeta(String key) {
        try {
            Object o = getClass().getDeclaredField(key).get(this);
            if (o != null)
                return o.toString();
        } catch (Exception ignored) {
        }
        return "";
    }

    private static HashSet<String> internedFields = new HashSet<String>() {{
        add("year");
        add("artist");
        add("album");
        add("genre");
        add("albumArtist");
        add("artist");
        add("codec");
    }};

    public void setMeta(String key, String value) {
        try {
            if (value != null && internedFields.contains(key))
                value = value.intern();
            getClass().getDeclaredField(key).set(this, value);
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackData trackData = (TrackData) o;

        return locationString.equals(trackData.locationString)
                && subsongIndex == trackData.subsongIndex;

    }

    @Override
    public int hashCode() {
        int result = subsongIndex;
        result = 31 * result + (locationString != null ? locationString.hashCode() : 0);
        return result;
    }
}
