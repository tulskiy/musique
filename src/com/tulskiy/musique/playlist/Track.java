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

}
