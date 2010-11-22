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

import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages playback.
 * <p/>
 * Idea fot the shuffle algorithm taken from streamer.c from DeadBeef project
 * <p/>
 * Author: Denis Tulskiy
 * Date: Jul 1, 2010
 */
public class PlaybackOrder {
    public enum Order {
        DEFAULT("Default"),
        REPEAT("Repeat"),
        REPEAT_TRACK("Repeat track"),
        REPEAT_ALBUM("Repeat album"),
        REPEAT_GROUP("Repeat group"),
        SHUFFLE("Shuffle"),
        SHUFFLE_ALBUMS("Shuffle albums"),
        SHUFFLE_GROUPS("Shuffle groups"),
        RANDOM("Random");

        private String text;

        Order(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private static Expression queueTupleTitle = Parser.parse("[%artist% - ]%title%");

    public class QueueTuple {
        public Track track;
        public Playlist playlist;

        QueueTuple(Track track, Playlist playlist) {
            this.track = track;
            this.playlist = playlist;
        }

        @Override
        public String toString() {
            return (String) queueTupleTitle.eval(track);
        }
    }

    private Playlist playlist;
    private Order order = Order.DEFAULT;
    private List<QueueTuple> queue = new ArrayList<QueueTuple>();
    private Track lastPlayed;
    private Expression albumFormat;

    public PlaybackOrder() {
        final Configuration config = Application.getInstance().getConfiguration();
        config.addPropertyChangeListener("playbackOrder.albumFormat", true, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String format = config.getString(evt.getPropertyName(), "%album%");
                albumFormat = Parser.parse(format);
            }
        });
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setLastPlayed(Track lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public Track getLastPlayed() {
        return lastPlayed;
    }

    public List<QueueTuple> getQueue() {
        return queue;
    }

    public void enqueue(Track track, Playlist playlist) {
        queue.add(new QueueTuple(track, playlist));
        updateQueuePositions();
    }

    public void updateQueuePositions() {
        for (int i = 0; i < queue.size(); i++) {
            queue.get(i).track.setQueuePosition(i + 1);
        }
    }

    public void flushQueue() {
        for (QueueTuple tuple : queue) {
            tuple.track.setQueuePosition(-1);
        }
        queue.clear();
        updateQueuePositions();
    }

    private Track getTrack(int index) {
        if (index != -1) {
            Track track = playlist.get(index);
            // technically, separator can not be the last track
            // so we just get the next track
            if (track.getLocation() == null)
                return playlist.get(index + 1);
            return track;
        } else {
            return null;
        }
    }

    private Track next(int index) {
        index = index < playlist.size() - 1 ? index + 1 : -1;
        if (index != -1) {
            Track track = playlist.get(index);
            // technically, separator can not be the last track
            // so we just get the next track
            if (track.getLocation() == null)
                return next(index);
            return track;
        } else {
            return null;
        }
    }

    private Track prev(int index) {
        index--;
        if (index >= 0) {
            Track track = playlist.get(index);
            if (track.getLocation() == null)
                return prev(index);
            return track;
        } else {
            return null;
        }
    }

    public Track next(Track currentTrack) {
        int index;

        if (!queue.isEmpty()) {
            QueueTuple tuple = queue.remove(0);
            Track track = tuple.track;
            setPlaylist(tuple.playlist);
            track.setQueuePosition(-1);
            updateQueuePositions();
            return track;
        }

        if (playlist == null || playlist.size() <= 0)
            return null;

        if (lastPlayed != null) {
            if (playlist.contains(lastPlayed)) {
                Track track = lastPlayed;
                lastPlayed = null;
                return track;
            }
        }

        if (currentTrack == null) {
            return playlist.get(0);
        } else {
            index = playlist.indexOf(currentTrack);
            if (index == -1)
                return playlist.get(0);

            int size = playlist.size();
            Track track;

            switch (order) {
                case DEFAULT:
                    return next(index);
                case REPEAT:
                    track = next(index);
                    return track != null ? track : getTrack(0);
                case REPEAT_TRACK:
                    return currentTrack;
                case REPEAT_ALBUM:
                    return nextPatternMatch(currentTrack, index, albumFormat, false);
                case REPEAT_GROUP:
                    if (index + 1 < playlist.size()) {
                        Track tr = playlist.get(index + 1);
                        if (tr.getLocation() != null) {
                            return tr;
                        }
                    }

                    for (int i = index; i >= 0; i--) {
                        if (playlist.get(i).getLocation() == null) {
                            return playlist.get(i + 1);
                        }
                    }
                    return playlist.get(0);
                case SHUFFLE_ALBUMS:
                    return nextPatternMatch(currentTrack, index, albumFormat, true);
                case SHUFFLE_GROUPS:
                    if (index + 1 < playlist.size()) {
                        Track tr = playlist.get(index + 1);
                        if (tr.getLocation() != null) {
                            return tr;
                        }
                    }

                    for (int i = index; i >= 0; i--) {
                        Track separator = playlist.get(i);
                        if (separator.getLocation() == null) {
                            separator = nextShuffle(separator, true, null);

                            return next(playlist.indexOf(separator));
                        }
                    }
                    return playlist.get(0);
                case RANDOM:
                    return getTrack((int) (Math.random() * size));
                case SHUFFLE:
                    return nextShuffle(currentTrack, false, null);
            }
        }

        return getTrack(index);
    }

    private Track nextPatternMatch(Track currentTrack, int index, Expression pattern, boolean shuffle) {
        Track track;
        Object result = pattern.eval(currentTrack);

        track = next(index);

        if (track != null) {
            if (equals(result, pattern.eval(track))) {
                return track;
            }
        }

        for (int i = index; i >= 0; i--) {
            track = playlist.get(i);
            if (!equals(result, pattern.eval(track))) {
                Track next = next(i);
                if (shuffle) {
                    return nextShuffle(next, false, pattern);
                } else {
                    return next;
                }
            }
        }

        return track;
    }

    public Track prev(Track currentTrack) {
        if (playlist == null || playlist.size() <= 0)
            return null;

        int index = playlist.indexOf(currentTrack);
        if (index == -1)
            return null;

        int size = playlist.size();

        Track track;
        switch (order) {
            case DEFAULT:
                return prev(index);
            case REPEAT:
                track = prev(index);
                return track != null ? track : getTrack(size - 1);
            case REPEAT_TRACK:
                return currentTrack;
            case REPEAT_ALBUM:
                return prevPatternMatch(currentTrack, index, albumFormat, false);
            case REPEAT_GROUP:
                if (index > 0) {
                    Track tr = playlist.get(index - 1);
                    if (tr.getLocation() != null) {
                        return tr;
                    }
                }

                for (int i = index + 1; i < size; i++) {
                    if (playlist.get(i).getLocation() == null) {
                        return playlist.get(i - 1);
                    }
                }

                return playlist.get(size - 1);
            case SHUFFLE_ALBUMS:
                return prevPatternMatch(currentTrack, index, albumFormat, true);
            case SHUFFLE_GROUPS:
                if (index > 0) {
                    Track tr = playlist.get(index - 1);
                    if (tr.getLocation() != null) {
                        return tr;
                    }
                }

                for (int i = index; i >= 0; i--) {
                    Track separator = playlist.get(i);
                    if (separator.getLocation() == null) {
                        separator = prevShuffle(separator, true, null);

                        return next(playlist.indexOf(separator));
                    }
                }
                return playlist.get(0);
            case RANDOM:
                return getTrack((int) (Math.random() * size));
            case SHUFFLE:
                return prevShuffle(currentTrack, false, null);
        }

        return getTrack(index);
    }

    private boolean equals(Object o1, Object o2) {
        return (o1 != null && o1.equals(o2))
                || (o1 == null && o2 == null);
    }

    private Track prevPatternMatch(Track currentTrack, int index, Expression pattern, boolean shuffle) {
        Track track;
        Object result = pattern.eval(currentTrack);

        track = prev(index);
        if (track != null) {
            if (equals(result, pattern.eval(track))) {
                return track;
            }
        }

        if (shuffle) {
            return prevShuffle(currentTrack, false, pattern);
        }

        for (int i = index; i < playlist.size(); i++) {
            track = playlist.get(i);
            if (!equals(result, pattern.eval(track))) {
                return prev(i);
            }
        }

        return track;
    }

    private Track nextShuffle(Track currentTrack, boolean searchSeparators, Expression pattern) {
        Track minRating = null;
        Track minGreater = null;
        Object patternValue = null;
        for (Track track : playlist) {
            if (track == currentTrack
                    || (searchSeparators && track.getLocation() != null)
                    || (!searchSeparators && track.getLocation() == null))
                continue;

            if (pattern != null) {
                Object value = pattern.eval(track);
                if (equals(patternValue, value)) {
                    continue;
                }
                patternValue = value;
            }

            if (minRating == null || track.getShuffleRating() < minRating.getShuffleRating()) {
                minRating = track;
            }

            if (track.getShuffleRating() >= currentTrack.getShuffleRating()) {
                if (minGreater == null || track.getShuffleRating() < minGreater.getShuffleRating()) {
                    minGreater = track;
                }
            }
        }

        return minGreater != null ? minGreater : minRating;
    }

    private Track prevShuffle(Track currentTrack, boolean searchSeparators, Expression pattern) {
        Track maxSmaller = null;
        Track maxRating = null;
        Object patternValue = null;

        for (Track track: playlist) {
            if (track == currentTrack
                    || (searchSeparators && track.getLocation() != null)
                    || (!searchSeparators && track.getLocation() == null))
                continue;

            if (pattern != null) {
                Object value = pattern.eval(track);
                if (equals(patternValue, value)) {
                    continue;
                }
                patternValue = value;
            }

            if (maxRating == null || track.getShuffleRating() > maxRating.getShuffleRating()) {
                maxRating = track;
            }

            if (track.getShuffleRating() <= currentTrack.getShuffleRating()) {
                if (maxSmaller == null || track.getShuffleRating() > maxSmaller.getShuffleRating()) {
                    maxSmaller = track;
                }
            }
        }

        return maxSmaller != null ? maxSmaller : maxRating;
    }
}
