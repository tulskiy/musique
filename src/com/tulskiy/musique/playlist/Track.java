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
import java.net.URI;
import java.util.Formatter;
import java.util.Random;

/**
 * Author: Denis Tulskiy
 * Date: Jun 15, 2010
 */

/**
 * Class used to represent track information
 * <p/>
 * <p><p><strong>Warning: getMeta(key) returns raw value of the variable,
 * getters, eg. getTrackNumber(), may return different values</strong>
 * <p/>
 * <p>I know it's stupid, but that's the way my title formatting works
 */
public class Track implements Cloneable {

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
    private int subsongIndex;
    private long startPosition;
    private long totalSamples;
    private URI location;

    //runtime stuff
    private String cueSheet;
    private String track;
    private String length;
    private String fileName;

    private static Random random;
    private int shuffleRating = nextRandom();
    private boolean played = false;

    public static int nextRandom() {
        if (random == null)
            random = new Random();
        return random.nextInt();
    }

    public int getShuffleRating() {
        return shuffleRating;
    }

    public void setShuffleRating(int shuffleRating) {
        this.shuffleRating = shuffleRating;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public Track copy() {
        try {
            Track t = (Track) this.clone();
            t.shuffleRating = nextRandom();
            return t;
        } catch (CloneNotSupportedException ignored) {
            return null;
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

    public URI getLocation() {
        return location;
    }

    public void setLocation(URI location) {
        this.location = location;
        fileName = Util.removeExt(location.getPath());
    }

    public File getFile() {
        return isFile() ? new File(location) : null;
    }

    public boolean isFile() {
        return "file".equals(location.getScheme());
    }

    public boolean isStream() {
        return "http".equals(location.getScheme());
    }

    public String getArtist() {
        return Util.firstNotEmpty(artist, albumArtist);
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return Util.firstNotEmpty(title, fileName);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumArtist() {
        return Util.firstNotEmpty(albumArtist, artist);
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
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
                    track = new Formatter().format("%02d", i).toString();
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
        this.totalTracks = totalTracks;
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

    public void setTotalDiscs(String totalDiscs) {
        this.totalDiscs = totalDiscs;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCueSheet() {
        return cueSheet;
    }

    public void setCueSheet(String cueSheet) {
        this.cueSheet = cueSheet;
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

    public String getFileName() {
        return fileName;
    }

    private int queuePosition = -1;

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public int getQueuePosition() {
        return queuePosition;
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
        return null;
    }

    public void setMeta(String key, String value) {
        try {
            getClass().getDeclaredField(key).set(this, value);
        } catch (Exception ignored) {
//            ignored.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Track{" +
               "artist='" + artist + '\'' +
               ", album='" + album + '\'' +
               ", title='" + title + '\'' +
               ", albumArtist='" + albumArtist + '\'' +
               ", trackNumber='" + trackNumber + '\'' +
               ", year='" + year + '\'' +
               ", genre='" + genre + '\'' +
               ", sampleRate=" + sampleRate +
               ", channels=" + channels +
               ", subsongIndex=" + subsongIndex +
               ", length='" + length + '\'' +
               ", location=" + location +
               '}';
    }
}
