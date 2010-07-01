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

import com.tulskiy.musique.audio.player.PlaybackOrder;
import com.tulskiy.musique.gui.playlist.SeparatorTrack;

import java.util.LinkedList;

/**
 * Manages playback.
 * <p/>
 * Shuffle algorithm taken from streamer.c from DeadBeef project
 * <p/>
 * Author: Denis Tulskiy
 * Date: Jul 1, 2010
 */
public class PlaylistOrder implements PlaybackOrder {
    class Tuple {
        Track track;
        Playlist playlist;

        Tuple(Track track, Playlist playlist) {
            this.track = track;
            this.playlist = playlist;
        }
    }

    private Playlist playlist;
    private Order order = Order.DEFAULT;
    private LinkedList<Tuple> queue = new LinkedList<Tuple>();
    private Track lastPlayed;
    private Track plMin, plMax;

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setLastPlayed(Track lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public void enqueue(Track track, Playlist playlist) {
        queue.add(new Tuple(track, playlist));
        updateQueuePositions();
    }

    private void updateQueuePositions() {
        for (int i = 0; i < queue.size(); i++) {
            queue.get(i).track.setQueuePosition(i + 1);
        }
    }

    public void flushQueue() {
        for (Tuple tuple : queue) {
            tuple.track.setQueuePosition(-1);
        }
        queue.clear();
        updateQueuePositions();
    }

    @Override
    public Track next(Track currentTrack) {
        int index;

        if (!queue.isEmpty()) {
            Tuple tuple = queue.poll();
            Track track = tuple.track;
            setPlaylist(tuple.playlist);
            track.setQueuePosition(-1);
            updateQueuePositions();
            return track;
        }

        if (playlist == null || playlist.size() <= 0)
            return null;

        if (currentTrack == null) {
            if (playlist.contains(lastPlayed))
                return lastPlayed;
            else
                return playlist.get(0);
        } else {
            index = playlist.indexOf(currentTrack);
            if (index == -1)
                return null;

            int size = playlist.size();

            switch (order) {
                case DEFAULT:
                    index = index < size - 1 ? index + 1 : -1;
                    break;
                case REPEAT:
                    index = (index + 1) % size;
                    break;
                case REPEAT_TRACK:
                    break;
                case RANDOM:
                    index = (int) (Math.random() * size);
                    break;
                case SHUFFLE:
                    //find non played minimum
                    Track min = null;
                    for (Track track : playlist) {
                        if (track instanceof SeparatorTrack || track.isPlayed())
                            continue;

                        if (min == null || track.getShuffleRating() < min.getShuffleRating()) {
                            min = track;
                        }
                    }
                    if (min == null) {
                        reshuffle();
                        min = plMin;
                    }

                    return min;
            }
        }

        return index != -1 ? playlist.get(index) : null;
    }

    private void reshuffle() {
        for (Track track : playlist) {
            track.setPlayed(false);
            track.setShuffleRating(Track.nextRandom());

            if (plMin == null || track.getShuffleRating() < plMin.getShuffleRating())
                plMin = track;

            if (plMax == null || track.getShuffleRating() > plMax.getShuffleRating())
                plMax = track;
        }
    }

    @Override
    public Track prev(Track currentTrack) {
        if (playlist == null || playlist.size() <= 0)
            return null;

        int index = playlist.indexOf(currentTrack);
        if (index == -1)
            return null;

        int size = playlist.size();

        switch (order) {
            case DEFAULT:
                index--;
                break;
            case REPEAT:
                index--;
                if (index < 0)
                    index += size;
                break;
            case REPEAT_TRACK:
                break;
            case RANDOM:
                index = (int) (Math.random() * size);
                break;
            case SHUFFLE:
                // find already played song with maximum shuffle rating below prev song
                Track max = null;
                Track amax = null;
                currentTrack.setPlayed(false);
                int rating = currentTrack.getShuffleRating();
                for (Track track : playlist) {
                    if (track instanceof SeparatorTrack)
                        continue;
                    if (track != currentTrack && track.isPlayed() &&
                        (amax == null || track.getShuffleRating() > amax.getShuffleRating())) {
                        amax = track;
                    }

                    if (track == currentTrack || track.getShuffleRating() > rating || !track.isPlayed()) {
                        continue;
                    }

                    if (max == null || track.getShuffleRating() > max.getShuffleRating()) {
                        max = track;
                    }
                }

                if (max == null) {
                    if (amax == null) {
                        reshuffle();
                        amax = plMax;
                    }

                    max = amax;
                }

                return max;
        }

        return index > -1 ? playlist.get(index) : null;
    }
}
