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

import java.util.HashMap;

/**
 * Author: Denis Tulskiy
 * Date: 12/3/10
 */
public class TrackDataCache {
    private static TrackDataCache instance = new TrackDataCache();

    public static TrackDataCache getInstance() {
        return instance;
    }

    private HashMap<TrackData, TrackData> cache = new HashMap<TrackData, TrackData>();

    public void cache(Track track) {
        TrackData key = track.getTrackData();
        TrackData trackData = cache.get(key);
        if (trackData == null) {
            cache.put(key, key);
        } else {
            track.setTrackData(trackData.merge(key));
        }
    }
}
