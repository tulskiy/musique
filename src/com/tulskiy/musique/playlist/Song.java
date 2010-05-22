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
import com.tulskiy.musique.db.Entity;
import com.tulskiy.musique.db.Id;
import com.tulskiy.musique.util.Util;

import java.io.File;

/**
 * @Author: Denis Tulskiy
 * @Date: Jan 4, 2010
 */

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@Entity("songs")
public class Song {
    @Id
    public int songID = -1;
    @Column
    private int playlistID;
    @Column
    private int playlistPosition;
    @Column
    private String filePath;

    @Column
    private String artist = "";
    @Column
    private String album = "";
    @Column
    private String title = "";
    @Column
    private String albumArtist = "";
    @Column
    private String trackNumber = "";
    @Column
    private String totalTracks = "";
    @Column
    private String discNumber = "";
    @Column
    private String totalDiscs = "";
    @Column
    private String year = "";
    @Column
    private String genre = "";
    @Column
    private String comment = "";

    @Column
    private int bitrate;
    @Column
    private int samplerate;
    @Column
    private int channels;
    @Column
    private int bps;
    @Column
    private int subsongIndex;
    @Column
    private long startPosition;
    @Column
    private long totalSamples;
    @Column
    private String codec = "";
    @Column
    private int cueID;

    private File file;
    private String cueSheet;
    private CUESheet cue;
    private String fileName;
    private String length;

    public int getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(int playlistID) {
        this.playlistID = playlistID;
    }

    public int getPlaylistPosition() {
        return playlistPosition;
    }

    public void setPlaylistPosition(int playlistPosition) {
        this.playlistPosition = playlistPosition;
    }

    public String getFilePath() {
        return file.getAbsolutePath();
    }

    public void setFilePath(String filePath) {
        file = new File(filePath);
        fileName = Util.removeExt(file.getName());
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        filePath = file.getAbsolutePath();
        fileName = Util.removeExt(file.getName());
    }

    public String getFileName() {
        return fileName;
    }

    public int getSongID() {
        return songID;
    }

    public void setSongID(int songID) {
        this.songID = songID;
    }

    public String getArtist() {
        return artist;
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
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        if (trackNumber != null) {
            String[] s = trackNumber.split("/");
            this.trackNumber = s[0];
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
            if (totalTracks != null) {
                return trackNumber + "/" + totalTracks;
            } else {
                return trackNumber;
            }
        }

        return "";
    }

    public String getDisc() {
        if (discNumber != null) {
            if (totalDiscs != null) {
                return discNumber + "/" + totalDiscs;
            } else {
                return discNumber;
            }
        }

        return "";
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getSamplerate() {
        return samplerate;
    }

    public void setSamplerate(int samplerate) {
        this.samplerate = samplerate;
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

    public String getLength() {
        if (length == null)
            length = Util.samplesToTime(getTotalSamples(), getSamplerate(), 0);
        return length;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public int getCueID() {
        if (cue == null)
            return -1;
        else
            return cue.getCueID();
    }

    public void setCueID(int cueID) {
        this.cueID = cueID;
        if (cueID != -1) {
            cue = new CUESheet();
            cue.setCueID(cueID);
        }

    }

    public void setCue(CUESheet sheet) {
        this.cue = sheet;
        setCueID(sheet.getCueID());
    }

    public CUESheet getCue() {
        return cue;
    }

    @Override
    public String toString() {
        return "Song{" +
                "filePath='" + getFilePath() + '\'' +
                '}';
    }
}
