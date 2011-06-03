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

import com.tulskiy.musique.gui.playlist.SeparatorTrack;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;

import java.util.Comparator;

/**
 * Author: Denis Tulskiy
 * Date: 6/3/11
 */
class TrackComparator implements Comparator<Track> {
    private final Expression e;

    public TrackComparator(Expression e) {
        this.e = e;
    }

    @Override
    public int compare(Track o1, Track o2) {
        try {
            if (o1 instanceof SeparatorTrack) {
                return -1;
            } else if (o2 instanceof SeparatorTrack) {
                return 1;
            }

            Object v1 = e.eval(o1);
            Object v2 = e.eval(o2);
            if (v1 != null && v2 != null) {
                return v1.toString().compareToIgnoreCase(v2.toString());
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
