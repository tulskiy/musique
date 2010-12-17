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

import java.io.File;
import java.net.URI;
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
@SuppressWarnings({"UnusedDeclaration"})
public class Track implements Cloneable {
    private static Random random = new Random();

    private TrackData trackData;
    private int shuffleRating;

    public Track() {
        this(new TrackData());
    }

    public Track(Track track) {
        this(track.trackData);
    }

    public Track(TrackData trackData) {
        this.trackData = trackData;
        shuffleRating = random.nextInt();
    }

    public int getShuffleRating() {
        return shuffleRating;
    }

    public void setShuffleRating(int shuffleRating) {
        this.shuffleRating = shuffleRating;
    }

    public Track copy() {
        return new Track(trackData.copy());
    }

    private int queuePosition = -1;

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public TrackData getTrackData() {
        return trackData;
    }

    public void setTrackData(TrackData trackData) {
        this.trackData = trackData;
    }

    /* leftovers i'm too lazy to clean */

    public int getSampleRate() {
        return trackData.getSampleRate();
    }

    public void clearTags() {
        trackData.clearTags();
    }

    public void setSampleRate(int sampleRate) {
        trackData.setSampleRate(sampleRate);
    }

    public int getChannels() {
        return trackData.getChannels();
    }

    public String getChannelsAsString() {
        return trackData.getChannelsAsString();
    }

    public void setChannels(int channels) {
        trackData.setChannels(channels);
    }

    public int getBps() {
        return trackData.getBps();
    }

    public void setBps(int bps) {
        trackData.setBps(bps);
    }

    public int getBitrate() {
        return trackData.getBitrate();
    }

    public void setBitrate(int bitrate) {
        trackData.setBitrate(bitrate);
    }

    public int getSubsongIndex() {
        return trackData.getSubsongIndex();
    }

    public void setSubsongIndex(int subsongIndex) {
        trackData.setSubsongIndex(subsongIndex);
    }

    public long getStartPosition() {
        return trackData.getStartPosition();
    }

    public void setStartPosition(long startPosition) {
        trackData.setStartPosition(startPosition);
    }

    public long getTotalSamples() {
        return trackData.getTotalSamples();
    }

    public void setTotalSamples(long totalSamples) {
        trackData.setTotalSamples(totalSamples);
    }

    public long getDateAdded() {
        return trackData.getDateAdded();
    }

    public void setDateAdded(long dateAdded) {
        trackData.setDateAdded(dateAdded);
    }

    public long getLastModified() {
        return trackData.getLastModified();
    }

    public void setLastModified(long lastModified) {
        trackData.setLastModified(lastModified);
    }

    public URI getLocation() {
        return trackData.getLocation();
    }

    public void setLocation(URI location) {
        trackData.setLocation(location);
    }

    public File getFile() {
        return trackData.getFile();
    }

    public boolean isFile() {
        return trackData.isFile();
    }

    public boolean isStream() {
        return trackData.isStream();
    }

    public String getArtist() {
        return trackData.getArtist();
    }

    public String getAlbum() {
        return trackData.getAlbum();
    }

    public String getTitle() {
        return trackData.getTitle();
    }

    public String getAlbumArtist() {
        return trackData.getAlbumArtist();
    }

    public String getTrackNumber() {
        return trackData.getTrackNumber();
    }

    public void setTrackNumber(String trackNumber) {
        trackData.setTrackNumber(trackNumber);
    }

    public String getTotalTracks() {
        return trackData.getTotalTracks();
    }

    public void setTotalDiscs(String totalDiscs) {
        trackData.setTotalDiscs(totalDiscs);
    }

    public void setTotalTracks(String totalTracks) {
        trackData.setTotalTracks(totalTracks);
    }

    public String getDiscNumber() {
        return trackData.getDiscNumber();
    }

    public void setDiscNumber(String discNumber) {
        trackData.setDiscNumber(discNumber);
    }

    public String getTotalDiscs() {
        return trackData.getTotalDiscs();
    }

    public String getYear() {
        return trackData.getYear();
    }

    public String getGenre() {
        return trackData.getGenre();
    }

    public String getComment() {
        return trackData.getComment();
    }

    public String getCueSheet() {
        return trackData.getCueSheet();
    }

    public void setCueSheet(String cueSheet) {
        trackData.setCueSheet(cueSheet);
    }

    public String getCodec() {
        return trackData.getCodec();
    }

    public void setCodec(String codec) {
        trackData.setCodec(codec);
    }

    public String getDirectory() {
        return trackData.getDirectory();
    }

    public void setDirectory(String directory) {
        trackData.setDirectory(directory);
    }

    public String getTrack() {
        return trackData.getTrack();
    }

    public String getDisc() {
        return trackData.getDisc();
    }

    public String getLength() {
        return trackData.getLength();
    }

    public boolean isCueEmbedded() {
        return trackData.isCueEmbedded();
    }

    public void setCueEmbedded(boolean cueEmbedded) {
        trackData.setCueEmbedded(cueEmbedded);
    }

    public String getCueLocation() {
        return trackData.getCueLocation();
    }

    public void setCueLocation(String cueLocation) {
        trackData.setCueLocation(cueLocation);
    }

    public boolean isCue() {
        return trackData.isCue();
    }

    public String getFileName() {
        return trackData.getFileName();
    }

    public void addMeta(String key, String value) {
        trackData.addMeta(key, value);
    }

    public String getMeta(String key) {
        return trackData.getMeta(key);
    }

    public void setMeta(String key, String value) {
        trackData.setMeta(key, value);
    }
}
