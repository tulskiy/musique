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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jaudiotagger.tag.TagFieldKey;

import com.tulskiy.musique.util.Util;

/**
 * Author: Denis Tulskiy
 * Date: 11/14/10
 */
public class TrackData implements Cloneable {

	// common jaudiotagger tag field values
	private Map<TagFieldKey, Set<String>> tagFields = new HashMap<TagFieldKey, Set<String>>();
	
	// meta fields
    private String trackNumber;
    private String totalTracks;
    private String discNumber;
    private String totalDiscs;

    // song info
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

    // runtime stuff
    private String cueSheet;
    private String track;
    private String length;
    private String fileName;
    private String directory;
    private long dateAdded;
    private long lastModified;

    private static HashSet<String> internedFields = new HashSet<String>() {{
        add("year");
        add("artist");
        add("album");
        add("genre");
        add("albumArtist");
        add("artist");
        add("codec");
    }};

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
    	// merge technical fields
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                Object value = field.get(newData);
                if (value != null && !(value instanceof Map<?, ?>)) {
            		field.set(this, value);
                }
            }
            catch (IllegalAccessException ignored) {
            }
        }

        // merge tag fields
        Iterator<Entry<TagFieldKey, Set<String>>> entries = newData.getAllTagFieldValuesIterator();
		while (entries.hasNext()) {
			Entry<TagFieldKey, Set<String>> entry = entries.next();
			addTagFieldValues(entry.getKey(), entry.getValue());
		}

		return this;
    }

    public void clearTags() {
    	tagFields.clear();
    	setCodec("");
    }

    // ------------------- meta methods ------------------- //

    public Iterator<Entry<TagFieldKey, Set<String>>> getAllTagFieldValuesIterator() {
		return tagFields.entrySet().iterator();
    }

    public Set<String> getTagFieldValuesSafeAsSet(TagFieldKey key) {
    	Set<String> result = tagFields.get(key);

    	if (result == null) {
    		result = new HashSet<String>();
    	}
    	
    	return result;
    }

    public List<String> getTagFieldValuesSafeAsList(TagFieldKey key) {
    	List<String> result = new ArrayList<String>();
    	
    	Set<String> values = getTagFieldValuesSafeAsSet(key);

    	if (!values.isEmpty()) {
    		result.addAll(Arrays.asList(values.toArray(new String[values.size()])));
    	}

    	return result;
    }
    
    // TODO add String.intern() support
    public void setTagFieldValues(TagFieldKey key, Set<String> values) {
    	tagFields.put(key, values);
    }
    
    // TODO add String.intern() support
    public void setTagFieldValues(TagFieldKey key, String value) {
    	Set<String> newValues = new HashSet<String>();
    	newValues.add(value);
    	setTagFieldValues(key, newValues);
    }
    
    // TODO add String.intern() support
    public void addTagFieldValues(TagFieldKey key, Set<String> values) {
    	Set<String> existingValues = getTagFieldValuesSafeAsSet(key);
    	existingValues.addAll(values);
    	tagFields.put(key, existingValues);
    }
    
    // TODO add String.intern() support
    public void addTagFieldValues(TagFieldKey key, String value) {
    	Set<String> existingValues = getTagFieldValuesSafeAsSet(key);
    	existingValues.add(value);
    	setTagFieldValues(key, existingValues);
    }
    
    public String getFirstTagFieldValue(TagFieldKey key) {
    	List<String> values = getTagFieldValuesSafeAsList(key);
    	
    	if (!values.isEmpty()) {
    		return values.get(0);
    	}
    	
    	return null;
    }

    // ------------------- common methods ------------------- //

    public String getArtist() {
        return Util.firstNotEmpty(getFirstTagFieldValue(TagFieldKey.ARTIST),
        		getFirstTagFieldValue(TagFieldKey.ALBUM_ARTIST));
    }
    
    public void addArtist(String value) {
    	addTagFieldValues(TagFieldKey.ARTIST, value);
    }

    public String getAlbum() {
        return getFirstTagFieldValue(TagFieldKey.ALBUM);
    }
    
    public void addAlbum(String value) {
    	addTagFieldValues(TagFieldKey.ALBUM, value);
    }

    public String getTitle() {
        return Util.firstNotEmpty(getFirstTagFieldValue(TagFieldKey.TITLE), getFileName());
    }
    
    public void addTitle(String value) {
    	addTagFieldValues(TagFieldKey.TITLE, value);
    }

    public String getAlbumArtist() {
        return Util.firstNotEmpty(getFirstTagFieldValue(TagFieldKey.ALBUM_ARTIST),
        		getFirstTagFieldValue(TagFieldKey.ARTIST));
    }
    
    public void addAlbumArtist(String value) {
    	addTagFieldValues(TagFieldKey.ALBUM_ARTIST, value);
    }

    public String getYear() {
    	// TODO think about TagFieldKey.DATE
        return getFirstTagFieldValue(TagFieldKey.YEAR);
    }
    
    public void addYear(String value) {
    	addTagFieldValues(TagFieldKey.YEAR, value);
    }

    public String getGenre() {
        return getFirstTagFieldValue(TagFieldKey.GENRE);
    }

    public Set<String> getGenres() {
        return getTagFieldValuesSafeAsSet(TagFieldKey.GENRE);
    }
    
    public void addGenre(String value) {
    	addTagFieldValues(TagFieldKey.GENRE, value);
    }

    public String getComment() {
        return getFirstTagFieldValue(TagFieldKey.COMMENT);
    }
    
    public void addComment(String value) {
    	addTagFieldValues(TagFieldKey.COMMENT, value);
    }

    // TODO rewrite all that TRACK/DISC number stuff
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

    /**
     * @return track number formatted to two digits
     */
    public String getTrackNumber() {
        return track;
    }

    public void setTrackNumber(String value) {
        if (value != null) {
            String[] s = value.split("/");
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

                trackNumber = s[0];

            }

            if (s.length > 1)
                totalTracks = s[1];
        }
    }

    public String getTrackTotal() {
        return totalTracks;
    }

    public void setTrackTotal(String value) {
        totalTracks = value.intern();
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

    public String getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(String value) {
        if (value != null && value.length() > 0) {
            String[] s = value.split("/");
            if (s.length > 0)
                discNumber = s[0];
            if (s.length > 1)
                totalDiscs = s[1];
        }
    }

    public String getDiscTotal() {
        return totalDiscs;
    }

    public void setDiscTotal(String value) {
        totalDiscs = value.intern();
    }

    public String getRecordLabel() {
    	return getFirstTagFieldValue(TagFieldKey.RECORD_LABEL);
    }

    public Set<String> getRecordLabels() {
    	return getTagFieldValuesSafeAsSet(TagFieldKey.RECORD_LABEL);
    }

    public void addRecordLabel(String value) {
    	addTagFieldValues(TagFieldKey.RECORD_LABEL, value);
    }

    public String getCatalogNo() {
    	return getFirstTagFieldValue(TagFieldKey.CATALOG_NO);
    }

    public Set<String> getCatalogNos() {
    	return getTagFieldValuesSafeAsSet(TagFieldKey.CATALOG_NO);
    }

    public void addCatalogNo(String value) {
    	addTagFieldValues(TagFieldKey.CATALOG_NO, value);
    }

    // ------------------- cuesheet methods ------------------- //

    public String getCueSheet() {
        return cueSheet;
    }

    public void setCueSheet(String cueSheet) {
        this.cueSheet = cueSheet;
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

    // ------------------- technical methods ------------------- //

    public String getLength() {
        if (length == null)
            length = Util.samplesToTime(totalSamples, sampleRate, 0);
        return length;
    }

    public String getFileName() {
        if (fileName == null) {
            fileName = Util.removeExt(getFile().getName());
        }
        return fileName;
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

    // ------------------- java object methods ------------------- //

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
