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
import java.util.*;
import java.util.Map.Entry;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.gui.model.FieldValues;
import com.tulskiy.musique.util.Util;

/**
 * Author: Denis Tulskiy
 * Date: 11/14/10
 */
public class TrackData implements Cloneable {

	// generic jaudiotagger tag field values
	private Map<FieldKey, FieldValues> tagFields = new HashMap<FieldKey, FieldValues>(5, 1f);
	
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

    private static Set<FieldKey> INTERNED_FIELDS = new LinkedHashSet<FieldKey>() {{
        add(FieldKey.ARTIST);
        add(FieldKey.ALBUM_ARTIST);
        add(FieldKey.YEAR);
        add(FieldKey.GENRE);
        add(FieldKey.TRACK);
        add(FieldKey.TRACK_TOTAL);
        add(FieldKey.DISC_NO);
        add(FieldKey.DISC_TOTAL);
        add(FieldKey.RECORD_LABEL);
        add(FieldKey.RATING);
    }};
	
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

    public TrackData() {
    }

    public TrackData(URI location, int subsongIndex) {
        locationString = location.toString();
        setSubsongIndex(subsongIndex);
    }

    public TrackData copy() {
        try {
        	TrackData copy = (TrackData) this.clone();
        	copy.tagFields = new EnumMap<FieldKey, FieldValues>(copy.tagFields);
            return copy;
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
        Iterator<Entry<FieldKey, FieldValues>> entries = newData.getAllTagFieldValuesIterator();
		while (entries.hasNext()) {
			Entry<FieldKey, FieldValues> entry = entries.next();
			addTagFieldValues(entry.getKey(), entry.getValue());
		}

		return this;
    }

    public void clearTags() {
    	tagFields.clear();
    	setCodec("");
    }

    // ------------------- meta methods ------------------- //

    public Iterator<Entry<FieldKey, FieldValues>> getAllTagFieldValuesIterator() {
		return tagFields.entrySet().iterator();
    }

    public FieldValues getTagFieldValues(FieldKey key) {
    	return tagFields.get(key);
    }

    public FieldValues getTagFieldValuesSafe(FieldKey key) {
    	FieldValues result = getTagFieldValues(key);

    	if (result == null) {
    		result = new FieldValues();
    	}
    	
    	return result;
    }
    
    public void setTagFieldValues(FieldKey key, FieldValues values) {
    	if (values.isEmpty()) {
    		return;
    	}

    	// handle additional business logic
    	if (FieldKey.TRACK.equals(key)) {
    		String track = values.get(0);
    		trackNumberFormatted = (Util.isEmpty(track) ?
    				"" : new Formatter().format("%02d", Integer.parseInt(track)).toString()).intern();
    	}

    	// handle technical tags
    	if (FieldKey.ENCODER.equals(key)) {
    		setEncoder(values.get(0));
    	}
    	else if (FieldKey.COVER_ART.equals(key)) {
    		// TODO skipping, should be handled in its own way
    	}
    	// handle common cases
    	else {
    		if (INTERNED_FIELDS.contains(key)) {
    			FieldValues valuesOptimized = new FieldValues();
    			for (int i = 0; i < values.size(); i++) {
    				String value = values.get(i);
					valuesOptimized.add(value == null ? null : value.intern());
    			}
        		tagFields.put(key, valuesOptimized);
    		}
    		else {
        		tagFields.put(key, values);
    		}
    	}
    }
    
    public void setTagFieldValues(FieldKey key, String value) {
    	setTagFieldValues(key, new FieldValues(value));
    }
    
    public void addTagFieldValues(FieldKey key, FieldValues values) {
    	FieldValues existingValues = getTagFieldValuesSafe(key);
    	existingValues.add(values);
    	setTagFieldValues(key, existingValues);
    }
    
    public void addTagFieldValues(FieldKey key, String value) {
    	FieldValues existingValues = getTagFieldValuesSafe(key);
    	existingValues.add(value);
    	setTagFieldValues(key, existingValues);
    }
    
    public String getFirstTagFieldValue(FieldKey key) {
    	FieldValues values = getTagFieldValues(key);
    	
    	if (!FieldValues.isEmptyEx(values)) {
    		return values.get(0);
    	}
    	
    	return null;
    }
    
    public void removeTagField(FieldKey key) {
    	tagFields.remove(key);
    }

    // ------------------- common methods ------------------- //

    public String getArtist() {
        return firstNotEmpty(FieldKey.ARTIST,
                FieldKey.ALBUM_ARTIST,
                FieldKey.BAND,
                FieldKey.COMPOSER
        );
    }

    private String firstNotEmpty(FieldKey... keys) {
        for (FieldKey key : keys) {
            String value = getFirstTagFieldValue(key);
            if (!Util.isEmpty(value))
                return value;
        }
        return null;
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
        String title = getFirstTagFieldValue(FieldKey.TITLE);
        if (Util.isEmpty(title))
            if (isFile())
                return getFileName();
            else
                return locationString;
        else
            return title;
    }
    
    public void addTitle(String value) {
    	addTagFieldValues(FieldKey.TITLE, value);
    }

    public String getAlbumArtist() {
        return firstNotEmpty(FieldKey.ALBUM_ARTIST,
                FieldKey.BAND, // foobar2000 uses BAND field as album artist in id3v2
                FieldKey.ARTIST,
                FieldKey.COMPOSER
        );
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

    public FieldValues getGenres() {
        return getTagFieldValuesSafe(FieldKey.GENRE);
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

    public void setTrack(String value) {
    	setTagFieldValues(FieldKey.TRACK, value);
    }

    public void setTrack(Integer value) {
    	if (value != null) {
    		setTrack(value.toString());
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

    public void setTrackTotal(String value) {
    	setTagFieldValues(FieldKey.TRACK_TOTAL, value);
    }

    public void setTrackTotal(Integer value) {
    	if (value != null) {
    		setTrackTotal(value.toString());
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

    public void setDisc(String value) {
    	setTagFieldValues(FieldKey.DISC_NO, value);
    }

    public void setDisc(Integer value) {
    	if (value != null) {
    		setDisc(value.toString());
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

    public void setDiscTotal(String value) {
    	setTagFieldValues(FieldKey.DISC_TOTAL, value);
    }

    public void setDiscTotal(Integer value) {
    	if (value != null) {
    		setDiscTotal(value.toString());
    	}
    }

    public String getRecordLabel() {
    	return getFirstTagFieldValue(FieldKey.RECORD_LABEL);
    }

    public FieldValues getRecordLabels() {
    	return getTagFieldValuesSafe(FieldKey.RECORD_LABEL);
    }

    public void addRecordLabel(String value) {
    	addTagFieldValues(FieldKey.RECORD_LABEL, value);
    }

    public String getCatalogNo() {
    	return getFirstTagFieldValue(FieldKey.CATALOG_NO);
    }

    public FieldValues getCatalogNos() {
    	return getTagFieldValuesSafe(FieldKey.CATALOG_NO);
    }

    public void addCatalogNo(String value) {
    	addTagFieldValues(FieldKey.CATALOG_NO, value);
    }

    public String getRating() {
    	return getFirstTagFieldValue(FieldKey.RATING);
    }

    public void addRating(String value) {
    	addTagFieldValues(FieldKey.RATING, value);
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
		if (locationString != null) {
			try {
				return new URI(locationString);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
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
    		if (getTagFieldValuesSafe(key).isEmpty()) {
    			setTagFieldValues(key, "");
    		}
    	}
    }
    
    public void removeEmptyCommonTagFields() {
    	for (FieldKey key : COMMON_TAG_FIELDS) {
    		removeEmptyTagField(key);
    	}
    }
    
    public void removeEmptyTagField(FieldKey key) {
    	removeEmptyTagFieldValues(key);
		if (Util.isEmpty(getFirstTagFieldValue(key))) {
			removeTagField(key);
		}
    }
    
    public void removeEmptyTagFields() {
    	for (FieldKey key : FieldKey.values()) {
    		removeEmptyTagField(key);
    	}
    }
    
    public void removeEmptyTagFieldValues(FieldKey key) {
    	FieldValues values = getTagFieldValues(key);
    	if (values != null) {
    		for (int i = 0; i < values.size(); i++) {
    			String value = values.get(i);
    			if (Util.isEmpty(value)) {
    				values.remove(i);
    			}
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
