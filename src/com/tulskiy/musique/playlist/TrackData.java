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

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.util.Util;

/**
 * Author: Denis Tulskiy
 * Date: 11/14/10
 */
public class TrackData implements Cloneable {

	// generic jaudiotagger tag field values
	private Map<FieldKey, Set<String>> tagFields = new HashMap<FieldKey, Set<String>>();
	
	// common tag fields (to be displayed in TrackInfoDialog even if missed)
	private static final FieldKey[] COMMON_TAG_FIELDS = {
		FieldKey.ARTIST,
		FieldKey.ALBUM_ARTIST,
		FieldKey.TITLE,
		FieldKey.ALBUM,
		FieldKey.YEAR,
		FieldKey.GENRE,
		FieldKey.TRACK,
		FieldKey.TRACK_TOTAL,
		FieldKey.DISC_NO,
		FieldKey.DISC_TOTAL,
		FieldKey.RECORD_LABEL,
		FieldKey.CATALOG_NO,
		FieldKey.COMMENT,
		FieldKey.RATING
	};
	
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
    private String encoder;

    // runtime stuff
    private String cueSheet;
    private String trackNumberFormatted;
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
        Iterator<Entry<FieldKey, Set<String>>> entries = newData.getAllTagFieldValuesIterator();
		while (entries.hasNext()) {
			Entry<FieldKey, Set<String>> entry = entries.next();
			addTagFieldValues(entry.getKey(), entry.getValue());
		}

