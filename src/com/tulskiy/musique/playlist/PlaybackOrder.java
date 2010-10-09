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

import com.tulskiy.musique.gui.playlist.SeparatorTrack;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.util.Util;

import java.util.LinkedList;

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
        REPEAT_TRACK("Repeat Track"),
        REPEAT_ALBUM("Repeat Album"),
        SHUFFLE("Shuffle"),
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

    class QueueTuple {
        Track track;
        Playlist playlist;

        QueueTuple(Track track, Playlist playlist) {
            this.track = track;
            this.playlist = playlist;
        }
    }

    private Playlist playlist;
    private Order order = Order.DEFAULT;
    private LinkedList<QueueTuple> queue = new LinkedList<QueueTuple>();
    private Track lastPlayed;
    private Expression albumFormat = Parser.parse("[%albumArtist%|]%album%[|%date%]");

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

    public void enqueue(Track track, Playlist playlist) {
        queue.add(new QueueTuple(track, playlist));
        updateQueuePositions();
    }

    private void updateQueuePositions() {
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
            if (track instanceof SeparatorTrack)
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
            QueueTuple tuple = queue.poll();
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
                    String album = (String) albumFormat.eval(currentTrack);

                    if (Util.isEmpty(album))
                        return next(index);

                    track = next(index);
                    if (track != null) {
                        if (album.equals(albumFormat.eval(track))) {
                            return track;
                        }
                    }

                    for (int i = index; i >= 0; i--) {
                        track = playlist.get(i);
                        Object value = albumFormat.eval(track);
                        if (!album.equals(value)) {
                            return next(i);
                        }
                    }

                    return track;
                case RANDOM:
                    return getTrack((int) (Math.random() * size));
                case SHUFFLE:
                    return nextShuffle(currentTrack);
            }
        }

        return getTrack(index);
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
                String album = (String) albumFormat.eval(currentTrack);

                track = prev(index);
                if (track != null) {
                    if (album.equals(albumFormat.eval(track))) {
                        return track;
                    }
                }

                for (int i = index; i < size; i++) {
                    track = playlist.get(i);
                    Object value = albumFormat.eval(track);
                    if (!album.equals(value)) {
                        return prev(i);
                    }
                }

                return track;
            case RANDOM:
                return getTrack((int) (Math.random() * size));
            case SHUFFLE:
                return prevShuffle(currentTrack);
        }

        return getTrack(index);
    }

    private Track nextShuffle(Track currentTrack) {
        Track minRating = null;
        Track minGreater = null;
        for (Track track : playlist) {
            if (track == currentTrack || track.getLocation() == null)
                continue;

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

    private Track prevShuffle(Track currentTrack) {
        Track maxSmaller = null;
        Track maxRating = null;

        for (Track track : playlist) {
            if (track == currentTrack || track.getLocation() == null)
                continue;

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