		return this;
    }

    public void clearTags() {
    	tagFields.clear();
    	setCodec("");
    }

    // ------------------- meta methods ------------------- //

    public Iterator<Entry<FieldKey, Set<String>>> getAllTagFieldValuesIterator() {
		return tagFields.entrySet().iterator();
    }

    public Set<String> getTagFieldValuesSafeAsSet(FieldKey key) {
    	Set<String> result = tagFields.get(key);

    	if (result == null) {
    		result = new HashSet<String>();
    	}
    	
    	return result;
    }

    public List<String> getTagFieldValuesSafeAsList(FieldKey key) {
    	List<String> result = new ArrayList<String>();
    	
    	Set<String> values = getTagFieldValuesSafeAsSet(key);

    	if (!values.isEmpty()) {
    		result.addAll(Arrays.asList(values.toArray(new String[values.size()])));
    	}

    	return result;
    }
    
    // TODO add String.intern() support
    public void setTagFieldValues(FieldKey key, Set<String> values) {
    	if (values.isEmpty()) {
    		return;
    	}

    	// handle additional business logic
    	if (FieldKey.TRACK.equals(key)) {
    		String track = values.iterator().next();
    		trackNumberFormatted = new Formatter().format("%02d", Integer.parseInt(track)).toString();
    	}

    	// handle technical tags
    	if (FieldKey.ENCODER.equals(key)) {
    		setEncoder(values.iterator().next());
    	}
    	// handle common cases
    	else {
    		tagFields.put(key, values);
    	}
    }
    
    // TODO add String.intern() support
    public void setTagFieldValues(FieldKey key, String value) {
    	Set<String> newValues = new HashSet<String>();
    	newValues.add(value);
    	setTagFieldValues(key, newValues);
    }
    
    // TODO add String.intern() support
    public void addTagFieldValues(FieldKey key, Set<String> values) {
    	Set<String> existingValues = getTagFieldValuesSafeAsSet(key);
    	existingValues.addAll(values);
    	setTagFieldValues(key, existingValues);
    }
    
    // TODO add String.intern() support
    public void addTagFieldValues(FieldKey key, String value) {
    	Set<String> existingValues = getTagFieldValuesSafeAsSet(key);
    	existingValues.add(value);
    	setTagFieldValues(key, existingValues);
    }
    
    public String getFirstTagFieldValue(FieldKey key) {
    	List<String> values = getTagFieldValuesSafeAsList(key);
    	
    	if (!values.isEmpty()) {
    		return values.get(0);
    	}
    	
    	return null;
    }
    
    public void removeTagField(FieldKey key) {
    	tagFields.remove(key);
    }

    // ------------------- common methods ------------------- //

    public String getArtist() {
        return Util.firstNotEmpty(getFirstTagFieldValue(FieldKey.ARTIST),
        		getFirstTagFieldValue(FieldKey.ALBUM_ARTIST));
    }
    
    public void addArtist(String value) {
    	addTagFieldValues(FieldKey.ARTIST, value);
    }

    public String getAlbum() {
        return getFirstTagFieldValue(FieldKey.ALBUM);
    }
    
    public void addAlbum(String value) {
    	addTagFieldValues(FieldKey.ALBUM, value);
    }

    public String getTitle() {
        return Util.firstNotEmpty(getFirstTagFieldValue(FieldKey.TITLE), getFileName());
    }
    
    public void addTitle(String value) {
    	addTagFieldValues(FieldKey.TITLE, value);
    }

    public String getAlbumArtist() {
        return Util.firstNotEmpty(getFirstTagFieldValue(FieldKey.ALBUM_ARTIST),
        		getFirstTagFieldValue(FieldKey.ARTIST));
    }
    
    public void addAlbumArtist(String value) {
    	addTagFieldValues(FieldKey.ALBUM_ARTIST, value);
    }

    public String getYear() {
        return getFirstTagFieldValue(FieldKey.YEAR);
    }
    
    public void addYear(String value) {
    	addTagFieldValues(FieldKey.YEAR, value);
    }

    public String getGenre() {
        return getFirstTagFieldValue(FieldKey.GENRE);
    }

    public Set<String> getGenres() {
        return getTagFieldValuesSafeAsSet(FieldKey.GENRE);
    }
    
    public void addGenre(String value) {
    	addTagFieldValues(FieldKey.GENRE, value);
    }

    public String getComment() {
        return getFirstTagFieldValue(FieldKey.COMMENT);
    }
    
    public void addComment(String value) {
    	addTagFieldValues(FieldKey.COMMENT, value);
    }

    public String getTrack() {
    	return getFirstTagFieldValue(FieldKey.TRACK);
    }

    public void addTrack(String value) {
    	addTagFieldValues(FieldKey.TRACK, value);
    }

    public void addTrack(Integer value) {
    	if (value != null) {
    		addTrack(value.toString());
    	}
    }

    /**
     * @return track number formatted to two digits
     */
    public String getTrackNumber() {
        return trackNumberFormatted;
    }

    public String getTrackTotal() {
    	return getFirstTagFieldValue(FieldKey.TRACK_TOTAL);
    }

    public void addTrackTotal(String value) {
    	addTagFieldValues(FieldKey.TRACK_TOTAL, value);
    }

    public void addTrackTotal(Integer value) {
    	if (value != null) {
    		addTrackTotal(value.toString());
    	}
    }

    public String getDisc() {
    	return getFirstTagFieldValue(FieldKey.DISC_NO);
    }

    public void addDisc(String value) {
    	addTagFieldValues(FieldKey.DISC_NO, value);
    }

    public void addDisc(Integer value) {
    	if (value != null) {
    		addDisc(value.toString());
    	}
    }

    public String getDiscTotal() {
    	return getFirstTagFieldValue(FieldKey.DISC_TOTAL);
    }

    public void addDiscTotal(String value) {
    	addTagFieldValues(FieldKey.DISC_TOTAL, value);
    }

    public void addDiscTotal(Integer value) {
    	if (value != null) {
    		addDiscTotal(value.toString());
    	}
    }

    public String getRecordLabel() {
    	return getFirstTagFieldValue(FieldKey.RECORD_LABEL);
    }

    public Set<String> getRecordLabels() {
    	return getTagFieldValuesSafeAsSet(FieldKey.RECORD_LABEL);
    }

    public void addRecordLabel(String value) {
    	addTagFieldValues(FieldKey.RECORD_LABEL, value);
    }

    public String getCatalogNo() {
    	return getFirstTagFieldValue(FieldKey.CATALOG_NO);
    }

    public Set<String> getCatalogNos() {
    	return getTagFieldValuesSafeAsSet(FieldKey.CATALOG_NO);
    }

    public void addCatalogNo(String value) {
    	addTagFieldValues(FieldKey.CATALOG_NO, value);
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

    public String getEncoder() {
        return encoder;
    }

    public void setEncoder(String encoder) {
        this.encoder= encoder.intern();
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

    // ------------------- business logic methods ------------------- //
    
    public void populateWithEmptyCommonTagFields() {
    	for (FieldKey key : COMMON_TAG_FIELDS) {
    		if (getTagFieldValuesSafeAsSet(key).isEmpty()) {
    			setTagFieldValues(key, "");
    		}
    	}
    }
    
    public void removeEmptyCommonTagFields() {
    	for (FieldKey key : COMMON_TAG_FIELDS) {
    		if (Util.isEmpty(getFirstTagFieldValue(key))) {
    			removeTagField(key);
    		}
    	}
    }
    
    public void removeEmptyTagFields() {
    	for (FieldKey key : FieldKey.values()) {
    		if (Util.isEmpty(getFirstTagFieldValue(key))) {
    			removeTagField(key);
    		}
    	}
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
